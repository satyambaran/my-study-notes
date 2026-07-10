# Java Concurrency & Functional Programming — Quick Reference Cheat Sheet

## Structure
Each section follows: topic name → one-line real-world use case → commented method signatures grouped logically.
Methods are listed as `// method1; method2;` with inline notes where behavior is non-obvious.
The final table captures key conceptual distinctions between commonly confused pairs.

## Purpose
Rapid revision reference for Java concurrency primitives (Thread, Lock, Semaphore, Atomic*, ExecutorService, CompletableFuture), functional interfaces (Function, Predicate, Consumer, Supplier), and Stream API (creation, intermediate, terminal, Collectors). Not a tutorial — assumes working Java knowledge.

# Java Concurrency & Functional Cheat Sheet

---

## 1. Thread
**Use:** OS-level unit of execution, run parallel tasks

```java
// new Thread(Runnable r); new Thread(r, "name");
// t.start(); t.run(); // start=new thread, run=same thread (bug if called directly)
// t.join(); t.join(millis); // block caller until t finishes
// t.interrupt(); t.isInterrupted(); Thread.interrupted(); // cooperative cancellation
// Thread.sleep(millis); Thread.yield(); // pause / hint scheduler
// t.setDaemon(true); t.isDaemon(); // JVM won't wait for daemon threads to exit
// t.getState(); // NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
// t.setName(); t.getName(); t.getId(); t.getPriority(); t.setPriority(int);
// Thread.currentThread(); // static ref to calling thread
// t.setUncaughtExceptionHandler(handler);
```

---

## 2. Runnable
**Use:** Task without return value, preferred over extending Thread

```java
// void run(); // single abstract method
// Runnable r = () -> { /* work */ };
// new Thread(r).start();
// executor.execute(r); executor.submit(r); // submit wraps in Future<?>
```

---

## 3. Callable<V>
**Use:** Task WITH return value + can throw checked exceptions

```java
// V call() throws Exception; // SAM
// Future<V> f = executor.submit(callable);
// List<Future<V>> invokeAll(Collection<Callable<V>>); // blocks until all done
// V invokeAny(Collection<Callable<V>>); // returns first successful result
```

---

## 4. Future<V>
**Use:** Handle to async result, cancellation

```java
// V get(); V get(timeout, unit); // blocking; throws ExecutionException, InterruptedException
// boolean cancel(mayInterruptIfRunning); // attempt cancel
// boolean isCancelled(); boolean isDone();
```

---

## 5. CompletableFuture<T>
**Use:** Composable async pipelines, non-blocking chaining

```java
// --- Creation ---
// CompletableFuture.supplyAsync(Supplier<T>); // async with return
// CompletableFuture.runAsync(Runnable); // async void
// CompletableFuture.completedFuture(value); // already resolved
// CompletableFuture.failedFuture(ex); // already failed (Java 9+)

// --- Transform (non-blocking) ---
// thenApply(Function<T,U>); // map: T -> U, same thread
// thenApplyAsync(fn); thenApplyAsync(fn, executor); // map on ForkJoinPool / custom pool
// thenCompose(Function<T, CompletableFuture<U>>); // flatMap: avoid nested futures
// thenAccept(Consumer<T>); thenAcceptAsync(c); // consume result, return Void
// thenRun(Runnable); // ignore result, run action

// --- Combine ---
// thenCombine(other, BiFunction); // combine two independent futures
// thenAcceptBoth(other, BiConsumer); // consume both results
// runAfterBoth(other, Runnable); // run after both complete
// applyToEither(other, Function); // first to complete wins
// acceptEither(other, Consumer); runAfterEither(other, Runnable);

// --- Error handling ---
// exceptionally(Function<Throwable, T>); // recover from error
// handle(BiFunction<T, Throwable, U>); // transform result or error
// whenComplete(BiConsumer<T, Throwable>); // side-effect on completion, doesn't change result

// --- Bulk ---
// CompletableFuture.allOf(cf1, cf2, ...); // waits for all, returns Void
// CompletableFuture.anyOf(cf1, cf2, ...); // first to complete

// --- Terminal ---
// join(); // like get() but throws unchecked CompletionException
// get(); getNow(defaultValue); // blocking retrieval
// complete(value); completeExceptionally(ex); // manually resolve
// orTimeout(duration, unit); completeOnTimeout(value, duration, unit); // Java 9+
```

