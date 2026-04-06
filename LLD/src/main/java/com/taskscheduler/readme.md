# Requirements
Design a task Scheduler that manages the execution of tasks at predefined times or intervals. It will be used to automate jobs like backups, notifications, report generation, and periodic cleanup tasks.
## Functional Requirements
- Should support both one-time and repeating tasks at fixed interval
- Exactly on time is required or slight delay is fine? Slight delay is fine
- Retry if fail? No for now but keep the design open
- If a task throws an exception during execution it should be caught and reported, but it should not crash the worker thread or block other tasks
- Multiple tasks running at once 
- Task cancellation
## Non-functional Requirements
- Should be thread safe
- Should be extensible

## Entities
- TaskSchedulerService: Central Orchestrator
- Scheduling strategy: getNextExecutionTime (when to run)
- Task: Executable work (what to run)
- Scheduled Task: Task with scheduling metadata and status 
- TaskStatus: 
- TaskExecutionObserver: Receives task lifecycle events

Manual Thread Pool: We create worker threads directly instead of using a built-in executor framework. Each worker runs a loop that pulls from the priority queue using lock-based synchronization. This demonstrates understanding of concurrency internals, which is exactly what interviewers are testing.
Timed Wait Pattern: Workers don't busy-wait. When the next task is in the future, the worker releases the lock and sleeps until either the delay expires or a new task is scheduled (which wakes all workers). This is the same mechanism that production-grade scheduled executors use internally.
Singleton: One scheduler per application. Uses thread-safe lazy initialization to ensure only one instance exists, even under concurrent access.

Command Pattern (Task)
The Problem: The scheduler needs to execute arbitrary work, but it shouldn't know or care what that work is. A backup task and a message-printing task have completely different logic, but the scheduler treats them identically.

The Solution: The Command pattern encapsulates work as an object. Every task implements the Task interface with a single execute() method. The scheduler invokes task.execute() without knowing the implementation details. The caller (client) creates a concrete command and hands it to the invoker (scheduler), which stores it in a queue and triggers execution later. The command itself holds everything it needs to run, so the invoker never touches the receiver's internals.

Strategy Pattern (SchedulingStrategy)
The Problem: Different tasks need different scheduling rules. A one-time reminder runs once. A health check runs every 30 seconds. A report runs on a CRON schedule. If we bake scheduling logic into the task itself, we'd need a different task class for every combination of work and timing.

The Solution: The Strategy pattern separates the what (Task) from the when (SchedulingStrategy). A single backup task can be paired with a one-time strategy in testing and a recurring strategy in production.

The Problem: When a task starts, completes, or fails, multiple systems might care: logging, metrics, alerting. If the scheduler directly calls a logger, adding metrics means modifying the scheduler. Adding alerting means modifying it again. Every new concern adds another hardcoded dependency, and the scheduler becomes a tangled mess that knows about logging libraries, metrics SDKs, and email clients.

The Solution: The Observer pattern decouples the scheduler from its listeners. The scheduler maintains a list of registered observers and notifies all of them when a lifecycle event occurs. Each observer decides independently what to do with the event.

Producer-Consumer Pattern (PriorityQueue + Workers)
The Problem: Scheduling and execution happen at different times and from different threads. The schedule() method adds tasks; worker threads consume them. Without coordination, workers might miss tasks or multiple workers might grab the same one.

The Solution: A shared PriorityQueue protected by synchronized/wait/notify implements the producer-consumer pattern. Producers call notifyAll() after adding a task. Consumers call wait() when the queue is empty or the next task isn't due yet.

Singleton Pattern (TaskSchedulerService)
We need a single scheduler instance that all parts of the application share. Multiple scheduler instances would compete for threads and create duplicate task executions.

Singleton is appropriate here because we genuinely need one scheduler managing one thread pool and one priority queue.


