import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SYNCHRONIZATION MECHANISMS IN JAVA
 * ====================================
 *
 * Java provides multiple ways to synchronize access to shared resources.
 * Each has different trade-offs:
 *
 * ┌────────────────────┬──────────────────────────────────────────────────┐
 * │ Mechanism          │ When to use                                      │
 * ├────────────────────┼──────────────────────────────────────────────────┤
 * │ synchronized       │ Simple mutual exclusion. Built-in, no imports.   │
 * │                    │ Can't interrupt waiting threads.                  │
 * │                    │ Automatically releases on exception.              │
 * ├────────────────────┼──────────────────────────────────────────────────┤
 * │ ReentrantLock      │ Need tryLock(), timed waits, or fairness.        │
 * │                    │ More flexible than synchronized.                  │
 * │                    │ MUST unlock in finally block (not auto-released). │
 * ├────────────────────┼──────────────────────────────────────────────────┤
 * │ Semaphore          │ Limit concurrent access to N (not just 1).       │
 * │                    │ E.g., connection pool with max 10 connections.    │
 * │                    │ acquire() decrements, release() increments.      │
 * ├────────────────────┼──────────────────────────────────────────────────┤
 * │ ReadWriteLock      │ Many readers, few writers.                        │
 * │                    │ Readers don't block each other; writers do.       │
 * └────────────────────┴──────────────────────────────────────────────────┘
 *
 * COMMON PITFALL: Creating a new Lock inside the method (per-call) gives
 * no synchronization at all — the lock must be SHARED across threads.
 */
public class SynchronizationMechanisms {

    private int count = 0;

    // ========== 1. synchronized keyword ==========
    // Intrinsic lock on `this`. Only one thread can execute at a time.
    // Lock is automatically released even if an exception is thrown.
    public synchronized void incrementWithSynchronized() {
        count++;
    }

    // ========== 2. ReentrantLock ==========
    // Must be a FIELD (shared), not a local variable!
    private final Lock lock = new ReentrantLock();

    public void incrementWithLock() {
        lock.lock();                // Acquire the lock
        try {
            count++;
        } finally {
            lock.unlock();          // ALWAYS unlock in finally
        }
    }

    // ========== 3. Semaphore ==========
    // Allows up to N threads to access the resource concurrently.
    // Semaphore(2) means 2 threads can enter at the same time.
    private final Semaphore semaphore = new Semaphore(2);

    public void accessResourceWithSemaphore() {
        try {
            semaphore.acquire();    // Decrements permits; blocks if 0
            System.out.println(Thread.currentThread().getName() + " accessing resource");
            Thread.sleep(1000);     // Simulate work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();    // Increments permits; wakes waiting threads
        }
    }

    // ========== Demo ==========
    public static void main(String[] args) throws InterruptedException {
        SynchronizationMechanisms demo = new SynchronizationMechanisms();

        // Launch 5 threads trying to access a resource limited to 2 concurrent
        System.out.println("=== Semaphore Demo (max 2 concurrent) ===");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(demo::accessResourceWithSemaphore, "Thread-" + i);
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.println("\n=== Counter Demo ===");
        demo.count = 0;
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) demo.incrementWithSynchronized();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) demo.incrementWithLock();
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("Final count (expected 20000): " + demo.count);
    }
}
