package docs.generics;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ============================================================
 * Part 4: DECODING Function & Consumer SIGNATURES
 * ============================================================
 *
 * Applies the PECS principle to understand why JDK uses
 * wildcards in andThen(), compose(), and Consumer.andThen().
 */
public class Part4_FunctionSignatures {

    public static void main(String[] args) {
        andThenDecoded();
        composeDecoded();
        consumerAndThenDecoded();
        chainingPipeline();
    }

    // =========================================================
    // 4A: Function.andThen() — decoded
    // =========================================================
    /*
     * SIGNATURE:
     * default <V> Function<T, V> andThen(Function<? super R, ? extends V> after)
     *
     * BREAKDOWN:
     * <V> → new type param: final output type
     * Function<T, V> → returns: T → V
     * ? super R → after CONSUMES our output R (can accept R or parent)
     * ? extends V → after PRODUCES V (can return V or subtype)
     *
     * FLOW: input(T) → this.apply → R → after.apply → V
     *
     * WHY ? super R?
     * If this returns Integer (R=Integer), you should be able to chain
     * a function that accepts Number. Without the wildcard, ONLY
     * Function<Integer, ?> would work — too restrictive.
     */
    static void andThenDecoded() {
        System.out.println("=== 4A: andThen decoded ===");

        Function<String, Integer> length = String::length; // String → Integer

        // --- Without wildcards, this would fail: ---
        // after is Function<Number, String> but R=Integer
        // Because ? super R: Number is super of Integer ✓
        Function<Number, String> format = n -> "[" + n + "]";

        Function<String, String> pipeline = length.andThen(format);
        System.out.println(pipeline.apply("hello")); // [5]
        System.out.println(pipeline.apply("hi")); // [2]
        System.out.println(pipeline.apply("generics")); // [8]

        // What's actually happening:
        // "hello" → length.apply("hello") = 5 (Integer)
        // → format.apply(5) = "[5]" (Number accepts Integer ✓)

        // --- Another example: Double output, Object consumer ---
        Function<Integer, Double> half = n -> n / 2.0; // Integer → Double
        Function<Number, String> describe = o -> "val=" + o; // Object/Number is super of Double ✓

        Function<Integer, String> pipe2 = half.andThen(describe);
        System.out.println(pipe2.apply(10)); // val=5.0
        System.out.println(pipe2.apply(7)); // val=3.5
    }

    // =========================================================
    // 4B: Function.compose() — decoded
    // =========================================================
    /*
     * SIGNATURE:
     * default <V> Function<V, R> compose(Function<? super V, ? extends T> before)
     *
     * BREAKDOWN:
     * <V> → new type param: initial input type
     * Function<V, R> → returns: V → R
     * ? super V → before CONSUMES the pipeline input V
     * ? extends T → before PRODUCES T (feeds into this.apply)
     *
     * FLOW: input(V) → before.apply → T → this.apply → R
     *
     * Think math: this.compose(before) = this( before(x) ) = f(g(x))
     *
     * WHY ? extends T?
     * If this expects Number (T=Number), before can return Integer
     * (Integer extends Number). Without the wildcard, before would
     * have to return exactly Number.
     */
    static void composeDecoded() {
        System.out.println("\n=== 4B: compose decoded ===");

        Function<Number, String> describe = n -> "Value: " + n.doubleValue();
        // T=Number, so compose needs before to produce something ? extends Number

        // before: String → Integer. Integer extends Number ✓
        Function<String, Integer> parse = Integer::parseInt;

        Function<String, String> composed = describe.compose(parse);
        System.out.println(composed.apply("42")); // Value: 42.0
        System.out.println(composed.apply("100")); // Value: 100.0

        // What's actually happening:
        // "42" → parse.apply("42") = 42 (Integer, which IS-A Number ✓)
        // → describe.apply(42) = "Value: 42.0"

        // --- Longer example ---
        Function<Number, Boolean> isPositive = n -> n.doubleValue() > 0;
        // compose with same parse:
        Function<String, Boolean> parseAndCheck = isPositive.compose(parse);
        System.out.println(parseAndCheck.apply("5")); // true
        System.out.println(parseAndCheck.apply("-3")); // false

        // --- compose vs andThen — same result, different direction ---
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;

        // andThen: LEFT to RIGHT → double first, then add
        System.out.println("andThen: " + doubleIt.andThen(addTen).apply(5)); // 20 (5*2=10, 10+10=20)

        // compose: RIGHT to LEFT → add first, then double (math-style)
        System.out.println("compose: " + doubleIt.compose(addTen).apply(5)); // 30 (5+10=15, 15*2=30)
    }

    // =========================================================
    // 4C: Consumer.andThen() — decoded
    // =========================================================
    /*
     * SIGNATURE:
     * default Consumer<T> andThen(Consumer<? super T> after)
     *
     * BREAKDOWN:
     * after CONSUMES T → ? super T
     * Both consumers run sequentially: this.accept(t); after.accept(t);
     *
     * WHY ? super T?
     * Consumer<Integer>.andThen(Consumer<Number>) should work,
     * because a Consumer<Number> can certainly accept an Integer.
     */
    static void consumerAndThenDecoded() {
        System.out.println("\n=== 4C: Consumer.andThen decoded ===");

        Consumer<Integer> printInt = n -> System.out.print("  Int:" + n);
        Consumer<Number> printNum = n -> System.out.print("  Num:" + n.doubleValue());
        Consumer<Object> printObj = o -> System.out.println("  Obj:" + o.getClass().getSimpleName());

        // Chain: printInt → printNum → printObj
        // Consumer<? super Integer> accepts both Consumer<Number> and Consumer<Object>
        Consumer<Integer> all = printInt.andThen(printNum).andThen(printObj);

        all.accept(42);
        // Output: Int:42 Num:42.0 Obj:Integer

        all.accept(7);
        // Output: Int:7 Num:7.0 Obj:Integer

        System.out.println();

        // --- Practical: log then validate ---
        Consumer<String> log = s -> System.out.println("  LOG: received '" + s + "'");
        Consumer<Object> audit = o -> System.out.println("  AUDIT: type=" + o.getClass().getSimpleName());

        Consumer<String> logAndAudit = log.andThen(audit);
        logAndAudit.accept("hello");
        // LOG: received 'hello'
        // AUDIT: type=String
    }

    // =========================================================
    // 4D: Building a multi-step pipeline
    // =========================================================
    static void chainingPipeline() {
        System.out.println("\n=== 4D: Multi-step pipeline ===");

        // Build a pipeline: String → Integer → Double → String
        Function<String, Integer> step1_parse = Integer::parseInt;
        Function<Number, Double> step2_half = n -> n.doubleValue() / 2; // Number super Integer ✓
        Function<Object, String> step3_wrap = o -> "[" + o + "]"; // Object super Double ✓

        Function<String, String> fullPipeline = step1_parse
                .andThen(step2_half) // ? super Integer accepts Number ✓
                .andThen(step3_wrap); // ? super Double accepts Object ✓

        System.out.println(fullPipeline.apply("100")); // [50.0]
        System.out.println(fullPipeline.apply("7")); // [3.5]
        System.out.println(fullPipeline.apply("0")); // [0.0]

        // Without ? super in andThen, step2 would need to be Function<Integer, Double>
        // and step3 would need to be Function<Double, String> — exact match only.
        // The wildcards give us flexibility to use broader types.
    }
}
