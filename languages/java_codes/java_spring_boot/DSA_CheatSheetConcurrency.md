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
> Java Thread maps 1:1 to OS/kernel thread (Platform Thread). Scheduled by OS scheduler — context switch is expensive (save/restore registers, stack, TLB flush). `start()` calls native `pthread_create` under the hood. `interrupt()` is cooperative — just sets a flag, doesn't kill the thread.

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
> Just a SAM interface — no thread creation by itself. Preferred over extending Thread because Java has single inheritance. `submit(Runnable)` internally wraps it in a `FutureTask` to get a `Future<?>` handle.

```java
// void run(); // single abstract method
// Runnable r = () -> { /* work */ };
// new Thread(r).start();
// executor.execute(r); executor.submit(r); // submit wraps in Future<?>
```

---

## 3. Callable<V>
**Use:** Task WITH return value + can throw checked exceptions
> Like Runnable but `call()` returns a value and can throw checked exceptions. When submitted, executor wraps it in `FutureTask<V>`. If `call()` throws, the exception is stored and re-thrown wrapped in `ExecutionException` when you call `future.get()`.

```java
// V call() throws Exception; // SAM
// Future<V> f = executor.submit(callable);
// List<Future<V>> invokeAll(Collection<Callable<V>>); // blocks until all done
// V invokeAny(Collection<Callable<V>>); // returns first successful result
```

---

## 4. Future<V>
**Use:** Handle to async result, cancellation
> `FutureTask` is the standard impl — uses a volatile `state` field (NEW→COMPLETING→NORMAL/EXCEPTIONAL/CANCELLED). `get()` parks the calling thread (via `LockSupport.park`) until result is ready. `cancel(true)` calls `interrupt()` on the worker thread — still cooperative.

```java
// new FutureTask<>(Callable<V>); new FutureTask<>(Runnable, V result);
// V get(); V get(timeout, unit); // blocking; throws ExecutionException, InterruptedException
// boolean cancel(mayInterruptIfRunning); // attempt cancel
// boolean isCancelled(); boolean isDone();
```

---

## 5. CompletableFuture<T>
**Use:** Composable async pipelines, non-blocking chaining
> Uses `ForkJoinPool.commonPool()` by default for async methods. Callback-driven — no thread blocks waiting for result. Internally maintains a Treiber stack of dependent actions (CAS-based). Implements both `Future` and `CompletionStage`. `join()` vs `get()`: join throws unchecked `CompletionException`, get throws checked `ExecutionException`.

```java
// --- Creation ---
// new CompletableFuture<>(); // incomplete future, resolve manually via complete()/completeExceptionally()
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
> All factory methods return `ThreadPoolExecutor` (or `ScheduledThreadPoolExecutor`) internally. Core design: worker threads pull tasks from a `BlockingQueue`. `newFixedThreadPool(n)` = coreSize=n, maxSize=n, `LinkedBlockingQueue` (unbounded — can OOM). `newCachedThreadPool` = core=0, max=MAX, `SynchronousQueue` (no buffering, direct handoff).

```java
// --- Direct constructor (interview favorite) ---
// new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
// new ThreadPoolExecutor(core, max, keepAlive, unit, workQueue, threadFactory, rejectionHandler);
// RejectionHandlers: AbortPolicy (default, throws), CallerRunsPolicy, DiscardPolicy, DiscardOldestPolicy

// --- Factory (Executors class) ---
// Executors.newFixedThreadPool(n); // bounded pool — CPU-bound work
// Executors.newCachedThreadPool(); // unbounded, reuses idle threads — short-lived IO
// Executors.newSingleThreadExecutor(); // sequential, ordered guarantee
// Executors.newScheduledThreadPool(n); // delayed/periodic tasks
// Executors.newWorkStealingPool(); // ForkJoinPool-backed (Java 8+)
// Executors.newVirtualThreadPerTaskExecutor(); // Java 21+ virtual threads

