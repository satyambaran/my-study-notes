import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.PriorityQueue;

public class STLmap {
    public static void main(String[] args) {
        HashMap<Integer, Integer> hm = new HashMap<>();
        hm.put(1, 2);
        hm.put(1, 3);
        hm.putIfAbsent(1, 4);
        hm.putIfAbsent(2, 1);
        hm.putIfAbsent(2, 2);
        System.out.println(hm.toString());
        System.out.println(hm.keySet());
        System.out.println(hm.values());

        Set<Entry<Integer, Integer>> entries = hm.entrySet();
        for (Entry<Integer, Integer> k : entries) {
            System.out.println(k.getKey());
            System.out.println(k.getValue());
            System.out.println(k.toString());
            System.out.println(k.setValue(5));
        }
        for (Entry<Integer, Integer> k : entries) {
            System.out.println(k);
        }

        Iterator<Integer> iterator = hm.keySet().iterator();
        System.out.println(iterator.next());
        Consumer<Integer> action = (s) -> System.out.println(s + 100);
        iterator.forEachRemaining(action);

    }
}

class Person implements Comparable<Person> {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public int compareTo(Person other) {
        // ! need to override compareTo function to use PriorityQueue and TreeMap
        int nameCompare = this.name.compareTo(other.name);
        if (nameCompare != 0) {
            return nameCompare;
        }
        return Integer.compare(this.age, other.age);
    }

    @Override
    public int hashCode() {
        // ! need to override hashCode to use HashMap
        return Objects.hash(this);
    }

    @Override
    public String toString() {
        //! this is needed for all three
        return name + " (" + age + ")";
    }
}

class PriorityQueueExample {
    public static void main(String[] args) {
        PriorityQueue<Person> queue = new PriorityQueue<>();
        queue.add(new Person("Alice", 30));
        queue.add(new Person("Bob", 25));
        queue.add(new Person("Alice", 30)); // Duplicate based on compareTo

        while (!queue.isEmpty()) {
            System.out.println(queue.poll()); // Output: Bob (25), Alice (30), Alice (30)
        }
    }
}
