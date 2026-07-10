import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// https://chatgpt.com/c/69c6c97c-497c-8320-b002-3f9124e28881
public class BoundedBlockingQueue<T> {
    private Object[] arr;
    private int capacity, head = 0, tail = 0, size = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    BoundedBlockingQueue(int capacity) {
        arr = new Object[capacity];
        this.capacity = capacity;
    }

    public void put(T item) {
        lock.lock();
        try {
            while (size == capacity) {
                notFull.await();
            }
            arr[tail] = item;
            tail = (tail + 1) % capacity;
            size++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T get() {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            T item = (T) arr[head];
            arr[head] = null;
            head = (head + 1) % capacity;
            size--;
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }
}

/*
 * Why while instead of if?
 * Multiple threads could wake up and Condition might no longer hold true
 * 
 * signal() wakes ONE thread
 * signalAll() wakes ALL
 * 
 * Thread A:
 * lock()
 * → await()
 * → → RELEASE LOCK
 * → → WAIT
 * 
 * Thread B:
 * lock() ✅
 * → modify queue
 * → signal()
 * → unlock()
 * 
 * Thread A:
 * wakes up
 * → Competes for lock
 * → re-acquire lock
 * → continue after await()
 * 
 * 🔔 Internally, await() does:
 * Releases the lock
 * Puts Thread A into waiting queue (notFull condition queue)
 * Suspends Thread A
 * 
 * 🔔 What signal() does
 * Picks one waiting thread from notFull
 * Moves it to lock queue (not runnable yet)
 * 👉 Important: It does NOT give the lock immediately
 */