// --- Submission ---
// execute(Runnable); // fire-and-forget, returns void
//   ⚠ NOT guaranteed to complete — task can throw (silently swallowed, no Future to check),
//     thread can be interrupted, or pool can shutdownNow(). No way to know if it finished.
//     Use submit() if you need completion/error tracking.
// submit(Callable<T>)  → Future<T>    // get() returns the Callable's result
// submit(Runnable)      → Future<?>    // get() returns null on completion
// submit(Runnable, T result) → Future<T> // get() returns the supplied `result` on completion
//   e.g.: AtomicInteger count = new AtomicInteger();
//         Future<AtomicInteger> f = executor.submit(() -> count.incrementAndGet(), count);
//         f.get() == count; // true — returns the same `count` ref you passed in
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
> Every Java object has a **monitor** (mutex + wait-set), managed via `monitorenter`/`monitorexit` bytecodes. JVM uses lock escalation: biased lock → thin lock (CAS spin) → fat lock (OS mutex). `wait()` releases the monitor and moves thread to the object's wait-set. `notify()` moves one thread from wait-set back to the entry-set to re-acquire the monitor.

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
> Built on `AbstractQueuedSynchronizer` (AQS) — maintains a CLH FIFO queue of waiting threads using CAS + `LockSupport.park/unpark`. Reentrant = same thread can re-acquire (increments hold count). Fair mode serves longest-waiting thread first but has lower throughput. Always `unlock()` in `finally` — no auto-release like `synchronized`.

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
> AQS-based — packs read count (upper 16 bits) and write count (lower 16 bits) into a single `int state`. Multiple readers can hold simultaneously; writer needs exclusive access. Write lock can downgrade to read lock (acquire read, release write) but NOT upgrade. `StampedLock` adds optimistic reads — no lock acquired, just validate afterward.

```java
// new ReentrantReadWriteLock(); new ReentrantReadWriteLock(fair);
// rwLock.readLock().lock(); rwLock.readLock().unlock(); // shared
// rwLock.writeLock().lock(); rwLock.writeLock().unlock(); // exclusive
// new StampedLock(); // no fairness param
// StampedLock (Java 8+): optimistic reads for even higher throughput
//   long stamp = sl.tryOptimisticRead(); ... sl.validate(stamp);
//   sl.readLock(); sl.writeLock(); sl.tryConvertToWriteLock(stamp);
```

---

## 10. Condition
**Use:** Multiple wait-sets per lock (e.g., notEmpty + notFull on bounded buffer)
> Each `Condition` maintains its own wait queue (separate from AQS sync queue). `await()` releases lock, moves thread from sync queue → condition queue, parks it. `signal()` moves one thread from condition queue → sync queue to re-compete for the lock. Unlike `notify()`, you can have multiple conditions per lock for fine-grained control.

```java
// Condition c = lock.newCondition();
// c.await(); c.await(timeout, unit); c.awaitUninterruptibly();
// c.awaitNanos(nanos); c.awaitUntil(deadline);
// c.signal(); c.signalAll();
```

---

## 11. Semaphore
**Use:** Limit concurrent access to N resources (connection pool, rate limiter)
> AQS-based — `state` = available permits. `acquire()` decrements via CAS, blocks if state < 0. `release()` increments — **no ownership check**, any thread can release (unlike ReentrantLock). This means permits can be "created" by releasing without acquiring. Fair mode uses FIFO queue; non-fair allows barging.

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
> AQS-based — `state` = count. `countDown()` decrements via CAS. `await()` parks until state reaches 0. **Cannot be reset** — that's the key difference from CyclicBarrier. Typical pattern: main thread awaits, N worker threads count down.

```java
// new CountDownLatch(count);
// countDown(); // decrement
// await(); await(timeout, unit); // block until count reaches 0
// getCount();
```

---

