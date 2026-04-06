# Task Scheduler — Low-Level Design

## Requirements
Design a task scheduler that manages the execution of tasks at predefined times or intervals. Used to automate jobs like backups, notifications, report generation, and periodic cleanup.

### Functional Requirements
- Support both **one-time** and **repeating** tasks at fixed intervals
- Slight execution delay is acceptable (not real-time)
- No retry on failure (but design should be open for it)
- Task exceptions must be caught and reported — never crash worker threads or block other tasks
- Multiple tasks running concurrently
- Task cancellation and rescheduling

### Non-functional Requirements
- Thread-safe
- Extensible (new task types, scheduling strategies, observers)

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     TaskSchedulerService (Singleton)                │
│─────────────────────────────────────────────────────────────────────│
│ - INSTANCE: volatile static TaskSchedulerService                   │
│ - workers: Thread[]                                                │
│ - taskQueue: PriorityQueue<ScheduledTask>                          │
│ - taskMap: Map<String, ScheduledTask>                              │
│ - observers: CopyOnWriteArrayList<Observer>                        │
│ - sequenceCounter: AtomicInteger                                   │
│ - isRunning: volatile boolean                                      │
│ - queueLock: ReentrantLock                                         │
│ - notFull / notEmpty: Condition                                    │
│─────────────────────────────────────────────────────────────────────│
│ + getInstance(): TaskSchedulerService                              │
│ + initialize(workerCount): void                                    │
│ + scheduleTask(task, strategy): String                             │
│ + cancelTask(taskId): boolean                                      │
│ + rescheduleTask(taskId, newStrategy): boolean                     │
│ + addObserver(observer): void                                      │
│ + shutdown(): void                                                 │
│ - pushTaskToQueue(task): void                                      │
│ - runWorker(): void                                                │
│ - executeTask(task): void                                          │
└────────────────┬──────────────────────┬────────────────────────────┘
                 │ has-many              │ has-many
                 ▼                       ▼
┌──────────────────────────┐  ┌─────────────────────┐
│    ScheduledTask         │  │  «interface» Observer │
│──────────────────────────│  │─────────────────────│
│ - id: String (UUID)      │  │ + onStart(task)      │
│ - task: Task             │  │ + onComplete(task)   │
│ - strategy: Strategy     │  │ + onFailure(task, e) │
│ - status: TaskStatus     │  └────────┬────────────┘
│ - lastExecutionTime      │           │ implements
│ - nextExecutionTime      │           ▼
│ - sequenceNumber: int    │  ┌─────────────────────┐
│──────────────────────────│  │  LoggingObserver     │
│ + reschedule(strategy)   │  └─────────────────────┘
│ + hasMoreExecutions()    │
│ + compareTo(other)       │
└──────┬──────────┬────────┘
       │ has-a    │ has-a
       ▼          ▼
┌────────────┐  ┌──────────────────────────────┐
│ «interface»│  │ «interface» SchedulingStrategy│
│   Task     │  │──────────────────────────────│
│────────────│  │ + getNextExecutionTime(last)  │
│ + execute()│  │   → Optional<LocalDateTime>   │
└──────┬─────┘  └──────────────┬───────────────┘
       │ implements            │ implements
       ▼                       ▼
┌────────────────┐  ┌────────────────────────────┐
│ PrintMessage   │  │ OneTimeSchedulingStrategy   │
│ DataBackupTask │  │ RecurringSchedulingStrategy  │
└────────────────┘  └────────────────────────────┘

