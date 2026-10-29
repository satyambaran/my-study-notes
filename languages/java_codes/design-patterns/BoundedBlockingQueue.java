import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fixed-capacity FIFO queue whose producers wait while it is full and whose
 * consumers wait while it is empty.
 *
 * <p>
 * All mutable queue state is protected by {@link #lock}. The class is final
 * so a subclass cannot access the public methods while bypassing these
 * synchronization invariants.
 */
public final class BoundedBlockingQueue<T> {
    // Object[] avoids creating a generic array, which Java does not permit.
    // The array reference is final because the queue capacity never changes.
    private final Object[] elements;

    // head points to the next element to remove; tail points to the next free
    // slot. size distinguishes an empty ring from a full ring when head == tail.
    private int head;
    private int tail;
    private int size;

    // A fair lock can be selected through the second constructor. Fairness can
    // reduce starvation, although it normally costs some throughput.
    private final ReentrantLock lock;

    // Producers await notFull; consumers await notEmpty. Separate conditions
    // avoid waking threads that cannot make progress after a state change.
    private final Condition notFull;
    private final Condition notEmpty;

    /**
     * Creates a queue using the higher-throughput, non-fair lock policy.
     *
     * @param capacity maximum number of elements
     * @throws IllegalArgumentException if capacity is not positive
     */
    public BoundedBlockingQueue(int capacity) {
        this(capacity, false);
    }

    /**
     * Creates a queue with a configurable lock fairness policy.
     *
     * @param capacity maximum number of elements
     * @param fair     whether the longest-waiting thread should be favored
     * @throws IllegalArgumentException if capacity is not positive
     */
    public BoundedBlockingQueue(int capacity, boolean fair) {
        // Zero capacity would make both producers and consumers wait forever.
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        elements = new Object[capacity];
        lock = new ReentrantLock(fair);

        // Conditions must be created from the same lock that protects size,
        // head, tail, and the backing array.
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
    }

    /**
     * Inserts an element, waiting until space becomes available when necessary.
     *
     * @throws NullPointerException if item is null
     * @throws InterruptedException if interrupted while acquiring the lock or
     *                              waiting for space
     */
    public void put(T item) throws InterruptedException {
        // Null is rejected because a null slot represents unused storage.
        Objects.requireNonNull(item, "item");

        // Unlike lock(), lockInterruptibly() honors cancellation while this
        // producer is waiting to acquire the lock.
        lock.lockInterruptibly();
        try {
            // await() can wake spuriously or another producer can fill the newly
            // available slot first, so the condition must be rechecked in a loop.
            while (size == elements.length) {
                // await() atomically releases the lock, suspends this thread,
                // then reacquires the lock before returning.
                notFull.await();
            }

            elements[tail] = item;
            tail = (tail + 1) % elements.length; // Wrap around the ring buffer.
            size++;

            // One new item can satisfy one waiting consumer. signal() transfers
            // one waiter to the lock queue; it does not immediately grant the lock.
            notEmpty.signal();
        } finally {
            // Always release the lock, including after interruption or failure.
            lock.unlock();
        }
    }

    /**
     * Removes and returns the oldest element, waiting when the queue is empty.
     *
     * @throws InterruptedException if interrupted while acquiring the lock or
     *                              waiting for an element
     */
    public T take() throws InterruptedException {
        // Consumers are cancellable even while contending to acquire the lock.
        lock.lockInterruptibly();
        try {
            // Use while rather than if for spurious wakeups and competition
            // between multiple consumers awakened at different times.
            while (size == 0) {
                notEmpty.await();
            }

            // Only values of T enter through put(), so this localized cast is
            // safe even though the backing storage has type Object[].
            @SuppressWarnings("unchecked")
            T item = (T) elements[head];

            // Clear the reference so removed objects can be garbage-collected.
            elements[head] = null;
            head = (head + 1) % elements.length; // Wrap around the ring buffer.
            size--;

            // Removing one item creates one free slot for one waiting producer.
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Compatibility alias for the original API. Prefer {@link #take()} because
     * it matches the terminology used by Java's BlockingQueue interface.
     */
    public T get() throws InterruptedException {
        return take();
    }

    /**
     * Returns a consistent snapshot of the current element count.
     */
    public int size() {
        lock.lock();
        try {
            // Reading under the same lock provides visibility of producer and
            // consumer updates; size therefore does not need to be volatile.
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of additional elements that can be inserted.
     */
    public int remainingCapacity() {
        lock.lock();
        try {
            return elements.length - size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the queue's fixed capacity.
     */
    public int capacity() {
        // elements is final and its length never changes, so no lock is needed.
        return elements.length;
    }
}
