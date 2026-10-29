package docs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/**
 * ============================================================ JAVA LAMBDA
 * FUNCTIONS — A Complete Guide with Examples
 * ============================================================
 *
 * WHAT IS A LAMBDA?
 * -----------------
 * A lambda is an anonymous function
 * — a block of code you can pass around like a variable. It implements exactly
 * ONE
 * abstract method of a @FunctionalInterface.
 *
 * SYNTAX: (parameters) -> expression (parameters) -> { statements; }
 *
 * SHORTHAND RULES:
 * - One param: parentheses optional x -> x * 2
 * - Zero params: empty parens () -> 42
 * - One expression: no braces/return x -> x + 1
 * - Multiple statements: braces + return needed (x, y) -> { int sum = x + y;
 * return sum; }
 */
public class LambdaFunctions {
    public static void main(String[] args) {
        example1_BasicSyntax();
        example2_FunctionalInterfaces();
        example3_MethodReferences();
        example4_Compose();
        example5_EffectivelyFinal();
        example6_CustomFunctionalInterface();
        example7_RealWorldUseCases();
    }

    // =========================================================
    // EXAMPLE 1: Basic Lambda Syntax Variations
    // =========================================================
    static void example1_BasicSyntax() {
        System.out.println("=== Example 1: Basic Syntax ===");
        // --- No parameters ---
        Runnable greet = () -> System.out.println("Hello!");
        greet.run(); // Hello!
        // --- One parameter (parens optional) ---
        Function<String, Integer> len = s -> s.length();
        System.out.println(len.apply("lambda")); // 6
        // --- Two parameters ---
        BinaryOperator<Integer> add = (a, b) -> a + b;
        System.out.println(add.apply(3, 4)); // 7
        // --- Multi-line body (braces + explicit return) ---
        Function<Integer, String> classify = n -> {
            if (n > 0)
                return "positive";
            else if (n < 0)
                return "negative";
            else
                return "zero";
        };
        System.out.println(classify.apply(-5)); // negative
    }

    // =========================================================
    // EXAMPLE 2: The Core Functional Interfaces (java.util.function)
    // =========================================================
    /*
     * Core functional interfaces:
     * Function<T, R> -> R apply(T t) : transform T -> R
     * UnaryOperator<T> -> T apply(T t) : transform T -> T
     * BinaryOperator<T> -> T apply(T a, T b) : combine two T values
     * Predicate<T> -> boolean test(T t) : test a condition
     * Consumer<T> -> void accept(T t) : perform a side effect
     * Supplier<T> -> T get() : produce a value
     * BiFunction<T, U, R> -> R apply(T t, U u) : transform two inputs
     */
    static void example2_FunctionalInterfaces() {
        System.out.println("\n=== Example 2: Functional Interfaces ===");

        // Function<T, R> — transform
        Function<String, String> upper = s -> s.toUpperCase();
        System.out.println(upper.apply("hello")); // HELLO

        // Predicate<T> — test
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println(isEven.test(4)); // true
        System.out.println(isEven.test(7)); // false

        // Consumer<T> — side effect
        Consumer<String> printer = s -> System.out.println(">> " + s);
        printer.accept("consumed!"); // >> consumed!

        // Supplier<T> — produce a value
        Supplier<Double> random = () -> Math.random();
        System.out.println("Random: " + random.get());

        // UnaryOperator<T> — same-type transform
        UnaryOperator<String> exclaim = s -> s + "!";
        System.out.println(exclaim.apply("wow")); // wow!

        // BiFunction<T, U, R> — two inputs
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println(repeat.apply("ha", 3)); // hahaha
    }

    // =========================================================
    // EXAMPLE 3: Method References (shorthand for lambdas)
    // =========================================================
    /*
     * Method reference forms:
     * ClassName::staticMethod -> x -> ClassName.staticMethod(x)
     * obj::instanceMethod -> x -> obj.instanceMethod(x)
     * ClassName::instanceMethod -> (obj, args) -> obj.instanceMethod(args)
     * ClassName::new -> args -> new ClassName(args)
     */
    static void example3_MethodReferences() {
        System.out.println("\n=== Example 3: Method References ===");
        List<String> words = Arrays.asList("banana", "apple", "cherry");

        // Lambda version
        words.sort((a, b) -> a.compareTo(b));
        // Method reference (equivalent, cleaner)
        words.sort(String::compareTo);
        System.out.println(words); // [apple, banana, cherry]

        // Constructor reference
        Function<String, StringBuilder> toSB = StringBuilder::new;
        StringBuilder sb = toSB.apply("built!");
        System.out.println(sb); // built!

        // Static method reference
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println(parse.apply("42")); // 42
    }

