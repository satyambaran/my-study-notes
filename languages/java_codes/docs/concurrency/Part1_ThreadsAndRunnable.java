package docs.concurrency;

/**
 * ============================================================
 * Part 1: THREADS AND RUNNABLE
 * ============================================================
 *
 * BIG PICTURE:
 * - A process is a running program.
 * - A thread is an execution path inside that process.
 * - Multiple threads in the same process share heap memory.
 * - Each thread has its own call stack.
 *
 * WHY THREADS EXIST:
 * - Run multiple tasks at the same time.
 * - Keep a program responsive while slow work happens elsewhere.
 * - Overlap I/O waits with useful work.
 *
 * CORE IDEA:
 * - Thread = the thing that runs.
 * - Runnable = the task to run.
 *
 * PRACTICAL RULE:
 * Prefer implementing Runnable (or using ExecutorService) instead of
 * subclassing Thread directly. It separates "what work to do" from
 * "how the work is scheduled".
 */
public class Part1_ThreadsAndRunnable {

    public static void main(String[] args) throws InterruptedException {
        example1_ThreadVsRunnable();
        example2_LifecycleAndJoin();
        example3_Interrupts();
        example4_DaemonThreads();
    }

    // =========================================================
    // 1A: Thread vs Runnable
    // =========================================================
    static void example1_ThreadVsRunnable() throws InterruptedException {
        System.out.println("=== 1A: Thread vs Runnable ===");

        // Less flexible: subclassing Thread couples task + execution mechanism.
        class GreetingThread extends Thread {
            @Override
            public void run() {
                System.out.println("Hello from subclassed Thread on " + Thread.currentThread().getName());
            }
        }

        // Better default: Runnable only describes the work.
        Runnable task = () -> {
            System.out.println("Hello from Runnable on " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable task2 = () -> {
            System.out.println("Hello from Runnable on " + Thread.currentThread().getName());
        };
        Thread t1 = new GreetingThread();
        Thread t2 = new Thread(task, "worker-runnable");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // Calling run() directly does NOT start a new thread.
        // It just runs synchronously on the current thread.
        task.run();
        task2.run();
    }

    // =========================================================
    // 1B: Lifecycle and join()
    // =========================================================
    static void example2_LifecycleAndJoin() throws InterruptedException {
        System.out.println("\n=== 1B: Thread lifecycle and join() ===");

        Thread worker = new Thread(() -> {
            System.out.println("worker state inside run: " + Thread.currentThread().getState());
            for (int i = 1; i <= 3; i++) {
                System.out.println("working step " + i);
            }
        }, "lifecycle-worker");

        System.out.println("before start: " + worker.getState()); // NEW
        worker.start();
        System.out.println("after start:  " + worker.getState()); // usually RUNNABLE

        // join() blocks the caller until the other thread finishes.
        worker.join();
        System.out.println("after join:   " + worker.getState()); // TERMINATED
    }

    // =========================================================
    // 1C: Interrupts
    // =========================================================
    /*
     * Interrupt is Java's cooperative cancellation signal.
     *
     * It does NOT forcibly kill a thread.
     * It sets the interrupted flag, and blocking methods like sleep()
     * react by throwing InterruptedException.
     *
     * RULE:
     * - Either propagate InterruptedException, or
     * - restore the interrupt with Thread.currentThread().interrupt()
     */
    static void example3_Interrupts() throws InterruptedException {
        System.out.println("\n=== 1C: Interrupts ===");

        Thread sleeper = new Thread(() -> {
            try {
                System.out.println("sleeper: going to sleep");
                Thread.sleep(5_000);
                System.out.println("sleeper: woke normally");
            } catch (InterruptedException e) {
                System.out.println("sleeper: interrupted while sleeping");
                Thread.currentThread().interrupt();
            }

            System.out.println("sleeper: interrupted flag = " + Thread.currentThread().isInterrupted());
        }, "interrupt-demo");

        sleeper.start();
        Thread.sleep(100);
        sleeper.interrupt();
        sleeper.join();
    }

    // =========================================================
    // 1D: Daemon threads
    // =========================================================
    /*
     * Daemon threads are background helpers.
     * The JVM may exit when only daemon threads remain.
     *
     * Examples: metrics loop, cache cleanup, background monitors.
     *
     * RULE:
     * Never put critical cleanup or must-finish business logic only on daemons.
     */
    static void example4_DaemonThreads() throws InterruptedException {
        System.out.println("\n=== 1D: Daemon threads ===");

        Thread daemon = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("daemon heartbeat from " + Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "daemon-monitor");

        daemon.setDaemon(true);
        daemon.start();

        Thread.sleep(650);
        daemon.interrupt();
        Thread.sleep(50);
        System.out.println("main thread done");
        

    }
}