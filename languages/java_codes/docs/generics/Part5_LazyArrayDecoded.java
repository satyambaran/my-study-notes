package docs.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * ============================================================
 * Part 5: DECODING LazyArray SIGNATURES
 * ============================================================
 *
 * Walks through each method in LazyArray and explains
 * exactly why the generics are written the way they are.
 *
 * LazyArray<S, T>:
 * S = Source element type (fixed for life of the chain)
 * T = Current "view" element type (changes with each map)
 */
public class Part5_LazyArrayDecoded {

    public static void main(String[] args) {
        staticFactory_of();
        varargs_of();
        map_decoded();
        pipeline_walkthrough();
        wildcards_in_map();
    }

    // =========================================================
    // 5A: public static <E> LazyArray<E, E> of(List<E> data)
    // =========================================================
    /*
     * BREAKDOWN:
     * static → belongs to the class, not an instance
     * <E> → declares method-level type param (static can't see S, T)
     * LazyArray<E, E> → returns S=E, T=E (source = view, no transforms yet)
     * List<E> data → E is inferred from the argument
     *
     * WHY static needs <E>:
     * The class has <S, T>, but static methods can't access them.
     * So of() declares its own <E> and sets both S and T to E.
     *
     * INSIDE:
     * List<E> copy = List.copyOf(data); // defensive copy
     * Function<E, E> pipeline = Function.identity(); // no-op transform
     * return new LazyArray<>(copy, pipeline);
     */
    static void staticFactory_of() {
        System.out.println("=== 5A: of(List<E>) decoded ===");

        // E is inferred from the list's element type:
        List<String> words = List.of("hello", "world", "java");
        MiniLazy<String, String> a = MiniLazy.of(words);
        // E=String → LazyArray<String, String>
        // pipeline = identity: "hello" → "hello" (no transform)

        System.out.println("of(List): " + a.toList()); // [hello, world, java]

        List<Integer> nums = List.of(10, 20, 30);
        MiniLazy<Integer, Integer> b = MiniLazy.of(nums);
        // E=Integer → LazyArray<Integer, Integer>

        System.out.println("of(List): " + b.toList()); // [10, 20, 30]
    }

    // =========================================================
    // 5B: public static <E> LazyArray<E, E> of(E... data)
    // =========================================================
    /*
     * BREAKDOWN:
     * 
     * @SafeVarargs → suppresses heap pollution warning (safe: read-only)
     * E... data → varargs: caller writes of("a", "b", "c")
     * Java creates E[] behind the scenes
     * Delegates to: → of(Arrays.asList(data))
     *
     * E is inferred from the individual arguments.
     */
    static void varargs_of() {
        System.out.println("\n=== 5B: of(E...) decoded ===");

        MiniLazy<String, String> a = MiniLazy.of("alpha", "beta", "gamma");
        // E=String (inferred from args)
        System.out.println("of(varargs): " + a.toList());

        MiniLazy<Integer, Integer> b = MiniLazy.of(1, 2, 3, 4, 5);
        // E=Integer
        System.out.println("of(varargs): " + b.toList());
    }

    // =========================================================
    // 5C: public <R> LazyArray<S, R> map(Function<? super T, ? extends R> fn)
    // =========================================================
    /*
     * BREAKDOWN:
     * <R> → method-level type: the NEW view type after mapping
     * LazyArray<S, R> → S unchanged (source stays), T becomes R
     * Function<? super T, ? extends R> fn:
     * ? super T → fn CONSUMES our T (can accept T or parent)
     * ? extends R → fn PRODUCES R (can return R or subtype)
     *
     * INSIDE:
     * Function<S, R> next = this.pipeline.andThen(fn::apply);
     * this.pipeline = Function<S, T> (source → current view)
     * fn::apply = T → R (current → new view)
     * next = Function<S, R> (source → new view)
     *
     * return new LazyArray<>(this.source, next);
     * Same source, updated pipeline. NOTHING COMPUTED YET.
     */
    static void map_decoded() {
        System.out.println("\n=== 5C: map() decoded ===");

        MiniLazy<String, String> words = MiniLazy.of("hello", "world", "java");

        // map(String::length): fn is Function<String, Integer>
        // ? super String accepts String ✓
        // ? extends Integer returns Integer ✓
        // R = Integer
        MiniLazy<String, Integer> lengths = words.map(String::length);
        // pipeline = identity.andThen(String::length)
        // S=String stays, T changes from String → Integer

        System.out.println("After map(length): " + lengths.toList()); // [5, 5, 4]

        // map(n -> "len=" + n): fn is Function<Integer, String>
        // R = String
        MiniLazy<String, String> formatted = lengths.map(n -> "len=" + n);
        // pipeline = identity.andThen(length).andThen(format)

        System.out.println("After map(format): " + formatted.toList()); // [len=5, len=5, len=4]
    }

