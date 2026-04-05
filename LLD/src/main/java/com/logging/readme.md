# LLD Design Notes — Logging System

## Problem Statement

Design a production-grade logging framework (like Log4j 2 / SLF4J + Logback) that supports:

- Multiple log levels with per-logger filtering
- Multiple output destinations (console, file, database, HTTP)
- Configurable, swappable message formatting
- Thread-safe, non-blocking (async) logging with back-pressure
- Named logger hierarchy with level inheritance
- Log file rotation by size
- Graceful shutdown with no message loss
- Extensible without modifying existing code

---

## Functional Requirements

1. **Log Levels** — Support `DEBUG < INFO < WARN < ERROR < FATAL`. Filter messages below a configurable minimum level per logger.
2. **Timestamps** — Every log entry carries the exact `LocalDateTime` it was created.
3. **Contextual Information** — Each entry captures logger name, originating thread name, log level, and message.
4. **Multiple Destinations** — Simultaneously write to console, file, database, HTTP endpoint, or any custom destination by registering multiple appenders on a logger.
5. **Configurable Logging** — Clients set log level, attach appenders, and choose formatters at runtime without recompiling.
6. **Log Formatting** — Consistent, pluggable formats (plain text, JSON). Special characters in messages should be handled by the formatter.
7. **Log Rotation** — `FileAppender` tracks cumulative bytes written. When a configurable size threshold is exceeded, the current file is renamed with a timestamp suffix and a fresh file is opened.

---

## Non-Functional Requirements

1. **Thread Safety** — Multiple threads must be able to log concurrently without interleaving or dropping messages. Each shared resource is protected independently (see Thread Safety section).
2. **Asynchronous Logging** — Callers must not block on I/O. Log calls return after enqueuing; a background worker drains the queue.
3. **Back-Pressure** — When the async queue is full (producer faster than consumer), `CallerRunsPolicy` forces the submitting thread to execute the task itself. This naturally throttles fast producers instead of OOM-ing the JVM.
4. **Performance** — Filtering (level check) happens on the caller thread before any object is allocated. Only passing messages pay the cost of `LogMessage` construction and queue insertion.
5. **Scalability** — `ConcurrentHashMap` for the logger registry; bounded queue-based async dispatch absorbs spikes. Back-pressure prevents memory exhaustion under sustained overload.
6. **Extensibility** — New destinations implement `LogAppender`; new formats implement `LogFormatter`. No existing class needs to change (Open/Closed Principle).
7. **Reliability** — `FileAppender` flushes after every write. `DatabaseAppender` uses connection pooling (`DataSource`). `HttpAppender` logs errors to stderr on failure without crashing. Shutdown hook + guarded `shutdown()` ensure the queue drains before the JVM exits.
8. **Usability** — Entry point is `LoggerFactory.getLogger(MyClass.class)`. Convenience methods `debug/info/warn/error/fatal` avoid passing `LogLevel` manually.
9. **Graceful Shutdown** — `LogManager` registers a JVM shutdown hook. Whether shutdown is triggered by `LogManager.getInstance().shutdown()` or by the JVM hook, a `volatile boolean stopped` guard ensures the drain-and-close sequence runs exactly once.

---

## Package Structure

```
com.logging
├── AsyncWorker.java              <- Bounded single-thread executor with CallerRunsPolicy
├── Demo.java                     <- End-to-end demo / driver
├── Logger.java                   <- Core logger: level + appenders + async dispatch
├── LoggerFactory.java            <- Public entry point; thin facade over LogManager
├── LogManager.java               <- Singleton registry; owns AsyncWorker + shutdown
└── models/
    ├── entities/
    │   └── LogMessage.java                    <- Immutable value object (level, msg, timestamp, thread)
    ├── enums/
    │   └── LogLevel.java                      <- DEBUG(0) INFO(1) WARN(2) ERROR(3) FATAL(4)
    ├── interfaces/
    │   ├── ILogger.java                       <- log(level, msg) + default convenience methods
    │   ├── LogAppender.java                   <- append(LogMessage), close(), get/setLogFormatter
    │   └── LogFormatter.java                  <- String format(LogMessage)
    └── implementations/
        ├── logappenders/
        │   ├── ConsoleAppender.java           <- Buffered write to stdout via PrintWriter
        │   ├── FileAppender.java              <- Synchronized write to file; size-based rotation
        │   ├── DatabaseAppender.java          <- INSERT into relational DB via DataSource
        │   └── HttpAppender.java              <- POST to remote endpoint (ELK, Splunk, etc.)
        └── logformatters/
            ├── TextFormatter.java             <- "timestamp [LEVEL] [thread] loggerName - message"
            └── JsonFormatter.java             <- Structured JSON object per entry
```