    // =========================================================
    // EXAMPLE 4: compose() and andThen() — FUNCTION CHAINING
    // =========================================================
    /*
     * compose(before): Creates a new function that runs `before` FIRST, then
     * `this`. result = this.apply( before.apply(input) )
     *
     * Think: "compose f with g" in math → f(g(x))
     *
     * andThen(after): Creates a new function that runs `this` FIRST, then `after`.
     * result = after.apply( this.apply(input) )
     *
     * PIPELINE DIRECTION: compose: input → before → this → output (right-to-left)
     * andThen: input → this → after → output (left-to-right)
     */
    static void example4_Compose() {
        System.out.println("\n=== Example 4: compose() and andThen() ===");
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;
        Function<Integer, String> stringify = x -> "Result: " + x;

        // --- andThen: left-to-right pipeline ---
        // input=5 → doubleIt(5)=10 → addTen(10)=20
        Function<Integer, Integer> doubleThenAdd = doubleIt.andThen(addTen);
        System.out.println(doubleThenAdd.apply(5)); // 20

        // --- compose: right-to-left pipeline ---
        // input=5 → addTen(5)=15 → doubleIt(15)=30
        Function<Integer, Integer> doubleComposeAdd = doubleIt.compose(addTen);
        System.out.println(doubleComposeAdd.apply(5)); // 30

        // --- Chaining many steps with andThen ---
        // input=3 → double(3)=6 → addTen(6)=16 → stringify(16)="Result: 16"
        Function<Integer, String> pipeline = doubleIt.andThen(addTen).andThen(stringify);
        System.out.println(pipeline.apply(3)); // Result: 16

        // --- compose for math-style notation ---
        // f(x) = x + 10, g(x) = x * 2
        // f.compose(g) means f(g(x)) = (x * 2) + 10
        Function<Integer, Integer> fComposeG = addTen.compose(doubleIt);
        System.out.println(fComposeG.apply(5)); // 20 (same as 5*2 + 10)

        // --- Real-world compose: parse then validate ---
        Function<String, Integer> parse = Integer::parseInt;
        Function<Integer, Integer> abs = Math::abs;
        Predicate<Integer> isValid = n -> n > 0 && n < 100;
        Function<String, Integer> parseAndAbs = abs.compose(parse);
        System.out.println(parseAndAbs.apply("-42")); // 42
        System.out.println(isValid.test(parseAndAbs.apply("-42"))); // true
    }

    // =========================================================
    // EXAMPLE 5: Effectively Final & Variable Capture
    // =========================================================
    /*
     * Lambdas can READ variables from their enclosing scope, but those variables
     * must be effectively final (never modified after initialization).
     */
    static void example5_EffectivelyFinal() {
        System.out.println("\n=== Example 5: Variable Capture ===");
        String prefix = "Item: "; // effectively final — never reassigned
        Consumer<String> printItem = s -> System.out.println(prefix + s);
        printItem.accept("apple"); // Item: apple
        printItem.accept("banana"); // Item: banana
        // This would NOT compile:
        // prefix = "Changed"; // ← modifying makes it non-final
        // Consumer<String> broken = s -> System.out.println(prefix + s);
    }

    // =========================================================
    // EXAMPLE 6: Custom @FunctionalInterface
    // =========================================================
    @FunctionalInterface
    interface Transformer<T> {
        T transform(T input);

        // You CAN have default methods — they don't count
        default Transformer<T> then(Transformer<T> next) {
            return input -> next.transform(this.transform(input));
        }
    }

    static void example6_CustomFunctionalInterface() {
        System.out.println("\n=== Example 6: Custom Functional Interface ===");
        Transformer<String> trim = s -> s.trim();
        Transformer<String> upper = s -> s.toUpperCase();
        Transformer<String> exclaim = s -> s + "!";
        // Chain: trim → upper → exclaim
        Transformer<String> pipeline = trim.then(upper).then(exclaim);
        System.out.println(pipeline.transform("  hello  ")); // HELLO!
    }

    // =========================================================
    // EXAMPLE 7: Real-World Use Cases
    // =========================================================
    static void example7_RealWorldUseCases() {
        System.out.println("\n=== Example 7: Real-World Use Cases ===");
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Dave", "Eve");

        // USE CASE 1: Sorting with custom comparator
        names.sort(Comparator.comparingInt(String::length));
        System.out.println("By length: " + names);

        // USE CASE 2: Event-handler style callbacks
        performOperation("data", result -> System.out.println("Got: " + result));

        // USE CASE 3: Strategy pattern — pass behavior as a parameter
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        System.out.println("Evens: " + filter(nums, n -> n % 2 == 0));
        System.out.println("> 4:   " + filter(nums, n -> n > 4));

        // USE CASE 4: Lazy evaluation
        Supplier<String> expensive = () -> {
            // This won't run unless .get() is called
            return "computed " + (10 * 10);
        };
        boolean needed = true;
        if (needed) {
            System.out.println(expensive.get());
        }
    }

    // Helper: callback-style
    static void performOperation(String input, Consumer<String> callback) {
        String result = input.toUpperCase();
        callback.accept(result);
    }

    // Helper: strategy pattern
    static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).toList();
    }
}
