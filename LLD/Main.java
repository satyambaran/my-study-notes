import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        List<String> names = List.of("Alice", "Bob", "Charlie");
        for (String name : names) {
            System.out.println(name);
        }
        HashMap<String, Integer> ages = new HashMap<>();
        ages.put("Alice", 30);
        ages.put("Bob", 25);
        ages.put("Charlie", 35);
        for (String name : ages.keySet()) {
            System.out.println(name + " is " + ages.get(name) + " years old.");
        }
        CustomMap<String, Integer> customMap = new CustomMap<>();
        customMap.put("Alice", 30);
        customMap.put("Bob", 25);
        customMap.put("Charlie", 35);
        for (String name : customMap.getKeys()) {
            System.out.println(name + " is " + customMap.get(name) + " years old.");
        }
        // After implementing, usage should look like:
        Counter c1 = Counter.getInstance();
        Counter c2 = Counter.getInstance();
        System.out.println("Same instance: " + (c1 == c2));
        for (int i = 0; i < 5; i++) {
            c1.increment();
        }
        System.out.println("Count after 5 increments: " + c1.getCount());
    }
}

class CustomMap<K, V> {
    List<K> keys;
    List<V> values;

    public CustomMap() {
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    public void put(K key, V value) {
        keys.add(key);
        values.add(value);
    }

    public V get(K key) {
        int index = keys.indexOf(key);
        if (index != -1) {
            return values.get(index);
        }
        return null;
    }

    public List<K> getKeys() { return keys; }
}

class Counter {
    private AtomicInteger count = new AtomicInteger(0);

    private Counter() {}

    private static volatile Counter instance;

    public static Counter getInstance() {
        if (instance == null) {
            synchronized (Counter.class) {
                if (instance == null) {
                    instance = new Counter();
                }
            }
        }
        return instance;
    }

    public synchronized void increment() { count.incrementAndGet(); }

    public int getCount() { return count.get(); }
}