## 13. CyclicBarrier
**Use:** Reusable sync point — N threads wait for each other (phased simulation)
> Uses `ReentrantLock` + `Condition` internally (not AQS directly). When last thread arrives, barrier trips — runs optional `barrierAction`, then resets `generation` so it can be reused. If any thread is interrupted/times out, barrier is **broken** for all waiting threads (`BrokenBarrierException`).

```java
// new CyclicBarrier(parties); new CyclicBarrier(parties, barrierAction);
// await(); await(timeout, unit); // blocks until all parties arrive
// reset(); getParties(); getNumberWaiting(); isBroken();
```

---

## 14. Phaser
**Use:** Flexible reusable barrier with dynamic registration (replaces CDL + CB)
> Packs phase number + registered parties + unarrived count into a single `long state` (CAS-updated). Parties can dynamically register/deregister between phases. Supports tree structure (parent Phaser) for scalability with many parties. Override `onAdvance()` to control termination.

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
> Uses `Unsafe.compareAndSwapInt/Long/Object` which maps to CPU `CMPXCHG` instruction — single atomic hardware op, no locks. Value stored as `volatile` field for visibility. Under high contention CAS can spin-waste CPU → use `LongAdder` instead (stripes across cells, sums on read). `AtomicStampedReference` solves ABA problem by pairing value with an int stamp.

```java
// new AtomicInteger(); new AtomicInteger(initialValue);
// new AtomicLong(); new AtomicLong(initialValue);
// new AtomicBoolean(); new AtomicBoolean(initialValue);
// new AtomicReference<>(); new AtomicReference<>(initialValue);
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
> `ConcurrentHashMap` (Java 8+): CAS on bins + `synchronized` on individual nodes (not segments). Uses `Node[]` table; tree-ifies bins at threshold 8. `CopyOnWriteArrayList`: every write clones the entire internal array — safe iterators (snapshot), but write-heavy = GC pressure. `BlockingQueue`: `put/take` use `ReentrantLock` + two `Condition`s (notEmpty, notFull).

```java
// new ConcurrentHashMap<>(); new ConcurrentHashMap<>(initialCapacity);
// new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
// ConcurrentHashMap<K,V> — segment/node-level locking
//   putIfAbsent(k,v); computeIfAbsent(k, fn); compute(k, biFn); merge(k, v, biFn);
//   forEach(parallelismThreshold, action); reduceValues(...); search(...);

// new CopyOnWriteArrayList<>(); new CopyOnWriteArrayList<>(collection);
// CopyOnWriteArrayList<E> — snapshot on write, great for read-heavy (listeners)
// CopyOnWriteArraySet<E> — backed by COWAL

// ConcurrentLinkedQueue<E> — lock-free unbounded FIFO
// ConcurrentLinkedDeque<E> — lock-free unbounded deque

// BlockingQueue<E> — producer-consumer
//   new ArrayBlockingQueue<>(capacity); new ArrayBlockingQueue<>(capacity, fair);
//   new LinkedBlockingQueue<>(); new LinkedBlockingQueue<>(capacity);
//   new PriorityBlockingQueue<>(); new PriorityBlockingQueue<>(capacity, comparator);
//   new SynchronousQueue<>(); new SynchronousQueue<>(fair);
//   new DelayQueue<>(); // elements must implement Delayed
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
> Each worker thread has its own **deque** — pushes/pops tasks from head (LIFO for locality), thieves steal from **tail** (FIFO for big tasks). `fork()` pushes to local deque, `join()` tries to execute locally before blocking. `commonPool` parallelism defaults to `Runtime.availableProcessors() - 1`. Parallel streams use the common pool.

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
> Each `Thread` object has a `ThreadLocalMap` (open-addressing hash table with linear probing). Key = `WeakReference<ThreadLocal>`, value = your object. Weak key means GC can reclaim ThreadLocal, but **value leaks** if you don't `remove()` — critical in thread pools. `InheritableThreadLocal` copies parent's map to child at thread creation time.

