package docs;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * ============================================================ JAVA STREAMS — A
 * Complete Guide with Examples
 * ============================================================
 *
 * WHAT IS A STREAM? ----------------- A Stream is a PIPELINE that processes a
 * sequence of elements. It does NOT store data — it pulls elements from a
 * source (collection, array, generator) and pushes them through a chain of
 * operations.
 *
 * KEY PROPERTIES:
 * 1. Lazy — intermediate operations don't execute until a terminal operation is
 * invoked.
 * 2. Single-use — a stream can only be consumed once.
 * 3. Non-mutating — streams don't modify the source collection.
 * 4. Optionally parallel — switch to parallel with .parallelStream().
 *
 * PIPELINE STRUCTURE: source → [intermediate ops...] → terminal op → result
 *
 * INTERMEDIATE (lazy, return Stream): filter, map, flatMap, sorted, distinct,
 * peek, limit, skip, mapToInt, mapToDouble, takeWhile, dropWhile
 *
 * TERMINAL (eager, trigger execution, return result): collect, toList, forEach,
 * reduce, count, min, max, anyMatch, allMatch, noneMatch, findFirst, findAny,
 * toArray
 */
public class StreamExamples {
    public static void main(String[] args) {
        example1_CreatingStreams();
        example2_FilterMapCollect();
        example3_FlatMap();
        example4_Reduce();
        example5_Collectors();
        example6_Sorting();
        example7_MatchAndFind();
        example8_PeekAndDebugging();
        example9_PrimitiveStreams();
        example10_Laziness();
        example11_ParallelStreams();
        example12_RealWorldUseCases();
    }

    // =========================================================
    // EXAMPLE 1: Ways to Create a Stream
    // =========================================================
    static void example1_CreatingStreams() {
        System.out.println("=== Example 1: Creating Streams ===");

        // From a Collection
        List<String> list = List.of("a", "b", "c");
        Stream<String> s1 = list.stream();

        // From an array
        String[] arr = { "x", "y", "z" };
        Stream<String> s2 = Arrays.stream(arr);

        // From values directly
        Stream<String> s3 = Stream.of("one", "two", "three");

        // From a range of ints
        IntStream range = IntStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5

        // Infinite stream with limit
        Stream<Double> randoms = Stream.generate(Math::random).limit(3);

        // Infinite stream with iterate
        Stream<Integer> powers = Stream.iterate(1, n -> n * 2).limit(10);

        // 1, 2, 4, 8, 16, 32, 64, 128, 256, 512
        // Print them
        System.out.println(s1.toList());
        System.out.println(s2.toList());
        System.out.println(s3.toList());
        range.forEach(n -> System.out.print(n + " ")); // 1 2 3 4 5
        System.out.println();
        System.out.println(randoms.toList());
        System.out.println(powers.toList());
    }

    // =========================================================
    // EXAMPLE 2: filter(), map(), collect() — The Big Three
    // =========================================================
    /*
     * filter(Predicate<T>) — keep elements matching a condition
     * map(Function<T, R>) — transform each element 
     * collect(Collector) — gather results into a
     * collection
     *
     * These three form the backbone of most stream pipelines.
     */
    static void example2_FilterMapCollect() {
        System.out.println("\n=== Example 2: filter / map / collect ===");
        List<String> names = List.of("Alice", "Bob", "Charlie", "Dave", "Eve");
        // Filter names with length > 3, convert to uppercase, collect to list
        List<String> result = names.stream().filter(name -> name.length() > 3) // Alice, Charlie, Dave
                .map(String::toUpperCase) // ALICE, CHARLIE, DAVE
                .collect(Collectors.toList());
        System.out.println(result); // [ALICE, CHARLIE, DAVE]
        // Chained: filter → map → map → collect
        List<Integer> lengths = names.stream().filter(n -> n.startsWith("C") || n.startsWith("D")).map(String::length)
                .collect(Collectors.toList());
        System.out.println(lengths); // [7, 4] (Charlie=7, Dave=4)
    }

