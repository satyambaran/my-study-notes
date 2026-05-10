import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * JAVA THREADING BASICS
 * =====================
 *
 * THREE WAYS TO CREATE THREADS:
 *
 * 1. Extend Thread class:
 *    - Override run() method.
 *    - Simple, but you lose ability to extend another class (single inheritance).
 *
 * 2. Implement Runnable interface:
 *    - Implement run() method, pass to new Thread(runnable).
 *    - Preferred: separates the task from the thread mechanism.
 *
 * 3. Lambda / Anonymous class:
 *    - new Thread(() -> { ... }).start();
 *    - Most concise for quick tasks.
 *
 * THREAD POOL (ThreadPoolExecutor):
 *    - Reuses a pool of threads instead of creating new ones per task.
 *    - Parameters: corePoolSize, maxPoolSize, keepAliveTime, workQueue.
 *    - If all threads busy and queue full -> RejectedExecutionException.
 *
 * KEY METHODS:
 *    - start()  -> Spawns a new OS thread and calls run().
 *    - join()   -> Calling thread blocks until this thread finishes.
 *    - sleep()  -> Pauses current thread for N milliseconds.
 *    - setDaemon(true) -> Daemon threads are killed when all non-daemon threads finish.
 *                         Use for background tasks (e.g., GC, heartbeat).
 *
 * INTERVIEW TIP: Never call run() directly — it executes in the CURRENT thread.
 *                Always use start() to spawn a new thread.
 */
public class ThreadBasics {

    public static void main(String[] args) throws InterruptedException {

        // === WAY 1: Extend Thread ===
        Thread t1 = new MyThread();
        t1.start();

        // === WAY 2: Implement Runnable ===
        Thread t2 = new Thread(new MyRunnable());
        t2.start();

        // === WAY 3: Lambda ===
        Thread t3 = new Thread(() -> {
            System.out.println("[Lambda] Running in: " + Thread.currentThread().getName());
        });
        t3.start();

        // === Wait for all to finish ===
        t1.join();
        t2.join();
        t3.join();
        System.out.println("All basic threads done.\n");

        // === THREAD POOL EXAMPLE ===
        threadPoolDemo();
    }

    /**
     * ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue)
     *
     * - corePoolSize=2: Always keep 2 threads alive.
     * - maxPoolSize=4: Scale up to 4 threads if queue is full.
     * - keepAliveTime=30s: Idle threads beyond core are killed after 30s.
     * - workQueue capacity=5: Hold up to 5 tasks in queue.
     *
     * Flow: task arrives ->
     *   1. If <corePoolSize threads -> create new thread.
     *   2. If core threads busy -> put in queue.
     *   3. If queue full & <maxPoolSize -> create new thread.
     *   4. If queue full & maxPoolSize reached -> reject (RejectedExecutionException).
     */
    static void threadPoolDemo() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5)
        );

        for (int i = 1; i <= 8; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("[Pool] Task " + taskId + " on " + Thread.currentThread().getName());
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            });
        }

        executor.shutdown(); // Finish queued tasks, then terminate
    }
}

/** Way 1: Extend Thread — simple but inflexible (can't extend another class) */
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("[MyThread] Running in: " + Thread.currentThread().getName());
    }
}

/** Way 2: Implement Runnable — preferred, separates task from thread */
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("[MyRunnable] Running in: " + Thread.currentThread().getName());
    }
}
