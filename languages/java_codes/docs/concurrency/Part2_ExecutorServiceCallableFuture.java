package docs.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================
 * Part 2: EXECUTORS, CALLABLE, AND FUTURE
 * ============================================================
 *
 * WHY EXECUTORS EXIST:
 * Creating raw threads for every task is expensive and hard to manage.
 * ExecutorService gives you a thread pool and a task queue.
 *
 * IMPORTANT TYPES:
 * - Executor: can execute a Runnable.
 * - ExecutorService: richer API with submit(), shutdown(), invokeAll().
 * - Callable<V>: like Runnable, but returns V and may throw checked exceptions.
 * - Future<V>: a handle to the result of async work.
 *
 * MENTAL MODEL:
 * submit(task) -> pool runs task later -> Future lets you inspect the outcome.
 */
public class Part2_ExecutorServiceCallableFuture {

    public static void main(String[] args) throws Exception {
        example1_FixedThreadPool();
        example2_RunnableVsCallable();
        example3_FutureGetCancelStatus();
        example4_InvokeAll();
    }

    // =========================================================
    // 2A: Fixed thread pool
    // =========================================================
    static void example1_FixedThreadPool() throws InterruptedException {
        System.out.println("=== 2A: Fixed thread pool ===");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int i = 1; i <= 4; i++) {
                final int taskId = i;
                pool.submit(() -> {
                    System.out.println("task " + taskId + " on " + Thread.currentThread().getName());
                });
            }
        } finally {
            shutdownAndAwait(pool);
        }
    }

    // =========================================================
    // 2B: Runnable vs Callable
    // =========================================================
    static void example2_RunnableVsCallable() throws InterruptedException, ExecutionException {
        System.out.println("\n=== 2B: Runnable vs Callable ===");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> runnableFuture = pool.submit(() -> {
                System.out.println("Runnable does side effects but returns no useful value");
            });

            Callable<Integer> callable = () -> 21 * 2;
            Future<Integer> callableFuture = pool.submit(callable);

            System.out.println("Runnable future get(): " + runnableFuture.get()); // null
            System.out.println("Callable future get(): " + callableFuture.get()); // 42
        } finally {
            shutdownAndAwait(pool);
        }
    }

    // =========================================================
    // 2C: Future status, timeout thinking, cancel()
    // =========================================================
    static void example3_FutureGetCancelStatus() throws InterruptedException {
        System.out.println("\n=== 2C: Future status and cancellation ===");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> slowFuture = pool.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return "cancelled cooperatively";
                    }
                    Thread.sleep(100);
                }
                return "finished normally";
            });

            Thread.sleep(5000);
            boolean cancelled = slowFuture.cancel(true);
            System.out.println("cancel requested: " + cancelled);
            System.out.println("isCancelled:      " + slowFuture.isCancelled());
            System.out.println("isDone:           " + slowFuture.isDone());
        } finally {
            shutdownAndAwait(pool);
        }
    }

    // =========================================================
    // 2D: invokeAll()
    // =========================================================
    /*
     * invokeAll() runs many Callables and gives back Futures in the same order
     * as the input list.
     */
    static void example4_InvokeAll() throws InterruptedException, ExecutionException {
        System.out.println("\n=== 2D: invokeAll() ===");

        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            tasks.add(() -> "alpha from " + Thread.currentThread().getName());
            tasks.add(() -> "beta from " + Thread.currentThread().getName());
            tasks.add(() -> "gamma from " + Thread.currentThread().getName());

            List<Future<String>> futures = pool.invokeAll(tasks);
            for (Future<String> future : futures) {
                System.out.println(future.get());
            }
        } finally {
            shutdownAndAwait(pool);
        }
    }

    static void shutdownAndAwait(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }
    }
}