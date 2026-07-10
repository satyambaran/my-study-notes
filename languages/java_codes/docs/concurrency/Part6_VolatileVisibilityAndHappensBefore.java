package docs.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================
 * Part 6: VOLATILE, MEMORY VISIBILITY, HAPPENS-BEFORE
 * ============================================================
 *
 * When multiple threads are involved, the problem is not only "who runs when?"
 * It is also "who can see which writes, and when?"
 *
 * There are two different concerns:
 *
 * 1. VISIBILITY
 * - If thread A writes a value, when does thread B see it?
 * - Without proper synchronization, B may keep reading a stale value.
 *
 * 2. ATOMICITY
 * - Does an operation happen as one indivisible step?
 * - count++ is NOT atomic.
 * - It is read -> add -> write.
 *
 * volatile helps with visibility, but it does NOT make compound actions atomic.
 *
 * KEY RULES:
 * - Use volatile for simple state publication / stop flags / latest value
 * visibility.
 * - Use synchronized, Lock, or Atomic* classes for compound state changes.
 * - happens-before is the formal memory-ordering guarantee in the Java Memory
 * Model.
 */
public class Part6_VolatileVisibilityAndHappensBefore {

    public static void main(String[] args) throws Exception {
        example1_VolatileStopFlag();
        example2_VolatileDoesNotFixCountPlusPlus();
        example3_HappensBeforeRules();
        example4_SafePublicationSketch();
    }

    // =========================================================
    // 6A: volatile as a stop flag
    // =========================================================
    /*
     * This is one of the classic good uses of volatile.
     *
     * One thread writes running=false.
     * Another thread repeatedly reads running.
     *
     * Because the field is volatile, the reader sees the latest write.
     */
    static void example1_VolatileStopFlag() throws InterruptedException {
        System.out.println("=== 6A: volatile stop flag ===");

        class Worker {
            private volatile boolean running = true;

            void stop() {
                running = false;
            }

            void runLoop() {
                long spins = 0;
                while (running) {
                    spins++;
                }
                System.out.println("worker stopped after spins=" + spins);
            }
        }

        Worker worker = new Worker();
        Thread thread = new Thread(worker::runLoop, "volatile-worker");
        thread.start();

        Thread.sleep(100);
        worker.stop();
        thread.join();
    }

    // =========================================================
    // 6B: volatile does not make count++ atomic
    // =========================================================
    /*
     * Even if a counter field is volatile, count++ still means:
     * 1. read current value
     * 2. add one
     * 3. write result back
     *
     * Two threads can read the same old value and both write back the same new
     * value.
     * One increment is lost.
     */
    static void example2_VolatileDoesNotFixCountPlusPlus() throws InterruptedException {
        System.out.println("\n=== 6B: volatile does not fix count++ ===");

        class VolatileCounter {
            volatile int value;

            void increment() {
                value++;
            }
        }

        VolatileCounter volatileCounter = new VolatileCounter();
        AtomicInteger atomicCounter = new AtomicInteger();

        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                volatileCounter.increment();
                atomicCounter.incrementAndGet();
            }
        };

        Thread t1 = new Thread(task, "inc-1");
        Thread t2 = new Thread(task, "inc-2");
        Thread t3 = new Thread(task, "inc-3");
        Thread t4 = new Thread(task, "inc-4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("expected total:  400000");
        System.out.println("volatile count:  " + volatileCounter.value + " (may be wrong)");
        System.out.println("atomic count:    " + atomicCounter.get() + " (correct)");
    }

    // =========================================================
    // 6C: happens-before rules you should know
    // =========================================================
    /*
     * happens-before means: if action A happens-before action B,
     * then B is guaranteed to observe the effects of A.
     *
     * HIGH-VALUE RULES:
     * - A write to a volatile field happens-before a later read of that same field.
     * - Exiting a synchronized block happens-before a later synchronized entry on
     * the same monitor.
     * - Thread.start() happens-before actions in the started thread.
     * - All actions in a thread happen-before another thread successfully returns
     * from join().
     * - Completion of a task happens-before Future.get() returns that result.
     */
    static void example3_HappensBeforeRules() throws InterruptedException {
        System.out.println("\n=== 6C: happens-before rules ===");

        class Holder {
            int value;
        }

        Holder holder = new Holder();

        Thread writer = new Thread(() -> holder.value = 42, "writer");
        writer.start();
        writer.join();

        // Because main joined writer, main is guaranteed to see writer's completed
        // writes.
        System.out.println("value after join: " + holder.value);

        final Object lock = new Object();
        synchronized (lock) {
            holder.value = 99;
        }
        synchronized (lock) {
            System.out.println("value after synchronized handoff on same lock: " + holder.value);
        }
    }

    // =========================================================
    // 6D: safe publication sketch
    // =========================================================
    /*
     * SAFE PUBLICATION means other threads will see a properly initialized object.
     *
     * Common safe publication mechanisms:
     * - store into a volatile field
     * - publish through synchronized code
     * - initialize in a static initializer
     * - publish via a thread-safe collection or concurrent structure
     * - use final fields correctly during construction
     */
    static void example4_SafePublicationSketch() {
        System.out.println("\n=== 6D: Safe publication ===");
        System.out.println("volatile = visibility of latest reference/value");
        System.out.println("synchronized = mutual exclusion + visibility boundary");
        System.out.println("Atomic* = atomic updates for specific patterns");
        System.out.println("final fields = stronger initialization guarantees when used correctly");
    }
}