---

## Design Patterns Applied

### 1. Singleton — `LogManager`
- `LogManager.getInstance()` uses double-checked locking with a `volatile` field.
- Single instance owns the logger registry (`ConcurrentHashMap`) and the `AsyncWorker`.
- **Why:** one central point for configuration, registry, and lifecycle management.

### 2. Facade — `LoggerFactory`
- `LoggerFactory.getLogger(String)` / `getLogger(Class<?>)` delegate to `LogManager`.
- Clients never touch `LogManager` directly — mirrors the SLF4J entry-point contract.
- **Why:** decouples client code from the registry implementation.

### 3. Strategy — `LogFormatter`
- `LogFormatter` interface: `String format(LogMessage)`.
- Each `LogAppender` holds one `LogFormatter`; formatters are swappable at runtime via `setLogFormatter(...)`.
- **Why:** swap output format (text <-> JSON <-> XML) without touching appender logic.

### 4. Observer (fan-out) — `LogAppender` list
- Each `Logger` holds a `List<LogAppender>`.
- On every log call, all appenders are invoked via a single async task: `targets.forEach(a -> a.append(logMessage))`.
- **Why:** add or remove destinations without changing `Logger`.

### 5. Template Method / Hierarchy — Logger parent chain
- Every `Logger` has a `parent` reference; the root logger's parent is `null`.
- `getEffectiveLogLevel()` walks the chain iteratively until it finds a non-null level, falling back to `DEBUG` at root.
- Appender lookup does the same: if this logger has no appenders, the walk finds the nearest ancestor that does.
- **Why:** enables per-package overrides (e.g., `com.db` -> DEBUG, `com.http` -> WARN) while the root provides a global default.

### 6. Value Object — `LogMessage`
- `LogMessage` is fully immutable (all fields `final`). Thread name is captured in the constructor.
- Safe to enqueue and read from the background worker thread with no synchronisation.
- **Why:** immutability eliminates the need for locking on the message object itself.

---

## Full Working Flow

```
Caller thread                              AsyncWorker thread (single, bounded queue)
-----------------------------------------  ------------------------------------------
logger.info("Order placed")
  |
  +-- getEffectiveLogLevel()                (iterative parent walk — O(depth))
  |    +-- INFO >= INFO -> pass
  |
  +-- new LogMessage(INFO, "Order placed", now(), loggerName)
  |    +-- captures Thread.currentThread().getName() in constructor
  |
  +-- parent walk for appenders
  |    +-- this logger has [FileAppender, ConsoleAppender]
  |
  +-- AsyncWorker.submit(Runnable)
       |
       +-- [queue has space] -> enqueue to ArrayBlockingQueue ------------>
       |                                                     task dequeued
       |                                                       |
       |                                                       +-- FileAppender.append(logMessage)
       |                                                       |    +-- synchronized block
       |                                                       |    +-- bytesWritten + len > maxSize? -> rotate()
       |                                                       |    +-- JsonFormatter.format(logMessage)
       |                                                       |    +-- fileWriter.write(...) + flush()
       |                                                       |
       |                                                       +-- ConsoleAppender.append(logMessage)
       |                                                            +-- TextFormatter.format(logMessage)
       |                                                            +-- printWriter.println(...)
       |
       +-- [queue FULL] -> CallerRunsPolicy
            +-- caller thread executes the task synchronously (back-pressure)
                 +-- same appender logic runs inline on caller thread

Caller returns <------------------------------------------------------------
```

### Filtering — two checkpoints

| Where | What is checked |
|---|---|
| `Logger.log()` (caller thread) | `msg.level >= logger.effectiveLevel` — fast path; avoids `LogMessage` allocation |
| `LogAppender.append()` (worker thread) | Each appender can add its own level filter (extension point) |

### Logger hierarchy example

```
root (INFO)  <----------- "com.http" (no level) <--- "com.http.client" (no level)
     +-- [ConsoleAppender]                                     |
                                                               | logger.debug("...")
                                                               |
                                               getEffectiveLogLevel()
                                               walks: client -> http -> root -> INFO
                                               DEBUG < INFO -> filtered out, returns immediately
```

---

## Back-Pressure — Bounded Queue

