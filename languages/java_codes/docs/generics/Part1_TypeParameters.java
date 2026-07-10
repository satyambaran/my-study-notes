package docs.generics;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * ============================================================
 * Part 1: TYPE PARAMETERS — <T>, <R>, <E>, <V>
 * ============================================================
 *
 * Type parameters are PLACEHOLDER NAMES for types. Conventions:
 *
 * <T> — "Type" (general-purpose)
 * <R> — "Return type" (result of a function)
 * <E> — "Element" (element of a collection)
 * <V> — "Value" (extra type in chaining)
 * <S> — "Source" (original type in a pipeline)
 * <K> — "Key" (map keys)
 *
 * They are ERASED at runtime (type erasure). The compiler uses
 * them for compile-time safety only.
 *
 * WHERE THEY CAN BE DECLARED:
 * - On a class: class Box<T> { ... }
 * - On an instance method: public <R> R convert(T input)
 * - On a static method: public static <E> List<E> of(E...)
 *
 * RULE: You can only USE a type variable if it was DECLARED somewhere:
 * - On the class → visible to all instance methods
 * - On the method → visible only in that method
 * - Static methods → MUST declare their own (can't see class-level)
 */
public class Part1_TypeParameters {

    public static void main(String[] args) {
        classLevelTypeParams();
        methodLevelTypeParam();
        staticMethodTypeParam();
        whyStaticNeedsOwnTypeParam();
    }

    // =========================================================
    // 1A: Class-level type params
    // =========================================================
    // Declared on the CLASS → available to all instance methods.

    static class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        // A and B come from the class — no need to re-declare
        A getFirst() {
            return first;
        }

        B getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    static void classLevelTypeParams() {
        System.out.println("=== 1A: Class-level type params ===");

        Pair<String, Integer> nameAge = new Pair<>("Alice", 30);
        // A=String, B=Integer — resolved when constructing

        String name = nameAge.getFirst(); // returns A=String
        Integer age = nameAge.getSecond(); // returns B=Integer

        System.out.println(nameAge); // (Alice, 30)
        System.out.println("Name: " + name + ", Age: " + age);

        // Different types for different instances:
        Pair<Double, Boolean> score = new Pair<>(99.5, true);
        System.out.println(score); // (99.5, true)
    }

    // =========================================================
    // 1B: Method-level type param — public <R> R convert(T input)
    // =========================================================
    // <R> is declared ON THE METHOD, not on the class.
    // T comes from the class; R is new and resolved per call.
    //
    // WHY needed?
    // public R convert(T input) ← ✗ COMPILE ERROR: R not declared
    // public <R> R convert(T input) ← ✓ R declared right here

    static class Box<T> {
        private final T value;

        Box(T value) {
            this.value = value;
        }

        T getValue() {
            return value;
        } // T from class — fine

        // <R> declared on this method — R is decided per call site
        <R> R convert(Function<T, R> transformer) {
            return transformer.apply(value);
        }
    }

    static void methodLevelTypeParam() {
        System.out.println("\n=== 1B: Method-level <R> ===");

        Box<String> box = new Box<>("hello");

        // Each call decides R from context:
        Integer len = box.convert(s -> s.length()); // R=Integer
        Boolean empty = box.convert(s -> s.isEmpty()); // R=Boolean
        String upper = box.convert(s -> s.toUpperCase()); // R=String
        char first = box.convert(s -> s.charAt(0)); // R=Character (unboxed)

        System.out.println("length:    " + len); // 5
        System.out.println("isEmpty:   " + empty); // false
        System.out.println("uppercase: " + upper); // HELLO
        System.out.println("charAt(0): " + first); // h

        // Explicit type witness (rarely needed):
        Number n = box.<Number>convert(s -> s.length()); // R=Number
        System.out.println("explicit:  " + n); // 5

        // Multiple conversions from the same box — T is fixed, R varies each time
        Box<Integer> numBox = new Box<>(42);
        String asStr = numBox.convert(i -> "value=" + i); // R=String
        Double asD = numBox.convert(i -> i * 1.5); // R=Double
        Boolean isEven = numBox.convert(i -> i % 2 == 0); // R=Boolean

        System.out.println("asString: " + asStr); // value=42
        System.out.println("asDouble: " + asD); // 63.0
        System.out.println("isEven:   " + isEven); // true
    }

    // =========================================================
    // 1C: Static method type param — public static <E> List<E> of(E...)
    // =========================================================
    // Static methods CAN'T see class type params (no instance → no T).
    // They must declare their OWN type params.
    //
    // WHY needed?
    // public static List<E> of(E... data) ← ✗ ERROR: E not declared
    // public static <E> List<E> of(E... data) ← ✓ E declared on method

    @SafeVarargs
    static <E> List<E> listOf(E... elements) {
        return Arrays.asList(elements);
    }

    static void staticMethodTypeParam() {
        System.out.println("\n=== 1C: Static method <E> ===");

        // E is inferred from the arguments:
        List<String> words = listOf("alpha", "beta", "gamma");
        List<Integer> nums = listOf(10, 20, 30);
        List<Double> doubles = listOf(1.1, 2.2, 3.3);

        System.out.println("strings:  " + words); // [alpha, beta, gamma]
        System.out.println("integers: " + nums); // [10, 20, 30]
        System.out.println("doubles:  " + doubles); // [1.1, 2.2, 3.3]

        // Mixed types → must specify E explicitly as common supertype:
        List<Number> mixed = Part1_TypeParameters.<Number>listOf(1, 2.5, 3L);
        System.out.println("mixed:    " + mixed); // [1, 2.5, 3]

        // Empty call → nothing to infer from, specify explicitly:
        List<String> empty = Part1_TypeParameters.<String>listOf();
        System.out.println("empty:    " + empty); // []
    }

    // =========================================================
    // 1D: Why static NEEDS its own type param
    // =========================================================

    static class Demo<T> {
        T instanceField;

        // ✓ Instance method can use T from the class
        T getField() {
            return instanceField;
        }

        //! ✗ This would NOT compile:
        // static T broken() { return null; }
        // static T broken2(T obj) { return obj; }
        // → "Cannot make a static reference to the non-static type T"
        // → Because: Demo<String> and Demo<Integer> share the SAME static methods
        // So which T would it be? String? Integer? Ambiguous.

        // ✓ Static method declares its own:
        static <E> E echo(E input) {
            return input;
        }
    }

    static void whyStaticNeedsOwnTypeParam() {
        System.out.println("\n=== 1D: Why static needs <E> ===");

        // Both share the SAME static method — T is irrelevant:
        String s = Demo.echo("hello"); // E=String
        Integer i = Demo.echo(42); // E=Integer
        System.out.println(s + ", " + i); // hello, 42

        // The class's <T> is per-instance:
        Demo<String> d1 = new Demo<>();
        d1.instanceField = "text";

        Demo<Integer> d2 = new Demo<>();
        d2.instanceField = 100;

        // But static methods belong to Demo itself, not Demo<String> or Demo<Integer>
        // That's why they can't reference T — it doesn't exist in static context.
    }

    /*
     * ============================================================
     * SUMMARY
     * ============================================================
     *
     * DECLARATION SITE VISIBLE TO EXAMPLE
     * ────────────────────── ───────────────────── ──────────────────
     * class Box<T> all instance methods T getValue()
     * public <R> R fn(T in) this method only R is per-call
     * static <E> List<E> of() this method only E inferred from args
     *
     * WHY the <R> / <E> is mandatory:
     * You can only USE a type if it's been DECLARED.
     * class-level types → declared on the class
     * method-only types → declared before return type: <R>
     * static methods → must always self-declare (can't see class types)
     * ============================================================
     */
}