    // =========================================================
    // 5D: Full pipeline walkthrough — step by step
    // =========================================================
    static void pipeline_walkthrough() {
        System.out.println("\n=== 5D: Pipeline walkthrough ===");

        // STEP 1: Create
        MiniLazy<String, String> step1 = MiniLazy.of("hello", "world");
        // source = ["hello", "world"]
        // pipeline = identity: String → String
        // S=String, T=String

        // STEP 2: map(String::toUpperCase)
        MiniLazy<String, String> step2 = step1.map(String::toUpperCase);
        // source = ["hello", "world"] (shared, unchanged)
        // pipeline = identity → toUpperCase: String → String → String
        // S=String, T=String

        // STEP 3: map(String::length)
        MiniLazy<String, Integer> step3 = step2.map(String::length);
        // source = ["hello", "world"] (still the same)
        // pipeline = identity → toUpperCase → length: String → String → String →
        // Integer
        // S=String, T=Integer

        // STEP 4: map(n -> n > 4)
        MiniLazy<String, Boolean> step4 = step3.map(n -> n > 4);
        // source = ["hello", "world"]
        // pipeline = identity → toUpperCase → length → (>4): String → ... → Boolean
        // S=String, T=Boolean

        // NOTHING HAS BEEN COMPUTED YET! All lazy.

        // STEP 5: Terminal operation — now it evaluates
        System.out.println("Materialized: " + step4.toList());
        // For "hello": "hello" → "HELLO" → 5 → true
        // For "world": "world" → "WORLD" → 5 → true
        // Output: [true, true]

        // indexOf with short-circuit:
        MiniLazy<Integer, String> nums = MiniLazy.of(1, 2, 3, 4, 5)
                .map(n -> {
                    System.out.println("  transforming: " + n);
                    return "item-" + n;
                });

        int idx = nums.indexOf("item-3");
        // Only transforms 1, 2, 3 — stops at first match!
        System.out.println("indexOf(item-3) = " + idx); // 2
    }

    // =========================================================
    // 5E: Why wildcards matter in map()
    // =========================================================
    static void wildcards_in_map() {
        System.out.println("\n=== 5E: Wildcards in map() ===");

        MiniLazy<Integer, Integer> nums = MiniLazy.of(1, 2, 3);

        // fn is Function<Number, String> — Number is SUPER of Integer
        // Without ? super T, this would FAIL (expects exact Function<Integer, ?>)
        Function<Number, String> numberToStr = n -> "N:" + n.doubleValue();
        MiniLazy<Integer, String> result = nums.map(numberToStr);
        // ? super Integer accepts Number ✓

        System.out.println("With ? super T: " + result.toList());
        // [N:1.0, N:2.0, N:3.0]

        // The flexibility means you can reuse broader functions:
        Function<Object, String> objToStr = o -> o.toString();
        MiniLazy<Integer, String> result2 = nums.map(objToStr);
        // ? super Integer accepts Object ✓

        System.out.println("With Object fn: " + result2.toList());
        // [1, 2, 3]
    }

    // =========================================================
    // Mini LazyArray implementation (mirrors your LazyArray)
    // =========================================================
    static final class MiniLazy<S, T> {
        private final List<S> source;
        private final Function<S, T> pipeline;

        private MiniLazy(List<S> source, Function<S, T> pipeline) {
            this.source = source;
            this.pipeline = pipeline;
        }

        // static <E> — must declare E because static can't see S, T
        public static <E> MiniLazy<E, E> of(List<E> data) {
            // return new MiniLazy<>(List.copyOf(data), Function.identity());
            // return new MiniLazy<>(List.copyOf(data), (E e) -> e);
            return new MiniLazy<>(List.copyOf(data), e -> e);
        }

        @SafeVarargs
        public static <E> MiniLazy<E, E> of(E... data) {
            return of(Arrays.asList(data));
        }

        // <R> — method-level type: the new view type
        // ? super T — fn accepts T or parent (PECS: consumer)
        // ? extends R — fn returns R or subtype (PECS: producer)
        public <R> MiniLazy<S, R> map(Function<? super T, ? extends R> fn) {
            Objects.requireNonNull(fn);
            Function<S, R> next = this.pipeline.andThen(fn::apply);
            // ! same
            // Function<S, R> next = this.pipeline.andThen((T t) -> fn.apply(t));
            return new MiniLazy<>(this.source, next);
        }

        // Terminal: short-circuits on first match
        public int indexOf(T target) {
            for (int i = 0; i < source.size(); i++) {
                T val = pipeline.apply(source.get(i));
                if (Objects.equals(val, target))
                    return i;
            }
            return -1;
        }

        // Terminal: materializes everything (defeats laziness)
        public List<T> toList() {
            List<T> out = new ArrayList<>(source.size());
            for (S s : source)
                out.add(pipeline.apply(s));
            return out;
        }
    }
    // ClassA::staticMethod -> x -> ClassA.staticMethod(x)
    // obj::instanceMethod -> x -> obj.instanceMethod(x)
    // ClassA::instanceMethod -> (obj, x) -> obj.instanceMethod(x)
    // ClassA::new -> x -> new ClassA(x) // depending on constructor
}