**Problem:** If the consumer thread is slower than producers (e.g. slow disk I/O, network latency on `HttpAppender`), an unbounded queue grows without limit until the JVM runs out of memory (OOM).
<!-- `IMP -->
**Trade-Off:** CallerRunsPolicy breaks chronological ordering. When the queue is full, the caller thread writes log #1025 directly while the consumer thread is still draining #500 from the queue. So a newer log can appear in the file before older ones that are still queued. This is a known trade-off: you get no message loss + back-pressure at the cost of strict ordering. Real frameworks like Logback's AsyncAppender accept this same trade-off.

**Solution:** `AsyncWorker` uses a `ThreadPoolExecutor` backed by `ArrayBlockingQueue(capacity=1024)` with `CallerRunsPolicy` as the rejection handler.

```
Producer threads          ArrayBlockingQueue (bounded, 1024)         Consumer thread
----------------          ----------------------------------         ---------------
  submit(task) ---------> [t1][t2][t3]...[t1024]  <---- dequeue ---> execute task
                                |
                          queue FULL?
                                |
                    +-----------+-----------+
                    | CallerRunsPolicy      |
                    | caller executes task  |
                    | itself (blocks until  |
                    | done = natural        |
                    | back-pressure)        |
                    +-----------------------+
```

**Why CallerRunsPolicy?**
- **No message loss** — every log message is guaranteed to be processed.
- **Natural throttling** — the fast producer slows down because it's busy running the task itself.
- **No OOM** — memory usage is bounded by `queueCapacity * sizeof(Runnable)`.
- **Graceful degradation** — under extreme load, logging degrades from async to sync, but the application keeps working.

---

## Log File Rotation

**Problem:** A single log file grows indefinitely, consuming all disk space.

**Solution:** `FileAppender` tracks `bytesWritten`. Before each write, if `bytesWritten + newBytes > maxFileSize`, it rotates.

```
bytesWritten + newEntry > 10 MB?
  |
  +-- NO  -> write + flush + increment counter
  |
  +-- YES -> rotate()
              +-- fileWriter.close()
              +-- rename: app.log -> app.log.20260404_153012
              +-- open new FileWriter("app.log")
              +-- bytesWritten = 0
```

**Why:** prevents unbounded disk growth. Default threshold is 10 MB; configurable per appender via `new FileAppender(formatter, path, maxSizeInBytes)`.

---

## Appender Overview

| Appender | Destination | Thread safety | Error handling |
|---|---|---|---|
| `ConsoleAppender` | `System.out` via `PrintWriter` | `PrintWriter` is internally synchronized | Swallows I/O errors (stdout rarely fails) |
| `FileAppender` | Local file via `FileWriter` | `synchronized append()` | RuntimeException on write failure |
| `DatabaseAppender` | SQL table via `DataSource` | Connection-per-call from pool (thread-safe) | Logs to stderr on `SQLException` (never crashes the app) |
| `HttpAppender` | Remote endpoint via `HttpClient` | `HttpClient` is thread-safe | Logs to stderr on HTTP errors / connection failures |

### DatabaseAppender — expected schema
```sql
CREATE TABLE logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp  TIMESTAMP     NOT NULL,
    log_level  VARCHAR(10)   NOT NULL,
    logger     VARCHAR(255)  NOT NULL,
    thread     VARCHAR(255)  NOT NULL,
    message    TEXT          NOT NULL
);
```

Uses `DataSource` (connection pool) so each `append()` call gets a pooled connection, executes a `PreparedStatement`, and returns it. No connection lifecycle management inside the appender.

### HttpAppender
- Uses Java 11+ `HttpClient` with configurable connect timeout (default 5s).
- POSTs the formatted message body with `Content-Type: application/json`.
- On HTTP >= 400 or connection failure, logs a warning to stderr — never throws.

---

## Thread Safety

| What | Mechanism | Reason |
|---|---|---|
| `LogManager` singleton init | DCL + `volatile` | Prevents partial construction being visible to other threads |
| `LogManager` logger registry | `ConcurrentHashMap` + `computeIfAbsent` | Lock-free reads; atomic create-if-absent |
| `LogManager.shutdown()` | `synchronized` + `volatile boolean stopped` | Idempotent; hook and explicit call can race |
| `LogMessage` | All fields `final` | Immutable; safe to publish across threads without locking |
| `FileAppender.append()` | `synchronized` | Prevents interleaved writes from concurrent tasks |
| `ConsoleAppender.append()` | `PrintWriter` (internally synchronized) | `println` on `PrintWriter` is atomic |
| `DatabaseAppender.append()` | Connection-per-call from pool | Each invocation uses its own `Connection` |
| `HttpAppender.append()` | `HttpClient` is thread-safe | Stateless request/response cycle |
| `AsyncWorker` queue | `ArrayBlockingQueue` (thread-safe) | Bounded producer-consumer handoff |

---

## Async Logging — Key Properties

- **One background thread** — `ThreadPoolExecutor(1, 1)`. Guarantees write ordering matches submission order.
- **Bounded queue** — `ArrayBlockingQueue(1024)`. Memory usage is capped regardless of producer throughput.
- **CallerRunsPolicy** — when queue is full, the submitting thread runs the task itself (back-pressure, not message loss).
- **Daemon thread** — the worker does not prevent JVM exit on its own. The shutdown hook + `awaitTermination(2s)` gives it time to drain before the JVM terminates.
- **Task ownership** — `Logger` builds the `Runnable` (fan-out to appenders). `AsyncWorker` is a generic executor with no knowledge of logging types.

---

## Shutdown Sequence

```
JVM exit / LogManager.shutdown()
  |
  +-- stopped guard (volatile boolean) -> skip if already shut down
  |
  +-- AsyncWorker.stop()
  |    +-- executor.shutdown()            <- no new tasks accepted
  |    +-- awaitTermination(2s)           <- give worker time to drain queue
  |    +-- shutdownNow() if still alive   <- force-interrupt as last resort
  |
  +-- for each logger -> for each appender -> appender.close()
       +-- ConsoleAppender: PrintWriter.close() (flushes buffer)
       +-- FileAppender: FileWriter.close() (flushes + closes OS handle)
       +-- DatabaseAppender: no-op (DataSource lifecycle managed externally)
       +-- HttpAppender: no-op (HttpClient has no explicit close)
