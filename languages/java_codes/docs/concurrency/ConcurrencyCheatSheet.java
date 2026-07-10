package docs.concurrency;

/**
 * ============================================================
 * JAVA CONCURRENCY CHEAT SHEET
 * ============================================================
 *
 * THREAD
 * - Represents an execution path.
 * - start() -> schedules a new thread.
 * - run() -> just executes normally on the current thread if called directly.
 * - join() -> wait for that thread to finish.
 * - interrupt() -> cooperative cancellation signal.
 *
 * RUNNABLE
 * - Functional interface: void run().
 * - Use for tasks that do not return a result.
 * - Good with Thread or ExecutorService.
 *
 * CALLABLE<V>
 * - Functional interface: V call() throws Exception.
 * - Use for tasks that return a value or may throw checked exceptions.
 *
 * FUTURE<V>
 * - Handle for async result.
 * - get() -> wait for result.
 * - cancel(true) -> request interruption.
 * - isDone(), isCancelled() -> status checks.
 * - Limitation: poor composition.
 *
 * COMPLETABLE FUTURE
 * - Future + pipeline composition.
 * - thenApply: transform result
 * - thenCompose: async flatMap
 * - thenCombine: combine two independent async results
 * - exceptionally / handle: error handling
 *
 * EXECUTOR
 * - Minimal abstraction that can run a task.
 * - execute(Runnable)
 *
 * EXECUTOR SERVICE
 * - Rich executor API.
 * - submit(), invokeAll(), shutdown(), shutdownNow(), awaitTermination()
 * invokeAll() returns List<Future<T>>
 * - Prefer over creating raw threads for every task.
 *
 * COMMON POOLS
 * - Executors.newSingleThreadExecutor() -> one worker, sequential tasks
 * - Executors.newFixedThreadPool(n) -> bounded worker count
 * - Executors.newCachedThreadPool() -> grows/shrinks, risky if abused
 * - Executors.newScheduledThreadPool(n) -> delayed / periodic jobs
 *
 * SYNCHRONIZATION
 * - synchronized -> monitor lock, simple mutual exclusion
 * - wait() -> release monitor and sleep until notified or interrupted
 * - notify() -> wake one waiting thread on the same monitor
 * - notifyAll() -> wake all waiting threads on the same monitor
 * - ReentrantLock -> explicit lock/unlock, richer API
 * - AtomicInteger -> atomic numeric updates without manual locking
 * - volatile -> visibility only, not atomicity for count++
 *
 * COORDINATION UTILITIES
 * - BlockingQueue -> producer-consumer
 * - CountDownLatch -> wait for N events
 * - Semaphore -> limit parallel access to a resource
 * - ConcurrentHashMap -> concurrent shared map
 *
 * RULES OF THUMB
 * - Prefer ExecutorService(=new Executors) over manually creating lots of
 * threads.
 * - Prefer Callable when you need a result.
 * - Prefer CompletableFuture when you need async composition.
 * - Do not swallow InterruptedException silently.
 * - Shared mutable state is the real source of pain; minimize it.
 */
public class ConcurrencyCheatSheet {

    public static void main(String[] args) {
        System.out.println("Java Concurrency Cheat Sheet");
        System.out.println("1. Thread = worker");
        System.out.println("2. Runnable = task with no result");
        System.out.println("3. Callable<V> = task with result");
        System.out.println("4. ExecutorService = manages thread pool");
        System.out.println("5. Future<V> = handle to async result");
        System.out.println("6. CompletableFuture = composable async pipeline");
        System.out.println("7. synchronized / wait / notify / notifyAll = monitor coordination tools");
        System.out.println("8. Lock / Atomic* = explicit shared-state safety tools");
    }
}