```java
// new ThreadLocal<>(); ThreadLocal.withInitial(Supplier);
// get(); set(value); remove(); // ALWAYS remove in pools to avoid leaks
// InheritableThreadLocal — child threads inherit parent's value
```

---

## 19. volatile
**Use:** Visibility guarantee without atomicity — flags, published refs
> Inserts memory barriers: `StoreStore + StoreLoad` on write, `LoadLoad + LoadStore` on read. Prevents CPU/compiler reordering across the barrier. Guarantees happens-before: write to volatile → subsequent read sees latest value. **Not atomic** for compound ops (`count++` = read + inc + write = 3 ops). Use for flags, published object refs, double-checked locking.

```java
// volatile boolean running = true; // writes visible to all threads immediately
// No atomicity: volatile int count; count++ is NOT atomic → use AtomicInteger
// Happens-before: write to volatile → subsequent read sees it
```

---

## 20. FunctionalInterface / Lambda
**Use:** Pass behavior as data, SAM types for lambdas
> Compiled to `invokedynamic` bytecode — `LambdaMetafactory` generates an implementation class at first invocation (cached after). **Not** anonymous inner classes — no `.class` file per lambda, lower overhead. Lambdas capture effectively-final variables only (copied into the generated class fields). Method refs are syntactic sugar over lambdas.

```java
// @FunctionalInterface — compiler enforces exactly one abstract method
// Lambda: (params) -> expression  OR  (params) -> { statements; }
// Method ref: Class::staticMethod, obj::instanceMethod, Class::new
```

---

## 21. Function<T, R>
**Use:** Transform T → R (map operations, adapters, pipelines)
> Core transformation interface. `andThen`/`compose` create a decorator chain — each returns a new `Function` that wraps the previous. `identity()` is useful as a no-op placeholder in APIs that require a Function parameter.

```java
// R apply(T t);
// andThen(Function<R, V>); // this → then: f.andThen(g) = g(f(x))
// compose(Function<V, T>); // before → this: f.compose(g) = f(g(x))
// Function.identity(); // returns input unchanged
//
// --- Example (3-step chain) ---
// Function<String, Integer> len   = String::length;        // "hello" → 5
// Function<Integer, Integer> dbl  = n -> n * 2;            // 5 → 10
// Function<Integer, String> stars = n -> "*".repeat(n);    // 10 → "**********"
//
// len.andThen(dbl).andThen(stars).apply("hello"); // "hello"→5→10→"**********"
// stars.compose(dbl).compose(len).apply("hello"); // same — reads right-to-left: len→dbl→stars
//
// --- Internal impl of andThen ---
// default <V> Function<T,V> andThen(Function<R,V> after) {
//     return (T t) -> after.apply(this.apply(t));  // this runs first, after runs on result
// }
// --- Internal impl of compose ---
// default <V> Function<V,R> compose(Function<V,T> before) {
//     return (V v) -> this.apply(before.apply(v)); // before runs first, this runs on result
// }

// BiFunction<T, U, R>: R apply(T t, U u); andThen(Function);
// UnaryOperator<T> extends Function<T,T>: same type in/out
// BinaryOperator<T> extends BiFunction<T,T,T>: minBy(cmp); maxBy(cmp);
```

---

## 22. Predicate<T>
**Use:** Test condition T → boolean (filter, validation)
> `and()`/`or()` use short-circuit evaluation (like `&&`/`||`). `Predicate.not()` (Java 11) is cleaner than `.negate()` — e.g., `filter(Predicate.not(String::isEmpty))`. Composed predicates chain via simple wrapper lambdas, no special optimization.

