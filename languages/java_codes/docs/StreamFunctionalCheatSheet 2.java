package docs;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * ============================================================
 * STREAM + FUNCTIONAL INTERFACES CHEAT SHEET
 * ============================================================
 *
 * STREAM BASICS
 * - A Stream is a pipeline over data, not a data structure.
 * - Source -> intermediate ops -> terminal op -> result.
 * - Intermediate ops are lazy.
 * - Terminal ops trigger execution.
 * - Streams are single-use.
 *
 * COMMON STREAM OPERATIONS
 * - filter(Predicate<T>) -> keep matching elements
 * - map(Function<T, R>) -> transform each element
 * - flatMap(Function<T, Stream<R>>) -> map many then flatten
 * - sorted() / sorted(comparator) -> reorder
 * - distinct() -> remove duplicates
 * - limit(n), skip(n) -> slice
 * - collect(...) -> gather results
 * - reduce(0, Integer::sum) -> fold into one value
 * List<Integer> ls = new ArrayList<>();
 * ls.stream().reduce(1, Math::max);
 * - count(), min(), max() -> terminal summaries
 * - anyMatch/allMatch/noneMatch -> boolean checks
 * - findFirst/findAny -> Optional result
 *
 * FUNCTIONAL INTERFACES
 * - Function<T, R> -> R apply(T t)
 * Use: transform input to output
 *
 * - Predicate<T> -> boolean test(T t)
 * Use: filter / condition checks
 *
 * - Consumer<T> -> void accept(T t)
 * Use: side effects like logging or printing
 *
 * - Supplier<T> -> T get()
 * Use: lazy value creation
 *
 * - UnaryOperator<T> -> T apply(T t)
 * Use: same-type transform
 *
 * - BinaryOperator<T> -> T apply(T a, T b)
 * Use: combine two same-type values
 *
 * WHERE THEY APPEAR IN STREAMS
 * - filter(Predicate<? super T>)
 * - map(Function<? super T, ? extends R>)
 * - forEach(Consumer<? super T>)
 * - reduce(BinaryOperator<T>)
 * - Stream.generate(Supplier<T>)
 *
 * QUICK MAPPING
 * - Want to keep items? -> Predicate + filter
 * - Want to transform items? -> Function + map
 * - Want a side effect? -> Consumer + forEach/peek
 * - Want to lazily create data? -> Supplier + generate/orElseGet
 * - Want to combine values? -> BinaryOperator + reduce
 *
 * METHOD REFERENCES
 * - String::toUpperCase
 * - Integer::parseInt
 * - System.out::println
 * - ArrayList::new
 *
 * PIPELINE EXAMPLE
 * - names.stream()
 * .filter(name -> name.length() > 3)
 * .map(String::toUpperCase)
 * .sorted()
 * .toList();
 */
public class StreamFunctionalCheatSheet {

    public static void main(String[] args) { 
        Function<String, Integer> length = String::length; 
        Predicate<String> longWord = s -> s.length() > 4; // always return boolean
        Consumer<String> print = System.out::println; // void return type
        Supplier<String> fallback = () -> "default"; // no input, one output
        UnaryOperator<String> upper = String::toUpperCase;
        BinaryOperator<Integer> add = Integer::sum;

        System.out.println("=== Stream + Functional Interfaces Cheat Sheet ===");
        System.out.println("Function: length('java') = " + length.apply("java"));
        System.out.println("Predicate: longWord('stream') = " + longWord.test("stream"));
        print.accept("Consumer: prints this line");
        System.out.println("Supplier: " + fallback.get());
        System.out.println("UnaryOperator: " + upper.apply("lambda"));
        System.out.println("BinaryOperator: 20 + 22 = " + add.apply(20, 22));
    }
}