┌─────────────────┐
│  «enum»         │
│  TaskStatus     │
│─────────────────│
│ SCHEDULED       │
│ RUNNING         │
│ COMPLETED       │
│ FAILED          │
│ CANCELLED       │
└─────────────────┘
```

---

## API (Service Layer)

| Method | Returns | Description |
|--------|---------|-------------|
| `scheduleTask(Task, SchedulingStrategy)` | `String` (task ID) | Schedule a new task, returns ID for tracking |
| `cancelTask(String taskId)` | `boolean` | Cancel a scheduled/running task |
| `rescheduleTask(String taskId, SchedulingStrategy)` | `boolean` | Replace a task's scheduling strategy |
| `addObserver(Observer)` | `void` | Register lifecycle listener |
| `shutdown()` | `void` | Stop all workers gracefully |

Returns **IDs, not objects** — forces all operations through the service (see main readme notes).

---

## Design Patterns Used

### 1. Command Pattern (Task)
**The Problem:** The scheduler needs to execute arbitrary work, but shouldn't know or care what that work is. A backup task and a message-printing task have completely different logic, but the scheduler treats them identically.

**The Solution:** The Command pattern encapsulates work as an object. Every task implements the `Task` interface with a single `execute()` method. The scheduler invokes `task.execute()` without knowing the implementation details. The caller (client) creates a concrete command and hands it to the invoker (scheduler), which stores it in a queue and triggers execution later. The command itself holds everything it needs to run, so the invoker never touches the receiver's internals.

```
Client (Demo) → creates → ConcreteCommand (PrintMessageTask)
                                    ↓
Invoker (Scheduler) → calls → task.execute()
                                    ↓
Receiver logic runs (print, backup, etc.)
```

### 2. Strategy Pattern (SchedulingStrategy)
**The Problem:** Different tasks need different scheduling rules. A one-time reminder runs once. A health check runs every 30 seconds. A report runs on a CRON schedule. If we bake scheduling logic into the task itself, we'd need a different task class for every combination of work × timing.

**The Solution:** The Strategy pattern separates the **what** (Task) from the **when** (SchedulingStrategy). A single backup task can be paired with a one-time strategy in testing and a recurring strategy in production. Strategies are interchangeable — `rescheduleTask()` swaps the strategy at runtime.

```
ScheduledTask has-a Task          (what to run)
ScheduledTask has-a Strategy      (when to run)