---

## 6. ExecutorService
**Use:** Thread pool management, decouple task submission from execution

```java
// --- Factory (Executors class) ---
// Executors.newFixedThreadPool(n); // bounded pool — CPU-bound work
// Executors.newCachedThreadPool(); // unbounded, reuses idle threads — short-lived IO
// Executors.newSingleThreadExecutor(); // sequential, ordered guarantee
// Executors.newScheduledThreadPool(n); // delayed/periodic tasks
// Executors.newWorkStealingPool(); // ForkJoinPool-backed (Java 8+)
// Executors.newVirtualThreadPerTaskExecutor(); // Java 21+ virtual threads

// --- Submission ---
// execute(Runnable); // fire-and-forget, returns void
// submit(Callable<T>)  → Future<T>    // get() returns the Callable's result
// submit(Runnable)      → Future<?>    // get() returns null on completion
// submit(Runnable, T result) → Future<T> // get() returns the supplied `result` on completion
// invokeAll(Collection<Callable<T>>); // blocks, returns List<Future<T>>
// invokeAny(Collection<Callable<T>>); // blocks, returns first success

// --- Lifecycle ---
// shutdown(); // no new tasks, finish existing
// shutdownNow(); // interrupt running, return unstarted
// awaitTermination(timeout, unit); // block until done or timeout
// isShutdown(); isTerminated();

// --- ScheduledExecutorService ---
// schedule(callable, delay, unit); schedule(runnable, delay, unit);
// scheduleAtFixedRate(r, initDelay, period, unit); // wall-clock period
// scheduleWithFixedDelay(r, initDelay, delay, unit); // gap after completion
```

---

## 7. synchronized / wait / notify
**Use:** Intrinsic locking, basic thread coordination

```java
// synchronized(obj) { ... } // mutual exclusion on obj's monitor
// synchronized method // lock on `this` (instance) or `Class` (static)
// obj.wait(); obj.wait(millis); // release lock, sleep until notify (must hold monitor)
// obj.notify(); // wake one waiting thread
// obj.notifyAll(); // wake all — usually preferred to avoid missed signals
```

---

## 8. ReentrantLock
**Use:** Explicit lock with tryLock, fairness, interruptible acquire

```java
// new ReentrantLock(); new ReentrantLock(fair); // fair=true → FIFO ordering
// lock(); unlock(); // always unlock in finally
// tryLock(); tryLock(timeout, unit); // non-blocking / timed attempt
// lockInterruptibly(); // throws InterruptedException if interrupted while waiting
// newCondition(); // create Condition variable (replaces wait/notify)
// isLocked(); isHeldByCurrentThread(); getHoldCount(); getQueueLength();
```

---

## 9. ReadWriteLock / ReentrantReadWriteLock
**Use:** Many readers, exclusive writer — read-heavy workloads (caches, config)

```java
// rwLock.readLock().lock(); rwLock.readLock().unlock(); // shared
// rwLock.writeLock().lock(); rwLock.writeLock().unlock(); // exclusive
// StampedLock (Java 8+): optimistic reads for even higher throughput
//   long stamp = sl.tryOptimisticRead(); ... sl.validate(stamp);
//   sl.readLock(); sl.writeLock(); sl.tryConvertToWriteLock(stamp);
```

---

## 10. Condition
**Use:** Multiple wait-sets per lock (e.g., notEmpty + notFull on bounded buffer)

```java
// Condition c = lock.newCondition();
// c.await(); c.await(timeout, unit); c.awaitUninterruptibly();
// c.awaitNanos(nanos); c.awaitUntil(deadline);
// c.signal(); c.signalAll();
```

---

## 11. Semaphore
**Use:** Limit concurrent access to N resources (connection pool, rate limiter)

```java
// new Semaphore(permits); new Semaphore(permits, fair);
// acquire(); acquire(n); // blocks until permit available
// tryAcquire(); tryAcquire(timeout, unit); tryAcquire(n, timeout, unit);
// release(); release(n); // return permits (can release without acquiring → increase count)
// availablePermits(); drainPermits(); // check / take all
```

