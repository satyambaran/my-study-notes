# LLD Design Notes — Logging System

## Problem Statement

Design a production-grade logging framework (like Log4j 2 / SLF4J + Logback) that supports:

- Multiple log levels with per-logger filtering
- Multiple output destinations (console, file, external services)
- Configurable, swappable message formatting
- Thread-safe, non-blocking (async) logging
- Named logger hierarchy with level inheritance
- Graceful shutdown with no message loss
- Extensible without modifying existing code

---

## Functional Requirements

1. **Log Levels** — Support `DEBUG < INFO < WARN < ERROR < FATAL`. Filter messages below a configurable minimum level per logger.
2. **Timestamps** — Every log entry carries the exact `LocalDateTime` it was created.
3. **Contextual Information** — Each entry captures logger name, originating thread name, log level, and message. Can be extended to include class/method/line via stack trace inspection.
4. **Multiple Destinations** — Simultaneously write to console, file, or any custom destination (HTTP, DB, etc.) by registering multiple appenders on a logger.
5. **Configurable Logging** — Clients set log level, attach appenders, and choose formatters at runtime without recompiling.
6. **Log Formatting** — Consistent, pluggable formats (plain text, JSON). Special characters in messages should be handled by the formatter.

---

## Non-Functional Requirements

1. **Thread Safety** — Multiple threads must be able to log concurrently without interleaving or dropping messages. Each shared resource is protected independently (see Thread Safety section).
2. **Asynchronous Logging** — Callers must not block on I/O. Log calls return after enqueuing; a background worker drains the queue.
3. **Performance** — Filtering (level check) happens on the caller thread before any object is allocated. Only passing messages pay the cost of `LogMessage` construction and queue insertion.
4. **Scalability** — `ConcurrentHashMap` for the logger registry; queue-based async dispatch absorbs spikes without back-pressure on callers.
5. **Extensibility** — New destinations implement `LogAppender`; new formats implement `LogFormatter`. No existing class needs to change (Open/Closed Principle).
6. **Reliability** — `FileAppender` flushes after every write. Shutdown hook + guarded `shutdown()` ensure the queue drains before the JVM exits, so no queued message is silently dropped.
7. **Usability** — Entry point is `LoggerFactory.getLogger(MyClass.class)`. Convenience methods `debug/info/warn/error/fatal` avoid passing `LogLevel` manually.
8. **Log Rotation** — Extension point on `FileAppender`: track cumulative bytes written; when a size threshold is crossed, close the current file, rename it with a timestamp suffix, and open a new one.
9. **Graceful Shutdown** — `LogManager` registers a JVM shutdown hook. Whether shutdown is triggered by `LogManager.getInstance().shutdown()` or by the JVM hook, a `volatile boolean stopped` guard ensures the drain-and-close sequence runs exactly once.

---

## Package Structure

```
com.logging
├── AsyncWorker.java         ← Generic single-threaded background task runner
├── Demo.java                ← End-to-end demo / driver
├── Logger.java              ← Core logger: level + appenders + async dispatch
├── LoggerFactory.java       ← Public entry point; thin facade over LogManager
├── LogManager.java          ← Singleton registry; owns AsyncWorker + shutdown
└── models/
    ├── entities/
    │   └── LogMessage.java               ← Immutable value object (level, message, timestamp, thread)
    ├── enums/
    │   └── LogLevel.java                 ← DEBUG(0) INFO(1) WARN(2) ERROR(3) FATAL(4)
    ├── interfaces/
    │   ├── ILogger.java                  ← log(level, msg) + default convenience methods
    │   ├── LogAppender.java              ← append(LogMessage), close(), get/setLogFormatter
    │   └── LogFormatter.java             ← String format(LogMessage)
    └── implementations/
        ├── logappenders/
        │   ├── ConsoleAppender.java      ← Buffered write to stdout via PrintWriter
        │   └── FileAppender.java         ← Synchronized write to file; append mode
        └── logformatters/
            ├── TextFormatter.java        ← "timestamp [LEVEL] [thread] loggerName - message"
            └── JsonFormatter.java        ← Structured JSON object per entry
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
- **Why:** swap output format (text ↔ JSON ↔ XML) without touching appender logic.

### 4. Observer (fan-out) — `LogAppender` list
- Each `Logger` holds a `List<LogAppender>`.
- On every log call, all appenders are invoked via a single async task: `targets.forEach(a -> a.append(logMessage))`.
- **Why:** add or remove destinations without changing `Logger`.

### 5. Template Method / Hierarchy — Logger parent chain
- Every `Logger` has a `parent` reference; the root logger's parent is `null`.
- `getEffectiveLogLevel()` walks the chain iteratively until it finds a non-null level, falling back to `DEBUG` at root.
- Appender lookup does the same: if this logger has no appenders, the walk finds the nearest ancestor that does.
- **Why:** enables per-package overrides (e.g., `com.db` → DEBUG, `com.http` → WARN) while the root provides a global default.

### 6. Builder / Value Object — `LogMessage`
- `LogMessage` is fully immutable (all fields `final`). Thread name is captured in the constructor.
- Safe to enqueue and read from the background worker thread with no synchronisation.
- **Why:** immutability eliminates the need for locking on the message object itself.

---

## Full Working Flow

```
Caller thread                         AsyncWorker thread
─────────────────────────────         ──────────────────────────────────────
logger.info("Order placed")
  │
  ├─ getEffectiveLogLevel()           (iterative parent walk — O(depth))
  │    └─ INFO >= INFO → pass
  │
  ├─ new LogMessage(INFO, "Order placed", now(), thisName)
  │    └─ captures thread name in constructor
  │
  ├─ parent walk for appenders
  │    └─ this logger has [FileAppender]
  │
  └─ AsyncWorker.submit(Runnable)
       └─ enqueues task to LinkedBlockingQueue ──────────────────────────►
                                                task dequeued by single thread
                                                  │
                                                  ├─ FileAppender.append(logMessage)
                                                  │    ├─ synchronized block (thread-safe)
                                                  │    ├─ JsonFormatter.format(logMessage)
                                                  │    └─ fileWriter.write(...) + flush()
                                                  │
                                                  └─ (next appender if multiple)

