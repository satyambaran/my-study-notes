package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Functional unit tests for {@link LazyArray}.
 *
 * Run with: cd /Users/satyambaran/Documents/distributed-kv javac
 * practice/LazyArray.java practice/LazyArrayTest.java java
 * practice.LazyArrayTest
 *
 * What we test (and why): 1. Basic map + indexOf returns the correct index, and
 * -1 when absent. 2. Original LazyArray is unchanged after map() (immutability
 * of the chain). 3. Chained maps compose left-to-right (function composition
 * order). 4. Intermediate LazyArrays remain independent of later chains. 5.
 * Empty source returns -1 for any target. 6. Null targets work via
 * Objects.equals (and null-producing maps too). 7. Type-changing maps (Integer
 * -> String) work end-to-end. 8. Short-circuit: once a match is found, no
 * further elements are transformed. 9. Lazy: map() alone does not invoke the
 * function; only indexOf does. 10. Source is defensively copied (mutating the
 * input list afterwards has no effect).
 */
public final class LazyArrayTest {
    // ---- tiny test harness (no JUnit dependency) ----
    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        run("basicMapAndIndexOf", LazyArrayTest::basicMapAndIndexOf);
        run("originalUnchangedAfterMap", LazyArrayTest::originalUnchangedAfterMap);
        run("chainedMapsComposeInOrder", LazyArrayTest::chainedMapsComposeInOrder);
        run("intermediateChainsAreIndependent", LazyArrayTest::intermediateChainsAreIndependent);
        run("emptySourceReturnsMinusOne", LazyArrayTest::emptySourceReturnsMinusOne);
        run("nullTargetAndNullMappedValues", LazyArrayTest::nullTargetAndNullMappedValues);
        run("typeChangingMap", LazyArrayTest::typeChangingMap);
        run("shortCircuitStopsTransforming", LazyArrayTest::shortCircuitStopsTransforming);
        run("mapIsLazyDoesNotInvokeFn", LazyArrayTest::mapIsLazyDoesNotInvokeFn);
        run("sourceIsDefensivelyCopied", LazyArrayTest::sourceIsDefensivelyCopied);
        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.out.println("Failures:");
            for (String f : failures)
                System.out.println("  - " + f);
            System.exit(1);
        }
    }
    // ---- tests ----

    static void basicMapAndIndexOf() {
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        LazyArray<Integer, Integer> doubled = arr.map(x -> x * 2);
        assertEquals(2, doubled.indexOf(6), "3*2 == 6 at index 2");
        assertEquals(4, doubled.indexOf(10), "5*2 == 10 at index 4");
        assertEquals(-1, doubled.indexOf(3), "no element doubles to 3");
    }

    static void originalUnchangedAfterMap() {
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        arr.map(x -> x * 2); // discard result
        assertEquals(2, arr.indexOf(3), "original still finds 3 at index 2");
    }

    static void chainedMapsComposeInOrder() {
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        // (x + 1) then (* 3): 1->6, 2->9, 3->12, 4->15, 5->18
        LazyArray<Integer, Integer> result = arr.map(x -> x + 1).map(x -> x * 3);
        assertEquals(1, result.indexOf(9), "(2+1)*3 == 9 at index 1");
        assertEquals(0, result.indexOf(6), "(1+1)*3 == 6 at index 0");
        assertEquals(-1, result.indexOf(7), "no value equals 7");
    }

    static void intermediateChainsAreIndependent() {
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        LazyArray<Integer, Integer> step1 = arr.map(x -> x + 10);
        LazyArray<Integer, Integer> step2 = step1.map(x -> x * 2);
        assertEquals(2, step1.indexOf(13), "3 + 10 == 13 at index 2");
        assertEquals(2, step2.indexOf(26), "(3 + 10) * 2 == 26 at index 2");
        assertEquals(2, arr.indexOf(3), "original still finds 3 at index 2");
    }

    static void emptySourceReturnsMinusOne() {
        LazyArray<Integer, Integer> arr = LazyArray.of(Collections.<Integer>emptyList());
        assertEquals(-1, arr.indexOf(1), "empty source has no matches");
        assertEquals(-1, arr.map(x -> x * 2).indexOf(0), "empty + map still -1");
    }

    static void nullTargetAndNullMappedValues() {
        LazyArray<Integer, Integer> arr = LazyArray.of(Arrays.asList(1, 2, 3));
        // Map produces null for the value 2.
        LazyArray<Integer, Integer> withNull = arr.map(x -> x == 2 ? null : x);
        assertEquals(1, withNull.indexOf(null), "null is found at index 1");
        assertEquals(2, withNull.indexOf(3), "3 is still at index 2");
    }

    static void typeChangingMap() {
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3);
        LazyArray<Integer, String> asStr = arr.map(x -> "v" + x);
        assertEquals(0, asStr.indexOf("v1"), "v1 at index 0");
        assertEquals(2, asStr.indexOf("v3"), "v3 at index 2");
        assertEquals(-1, asStr.indexOf("v9"), "v9 absent");
    }

    /**
     * Verifies short-circuit: indexOf must stop calling the transformation as soon
     * as a match is found. We instrument the function with a counter and assert
     * that it was invoked at most (matchIndex + 1) times.
     */
    static void shortCircuitStopsTransforming() {
        AtomicInteger calls = new AtomicInteger();
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        Function<Integer, Integer> doubler = x -> {
            calls.incrementAndGet();
            return x * 2;
        };
        int idx = arr.map(doubler).indexOf(6); // 3 is at index 2 -> 3 calls
        assertEquals(2, idx, "match found at index 2");
        assertEquals(3, calls.get(),
                "doubler must have been called exactly 3 times (indices 0,1,2), was " + calls.get());
        // And on a miss it must walk the whole list exactly once.
        calls.set(0);
        int missIdx = arr.map(doubler).indexOf(999);
        assertEquals(-1, missIdx, "no match");
        assertEquals(5, calls.get(), "doubler called once per element on miss");
    }

    /** map() must NOT invoke the function until indexOf is called. */
    static void mapIsLazyDoesNotInvokeFn() {
        AtomicInteger calls = new AtomicInteger();
        LazyArray<Integer, Integer> arr = LazyArray.of(1, 2, 3, 4, 5);
        LazyArray<Integer, Integer> mapped = arr.map(x -> {
            calls.incrementAndGet();
            return x * 2;
        });
        assertEquals(0, calls.get(), "map() alone must not invoke the function");
        mapped.indexOf(4); // forces evaluation up to first match (index 1)
        assertTrue(calls.get() > 0, "indexOf should trigger evaluation");
    }

    static void sourceIsDefensivelyCopied() {
        List<Integer> mutable = new ArrayList<>(Arrays.asList(1, 2, 3));
        LazyArray<Integer, Integer> arr = LazyArray.of(mutable);
        mutable.set(0, 999); // mutate after construction
        mutable.add(42);
        assertEquals(0, arr.indexOf(1), "snapshot preserved: 1 still at index 0");
        assertEquals(3, arr.size(), "snapshot preserved: size still 3");
    }
    // ---- assertion helpers ----

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (AssertionError ae) {
            failed++;
            failures.add(name + ": " + ae.getMessage());
            System.out.println("FAIL  " + name + " -> " + ae.getMessage());
        } catch (Throwable t) {
            failed++;
            failures.add(name + ": unexpected " + t);
            System.out.println("ERROR " + name + " -> " + t);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new AssertionError(msg);
    }
}
