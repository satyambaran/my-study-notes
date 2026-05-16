package docs.concurrency;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ============================================================
 * Part 5: WAIT, NOTIFY, NOTIFYALL, AND MONITORS
 * ============================================================
 *
 * This file explains the old but still important monitor-based coordination
 * model built into every Java object.
 *
 * BIG IDEA:
 * - Every object in Java can act as a monitor lock.
 * - synchronized(lock) means "enter that monitor".
 * - wait(), notify(), and notifyAll() are monitor methods on Object.
 * - They are for communication between threads that share a condition.
 *
 * WHY THIS EXISTS:
 * Locks alone solve mutual exclusion: "only one thread enters here".
 * wait/notify solves coordination: "go to sleep until some condition changes".
 *
 * CORE RULES:
 * 1. You must own the monitor before calling wait/notify/notifyAll.
 * 2. wait() releases the monitor and suspends the thread.
 * 3. When awakened, the thread must re-acquire the same monitor before it
 * continues.
 * 4. Always wait in a while-loop, never an if-statement.
 * 5. Prefer notifyAll() over notify() unless you are sure only one waiter type
 * exists.
 *
 * TERMINOLOGY:
 * - monitor = the lock associated with an object
 * - guarded block = code that waits until a condition becomes true
 * - condition predicate = boolean rule you are waiting for
 */
public class Part5_WaitNotifyAndMonitors {

    public static void main(String[] args) throws Exception {
        example1_SleepVsWait();
        example2_GuardedBlock();
        example3_ProducerConsumerWithWaitNotifyAll();
        example4_WhyWhileNotIf();
    }

    // =========================================================
    // 5A: sleep() vs wait()
    // =========================================================
    /*
     * Thread.sleep(...)
     * - Belongs to Thread
     * - Pauses current thread for time
     * - Does NOT release any lock already held
     *
     * Object.wait()
     * - Belongs to Object
     * - Must be called while holding that object's monitor
     * - Releases that monitor while waiting
     * - Used for condition-based coordination, not just time delay
     */
    static void example1_SleepVsWait() throws InterruptedException {
        System.out.println("=== 5A: sleep() vs wait() ===");

        Object lock = new Object();

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                System.out.println("waiter: acquired lock, now waiting and releasing it");
                try {
                    lock.wait();
                    System.out.println("waiter: reacquired lock after notification");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "waiter");

        Thread notifier = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            synchronized (lock) {
                System.out.println("notifier: acquired lock because wait() released it");
                lock.notifyAll();
            }
        }, "notifier");

        waiter.start();
        notifier.start();
        waiter.join();
        notifier.join();
    }

    // =========================================================
    // 5B: Guarded block pattern
    // =========================================================
    /*
     * This is the standard pattern:
     *
     * synchronized (lock) {
     * while (!condition) {
     * lock.wait();
     * }
     * // condition is now true
     * }
     *
     * WHY while?
     * - Another thread may consume the condition first.
     * - Threads may wake up spuriously.
     * - notifyAll() wakes many waiters, but only some should proceed.
     */
    static void example2_GuardedBlock() throws InterruptedException {
        System.out.println("\n=== 5B: Guarded block ===");

        class SignalBox {
            private final Object lock = new Object();
            private boolean ready;

            void awaitReady() throws InterruptedException {
                synchronized (lock) {
                    while (!ready) {
                        lock.wait();
                    }
                }
            }

            void markReady() {
                synchronized (lock) {
                    ready = true;
                    lock.notifyAll();
                }
            }
        }

        SignalBox box = new SignalBox();

        Thread reader = new Thread(() -> {
            try {
                System.out.println("reader: waiting for ready=true");
                box.awaitReady();
                System.out.println("reader: observed ready=true");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "guarded-reader");

        Thread writer = new Thread(() -> {
            box.markReady();
            System.out.println("writer: changed condition and notified waiters");
        }, "guarded-writer");

        reader.start();
        Thread.sleep(100);
        writer.start();
        reader.join();
        writer.join();
    }

    // =========================================================
    // 5C: Producer-consumer using wait()/notifyAll()
    // =========================================================
    /*
     * This is the low-level version of what BlockingQueue gives you.
     *
     * It works, but it is easier to get wrong:
     * - missed signals
     * - wrong condition checks
     * - using if instead of while
     * - using notify when multiple waiter types exist
     *
     * That is why BlockingQueue is usually the better tool in production code.
     */
    static void example3_ProducerConsumerWithWaitNotifyAll() throws InterruptedException {
        System.out.println("\n=== 5C: Producer-consumer with wait/notifyAll ===");

        class BoundedBuffer {
            private final Object lock = new Object();
            private final Queue<Integer> queue = new LinkedList<>();
            private final int capacity;

            BoundedBuffer(int capacity) {
                this.capacity = capacity;
            }

            void put(int value) throws InterruptedException {
                synchronized (lock) {
                    while (queue.size() == capacity) {
                        lock.wait();
                    }
                    queue.add(value);
                    lock.notifyAll();
                }
            }

            int take() throws InterruptedException {
                synchronized (lock) {
                    while (queue.isEmpty()) {
                        lock.wait();
                    }
                    int value = queue.remove();
                    lock.notifyAll();
                    return value;
                }
            }
        }

        BoundedBuffer buffer = new BoundedBuffer(2);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    buffer.put(i);
                    System.out.println("produced " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "buffer-producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    int value = buffer.take();
                    System.out.println("consumed " + value);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "buffer-consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }

    // =========================================================
    // 5D: Why while, not if
    // =========================================================
    static void example4_WhyWhileNotIf() {
        System.out.println("\n=== 5D: Why while, not if ===");
        System.out.println("Rule 1: waiters can wake up without the condition becoming true.");
        System.out.println("Rule 2: notifyAll wakes many threads, but only one may be able to proceed.");
        System.out.println("Rule 3: a different thread may consume the condition before you reacquire the lock.");
        System.out.println("Therefore: always re-check the condition in a while-loop.");
    }
}