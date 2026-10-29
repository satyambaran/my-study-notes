package docs.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Part 2: WILDCARDS — ?, ? extends T, ? super T
 * ============================================================
 *
 * SYNTAX MEANING MNEMONIC
 * ────────────────── ────────────────────────────── ────────────
 * <?> any unknown type = ? extends Object
 * <? extends T> unknown type that IS-A T "at most T" / read
 * <? super T> unknown type that is PARENT of T "at least T" / write
 *
 * KEY INSIGHT:
 * ? extends T → safe to READ (as T), CANNOT write
 * ? super T → safe to WRITE (T into it), can only read as Object
 */
public class Part2_WildcardsAndBounds {

    public static void main(String[] args) {
        unboundedWildcard();
        upperBound_Extends();
        upperBound_ReadOnly();
        lowerBound_Super();
        lowerBound_WriteOnly();
        extendsVsSuperSideBySide();
    }

    // =========================================================
    // 2A: Unbounded Wildcard — <?>
    // =========================================================
    static void unboundedWildcard() {
        System.out.println("=== 2A: Unbounded Wildcard <?> ===");

        // <?> means "any type" — useful when you don't care about the element type
        List<String> strings = List.of("a", "b", "c");
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.1, 2.2);

        printSize(strings); // Size: 3
        printSize(ints); // Size: 3
        printSize(doubles); // Size: 2
    }

    static void printSize(List<?> list) {
        // Can call methods that don't depend on the element type
        System.out.println("Size: " + list.size());

        // Can read as Object (everything is an Object):
        Object first = list.get(0);
        System.out.println("First (as Object): " + first);

        // CANNOT write (except null):
        // list.add("x"); // ✗ COMPILE ERROR — don't know the actual type
    }

    // =========================================================
    // 2B: ? extends T — UPPER BOUND (read-from / producer)
    // =========================================================
    /*
     * "? extends Number" means: the actual type is Number OR a subclass
     * Could be: Number, Integer, Double, Long, Short, Float, ...
     *
     * SAFE TO READ — you get at least a Number
     * NOT SAFE TO WRITE — compiler doesn't know if it's a List<Integer>
     * or List<Double>, so it can't allow adds
     */
    static void upperBound_Extends() {
        System.out.println("\n=== 2B: ? extends T (upper bound) ===");

        List<Integer> ints = List.of(1, 2, 3, 4, 5);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);
        List<Long> longs = List.of(100L, 200L, 300L);

        // ALL work — Integer, Double, Long all extend Number
        System.out.println("Sum ints:    " + sumAll(ints)); // 15.0
        System.out.println("Sum doubles: " + sumAll(doubles)); // 7.5
        System.out.println("Sum longs:   " + sumAll(longs)); // 600.0

        // Without ? extends, ONLY List<Number> would be accepted—
        // List<Integer> would be REJECTED even though Integer IS-A Number!
        // (Because List<Integer> is NOT a subtype of List<Number> in Java)
    }

    static double sumAll(List<? extends Number> numbers) {
        double sum = 0;
        for (Number n : numbers) { // ✓ Safe — read as Number
            sum += n.doubleValue();
        }
        return sum;
    }

    // Demonstrating the read-only nature
    static void upperBound_ReadOnly() {
        System.out.println("\n=== 2B+: extends is READ-ONLY ===");

        List<Integer> ints = new ArrayList<>(List.of(10, 20, 30));
        List<? extends Number> nums = ints;

        // ✓ READING works — guaranteed to be at least Number:
        Number first = nums.get(0);
        Number second = nums.get(1);
        System.out.println("Read: " + first + ", " + second); // 10, 20

        // Can iterate:
        for (Number n : nums) {
            System.out.print(n.intValue() + " ");
        }
        System.out.println();

        // ✗ WRITING blocked:
        // nums.add(42); // ERROR — what if it's actually List<Double>?
        // nums.add(3.14); // ERROR — what if it's actually List<Integer>?
        // nums.add(null); // This is the ONLY thing allowed (null fits any type)

        // WHY? The compiler sees List<? extends Number> and thinks:
        // "This could be List<Integer>, List<Double>, or List<Number>.
        // I can't let you add an Integer because it might be List<Double>.
        // I can't let you add a Double because it might be List<Integer>."
    }

    // =========================================================
    // 2C: ? super T — LOWER BOUND (write-to / consumer)
    // =========================================================
    /*
     * "? super Integer" means: the actual type is Integer OR a superclass
     * Could be: Integer, Number, Object
     *
     * SAFE TO WRITE Integer — all those types can hold an Integer
     * READ only returns Object — compiler doesn't know if it's
     * Number or Object
     */
    static void lowerBound_Super() {
        System.out.println("\n=== 2C: ? super T (lower bound) ===");

        List<Integer> intList = new ArrayList<>();
        List<Number> numList = new ArrayList<>();
        List<Object> objList = new ArrayList<>();

        // ALL work — Integer, Number, Object are all supers of Integer
        fillWithInts(intList); // ✓ Integer super Integer
        fillWithInts(numList); // ✓ Number super Integer
        fillWithInts(objList); // ✓ Object super Integer

        System.out.println("intList: " + intList); // [1, 2, 3]
        System.out.println("numList: " + numList); // [1, 2, 3]
        System.out.println("objList: " + objList); // [1, 2, 3]

        // Without ? super, ONLY List<Integer> would be accepted—
        // List<Number> would be REJECTED even though it can hold Integers!
    }

    static void fillWithInts(List<? super Integer> list) {
        list.add(1); // ✓ Safe — whatever the type, it can hold Integer
        list.add(2);
        list.add(3);
    }

    // Demonstrating the write-only nature
    static void lowerBound_WriteOnly() {
        System.out.println("\n=== 2C+: super is WRITE-FRIENDLY ===");

        List<Number> numbers = new ArrayList<>();
        List<? super Integer> sink = numbers;

        // ✓ WRITING works — Integer fits into any supertype:
        sink.add(1);
        sink.add(2);
        sink.add(3);
        System.out.println("After writes: " + sink); // [1, 2, 3]

        // ✗ READING gives Object (not useful without casting):
        Object first = sink.get(0); // Only guaranteed to be Object
        // Integer i = sink.get(0); // ERROR — might be List<Number> or List<Object>
        System.out.println("Read as Object: " + first);

        // WHY? The compiler sees List<? super Integer> and thinks:
        // "This could be List<Integer>, List<Number>, or List<Object>.
        // I can safely ADD Integer (all those types accept it).
        // But I can't tell you what type you'll GET back — could be anything."
    }

    // =========================================================
    // 2D: Side-by-side comparison
    // =========================================================
    static void extendsVsSuperSideBySide() {
        System.out.println("\n=== 2D: extends vs super — side by side ===");

        List<Integer> integers = new ArrayList<>(List.of(10, 20, 30));

        // EXTENDS: read ✓, write ✗
        List<? extends Number> reader = integers;
        Number val = reader.get(0); // ✓ read as Number
        // Integer valInteger = 5;
        // reader.add(valInteger);
        // reader.add(42); // ✗ can't write
        System.out.println("extends read: " + val); // 10

        // SUPER: write ✓, read as Object only
        List<? super Integer> writer = integers;
        writer.add(40); // ✓ write Integer
        Object obj = writer.get(0); // ✓ read as Object only
        System.out.println("super write then read: " + obj); // 10
        System.out.println("list after super add:  " + integers); // [10, 20, 30, 40]

        /*
         * ──────────────────────────────────────────────────────────
         * Wildcard CAN READ as CAN WRITE USE WHEN
         * ──────────────────────────────────────────────────────────
         * ? extends T T NOTHING reading from
         * ? super T Object only T writing to
         * T (exact) T T both
         * ──────────────────────────────────────────────────────────
         */
    }
}
