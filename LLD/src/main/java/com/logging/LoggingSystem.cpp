#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <unordered_map>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <thread>
#include <functional>
#include <memory>
#include <chrono>
#include <iomanip>
#include <ctime>
#include <filesystem>
#include <atomic>

// ─── LogLevel ───────────────────────────────────────────────────────────────────

enum class LogLevel { DEBUG = 0, INFO = 1, WARN = 2, ERROR = 3, FATAL = 4 };

static const char* logLevelName(LogLevel level) {
    switch (level) {
        case LogLevel::DEBUG: return "DEBUG";
        case LogLevel::INFO:  return "INFO";
        case LogLevel::WARN:  return "WARN";
        case LogLevel::ERROR: return "ERROR";
        case LogLevel::FATAL: return "FATAL";
    }
    return "UNKNOWN";
}

static int logLevelValue(LogLevel level) { return static_cast<int>(level); }

static bool isGreaterOrEqual(LogLevel a, LogLevel b) {
    return static_cast<int>(a) >= static_cast<int>(b);
}

// ─── Timestamp helper ───────────────────────────────────────────────────────────

static std::string nowTimestamp() {
    auto now = std::chrono::system_clock::now();
    auto time = std::chrono::system_clock::to_time_t(now);
    auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
        now.time_since_epoch()) % 1000;
    std::ostringstream oss;
    oss << std::put_time(std::localtime(&time), "%Y-%m-%d %H:%M:%S")
        << '.' << std::setfill('0') << std::setw(3) << ms.count();
    return oss.str();
}

static std::string nowFileSuffix() {
    auto now = std::chrono::system_clock::now();
    auto time = std::chrono::system_clock::to_time_t(now);
    std::ostringstream oss;
    oss << std::put_time(std::localtime(&time), "%Y%m%d_%H%M%S");
    return oss.str();
}

static std::string currentThreadName() {
    std::ostringstream oss;
    oss << std::this_thread::get_id();
    return oss.str();
}

// ─── LogMessage (immutable value object) ────────────────────────────────────────

struct LogMessage {
    const LogLevel logLevel;
    const std::string message;
    const std::string timestamp;
    const std::string loggerName;
    const std::string threadName;

    LogMessage(LogLevel level, std::string msg, std::string loggerName)
        : logLevel(level), message(std::move(msg)), timestamp(nowTimestamp()),
          loggerName(std::move(loggerName)), threadName(currentThreadName()) {}
};

// ─── LogFormatter (interface) ───────────────────────────────────────────────────

class LogFormatter {
public:
    virtual ~LogFormatter() = default;
    virtual std::string format(const LogMessage& msg) = 0;
};

// ─── TextFormatter ──────────────────────────────────────────────────────────────

class TextFormatter : public LogFormatter {
public:
    std::string format(const LogMessage& msg) override {
        std::ostringstream oss;
        oss << msg.timestamp << " "
            << "[" << logLevelValue(msg.logLevel) << "] "
            << "[" << msg.threadName << "] "
            << msg.loggerName << " - "
            << msg.message;
        return oss.str();
    }
};

// ─── JsonFormatter ──────────────────────────────────────────────────────────────

class JsonFormatter : public LogFormatter {
public:
    std::string format(const LogMessage& msg) override {
        std::ostringstream oss;
        oss << "{\n"
            << "\t\"timestamp\": \"" << msg.timestamp << "\",\n"
            << "\t\"loggerName\": \"" << msg.loggerName << "\",\n"
            << "\t\"threadName\": \"" << msg.threadName << "\",\n"
            << "\t\"logLevel\": \"" << logLevelValue(msg.logLevel) << "\",\n"
            << "\t\"message\": \"" << msg.message << "\"\n"
            << "}";
        return oss.str();
    }
};

// ─── LogAppender (interface) ────────────────────────────────────────────────────

class LogAppender {
public:
    virtual ~LogAppender() = default;
    virtual void append(const LogMessage& msg) = 0;
    virtual void close() = 0;

    void setLogFormatter(std::shared_ptr<LogFormatter> fmt) { formatter_ = std::move(fmt); }
    LogFormatter& getLogFormatter() { return *formatter_; }

protected:
    std::shared_ptr<LogFormatter> formatter_;
};

// ─── ConsoleAppender ────────────────────────────────────────────────────────────

class ConsoleAppender : public LogAppender {
public:
    explicit ConsoleAppender(std::shared_ptr<LogFormatter> fmt) { formatter_ = std::move(fmt); }

    void append(const LogMessage& msg) override {
        std::cout << formatter_->format(msg) << "\n";
    }

    void close() override { std::cout.flush(); }
};

// ─── FileAppender (with size-based rotation) ────────────────────────────────────