```java
// boolean test(T t);
// and(Predicate); or(Predicate); negate();
// Predicate.isEqual(targetRef); // Objects.equals check
// Predicate.not(predicate); // Java 11+
//
// --- Example (3-step chain) ---
// Predicate<String> notEmpty = s -> !s.isEmpty();
// Predicate<String> startsA  = s -> s.startsWith("A");
// Predicate<String> shortLen = s -> s.length() <= 10;
//
// names.stream().filter(notEmpty.and(startsA).and(shortLen)).toList();
//   // "" ✗(notEmpty fails, short-circuits) | "Apple" ✓✓✓ | "Abracadabra!" ✗(shortLen fails)
// names.stream().filter(notEmpty.or(startsA).negate()).toList();
//   // keeps only empty strings that don't start with A → effectively just ""
//
// --- Internal impl of and ---
// default Predicate<T> and(Predicate<T> other) {
//     return (T t) -> this.test(t) && other.test(t); // short-circuits if this is false
// }
// --- Internal impl of negate ---
// default Predicate<T> negate() { return (T t) -> !this.test(t); }

// BiPredicate<T, U>: boolean test(T t, U u); and(); or(); negate();
```

---

## 23. Consumer<T>
**Use:** Side-effect on T, no return (forEach, logging, event handling)
> `andThen()` chains consumers sequentially — first consumer runs, then second. If the first throws, second never executes. Common in `forEach`, `peek`, `ifPresent`. No `compose()` because consumers don't produce output to feed backward.

```java
// void accept(T t);
// andThen(Consumer); // chain: first.andThen(second)
//
// --- Example (3-step chain) ---
// Consumer<String> trim  = s -> s = s.trim();        // step 1: (side-effect on local, illustrative)
// Consumer<String> print = System.out::println;       // step 2: print to console
// Consumer<String> log   = s -> logger.info(s);       // step 3: log it
// list.forEach(trim.andThen(print).andThen(log));     // all 3 run sequentially per element
//
// --- Internal impl of andThen ---
// default Consumer<T> andThen(Consumer<T> after) {
//     return (T t) -> { this.accept(t); after.accept(t); }; // sequential, no compose()
// }

// BiConsumer<T, U>: void accept(T t, U u); andThen();
```

---

## 24. Supplier<T>
**Use:** Lazy factory, deferred computation, default value provider
> Called only when value is actually needed — that's the whole point (lazy evaluation). `orElse(val)` evaluates `val` eagerly even if Optional is present; `orElseGet(supplier)` calls supplier only when empty — important difference for expensive computations.

```java
// T get();
// Used in: Optional.orElseGet(), CompletableFuture.supplyAsync(),
//          ThreadLocal.withInitial(), Objects.requireNonNull(obj, supplier)
//
// --- Example (3-step lazy chain) ---
// Supplier<String> fromCache = () -> cache.get(key);         // step 1: check cache
// Supplier<String> fromDB    = () -> db.query(key);          // step 2: fallback to DB
// Supplier<String> fallback  = () -> "default";              // step 3: hardcoded default
//
// Optional.ofNullable(fromCache.get())       // try cache
//     .or(() -> Optional.ofNullable(fromDB.get()))  // miss → try DB
//     .orElseGet(fallback);                         // miss → "default"
//
// ⚠ orElse(expensiveCall()) — expensiveCall() runs ALWAYS, even if Optional has value
//   orElseGet(() -> expensiveCall()) — runs ONLY when empty
```

---

## 25. Stream<T>
**Use:** Declarative data processing pipelines over collections/arrays/generators
> Lazy pipeline: intermediate ops just build a chain of `Stage` objects, nothing executes until a terminal op triggers traversal via `Spliterator`. **Not reusable** — consuming a stream twice throws `IllegalStateException`. Parallel streams split work via `Spliterator.trySplit()` and execute on `ForkJoinPool.commonPool()`. Stateful ops (`sorted`, `distinct`) break pipeline parallelism benefits.

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

