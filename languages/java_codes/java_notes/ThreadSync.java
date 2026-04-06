import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSync {
    public static void main(String[] args) {

    }

    Semaphore semaphore = new Semaphore(2); // Allows 2 threads at a time

    public void accessResource() {
        try {
            semaphore.acquire(); // Acquire a permit
            System.out.println(Thread.currentThread().getName() + " accessing resource");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release(); // Release a permit
        }
    }

    public synchronized void increment() {
        count++;
    }

    public void increment2() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}