Same Task + different Strategy = different scheduling behavior
```

### 3. Observer Pattern (Observer)
**The Problem:** When a task starts, completes, or fails, multiple systems might care: logging, metrics, alerting. If the scheduler directly calls a logger, adding metrics means modifying the scheduler. Every new concern adds another hardcoded dependency.

**The Solution:** The scheduler maintains a list of registered observers and notifies all of them when a lifecycle event occurs. Each observer decides independently what to do with the event. Adding a new concern (e.g., `MetricsObserver`) requires zero changes to the scheduler.

```
Scheduler → notifyAll() → [LoggingObserver, MetricsObserver, AlertObserver]
```

### 4. Producer-Consumer Pattern (PriorityQueue + Workers)
**The Problem:** Scheduling and execution happen at different times and from different threads. The `schedule()` method adds tasks; worker threads consume them. Without coordination, workers might miss tasks or multiple workers might grab the same one.

**The Solution:** A shared `PriorityQueue` protected by `ReentrantLock` + `Condition` variables implements the producer-consumer pattern. Producers call `notEmpty.signalAll()` after adding a task. Consumers call `notEmpty.await()` when the queue is empty or the next task isn't due yet. Bounded queue (capacity=1000) uses `notFull.await()` to apply backpressure when full.

### 5. Singleton Pattern (TaskSchedulerService)
We need a single scheduler instance sharing one thread pool and one priority queue. Multiple instances would compete for threads and create duplicate task executions.

Uses **volatile + double-checked locking** for thread-safe lazy initialization (see main readme for why `volatile` is needed).

---

## Concurrency Model: Manual Thread Pool with Timed Wait

We create worker threads directly instead of using `ExecutorService`. This demonstrates understanding of concurrency internals, which is exactly what interviewers test.

### Timed Wait Pattern (the core of runWorker)
Workers don't busy-wait or sleep. They use **timed `Condition.await()`**:

```
Worker loop:
  1. Acquire lock
  2. If queue empty → await() (releases lock, sleeps until signaled)
  3. Peek at top task (don't remove yet)
  4. If cancelled → poll and skip
  5. Calculate delay = nextExecutionTime - now
  6. If delay > 0 → await(delay) (releases lock, sleeps until timeout OR signal)
  7. If delay ≤ 0 → poll() the task, release lock, execute it
  8. After execution, if recurring → re-enqueue with updated next time
```

**Why peek before poll?** If we poll (remove) a task and then sleep, that worker is blocked and can't handle other tasks that are ready sooner. By peeking and using timed await, the worker releases the lock during the wait — other workers can process tasks that become ready, and a newly scheduled sooner task wakes all workers via `signalAll()`.

**Why `signalAll()` on schedule?** A new task might be due sooner than what sleeping workers are waiting on. `signalAll()` wakes them all to re-evaluate the queue top.

### Thread Safety Mechanisms

| Mechanism | Protects | Why this choice |
|-----------|----------|-----------------|
| `ReentrantLock` + `Condition` | taskQueue access | Need timed wait (`await(delay)`) — `synchronized` only has `wait()` with no timeout granularity |
| `volatile boolean isRunning` | shutdown flag | Simple flag, no compound operations needed |
| `volatile TaskStatus status` | task status field | Read by multiple workers, no compound check-then-act |
| `AtomicInteger sequenceCounter` | insertion ordering | Lock-free increment, no contention |
| `CopyOnWriteArrayList<Observer>` | observer list | Reads vastly outnumber writes (observers registered once, notified on every task) |
| `ConcurrentHashMap<String, ScheduledTask>` | task lookup map | Concurrent reads for cancel/reschedule |

### Why Not `ScheduledExecutorService`?
In production, you'd use `ScheduledThreadPoolExecutor`. In an interview, building it manually demonstrates you understand:
- Lock-based synchronization and condition variables
- Timed wait vs busy-wait vs sleep
- Producer-consumer coordination
- Priority queue ordering with tiebreakers

---

## Key Design Decisions

### Sequence Number for FIFO Ordering
`PriorityQueue` doesn't guarantee insertion order for equal priorities. Without a tiebreaker, tasks scheduled for the same time execute in arbitrary order. `sequenceNumber` (from `AtomicInteger`) ensures FIFO within the same execution time:
```java
public int compareTo(ScheduledTask o) {
    int timeCompare = this.nextExecutionTime.compareTo(o.nextExecutionTime);
    return timeCompare != 0 ? timeCompare : Integer.compare(this.sequenceNumber, o.sequenceNumber);
}
```

### Task Lookup Map (`taskMap`)
The `PriorityQueue` doesn't support efficient lookup by ID — it's O(n). A parallel `ConcurrentHashMap<String, ScheduledTask>` gives O(1) cancel/reschedule by ID. The map is the source of truth for "does this task exist"; the queue is only for ordering.

### Bounded Queue with Backpressure
Queue is bounded at 1000 tasks. When full, `scheduleTask()` blocks on `notFull.await()` instead of throwing or dropping. This prevents OOM from unbounded scheduling and naturally throttles producers.

---

## Bugs Fixed From Original Code

| Bug | Impact | Fix |
|-----|--------|-----|
| Singleton `INSTANCE` never assigned | `getInstance()` always returns null | Double-checked locking with volatile |
| `Condition` initialized from null `queueLock` | NPE on class load | Initialize `queueLock` at declaration |
| `volatile AtomicInteger` | Redundant, misleading | Removed `volatile` — `AtomicInteger` is already thread-safe |
| `runWorker()` accesses queue without lock | Race conditions, `IllegalMonitorStateException` | All queue access wrapped in `queueLock.lock()/unlock()` |
| Worker `Thread.sleep()` after removing task | Blocks worker, starves other ready tasks | Timed `await()` while peeking (not removing) |
| `shutdown()` doesn't wake workers | Workers stuck in `await()` forever | `signalAll()` before interrupting |
| `signal()` on schedule | Only wakes one worker — new sooner task may not be picked up | `signalAll()` wakes all workers to re-evaluate |
| No task map | Can't implement cancel/reschedule | `ConcurrentHashMap` for O(1) lookup |
| No FIFO tiebreaker | Nondeterministic order for same-time tasks | `sequenceNumber` in `compareTo()` |

---

## Extension Points

| Feature | How to add |
|---------|-----------|
| New task type | Implement `Task` interface (e.g., `EmailNotificationTask`) |
| New schedule (e.g., CRON) | Implement `SchedulingStrategy` (e.g., `CronSchedulingStrategy`) |
| New observer (e.g., metrics) | Implement `Observer` (e.g., `MetricsObserver`) |
| Retry on failure | Add retry count to `ScheduledTask`, re-enqueue in catch block |
| Task priority | Add priority field to `ScheduledTask`, use in `compareTo()` |
| Persistence | Save/load `taskMap` to disk or DB on schedule/shutdown |