**Mutex = `new Semaphore(1)`** — binary semaphore, but prefer ReentrantLock for mutual exclusion (it has ownership semantics, Semaphore doesn't).

---

## 12. CountDownLatch
**Use:** One-shot barrier — N threads signal, waiters proceed (service startup)

```java
// new CountDownLatch(count);
// countDown(); // decrement
// await(); await(timeout, unit); // block until count reaches 0
// getCount();
```

---

## 13. CyclicBarrier
**Use:** Reusable sync point — N threads wait for each other (phased simulation)

```java
// new CyclicBarrier(parties); new CyclicBarrier(parties, barrierAction);
// await(); await(timeout, unit); // blocks until all parties arrive
// reset(); getParties(); getNumberWaiting(); isBroken();
```

---

## 14. Phaser
**Use:** Flexible reusable barrier with dynamic registration (replaces CDL + CB)

```java
// new Phaser(); new Phaser(parties); new Phaser(parent);
// register(); bulkRegister(n); // add parties dynamically
// arrive(); arriveAndDeregister(); arriveAndAwaitAdvance();
// awaitAdvance(phase); awaitAdvanceInterruptibly(phase, timeout, unit);
// getPhase(); getRegisteredParties(); getArrivedParties();
// onAdvance(phase, registeredParties); // override to control termination
```

---

## 15. Atomic Classes
**Use:** Lock-free thread-safe single-variable ops (counters, flags, CAS loops)

```java
// AtomicInteger / AtomicLong / AtomicBoolean / AtomicReference<V>
// get(); set(val); lazySet(val); // volatile read/write
// getAndSet(val); compareAndSet(expect, update); // CAS
// getAndIncrement(); getAndDecrement(); incrementAndGet(); decrementAndGet();
// getAndAdd(delta); addAndGet(delta);
// getAndUpdate(UnaryOperator); updateAndGet(UnaryOperator); // arbitrary CAS loop
// getAndAccumulate(val, BinaryOperator); accumulateAndGet(val, BinaryOperator);

// AtomicIntegerArray / AtomicLongArray / AtomicReferenceArray — same ops, indexed
// AtomicStampedReference<V> — CAS with int stamp (solves ABA)
// AtomicMarkableReference<V> — CAS with boolean mark

// LongAdder / LongAccumulator (Java 8+) — high-contention counters
// LongAdder: add(delta); increment(); decrement(); sum(); reset();
// LongAccumulator: accumulate(val); get(); reset();
```

---

## 16. Concurrent Collections
**Use:** Thread-safe data structures without external synchronization

```java
// ConcurrentHashMap<K,V> — segment/node-level locking
//   putIfAbsent(k,v); computeIfAbsent(k, fn); compute(k, biFn); merge(k, v, biFn);
//   forEach(parallelismThreshold, action); reduceValues(...); search(...);

// CopyOnWriteArrayList<E> — snapshot on write, great for read-heavy (listeners)
// CopyOnWriteArraySet<E> — backed by COWAL

// ConcurrentLinkedQueue<E> — lock-free unbounded FIFO
// ConcurrentLinkedDeque<E> — lock-free unbounded deque

// BlockingQueue<E> — producer-consumer
//   put(e); take(); // blocks
//   offer(e); offer(e, timeout, unit); poll(); poll(timeout, unit); // timed
//   peek(); drainTo(collection); remainingCapacity();
//   Impls: ArrayBlockingQueue, LinkedBlockingQueue, PriorityBlockingQueue,
//          SynchronousQueue (0-capacity handoff), DelayQueue

// ConcurrentSkipListMap / ConcurrentSkipListSet — concurrent sorted (TreeMap analog)
```

---

## 17. ForkJoinPool
**Use:** Divide-and-conquer parallelism, work-stealing (parallel streams use this)

```java
// ForkJoinPool.commonPool(); new ForkJoinPool(parallelism);
// invoke(ForkJoinTask); submit(ForkJoinTask); execute(ForkJoinTask);
// RecursiveTask<V> — compute() returns V; fork(); join();
// RecursiveAction — compute() returns void
// task.fork(); // async submit to pool
// task.join(); // block for result
// invokeAll(t1, t2); // fork both, join both
```

---

## 18. ThreadLocal<T>
**Use:** Per-thread isolated state (SimpleDateFormat, DB connection, request context)

```java
// new ThreadLocal<>(); ThreadLocal.withInitial(Supplier);
// get(); set(value); remove(); // ALWAYS remove in pools to avoid leaks
// InheritableThreadLocal — child threads inherit parent's value
```

---

## 19. volatile
**Use:** Visibility guarantee without atomicity — flags, published refs

```java
// volatile boolean running = true; // writes visible to all threads immediately
// No atomicity: volatile int count; count++ is NOT atomic → use AtomicInteger
// Happens-before: write to volatile → subsequent read sees it
```

---

## 20. FunctionalInterface / Lambda
**Use:** Pass behavior as data, SAM types for lambdas

```java
// @FunctionalInterface — compiler enforces exactly one abstract method
// Lambda: (params) -> expression  OR  (params) -> { statements; }
// Method ref: Class::staticMethod, obj::instanceMethod, Class::new
```

---

## 21. Function<T, R>
**Use:** Transform T → R (map operations, adapters, pipelines)

```java
// R apply(T t);
// andThen(Function<R, V>); // this → then: f.andThen(g) = g(f(x))
// compose(Function<V, T>); // before → this: f.compose(g) = f(g(x))
// Function.identity(); // returns input unchanged

// BiFunction<T, U, R>: R apply(T t, U u); andThen(Function);
// UnaryOperator<T> extends Function<T,T>: same type in/out
// BinaryOperator<T> extends BiFunction<T,T,T>: minBy(cmp); maxBy(cmp);
```

---

## 22. Predicate<T>
**Use:** Test condition T → boolean (filter, validation)

```java
// boolean test(T t);
// and(Predicate); or(Predicate); negate();
// Predicate.isEqual(targetRef); // Objects.equals check
// Predicate.not(predicate); // Java 11+

// BiPredicate<T, U>: boolean test(T t, U u); and(); or(); negate();
```

---

## 23. Consumer<T>
**Use:** Side-effect on T, no return (forEach, logging, event handling)

```java
// void accept(T t);
// andThen(Consumer); // chain: first.andThen(second)

// BiConsumer<T, U>: void accept(T t, U u); andThen();
```

---

## 24. Supplier<T>
**Use:** Lazy factory, deferred computation, default value provider

```java
// T get();
// Used in: Optional.orElseGet(), CompletableFuture.supplyAsync(),
//          ThreadLocal.withInitial(), Objects.requireNonNull(obj, supplier)
```

---

## 25. Stream<T>
**Use:** Declarative data processing pipelines over collections/arrays/generators

```java
// --- Creation ---
// collection.stream(); collection.parallelStream();
// Stream.of(a, b, c); Stream.empty();
// Arrays.stream(arr); Arrays.stream(arr, startInc, endExc);
// Stream.iterate(seed, unaryOp); // infinite: 0, 1, 2, ...
// Stream.iterate(seed, predicate, unaryOp); // finite (Java 9+)
// Stream.generate(supplier); // infinite: random, constant
// Stream.concat(s1, s2); // merge two streams
// IntStream.range(startInc, endExc); IntStream.rangeClosed(startInc, endInc);
// Stream.ofNullable(val); // 0 or 1 element (Java 9+)
// BufferedReader.lines(); Pattern.splitAsStream(input); String.chars();

// --- Intermediate (lazy, return Stream) ---
// filter(Predicate); // keep matching
// map(Function); // transform elements
// flatMap(Function<T, Stream<R>>); // 1-to-many, flatten
// mapToInt(); mapToLong(); mapToDouble(); // to primitive stream
// flatMapToInt(); flatMapToLong(); flatMapToDouble();
// distinct(); // removes dupes (equals/hashCode)
// sorted(); sorted(Comparator); // natural or custom order
// peek(Consumer); // debug side-effect, don't mutate
// limit(n); skip(n); // truncate / offset
// takeWhile(Predicate); dropWhile(Predicate); // Java 9+ ordered short-circuit

// --- Terminal (eager, trigger pipeline) ---
// forEach(Consumer); forEachOrdered(Consumer); // iterate; ordered respects encounter order
// collect(Collector); // accumulate into container
// toList(); // Java 16+ unmodifiable list
// toArray(); toArray(generator); // e.g., toArray(String[]::new)
// reduce(identity, BinaryOperator); reduce(BinaryOperator); // fold
// reduce(identity, BiFunction accumulator, BinaryOperator combiner); // parallel-safe
// count(); min(Comparator); max(Comparator);
// findFirst(); findAny(); // Optional<T>; findAny better for parallel
// anyMatch(Predicate); allMatch(Predicate); noneMatch(Predicate); // short-circuit
// iterator(); spliterator();

// --- Collectors (java.util.stream.Collectors) ---
// toList(); toUnmodifiableList(); toSet(); toUnmodifiableSet();
// toMap(keyFn, valueFn); toMap(keyFn, valueFn, mergeFn); toMap(..., mapSupplier);
// toUnmodifiableMap(keyFn, valueFn);
// toConcurrentMap(keyFn, valueFn);
// toCollection(Supplier); // e.g., TreeSet::new
// joining(); joining(delimiter); joining(delim, prefix, suffix);
// counting(); summingInt(fn); summingLong(); summingDouble();
// averagingInt(); averagingLong(); averagingDouble();
// summarizingInt(); // IntSummaryStatistics (count, sum, min, avg, max)
// maxBy(Comparator); minBy(Comparator);
// groupingBy(classifier); groupingBy(classifier, downstream);
// groupingBy(classifier, mapFactory, downstream); // e.g., TreeMap
// groupingByConcurrent(classifier);
// partitioningBy(Predicate); // Map<Boolean, List<T>>
// mapping(fn, downstream); flatMapping(fn, downstream); filtering(pred, downstream);
// reducing(identity, op); reducing(op);
// collectingAndThen(downstream, finisher); // post-process result
// teeing(d1, d2, merger); // Java 12+ two collectors merged
```

---

## 26. Optional<T>
**Use:** Explicit nullable return, avoid NPE, fluent chaining

```java
// Optional.of(val); // NPE if null
// Optional.ofNullable(val); Optional.empty();
// isPresent(); isEmpty(); // Java 11+
// get(); // throws NoSuchElementException if empty — avoid
// orElse(default); orElseGet(supplier); orElseThrow(); orElseThrow(exSupplier);
// ifPresent(Consumer); ifPresentOrElse(Consumer, Runnable); // Java 9+
// map(Function); flatMap(Function<T, Optional<U>>); filter(Predicate);
// or(Supplier<Optional<T>>); // Java 9+ lazy fallback
// stream(); // 0 or 1 element stream (Java 9+)
```

---

## 27. Comparator<T>
**Use:** Custom ordering for sort, priority queues, tree structures

```java
// int compare(T a, T b);
// Comparator.naturalOrder(); Comparator.reverseOrder();
// Comparator.comparing(keyExtractor); comparing(keyExtractor, keyComparator);
// comparingInt(); comparingLong(); comparingDouble();
// thenComparing(keyExtractor); thenComparing(comparator);
// thenComparingInt(); thenComparingLong(); thenComparingDouble();
// reversed();
// Comparator.nullsFirst(cmp); Comparator.nullsLast(cmp);
```

---

## Quick Mental Model

| Concept | Key Distinction |
|---|---|
| `Runnable` vs `Callable` | void vs returns V + throws |
| `execute()` vs `submit()` | fire-forget vs Future handle |
| `shutdown()` vs `shutdownNow()` | graceful vs interrupt |
| `synchronized` vs `ReentrantLock` | implicit vs explicit (tryLock, fair, conditions) |
| `Semaphore(1)` vs `ReentrantLock` | no ownership vs reentrant + owned |
| `CountDownLatch` vs `CyclicBarrier` | one-shot countdown vs reusable rendezvous |
| `volatile` vs `Atomic*` | visibility only vs visibility + atomicity |
| `thenApply` vs `thenCompose` | map vs flatMap (avoids `CF<CF<T>>`) |
| `map` vs `flatMap` (Stream) | 1:1 vs 1:many + flatten |
| `reduce` vs `collect` | fold to single value vs mutable accumulation |
| `findFirst` vs `findAny` | deterministic vs parallel-friendly |
| `peek` vs `forEach` | intermediate (debug) vs terminal |
| `orElse` vs `orElseGet` | eager default vs lazy supplier |