```

---

## Responsibility Split

| Class | Owns |
|---|---|
| `LoggerFactory` | Static facade; returns named `Logger` instances from `LogManager` |
| `LogManager` | Singleton registry of all `Logger` instances; owns `AsyncWorker`; manages shutdown |
| `Logger` | Holds min level + list of appenders; builds `LogMessage`; dispatches async task |
| `LogMessage` | Immutable value object — level, message, timestamp, logger name, thread name |
| `AsyncWorker` | Generic bounded single-thread executor; back-pressure via `CallerRunsPolicy` |
| `LogAppender` | Knows *where* to write; holds its own `LogFormatter` |
| `LogFormatter` | Knows *how* to format a `LogMessage` into a String |

---

## Extension Points (not yet implemented)

| Feature | How to add |
|---|---|
| **MDC (Mapped Diagnostic Context)** | `ThreadLocal<Map<String,String>>` per thread; snapshot in `LogMessage` constructor; serialize in formatters. Solves cross-request log correlation in multi-threaded servers |
| **Structured logging fields** | Add `Map<String, Object> context` to `LogMessage`; pass per-call context alongside the message. Enables machine-parseable key-value fields for log aggregation tools |
| **Appender-level filtering** | Add `LogLevel minLevel` field to `LogAppender`; skip in `append()` if below threshold |
| **Config file (YAML/XML)** | `LogManager` reads config at startup; creates loggers, sets levels, wires appenders |
| **Additivity flag** | `boolean additive` on `Logger`; when true, also propagate to parent's appenders (like Log4j) |
| **Time-based rotation** | In `FileAppender`, rotate based on date/hour instead of (or in addition to) file size |
| **Retry / fallback** | Wrap any `LogAppender` in a `RetryAppender` that retries N times, then falls back to stderr |
| **Batch HTTP writes** | Buffer N messages in `HttpAppender`, flush as a single POST batch for throughput |

---

## Comparison with Real Frameworks

| Concept here | Log4j 2 | SLF4J + Logback |
|---|---|---|
| `LoggerFactory` | `LogManager` | `LoggerFactory` |
| `LogManager` | `LoggerContext` | `LoggerContext` (Logback) |
| `Logger` (class) | `Logger` | `Logger` (Logback impl) |
| `LogAppender` | `Appender` | `Appender` |
| `LogFormatter` | `Layout` | `Encoder` |
| `LogLevel` | `Level` | `Level` |
| `AsyncWorker` | `AsyncLoggerConfig` (LMAX Disruptor) | `AsyncAppender` (Logback) |
| Parent chain walk | Logger hierarchy + additivity | Logger hierarchy + additivity |
| `CallerRunsPolicy` | `AsyncQueueFullPolicy` | `discardingThreshold` on `AsyncAppender` |
