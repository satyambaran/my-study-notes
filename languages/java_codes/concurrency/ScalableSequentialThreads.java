/**
 * N THREADS PRINTING SEQUENTIALLY
 * =================================
 *
 * CONCEPT: N threads take turns printing numbers 1..max in order.
 * Thread-1 prints 1, Thread-2 prints 2, ..., Thread-N prints N,
 * Thread-1 prints N+1, and so on.
 *
 * KEY MECHANISM:
 * - A shared counter `count` tracks the next number to print.
 * - Each thread checks: is it MY turn? (count % totalThreads == myId)
 *   - YES -> print, increment count, notifyAll() to wake others.
 *   - NO  -> wait() until someone else prints.
 *
 * WHY notifyAll() instead of notify()?
 * - With N threads, notify() wakes only ONE arbitrary thread.
 * - That thread might not be the one whose turn it is -> deadlock risk.
 * - notifyAll() wakes ALL threads; each re-checks its condition.
 * - Slightly less efficient but correct. For production, use Condition per thread.
 *
 * SCALABILITY NOTE:
 * - This uses a single lock -> all threads contend on it.
 * - For high N, consider using N Condition objects (one per thread) for targeted wakeups.
 *
 * OUTPUT (N=3, max=9): T1:1 T2:2 T3:3 T1:4 T2:5 T3:6 T1:7 T2:8 T3:9
 */
public class ScalableSequentialThreads {

    private static final Object lock = new Object();
    private static int count = 1;
    private static final int MAX = 15;           // Total numbers to print
    private static final int NUM_THREADS = 3;    // Number of threads

    public static void main(String[] args) {
        for (int i = 0; i < NUM_THREADS; i++) {
            new Thread(new SequentialPrinter(i + 1, NUM_THREADS)).start();
        }
    }

    static class SequentialPrinter implements Runnable {
        private final int threadId;       // 1-based thread ID
        private final int totalThreads;

        SequentialPrinter(int threadId, int totalThreads) {
            this.threadId = threadId;
            this.totalThreads = totalThreads;
        }

        @Override
        public void run() {
            synchronized (lock) {
                while (count <= MAX) {
                    // This thread's turn when: count maps to its threadId
                    // Thread-1 handles count=1,4,7,... Thread-2 handles count=2,5,8,...
                    if ((count - 1) % totalThreads == threadId - 1) {
                        System.out.println("T" + threadId + ":" + count);
                        count++;
                        lock.notifyAll(); // Wake ALL waiting threads
                    } else {
                        try {
                            lock.wait(); // Not my turn, sleep
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                lock.notifyAll(); // Ensure no thread remains stuck in wait() after MAX
            }
        }
    }
}
