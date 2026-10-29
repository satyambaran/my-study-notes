package docs.generics;

/**
 * ============================================================
 * GENERICS + LAMBDAS CHEAT SHEET
 * ============================================================
 *
 * Compact reference sheet.
 *
 * Goal: fast scanning, not step-by-step teaching.
 */
public class GenericsLambdaCheatSheet {

    public static void main(String[] args) {
        print("TYPE PARAMS",
                "<T> general type",
                "<R> return/result type",
                "<E> element type",
                "<K, V> map key/value",
                "class Box<T> -> class-level type param",
                "<R> R map(T x) -> method-level type param",
                "static methods must declare their own type params");

        print("WILDCARDS",
                "? = unknown type",
                "? extends T = read as T, do not add T",
                "? super T = add T safely, read as Object",
                "<T extends Number> = upper bound on a type variable");

        print("PECS",
                "Producer Extends, Consumer Super",
                "read from parameter -> ? extends T",
                "write to parameter -> ? super T",
                "read + write -> exact T",
                "copy(List<? extends T> src, List<? super T> dest)");

        print("LAMBDA SYNTAX",
                "() -> 42",
                "x -> x * 2",
                "(a, b) -> a + b",
                "x -> { return x + 1; }",
                "captures only final/effectively-final locals");

        print("METHOD REFERENCES",
                "Class::staticMethod",
                "obj::instanceMethod",
                "Class::instanceMethod",
                "Class::new",
                "examples: String::length, Integer::parseInt, System.out::println");

        print("FUNCTIONAL INTERFACES",
                "Function<T, R> -> R apply(T)",
                "Predicate<T> -> boolean test(T)",
                "Consumer<T> -> void accept(T)",
                "Supplier<T> -> T get()",
                "UnaryOperator<T> -> T apply(T)",
                "BinaryOperator<T> -> T apply(T, T)",
                "Runnable -> void run()");

        print("COMMON JDK SIGNATURES",
                "Function.andThen(Function<? super R, ? extends V>)",
                "Function.compose(Function<? super V, ? extends T>)",
                "Consumer.andThen(Consumer<? super T>)",
                "Stream.map(Function<? super T, ? extends R>)",
                "Stream.filter(Predicate<? super T>)",
                "Stream.forEach(Consumer<? super T>)");

        print("HOW TO READ ? super / ? extends",
                "input positions usually use ? super T",
                "output positions usually use ? extends R",
                "reason: broader consumers accepted, narrower producers accepted",
                "decode scary signatures with PECS");

        print("QUICK RULES",
                "static method needs a generic -> declare <T> on the method",
                "returns a value -> Function / Supplier",
                "side effect only -> Consumer / Runnable",
                "boolean answer -> Predicate",
                "same-type transform -> UnaryOperator",
                "same-type combine -> BinaryOperator");
    }

    static void print(String title, String... lines) {
        System.out.println("=== " + title + " ===");
        for (String line : lines) {
            System.out.println("- " + line);
        }
        System.out.println();
    }
}