    // =========================================================
    // EXAMPLE 3: flatMap() — Flatten Nested Structures
    // =========================================================
    /*
     * map() transforms each element 1-to-1. flatMap() transforms each element into
     * a stream, then flattens them.
     *
     * Use when each element maps to MULTIPLE results (List of Lists, etc.)
     *
     * map: Stream<T> → Stream<R> (1:1) flatMap: Stream<T> → Stream<R> (1:many,
     * flattened)
     */
    static void example3_FlatMap() {
        System.out.println("\n=== Example 3: flatMap ===");
        // Problem: we have a list of sentences, want all individual words
        List<String> sentences = List.of("hello world", "foo bar baz", "java streams");
        // map gives Stream<Stream<String>> — nested! Not useful.
        // flatMap gives Stream<String> — flattened!
        List<String> words = sentences.stream().flatMap(sentence -> Arrays.stream(sentence.split(" ")))
                .collect(Collectors.toList());
        System.out.println(words);
        // [hello, world, foo, bar, baz, java, streams]
        // Another example: flatten list of lists
        List<List<Integer>> nested = List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6, 7, 8, 9));
        List<Integer> flat = nested.stream().flatMap(Collection::stream).collect(Collectors.toList());
        System.out.println(flat); // [1, 2, 3, 4, 5, 6, 7, 8, 9]
    }

    // =========================================================
    // EXAMPLE 4: reduce() — Combine All Elements Into One
    // =========================================================
    /*
     * reduce(identity, accumulator)
     *
     * Three forms: reduce(BinaryOperator) → Optional<T> reduce(T identity,
     * BinaryOp) → T reduce(U identity, BiFunc, BinaryOp) → U (for parallel)
     *
     * Think of it like folding a list into a single value.
     */
    static void example4_Reduce() {
        System.out.println("\n=== Example 4: reduce ===");
        List<Integer> nums = List.of(1, 2, 3, 4, 5);

        // Sum: identity=0, accumulator=(a,b)->a+b
        int sum = nums.stream().reduce(0, Integer::sum);
        System.out.println("Sum: " + sum); // 15

        // Product: identity=1
        int product = nums.stream().reduce(1, (a, b) -> a * b);
        System.out.println("Product: " + product); // 120
        // Max (without identity — returns Optional)
        Optional<Integer> max = nums.stream().reduce(Integer::max);
        max.ifPresent(m -> System.out.println("Max: " + m)); // 5
        // String concatenation with reduce
        List<String> words = List.of("Java", "Streams", "Are", "Powerful");
        String joined = words.stream().reduce("", (a, b) -> a + " " + b).trim();
        System.out.println(joined); // Java Streams Are Powerful
    }

    // =========================================================
    // EXAMPLE 5: Collectors — Powerful Terminal Operations
    // =========================================================
    static void example5_Collectors() {
        System.out.println("\n=== Example 5: Collectors ===");
        List<String> names = List.of("Alice", "Bob", "Charlie", "Alice", "Dave", "Bob");

        // toList / toSet / toUnmodifiableList
        // List<String> list = names.stream().collect(Collectors.toList());
        Set<String> set = names.stream().collect(Collectors.toSet());
        System.out.println("Set (no dupes): " + set);

        // joining — concatenate strings
        String csv = names.stream().collect(Collectors.joining(", "));
        System.out.println("Joined: " + csv);

        // groupingBy — group elements by a classifier
        Map<Integer, List<String>> byLength = names.stream().collect(Collectors.groupingBy(String::length));
        System.out.println("By length: " + byLength);
        // {3=[Bob, Bob], 4=[Dave], 5=[Alice, Alice], 7=[Charlie]}

        // groupingBy with counting
        Map<String, Long> frequency = names.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println("Frequency: " + frequency);
        // {Alice=2, Bob=2, Charlie=1, Dave=1}

        // partitioningBy — split into two groups (true/false)
        Map<Boolean, List<String>> partition = names.stream().collect(Collectors.partitioningBy(n -> n.length() > 3));
        System.out.println("Partition: " + partition);

        // toMap
        Map<String, Integer> nameLengths = names.stream().distinct().collect(Collectors.toMap(Function.identity(), // key
                String::length // value
        ));
        System.out.println("Name→Length: " + nameLengths);
        // summarizingInt — get count, sum, min, max, avg at once
        IntSummaryStatistics stats = names.stream().collect(Collectors.summarizingInt(String::length));
        System.out.println("Stats: " + stats);
    }

    // =========================================================
    // EXAMPLE 6: Sorting
    // =========================================================
    static void example6_Sorting() {
        System.out.println("\n=== Example 6: Sorting ===");
        List<String> names = List.of("Charlie", "Alice", "Eve", "Bob", "Dave");
        // Natural order
        List<String> sorted = names.stream().sorted().toList();
        System.out.println("Natural: " + sorted);
        // By length
        List<String> byLen = names.stream().sorted(Comparator.comparingInt(String::length)).toList();
        System.out.println("By length: " + byLen);
        // Reverse order
        List<String> reversed = names.stream().sorted(Comparator.reverseOrder()).toList();
        System.out.println("Reversed: " + reversed);
        // Multi-key: by length, then alphabetically
        List<String> multi = names.stream()
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())).toList();
        System.out.println("Length then alpha: " + multi);
    }

    // =========================================================
    // EXAMPLE 7: Match and Find Operations
    // =========================================================
    /*
     * anyMatch(predicate) — true if ANY element matches allMatch(predicate) — true
     * if ALL elements match noneMatch(predicate) — true if NO element matches
     * findFirst() — first element (Optional) findAny() — any element (useful in
     * parallel)
     *
     * These are SHORT-CIRCUITING — they stop as soon as they know the answer.
     */
    static void example7_MatchAndFind() {
        System.out.println("\n=== Example 7: Match & Find ===");
        List<Integer> nums = List.of(2, 4, 6, 8, 11);
        boolean anyOdd = nums.stream().anyMatch(n -> n % 2 != 0);
        boolean allEven = nums.stream().allMatch(n -> n % 2 == 0);
        boolean noneNeg = nums.stream().noneMatch(n -> n < 0);
        System.out.println("Any odd?  " + anyOdd); // true (11)
        System.out.println("All even? " + allEven); // false (11)
        System.out.println("None neg? " + noneNeg); // true
        Optional<Integer> first = nums.stream().filter(n -> n > 5).findFirst();
        first.ifPresent(n -> System.out.println("First > 5: " + n)); // 6
    }

    // =========================================================
    // EXAMPLE 8: peek() — Debugging Stream Pipelines
    // =========================================================
    /*
     * peek(Consumer) lets you "spy" on elements as they flow through. It does NOT
     * modify the stream — use it for debugging/logging only.
     */
    static void example8_PeekAndDebugging() {
        System.out.println("\n=== Example 8: peek (debugging) ===");
        List<String> result = List.of("one", "two", "three", "four").stream()
                .peek(s -> System.out.println("  before filter: " + s)).filter(s -> s.length() > 3)
                .peek(s -> System.out.println("  after filter:  " + s)).map(String::toUpperCase)
                .peek(s -> System.out.println("  after map:     " + s)).toList();
        System.out.println("Result: " + result);
        // Notice: "one" and "two" never reach "after filter" — laziness!
    }

    // =========================================================
    // EXAMPLE 9: Primitive Streams (IntStream, LongStream, DoubleStream)
    // =========================================================
    /*
     * Avoid boxing overhead for numeric operations. Extra methods: sum(),
     * average(), range(), rangeClosed(), asDoubleStream()
     */
    static void example9_PrimitiveStreams() {
        System.out.println("\n=== Example 9: Primitive Streams ===");

        // IntStream from range
        int sum = IntStream.rangeClosed(1, 100).sum();
        System.out.println("Sum 1-100: " + sum); // 5050

        // Average
        OptionalDouble avg = IntStream.of(10, 20, 30, 40).average();
        avg.ifPresent(a -> System.out.println("Average: " + a)); // 25.0

        // mapToInt to avoid boxing
        List<String> words = List.of("hello", "world", "java");
        int totalChars = words.stream().mapToInt(String::length) // Stream<String> → IntStream
                .sum();
        System.out.println("Total chars: " + totalChars); // 14

        // Generate fibonacci (iterate with seed)
        System.out.print("Fibonacci: ");
        Stream.iterate(new int[] { 0, 1 }, f -> new int[] { f[1], f[0] + f[1] }).limit(10).map(f -> f[0])
                .forEach(n -> System.out.print(n + " "));
        System.out.println();
        // 0 1 1 2 3 5 8 13 21 34
    }

    // =========================================================
    // EXAMPLE 10: Laziness Demonstrated
    // =========================================================
    /*
     * Intermediate operations are NOT executed until a terminal operation is
     * called. Even then, elements are processed ONE AT A TIME through the full
     * pipeline (not stage by stage).
     */
    static void example10_Laziness() {
        System.out.println("\n=== Example 10: Laziness ===");
        // Nothing happens here — no terminal op!
        Stream<Integer> lazy = List.of(1, 2, 3, 4, 5).stream().filter(n -> {
            System.out.println("  filtering: " + n);
            return n > 2;
        }).map(n -> {
            System.out.println("  mapping: " + n);
            return n * 10;
        });
        System.out.println("Stream created — nothing printed yet!");

        // NOW the pipeline executes, but with short-circuiting
        // findFirst only needs the FIRST matching element
        Optional<Integer> first = lazy.findFirst();
        System.out.println("First result: " + first.orElse(-1));
        // Output: filtering 1, filtering 2, filtering 3, mapping 3
        // Elements 4 and 5 are NEVER processed!
    }

    // =========================================================
    // EXAMPLE 11: Parallel Streams
    // =========================================================
    /*
     * .parallelStream() or .parallel() splits work across threads.
     *
     * WHEN TO USE: ✓ Large data sets (10,000+ elements) ✓ CPU-intensive per-element
     * work ✓ No shared mutable state
     *
     * WHEN NOT TO USE: ✗ Small collections (thread overhead > savings) ✗
     * Order-dependent operations ✗ I/O-bound tasks (use async/virtual threads
     * instead) ✗ When side effects depend on order
     */
    static void example11_ParallelStreams() {
        System.out.println("\n=== Example 11: Parallel Streams ===");
        List<Integer> bigList = IntStream.rangeClosed(1, 1_000_000).boxed().collect(Collectors.toList());
        // Sequential
        long t1 = System.nanoTime();
        long seqSum = bigList.stream().reduce(0L, (a, b) -> a + b, Long::sum);
        long seqTime = System.nanoTime() - t1;
        // Parallel
        long t2 = System.nanoTime();
        long parSum = bigList.parallelStream().reduce(0L, (a, b) -> a + b, Long::sum);
        long parTime = System.nanoTime() - t2;
        System.out.println("Sequential sum: " + seqSum + " in " + seqTime / 1_000_000 + "ms");
        System.out.println("Parallel sum:   " + parSum + " in " + parTime / 1_000_000 + "ms");
    }
    // =========================================================
    // EXAMPLE 12: Real-World Use Cases
    // =========================================================

    record Employee(String name, String dept, double salary) {
    }

    static void example12_RealWorldUseCases() {
        System.out.println("\n=== Example 12: Real-World Use Cases ===");
        List<Employee> employees = List.of(new Employee("Alice", "Engineering", 95000),
                new Employee("Bob", "Engineering", 85000), new Employee("Charlie", "Marketing", 72000),
                new Employee("Dave", "Marketing", 68000), new Employee("Eve", "Engineering", 110000),
                new Employee("Frank", "Sales", 60000));

        // USE CASE 1: Average salary by department
        Map<String, Double> avgSalary = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, Collectors.averagingDouble(Employee::salary)));
        System.out.println("Avg salary by dept: " + avgSalary);

        // USE CASE 2: Top 3 earners
        List<String> topEarners = employees.stream().sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3).map(Employee::name).toList();
        System.out.println("Top 3 earners: " + topEarners);

        // USE CASE 3: Department headcount
        Map<String, Long> headcount = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
        System.out.println("Headcount: " + headcount);

        // USE CASE 4: Highest paid per department
        Map<String, Optional<Employee>> highest = employees.stream().collect(
                Collectors.groupingBy(Employee::dept, Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
        highest.forEach((dept, emp) -> emp.ifPresent(e -> System.out.println(dept + " top: " + e.name())));

        // USE CASE 5: Comma-separated names of engineers earning > 90k
        String highEngineers = employees.stream().filter(e -> e.dept().equals("Engineering"))
                .filter(e -> e.salary() > 90000).map(Employee::name).collect(Collectors.joining(", "));
        System.out.println("High-earning engineers: " + highEngineers);

        // USE CASE 6: Total payroll
        double total = employees.stream().mapToDouble(Employee::salary).sum();
        System.out.println("Total payroll: $" + total);
        
        // USE CASE 7: Does anyone earn > 100k?
        boolean anyHighEarner = employees.stream().anyMatch(e -> e.salary() > 100000);
        System.out.println("Anyone > 100k? " + anyHighEarner);
        /*
         * STREAM OPERATIONS CHEAT SHEET:
         * ────────────────────────────────────────────────── INTERMEDIATE (lazy) │
         * TERMINAL (eager) ─────────────────────────┼────────────────────────
         * filter(Predicate) │ collect(Collector) map(Function) │ toList()
         * flatMap(Function) │ forEach(Consumer) sorted() │ reduce(BinaryOp)
         * sorted(Comparator) │ count() distinct() │ min(Comparator) peek(Consumer) │
         * max(Comparator) limit(long) │ anyMatch(Predicate) skip(long) │
         * allMatch(Predicate) takeWhile(Predicate) │ noneMatch(Predicate)
         * dropWhile(Predicate) │ findFirst() mapToInt/Long/Double │ findAny() │
         * toArray() ──────────────────────────────────────────────────
         */
    }
}