class FileAppender : public LogAppender {
public:
    static constexpr long DEFAULT_MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    FileAppender(std::shared_ptr<LogFormatter> fmt, std::string filePath)
        : FileAppender(std::move(fmt), std::move(filePath), DEFAULT_MAX_FILE_SIZE) {}

    FileAppender(std::shared_ptr<LogFormatter> fmt, std::string filePath, long maxFileSize)
        : filePath_(std::move(filePath)), maxFileSize_(maxFileSize), bytesWritten_(0) {
        formatter_ = std::move(fmt);
        if (std::filesystem::exists(filePath_)) {
            bytesWritten_ = static_cast<long>(std::filesystem::file_size(filePath_));
        }
        file_.open(filePath_, std::ios::app);
        if (!file_.is_open()) {
            throw std::runtime_error("Failed to open log file: " + filePath_);
        }
    }

    void append(const LogMessage& msg) override {
        std::lock_guard<std::mutex> lock(mutex_);
        std::string formatted = formatter_->format(msg) + "\n";
        long len = static_cast<long>(formatted.size());
        if (bytesWritten_ + len > maxFileSize_) {
            rotate();
        }
        file_ << formatted;
        file_.flush();
        bytesWritten_ += len;
    }

    void close() override {
        std::lock_guard<std::mutex> lock(mutex_);
        if (file_.is_open()) file_.close();
    }

private:
    void rotate() {
        file_.close();
        std::string rotatedName = filePath_ + "." + nowFileSuffix();
        try {
            std::filesystem::rename(filePath_, rotatedName);
        } catch (const std::filesystem::filesystem_error& e) {
            std::cerr << "Failed to rotate log file: " << filePath_ << " -> " << rotatedName << "\n";
        }
        file_.open(filePath_, std::ios::app);
        bytesWritten_ = 0;
    }

    std::string filePath_;
    long maxFileSize_;
    long bytesWritten_;
    std::ofstream file_;
    std::mutex mutex_;
};

// ─── AsyncWorker (bounded queue + caller-runs back-pressure) ────────────────────

class AsyncWorker {
public:
    static constexpr int DEFAULT_QUEUE_CAPACITY = 1024;

    explicit AsyncWorker(std::string threadName, int queueCapacity = DEFAULT_QUEUE_CAPACITY)
        : threadName_(std::move(threadName)), queueCapacity_(queueCapacity), stopped_(false) {
        worker_ = std::thread(&AsyncWorker::run, this);
    }

    ~AsyncWorker() { stop(); }

    void submit(std::function<void()> task) {
        std::unique_lock<std::mutex> lock(mutex_);
        if (stopped_) {
            throw std::runtime_error("AsyncWorker " + threadName_ + " is shutdown, cannot accept new tasks");
        }
        // CallerRunsPolicy: if queue is full, caller executes the task itself (back-pressure)
        if (static_cast<int>(queue_.size()) >= queueCapacity_) {
            lock.unlock();
            task(); // execute on caller thread
            return;
        }
        queue_.push(std::move(task));
        cv_.notify_one();
    }

    void stop() {
        {
            std::lock_guard<std::mutex> lock(mutex_);
            if (stopped_) return;
            stopped_ = true;
            cv_.notify_one();
        }
        if (worker_.joinable()) worker_.join();
    }

private:
    void run() {
        while (true) {
            std::function<void()> task;
            {
                std::unique_lock<std::mutex> lock(mutex_);
                cv_.wait(lock, [this] { return stopped_ || !queue_.empty(); });
                if (stopped_ && queue_.empty()) return;
                task = std::move(queue_.front());
                queue_.pop();
            }
            task();
        }
    }

    std::string threadName_;
    int queueCapacity_;
    std::atomic<bool> stopped_;
    std::queue<std::function<void()>> queue_;
    std::mutex mutex_;
    std::condition_variable cv_;
    std::thread worker_;
};

// ─── Forward declarations ───────────────────────────────────────────────────────

class Logger;
class LogManager;

// ─── LogManager (Singleton) ─────────────────────────────────────────────────────

class LogManager {
public:
    static LogManager& getInstance() {
        static LogManager instance; // thread-safe in C++11+ (Meyers' Singleton)
        return instance;
    }

    Logger* getLogger(const std::string& name);
    Logger* getRootLogger() { return rootLogger_.get(); }
    AsyncWorker& getProcessor() { return *processor_; }

    void shutdown() {
        std::lock_guard<std::mutex> lock(shutdownMutex_);
        if (stopped_) return;
        stopped_ = true;
        processor_->stop();
        closeAllAppenders();
    }

    LogManager(const LogManager&) = delete;
    LogManager& operator=(const LogManager&) = delete;

private:
    LogManager();
    void closeAllAppenders();

    std::unique_ptr<Logger> rootLogger_;
    std::unique_ptr<AsyncWorker> processor_;
    std::unordered_map<std::string, std::unique_ptr<Logger>> loggers_;
    std::mutex registryMutex_;
    std::mutex shutdownMutex_;
    bool stopped_ = false;
};

