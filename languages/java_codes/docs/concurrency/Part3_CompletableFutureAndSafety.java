package docs.concurrency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ============================================================
 * Part 3: COMPLETABLE FUTURE, SYNCHRONIZATION, AND SAFETY
 * ============================================================
 *
 * ON TOP OF Future:
 * - Future lets you wait for a result.
 * - CompletableFuture lets you build pipelines: thenApply, thenCompose,
 * thenCombine, exceptionally.
 *
 * THREAD SAFETY BASICS:
 * When multiple threads read/write shared mutable state, you need coordination.
 *
 * COMMON TOOLS:
 * - synchronized: built-in monitor lock
 * - ReentrantLock: explicit lock API
 * - AtomicInteger: lock-free atomic updates for simple numeric state
 * - volatile: visibility guarantee, not atomicity for compound updates
 */
public class Part3_CompletableFutureAndSafety {

    public static void main(String[] args) throws Exception {
        example1_CompletableFuturePipeline();
        example2_CombineStages();
        example3_SynchronizedVsAtomic();
        example4_LockExample();
    }

    // =========================================================
    // 3A: CompletableFuture pipeline
    // =========================================================
    static void example1_CompletableFuturePipeline() throws InterruptedException, ExecutionException {
        System.out.println("=== 3A: CompletableFuture pipeline ===");

        CompletableFuture<String> pipeline = CompletableFuture.supplyAsync(() -> "java")
                .thenApply(String::toUpperCase)
                .thenApply(s -> s + " concurrency")
                .exceptionally(ex -> "fallback: " + ex.getMessage());

        System.out.println(pipeline.get());
    }

    // =========================================================
    // 3B: Combining independent async tasks
    // =========================================================
    static void example2_CombineStages() throws InterruptedException, ExecutionException {
        System.out.println("\n=== 3B: Combining stages ===");

        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> 100);
        CompletableFuture<Integer> discount = CompletableFuture.supplyAsync(() -> 15);

        CompletableFuture<Integer> finalPrice = price.thenCombine(discount, (base, off) -> base - off);
        System.out.println("final price: " + finalPrice.get());
    }

    // =========================================================
    // 3C: synchronized vs AtomicInteger
    // =========================================================
    /*
     * count++ is NOT atomic.
     * It is read -> add -> write.
     * Two threads can interleave and lose updates.
     */
    static void example3_SynchronizedVsAtomic() throws InterruptedException {
        System.out.println("\n=== 3C: synchronized vs AtomicInteger ===");

        class SyncCounter {
            private int value;

            synchronized void increment() {
                value++;
            }

            synchronized int get() {
                return value;
            }
        }

        SyncCounter syncCounter = new SyncCounter();
        AtomicInteger atomicCounter = new AtomicInteger();

        Runnable syncTask = () -> {
            for (int i = 0; i < 1_000; i++) {
                syncCounter.increment();
                atomicCounter.incrementAndGet();
            }
        };

        Thread t1 = new Thread(syncTask, "counter-1");
        Thread t2 = new Thread(syncTask, "counter-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("syncCounter:   " + syncCounter.get());
        System.out.println("atomicCounter: " + atomicCounter.get());
    }

    // =========================================================
    // 3D: ReentrantLock example
    // =========================================================
    static void example4_LockExample() throws InterruptedException {
        System.out.println("\n=== 3D: ReentrantLock ===");

        class LockedCounter {
            private final ReentrantLock lock = new ReentrantLock();
            private int value;

            void increment() {
                lock.lock();
                try {
                    value++;
                } finally {
                    lock.unlock();
                }
            }

            int get() {
                lock.lock();
                try {
                    return value;
                } finally {
                    lock.unlock();
                }
            }
        }

        LockedCounter counter = new LockedCounter();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            pool.submit(() -> {
                for (int i = 0; i < 500; i++) {
                    counter.increment();
                }
            });
            pool.submit(() -> {
                for (int i = 0; i < 500; i++) {
                    counter.increment();
                }
            });
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }

        System.out.println("locked count: " + counter.get());
    }
}