**Examples** — `List<String> names = List.of("Alice", "Bob", "Anna", "Bob", "Charlie");`
```java
// --- Creation examples ---
// Stream.of("a","b","c")                                → [a, b, c]
// Stream.empty()                                         → []
// Arrays.stream(new int[]{1,2,3})                        → IntStream [1, 2, 3]
// Arrays.stream(names.toArray(), 1, 3)                   → [Bob, Anna] (subarray index 1..2)
// Stream.iterate(0, n -> n + 2)                          → 0, 2, 4, 6, ... (infinite)
// Stream.iterate(0, n -> n < 10, n -> n + 2)             → 0, 2, 4, 6, 8
// Stream.generate(Math::random)                          → 0.73, 0.12, ...
// Stream.concat(Stream.of(1,2), Stream.of(3,4))          → [1, 2, 3, 4]
// IntStream.range(0, 5)                                  → 0, 1, 2, 3, 4
// IntStream.rangeClosed(1, 5)                             → 1, 2, 3, 4, 5
// Stream.ofNullable(null)                                → []
// Stream.ofNullable("hi")                                → ["hi"]
// "hello".chars()                                        → IntStream [104, 101, 108, 108, 111]
// Pattern.compile(",").splitAsStream("a,b,c")            → [a, b, c]

// --- Intermediate examples ---
// .filter(s -> s.startsWith("A"))                        → [Alice, Anna]
// .map(String::toUpperCase)                              → [ALICE, BOB, ANNA, BOB, CHARLIE]
// nested.stream().flatMap(Collection::stream)             → [1, 2, 3, 4, 5]
// .mapToInt(String::length)                              → IntStream [5, 3, 4, 3, 7]
// nested.stream().flatMapToInt(l -> l.stream().mapToInt(Integer::intValue))
//                                                        → IntStream [1, 2, 3, 4, 5]
// .distinct()                                            → [Alice, Bob, Anna, Charlie]
// .sorted()                                              → [Alice, Anna, Bob, Bob, Charlie]
// .sorted(comparingInt(String::length))                  → [Bob, Bob, Anna, Alice, Charlie]
// .peek(s -> System.out.println("processing: " + s))    → prints each element, passes it through unchanged
// .limit(3)                                              → [Alice, Bob, Anna]
// .skip(2)                                               → [Anna, Bob, Charlie]
// Stream.of(1,2,3,4,5).takeWhile(n -> n < 4)            → [1, 2, 3]
// Stream.of(1,2,3,4,5).dropWhile(n -> n < 4)            → [4, 5]

// --- Terminal examples ---
// .forEach(System.out::println)                          → prints each (no order guarantee in parallel)
// .forEachOrdered(System.out::println)                   → prints each (preserves encounter order in parallel)
// .collect(Collectors.toList())                           → [Alice, Bob, Anna, Bob, Charlie] (mutable)
// .toList()                                              → [Alice, Bob, Anna, Bob, Charlie] (unmodifiable, Java 16+)
// .toArray(String[]::new)                                → String[5]
// .reduce(0, Integer::sum)  on [1,2,3,4]                → 10
// .reduce(Integer::sum)     on [1,2,3,4]                → Optional[10]
// .reduce("", String::concat, String::concat)            → "AliceBob..." (3-arg: identity, accumulator, combiner for parallel)
// .count()                                               → 5
// .min(naturalOrder())                                   → Optional[Alice]
// .max(comparingInt(String::length))                     → Optional[Charlie]
// .findFirst()                                           → Optional[Alice]  (deterministic)
// .findAny()                                             → Optional[any]    (non-deterministic in parallel)
// .anyMatch(s -> s.equals("Bob"))                        → true   (short-circuits on first match)
// .allMatch(s -> s.length() > 2)                         → true   (short-circuits on first false)
// .noneMatch(s -> s.isEmpty())                           → true   (short-circuits on first true)
// .iterator()                                            → Iterator<String> for imperative loops

// --- Collector examples ---
// .collect(toList())                                     → [Alice, Bob, Anna, Bob, Charlie] (mutable ArrayList)
// .collect(toUnmodifiableList())                          → same, but throws UnsupportedOperationException on add/set
// .collect(toSet())                                      → {Alice, Bob, Anna, Charlie}
// .collect(toCollection(TreeSet::new))                   → sorted set: [Alice, Anna, Bob, Charlie]
// .distinct().collect(toMap(identity(), String::length))  → {Alice=5, Bob=3, Anna=4, Charlie=7}
// .collect(toMap(identity(), String::length, (a,b)->a))  → mergeFn handles dup key "Bob": keeps first
// .collect(joining(", "))                                → "Alice, Bob, Anna, Bob, Charlie"
// .collect(joining(", ", "[", "]"))                      → "[Alice, Bob, Anna, Bob, Charlie]"
// .collect(counting())                                   → 5
// .collect(summingInt(String::length))                   → 22
// .collect(averagingInt(String::length))                 → 4.4
// .collect(summarizingInt(String::length))               → {count=5, sum=22, min=3, avg=4.4, max=7}
// .collect(maxBy(comparingInt(String::length)))           → Optional[Charlie]
// .collect(minBy(comparingInt(String::length)))           → Optional[Bob]
// .collect(groupingBy(String::length))                   → {3=[Bob,Bob], 4=[Anna], 5=[Alice], 7=[Charlie]}
// .collect(groupingBy(String::length, counting()))       → {3=2, 4=1, 5=1, 7=1}
// .collect(groupingBy(s->s.charAt(0), TreeMap::new, toList()))
//                                                        → {A=[Alice,Anna], B=[Bob,Bob], C=[Charlie]}
// .collect(groupingByConcurrent(String::length))         → ConcurrentMap (same keys, thread-safe)
// .collect(partitioningBy(s -> s.length() > 3))          → {false=[Bob,Bob], true=[Alice,Anna,Charlie]}
// .collect(groupingBy(String::length, mapping(String::toUpperCase, toList())))
//                                                        → {3=[BOB,BOB], 5=[ALICE], ...}
// .collect(groupingBy(String::length, filtering(s -> !s.equals("Bob"), toList())))
//                                                        → {3=[], 4=[Anna], 5=[Alice], 7=[Charlie]}
// .collect(groupingBy(String::length, flatMapping(s -> s.chars().boxed(), toList())))
//                                                        → {3=[66,111,98,66,111,98], ...} (chars of each name)
// .collect(reducing("", (a,b) -> a+b))                  → "AliceBobAnnaBobCharlie"
// .collect(collectingAndThen(toList(), unmodifiableList)) → unmodifiable list
// .collect(teeing(counting(), joining(", "), (c,j) -> c+" names: "+j))
//                                                        → "5 names: Alice, Bob, ..."
```