// ─── Logger ─────────────────────────────────────────────────────────────────────

class Logger {
public:
    Logger(std::string name, Logger* parent) : name_(std::move(name)), parent_(parent) {}

    void addAppender(std::shared_ptr<LogAppender> appender) {
        appenders_.push_back(std::move(appender));
    }

    const std::vector<std::shared_ptr<LogAppender>>& getAppenders() const { return appenders_; }

    void setLogLevel(LogLevel level) { logLevel_ = level; hasLevel_ = true; }

    LogLevel getEffectiveLogLevel() const {
        for (const Logger* l = this; l != nullptr; l = l->parent_) {
            if (l->hasLevel_) return l->logLevel_;
        }
        return LogLevel::DEBUG;
    }

    void log(LogLevel level, const std::string& message) {
        if (!isGreaterOrEqual(level, getEffectiveLogLevel())) return;
        auto logMessage = std::make_shared<LogMessage>(level, message, name_);
        for (Logger* l = this; l != nullptr; l = l->parent_) {
            if (!l->appenders_.empty()) {
                auto targets = l->appenders_; // copy for lambda capture
                LogManager::getInstance().getProcessor().submit(
                    [targets, logMessage]() {
                        for (auto& a : targets) a->append(*logMessage);
                    });
                return;
            }
        }
    }

    // Convenience methods
    void debug(const std::string& msg) { log(LogLevel::DEBUG, msg); }
    void info(const std::string& msg)  { log(LogLevel::INFO, msg); }
    void warn(const std::string& msg)  { log(LogLevel::WARN, msg); }
    void error(const std::string& msg) { log(LogLevel::ERROR, msg); }
    void fatal(const std::string& msg) { log(LogLevel::FATAL, msg); }

private:
    std::string name_;
    LogLevel logLevel_ = LogLevel::DEBUG;
    bool hasLevel_ = false; // tracks whether logLevel_ was explicitly set
    std::vector<std::shared_ptr<LogAppender>> appenders_;
    Logger* parent_;
};

// ─── LogManager implementation ──────────────────────────────────────────────────

LogManager::LogManager() {
    rootLogger_ = std::make_unique<Logger>("root", nullptr);
    processor_ = std::make_unique<AsyncWorker>("LogManager-AsyncWorker");
}

Logger* LogManager::getLogger(const std::string& name) {
    std::lock_guard<std::mutex> lock(registryMutex_);
    auto it = loggers_.find(name);
    if (it != loggers_.end()) return it->second.get();
    auto logger = std::make_unique<Logger>(name, rootLogger_.get());
    Logger* ptr = logger.get();
    loggers_[name] = std::move(logger);
    return ptr;
}

void LogManager::closeAllAppenders() {
    auto closeAppenders = [](Logger* logger) {
        for (auto& a : logger->getAppenders()) a->close();
    };
    closeAppenders(rootLogger_.get());
    for (auto& [name, logger] : loggers_) {
        closeAppenders(logger.get());
    }
}

// ─── LoggerFactory (Facade) ─────────────────────────────────────────────────────

class LoggerFactory {
public:
    static Logger* getLogger(const std::string& name) {
        return LogManager::getInstance().getLogger(name);
    }
};

// ─── Demo ───────────────────────────────────────────────────────────────────────

int main() {
    // Configure root logger (console, text format, INFO+)
    Logger* rootLogger = LogManager::getInstance().getRootLogger();
    rootLogger->setLogLevel(LogLevel::INFO);
    rootLogger->addAppender(std::make_shared<ConsoleAppender>(std::make_shared<TextFormatter>()));

    // Named logger for "com.myapp" (file, JSON format, WARN+)
    Logger* appLogger = LoggerFactory::getLogger("com.myapp");
    appLogger->setLogLevel(LogLevel::WARN);
    appLogger->addAppender(std::make_shared<FileAppender>(std::make_shared<JsonFormatter>(), "app.log"));

    // Log from a named logger; its appenders handle it
    appLogger->debug("Application starting up");          // filtered: DEBUG < WARN
    appLogger->info("Server listening on port 8080");     // filtered: INFO < WARN
    appLogger->warn("Connection pool nearing capacity");  // passes
    appLogger->error("Failed to reach database");         // passes

    // Log from a class-named logger (no own appenders -> propagates to root)
    Logger* serviceLogger = LoggerFactory::getLogger("LoggingSystem");
    serviceLogger->info("Service layer initialized");     // goes to root -> console
    serviceLogger->debug("Below root's INFO threshold");  // filtered

    std::this_thread::sleep_for(std::chrono::milliseconds(500));
    LogManager::getInstance().shutdown();
    std::cout << "Shutdown complete." << std::endl;

    return 0;
}
