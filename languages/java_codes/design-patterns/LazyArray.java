package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * LazyArray<T>
 *
 * A wrapper around an immutable source list that records map() transformations
 * without applying them. Transformations are only evaluated when a terminal
 * operation (indexOf) is called, and evaluation short-circuits as soon as a
 * match is found.
 *
 * Design notes: - The underlying source array is shared (never mutated) across
 * chained maps. - Each map() returns a new LazyArray that holds the same source
 * plus an additional transformation appended to a transformation pipeline. -
 * indexOf(target) walks the source one element at a time, applies the pipeline
 * to that element, and returns the first index where the result equals the
 * target. It stops as soon as a match is found (short-circuit). - Because we
 * never materialize the transformed array, memory stays O(N) in the source
 * regardless of how many maps are chained.
 *
 * Type erasure caveat: In Python the example uses a single LazyArray that
 * changes element type freely (int -> int, but could be int -> str). In Java we
 * model this with two type parameters internally: - S: source element type
 * (fixed for the life of a chain) - T: current "view" element type after all
 * recorded maps Each map(Function<? super T, ? extends R>) returns
 * LazyArray<R>.
 */
public final class LazyArray<S, T> {
    /** Immutable source list, shared across the chain. */
    private final List<S> source;
    /** Composed pipeline: source element -> current view element. */
    private final Function<S, T> pipeline;
    // ---- Construction ----

    private LazyArray(List<S> source, Function<S, T> pipeline) {
        this.source = source;
        this.pipeline = pipeline;
    }

    /**
     * Public factory: build a LazyArray from a list. The list is defensively
     * copied.
     */
    public static <E> LazyArray<E, E> of(List<E> data) {
        Objects.requireNonNull(data, "data");
        List<E> copy = List.copyOf(data); // immutable snapshot
        return new LazyArray<>(copy, Function.identity());
    }

    /** Convenience varargs factory. */
    @SafeVarargs
    public static <E> LazyArray<E, E> of(E... data) {
        Objects.requireNonNull(data, "data");
        return of(Arrays.asList(data));
    }
    // ---- Lazy transformation ----

    /**
     * Records a transformation without applying it. Returns a new LazyArray; the
     * current one is unchanged.
     */
    public <R> LazyArray<S, R> map(Function<? super T, ? extends R> fn) {
        Objects.requireNonNull(fn, "fn");
        Function<S, R> next = this.pipeline.andThen(fn::apply);
        return new LazyArray<>(this.source, next);
    }
    // ---- Terminal operation ----

    /**
     * Applies the recorded transformation lazily, element by element, and returns
     * the index of the first element whose transformed value equals {@code target}.
     * Returns -1 if no element matches.
     *
     * Short-circuits on the first match: elements after the match are never
     * transformed.
     */
    public int indexOf(T target) {
        for (int i = 0; i < source.size(); i++) {
            T transformed = pipeline.apply(source.get(i));
            if (Objects.equals(transformed, target)) {
                return i;
            }
        }
        return -1;
    }

    /** Size of the underlying source (transformation does not change length). */
    public int size() {
        return source.size();
    }

    /**
     * Materializes the transformed view as a new list. Provided for
     * debugging/inspection only; defeats laziness, so prefer indexOf.
     */
    public List<T> toList() {
        List<T> out = new ArrayList<>(source.size());
        for (S s : source) {
            out.add(pipeline.apply(s));
        }
        return out;
    }
}
