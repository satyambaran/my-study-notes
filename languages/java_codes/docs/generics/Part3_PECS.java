package docs.generics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ============================================================
 * Part 3: PECS — Producer Extends, Consumer Super
 * ============================================================
 *
 * THE GOLDEN RULE (from Joshua Bloch's Effective Java):
 *
 * PECS = Producer Extends, Consumer Super
 *
 * If a parameterized type:
 * PRODUCES values (you read FROM it) → use ? extends T
 * CONSUMES values (you write TO it) → use ? super T
 * Does BOTH → use exact type T (no wildcard)
 *
 * MNEMONIC:
 * extends = EXIT (data comes OUT)
 * super = STORE (data goes IN)
 */
public class Part3_PECS {

    public static void main(String[] args) {
        basicPECS();
        copyExample();
        stackExample();
        maxExample();
        transformerExample();
    }

    // =========================================================
    // 3A: Basic PECS — copy from producer to consumer
    // =========================================================
    static void basicPECS() {
        System.out.println("=== 3A: Basic PECS ===");

        List<Integer> source = List.of(1, 2, 3); // PRODUCER: we read from it
        List<Number> destination = new ArrayList<>(); // CONSUMER: we write to it

        // source PRODUCES integers → ? extends Integer
        // destination CONSUMES integers → ? super Integer
        copy(source, destination);
        System.out.println("Copied: " + destination); // [1, 2, 3]

        // More combos that work:
        List<Double> doubles = List.of(1.1, 2.2);
        List<Object> objects = new ArrayList<>();

        copy(doubles, objects); // Double extends Number ✓, Object super Number ✓
        copy(source, objects); // Integer extends Number ✓, Object super Number ✓
        System.out.println("Objects: " + objects); // [1.1, 2.2, 1, 2, 3]
    }

    // Classic PECS: src=producer(extends), dest=consumer(super)
    static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) { // ✓ read from producer
            dest.add(item); // ✓ write to consumer
        }
    }

    // =========================================================
    // 3B: Real-world copy — from any Collection to any List
    // =========================================================
    static void copyExample() {
        System.out.println("\n=== 3B: Collection → List copy ===");

        // Works with ANY collection type as source
        java.util.Set<Integer> setSource = java.util.Set.of(10, 20, 30);
        List<Number> dest = new ArrayList<>();

        copyFrom(setSource, dest);
        System.out.println("From Set: " + dest); // [10, 20, 30] (order may vary)
    }

    static <T> void copyFrom(Collection<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }

    // =========================================================
    // 3C: Stack push-all / pop-into
    // =========================================================
    /*
     * pushAll → src PRODUCES elements we push → ? extends E
     * popInto → dst CONSUMES elements we pop → ? super E
     */
    static void stackExample() {
        System.out.println("\n=== 3C: Stack push/pop with PECS ===");

        MyStack<Number> stack = new MyStack<>();

        // Push Integers into Stack<Number> — Integer extends Number ✓
        List<Integer> ints = List.of(1, 2, 3);
        stack.pushAll(ints);
        System.out.println("After pushAll: " + stack); // [1, 2, 3]

        // Pop into List<Object> — Object super Number ✓
        List<Object> results = new ArrayList<>();
        stack.popInto(results);
        System.out.println("Popped into:   " + results); // [3, 2, 1]
    }

    static class MyStack<E> {
        private final List<E> data = new ArrayList<>();

        void push(E item) {
            data.add(item);
        }

        E pop() {
            if (data.isEmpty())
                throw new RuntimeException("empty");
            return data.remove(data.size() - 1);
        }

        boolean isEmpty() {
            return data.isEmpty();
        }

        // PRODUCER: src produces items → ? extends E
        void pushAll(Collection<? extends E> src) {
            for (E item : src) {
                push(item);
            }
        }

        // CONSUMER: dst consumes items → ? super E
        void popInto(Collection<? super E> dst) {
            while (!isEmpty()) {
                dst.add(pop());
            }
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }

    // =========================================================
    // 3D: max() — reading from a collection (extends)
    // =========================================================
    static void maxExample() {
        System.out.println("\n=== 3D: max() with extends ===");

        List<Integer> ints = List.of(3, 1, 4, 1, 5, 9, 2, 6);
        List<Double> doubles = List.of(2.7, 1.4, 3.1, 0.5);

        // Both work because Integer & Double are Comparable
        System.out.println("Max ints:    " + max(ints)); // 9
        System.out.println("Max doubles: " + max(doubles)); // 3.1
    }

    // Only READS from list → ? extends T
    static <T extends Comparable<T>> T max(List<? extends T> list) {
        if (list.isEmpty())
            throw new IllegalArgumentException("empty");
        T best = list.get(0);
        for (T item : list) {
            if (item.compareTo(best) > 0)
                best = item;
        }
        return best;
    }

    // =========================================================
    // 3E: Transform list — reading + writing (both PECS)
    // =========================================================
    static void transformerExample() {
        System.out.println("\n=== 3E: Transform with PECS ===");

        List<Integer> source = List.of(1, 2, 3, 4, 5);
        List<Object> dest = new ArrayList<>();

        // Transform each Integer → String, reading from source, writing to dest
        transform(source, dest, n -> "item-" + n);
        System.out.println("Transformed: " + dest); // [item-1, item-2, ...]

        // Another: Double source → Number dest
        List<Double> prices = List.of(9.99, 19.99, 29.99);
        List<Number> rounded = new ArrayList<>();
        transform(prices, rounded, d -> Math.round(d));
        System.out.println("Rounded: " + rounded); // [10, 20, 30]
    }

    // src=PRODUCER(extends), dest=CONSUMER(super)
    static <T, R> void transform(
            List<? extends T> src, // reads from → extends
            List<? super R> dest, // writes to → super
            java.util.function.Function<T, R> fn) {
        for (T item : src) {
            dest.add(fn.apply(item));
        }
    }

    /*
     * ============================================================
     * PECS DECISION FLOWCHART
     * ============================================================
     *
     * "Am I reading FROM this parameter or writing TO it?"
     *
     * Reading (producing values) → ? extends T
     * Writing (consuming values) → ? super T
     * Both → just T (no wildcard)
     * Don't care about type → ?
     *
     * REAL JDK EXAMPLES OF PECS:
     * Collections.copy(List<? super T> dest, List<? extends T> src)
     * Stream.forEach(Consumer<? super T> action)
     * Stream.map(Function<? super T, ? extends R> mapper)
     * Optional.orElseGet(Supplier<? extends T> supplier)
     * ============================================================
     */
}