---

## 26. Optional<T>
**Use:** Explicit nullable return, avoid NPE, fluent chaining
> Just a wrapper object — **not serializable**, not intended for fields or method parameters (only return types). `of(null)` throws NPE immediately (fail-fast). `get()` without `isPresent()` is a code smell — prefer `orElse`/`map`/`ifPresent`. Boxing overhead: use `OptionalInt`/`OptionalLong`/`OptionalDouble` for primitives.

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
> `comparing()` extracts a `Comparable` key and delegates to its natural order. `thenComparing()` creates a chained comparator that breaks ties. `nullsFirst`/`nullsLast` wrap another comparator to handle nulls. `reversed()` simply negates the result. Arrays.sort / Collections.sort use TimSort (stable, O(n log n)).

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

## 28. Reflection API
**Use:** Inspect/modify classes, methods, fields at runtime (frameworks, DI, ORM, serialization)
> Reflection bypasses compile-time type checks — operates on `Class<?>` metadata loaded by the ClassLoader. Every `.class` file becomes a `Class` object in the metaspace. `setAccessible(true)` disables Java access control (private fields etc.) — this is how frameworks like Spring, Hibernate, Jackson inject values. **Slow** compared to direct calls (~10-100x) due to no JIT inlining + security checks. Module system (Java 9+) restricts deep reflection via `--add-opens`.

