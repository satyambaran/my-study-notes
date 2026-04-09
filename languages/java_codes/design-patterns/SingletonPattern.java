/**
 * SINGLETON PATTERN — Double-Checked Locking
 * ============================================
 *
 * INTENT: Ensure a class has exactly ONE instance, with a global access point.
 *
 * USE CASES:
 * - Database connection pool, Logger, Configuration manager, Thread pool.
 *
 * WHY DOUBLE-CHECKED LOCKING?
 * - Lazy initialization (instance created only when first needed).
 * - Thread-safe without synchronizing every call to getInstance().
 * - Only synchronizes on the FIRST creation; subsequent calls skip the lock.
 *
 * WHY `volatile`?
 * - Without volatile, Thread A might see a partially-constructed object.
 * - The JVM can reorder instructions:
 *     1. Allocate memory
 *     2. Assign reference to `instance` <- Thread B sees non-null here!
 *     3. Call constructor              <- But object isn't fully initialized
 * - `volatile` prevents this reordering (establishes a happens-before relationship).
 *
 * OTHER WAYS TO IMPLEMENT SINGLETON:
 *
 * 1. Eager initialization (simplest, no threading issues):
 *    private static final Singleton INSTANCE = new Singleton();
 *
 * 2. Enum singleton (recommended by Joshua Bloch, Effective Java):
 *    enum Singleton { INSTANCE; }
 *    - Thread-safe, serialization-safe, reflection-proof.
 *
 * 3. Bill Pugh (static inner class):
 *    private static class Holder { static final Singleton INSTANCE = new Singleton(); }
 *    - Lazy + thread-safe without synchronization (uses classloader guarantee).
 *
 * ANTI-PATTERNS TO AVOID:
 * - Don't make Singleton serializable without readResolve().
 * - Don't assume Singleton survives different ClassLoaders.
 */
public class SingletonPattern {

    // volatile ensures visibility + prevents instruction reordering
    private static volatile SingletonPattern instance = null;

    // Private constructor prevents external instantiation
    private SingletonPattern() {
        // Prevent reflection attacks
        if (instance != null) {
            throw new RuntimeException("Use getInstance() instead");
        }
    }

    public static SingletonPattern getInstance() {
        if (instance == null) {                      // 1st check (no lock)
            synchronized (SingletonPattern.class) {  // Lock only on first creation
                if (instance == null) {              // 2nd check (with lock)
                    instance = new SingletonPattern();
                }
            }
        }
        return instance;
    }

    // Demo
    public static void main(String[] args) {
        SingletonPattern s1 = SingletonPattern.getInstance();
        SingletonPattern s2 = SingletonPattern.getInstance();
        System.out.println("Same instance? " + (s1 == s2)); // true
    }
}
