/**
 * EVEN-ODD PRINTER USING wait()/notify()
 * ========================================
 *
 * CONCEPT: Two threads alternate printing even and odd numbers using
 * intrinsic locks (synchronized) with wait/notify signaling.
 *
 * KEY MECHANISM:
 * - A shared boolean flag (`isOddTurn`) controls which thread runs.
 * - The thread whose turn it is prints, flips the flag, and calls notify().
 * - The other thread is blocked in wait() until notified.
 *
 * WHY synchronized + wait/notify?
 * - synchronized ensures mutual exclusion (only one thread in the block at a time).
 * - wait() releases the lock and suspends the thread until notify() is called.
 * - notify() wakes up one waiting thread so it can re-acquire the lock.
 *
 * PATTERN: This is the classic "turn-based" coordination pattern.
 *
 * COMMON INTERVIEW FOLLOW-UPS:
 * - "What if you use notifyAll() instead of notify()?" -> Works the same here
 *    since only 2 threads. notifyAll() is safer when >2 threads share the lock.
 * - "Why use while() instead of if() for the wait condition?" -> Spurious wakeups.
 *    The JVM can wake a thread without notify(), so always re-check the condition.
 * - "Can you do this with Lock/Condition instead?" -> See SynchronizationMechanisms.java
 *
 * OUTPUT: Odd: 1, Even: 2, Odd: 3, Even: 4, ... Odd: 9, Even: 10
 */
public class EvenOddWithWaitNotify {

    private final int limit;
    private boolean isOddTurn = true;

    public EvenOddWithWaitNotify(int limit) {
        this.limit = limit;
    }

    public void printOdd() {
        synchronized (this) {
            for (int i = 1; i <= limit; i += 2) {
                // Wait until it's the odd thread's turn
                while (!isOddTurn) {
                    try {
                        wait(); // Releases the lock, thread sleeps
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt flag
                    }
                }
                System.out.println("Odd: " + i);
                isOddTurn = false; // Hand control to even thread
                notify();          // Wake up the even thread
            }
        }
    }

    public void printEven() {
        synchronized (this) {
            for (int i = 2; i <= limit; i += 2) {
                // Wait until it's the even thread's turn
                while (isOddTurn) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Even: " + i);
                isOddTurn = true; // Hand control to odd thread
                notify();         // Wake up the odd thread
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        EvenOddWithWaitNotify printer = new EvenOddWithWaitNotify(10);

        // Method references (::) are syntactic sugar for lambda: () -> printer.printOdd()
        Thread oddThread = new Thread(printer::printOdd, "Odd-Thread");
        Thread evenThread = new Thread(printer::printEven, "Even-Thread");

        oddThread.start();
        evenThread.start();

        // join() blocks the main thread until these threads finish
        oddThread.join();
        evenThread.join();
        System.out.println("Done!");
    }
}
