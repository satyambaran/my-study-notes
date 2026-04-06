package com.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogManager {
    private static volatile LogManager instance;
    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final Logger rootLogger;
    private final AsyncWorker processor;
    private volatile boolean stopped = false;

    private LogManager() {
        this.rootLogger = new Logger("root", null);
        this.loggers.put("root", rootLogger);
        this.processor = new AsyncWorker("LogManager-AsyncWorker");
        // Add a shutdown hook to ensure graceful shutdown of the log processor and appenders.
        // It will be invoked when the JVM is shutting down, allowing us to clean up resources properly.
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "LogManager-ShutdownHook"));
    }

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) {
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }

    /** Returns an existing logger by name, or creates one parented to root. */
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, n -> new Logger(n, rootLogger));
    }

    public Logger getRootLogger() {
        return rootLogger;
    }

    public AsyncWorker getProcessor() {
        return processor;
    }

    public synchronized void shutdown() {
        if (stopped) return;
        stopped = true;
        processor.stop();
        loggers.values().forEach(logger -> logger.getAppenders().forEach(a -> a.close()));
    }
}