```java
// --- Getting Class object ---
// Class<?> c = MyClass.class;                    // compile-time
// Class<?> c = obj.getClass();                   // runtime from instance
// Class<?> c = Class.forName("com.example.MyClass"); // by fully-qualified name (ClassNotFoundException)

// --- Inspecting class metadata ---
// c.getName(); c.getSimpleName(); c.getCanonicalName();
// c.getSuperclass(); c.getInterfaces();
// c.getModifiers();  // Modifier.isPublic(mod), isAbstract(mod), isFinal(mod)
// c.isInterface(); c.isEnum(); c.isArray(); c.isAnnotation(); c.isPrimitive();
// c.getPackage(); c.getModule(); // Java 9+

// --- Constructors ---
// c.getConstructors();                           // public only
// c.getDeclaredConstructors();                   // all (including private)
// Constructor<?> ctor = c.getDeclaredConstructor(String.class, int.class);
// ctor.setAccessible(true);                      // bypass private
// Object obj = ctor.newInstance("arg", 42);

// --- Fields ---
// c.getFields();                                 // public (including inherited)
// c.getDeclaredFields();                         // all declared (no inherited)
// Field f = c.getDeclaredField("name");
// f.setAccessible(true);
// Object val = f.get(obj);                       // read field value
// f.set(obj, "newValue");                        // write field value (even final — but fragile)
// f.getType(); f.getGenericType();               // field type info
// f.getAnnotations();

// --- Methods ---
// c.getMethods();                                // public (including inherited from Object)
// c.getDeclaredMethods();                        // all declared (no inherited)
// Method m = c.getDeclaredMethod("doStuff", String.class); // name + param types
// m.setAccessible(true);
// Object result = m.invoke(obj, "arg");          // call method on instance
// Object result = m.invoke(null, "arg");         // call static method (null instance)
// m.getReturnType(); m.getParameterTypes(); m.getExceptionTypes();
// m.isVarArgs(); m.isBridge(); m.isSynthetic();

// --- Annotations ---
// c.getAnnotation(MyAnnotation.class);           // null if not present
// c.isAnnotationPresent(MyAnnotation.class);     // boolean check
// c.getAnnotations();                            // all annotations (including inherited)
// c.getDeclaredAnnotations();                    // only directly declared
// f.getAnnotation(Column.class); m.getAnnotation(Override.class); // on fields/methods too

// --- Arrays (reflective) ---
// Class<?> arrClass = int[].class;
// Array.newInstance(int.class, 10);              // creates int[10]
// Array.get(arr, 0); Array.set(arr, 0, 42);     // read/write by index
// Array.getLength(arr);

// --- Proxy (dynamic interface implementation) ---
// Object proxy = Proxy.newProxyInstance(
//     classLoader,
//     new Class<?>[]{ MyInterface.class },
//     (proxy, method, args) -> { /* InvocationHandler logic */ }
// );
// // Used by: Spring AOP, Mockito, RPC stubs
// // Limitation: only works with interfaces, not concrete classes (use CGLIB/ByteBuddy for classes)
```

**Common interview questions:**
```
Q: How does Spring inject @Autowired private fields?
A: Field.setAccessible(true) → Field.set(bean, dependency)

Q: How does Jackson/Gson serialize private fields?
A: getDeclaredFields() → setAccessible(true) → get(obj) for each field

Q: Why is reflection slow?
A: No JIT inlining, security manager checks, boxing/unboxing args via Object[], no compile-time optimization

Q: How to break Singleton with reflection?
A: Constructor.setAccessible(true) → newInstance() — prevented by enum singleton

Q: getFields() vs getDeclaredFields()?
A: getFields() = public (own + inherited); getDeclaredFields() = all access levels (own only, no inherited)

Q: getMethods() vs getDeclaredMethods()?
A: getMethods() = public (own + inherited + Object); getDeclaredMethods() = all access levels (own only)
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