Caller returns immediately ◄──────────────────────────────────────────────
```

### Filtering — two checkpoints

| Where | What is checked |
|---|---|
| `Logger.log()` (caller thread) | `msg.level >= logger.effectiveLevel` — fast path; avoids `LogMessage` allocation |
| `LogAppender.append()` (worker thread) | Each appender can add its own level filter (extension point) |

### Logger hierarchy example

```
root (INFO)  ←─────────── "com.http" (no level) ←─── "com.http.client" (no level)
     └── [ConsoleAppender]                                     │
                                                               │ logger.debug("…")
                                                               │
                                               getEffectiveLogLevel()
                                               walks: client → http → root → INFO
                                               DEBUG < INFO → filtered out, returns immediately
```

---

## Thread Safety

| What | Mechanism | Reason |
|---|---|---|
| `LogManager` singleton init | DCL + `volatile` | Prevents partial construction being visible to other threads |
| `LogManager` logger registry | `ConcurrentHashMap` + `computeIfAbsent` | Lock-free reads; atomic create-if-absent |
| `LogManager.shutdown()` | `synchronized` + `volatile boolean stopped` | Idempotent; hook and explicit call can race |
| `LogMessage` | All fields `final` | Immutable; safe to publish across threads without locking |
| `FileAppender.append()` | `synchronized` | Prevents interleaved writes from concurrent worker tasks |
| `ConsoleAppender.append()` | `PrintWriter` (internally synchronized) | `println` on `PrintWriter` is atomic |
| `AsyncWorker` queue | `LinkedBlockingQueue` (inside `ExecutorService`) | Thread-safe producer-consumer handoff |

---

## Async Logging — Key Properties

- **One background thread** — `newSingleThreadExecutor`. Guarantees write ordering matches submission order.
- **Unbounded queue** — under extreme load, messages accumulate in memory. Extension point: swap to a bounded `ArrayBlockingQueue` with a drop/block policy.
- **Daemon thread** — the worker does not prevent JVM exit on its own. The shutdown hook + `awaitTermination(2s)` gives it time to drain before the JVM terminates.
- **Task ownership** — `Logger` builds the `Runnable` (fan-out to appenders). `AsyncWorker` is a generic executor with no knowledge of logging types.

---

## Shutdown Sequence

```
JVM exit / LogManager.shutdown()
  │
  ├─ stopped guard (volatile boolean) → skip if already shut down
  │
  ├─ AsyncWorker.stop()
  │    ├─ executor.shutdown()            ← no new tasks accepted
  │    ├─ awaitTermination(2s)           ← give worker time to drain queue
  │    └─ shutdownNow() if still alive   ← force-interrupt as last resort
  │
  └─ for each logger → for each appender → appender.close()
       ├─ ConsoleAppender: PrintWriter.close() (flushes buffer)
       └─ FileAppender: FileWriter.close() (flushes + closes OS handle)
```

---

## Extension Points

| Feature | How to add |
|---|---|
| **Log rotation** | In `FileAppender.append()`, track bytes written; when threshold exceeded, rename current file with timestamp and open a new `FileWriter` |
| **Remote / HTTP appender** | Implement `LogAppender`; POST `formatter.format(msg)` to an endpoint |
| **Database appender** | Implement `LogAppender`; `INSERT INTO logs (...)` in `append()` |
| **MDC (per-request context)** | `ThreadLocal<Map<String,String>>` — read in `LogMessage` constructor and embed in formatters |
| **Appender-level filtering** | Add `LogLevel minLevel` field to `LogAppender`; skip in `append()` if below threshold |
| **Config file (YAML/XML)** | `LogManager` reads config at startup; creates loggers, sets levels, wires appenders |
| **Bounded async queue** | Replace `newSingleThreadExecutor` in `AsyncWorker` with a `ThreadPoolExecutor` backed by `ArrayBlockingQueue` + `CallerRunsPolicy` |
| **Structured logging fields** | Add `Map<String, Object> context` to `LogMessage`; `JsonFormatter` serialises it alongside the message |

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
| `AsyncWorker` | `AsyncLoggerConfig` | `AsyncAppender` (Logback) |
| Parent chain walk | Logger hierarchy + additivity | Logger hierarchy + additivity |
