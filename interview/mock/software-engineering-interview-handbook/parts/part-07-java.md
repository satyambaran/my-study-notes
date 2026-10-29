# Part VII — Java

## Part Introduction
This part moves from source code to silicon: how a `.java` file becomes
running behavior, how memory is managed automatically, how the standard
Collections library is built, and how Java's concurrency toolkit lets many
threads cooperate safely. The ten chapters build on each other: JVM
explains the machine everything runs on; Garbage Collection explains how it
reclaims the heap Collections and HashMap allocate into; HashMap Internals
and ConcurrentHashMap contrast single-threaded and concurrent hashing; and
the final four — ExecutorService, CompletableFuture, synchronized,
ReentrantLock — build up to the Java Memory Model, the rulebook that makes
Java's concurrency primitives sound.

Throughout, a deliberate distinction is drawn between what the **Java
Language/Virtual Machine Specification (JLS/JVMS)** guarantees — behavior
every conforming JVM must provide — and what is merely a **HotSpot
implementation detail** that other conformant JVMs (OpenJ9, GraalVM) may
implement differently. Interviewers routinely probe exactly this boundary,
and conflating the two is a common source of subtly wrong answers.

## Source-to-Runtime Roadmap
```text
  YourCode.java
        |  javac (compiler)
        v
  YourCode.class   (JVM bytecode: constant pool + instructions)
        |  loaded by
  ClassLoader   (Bootstrap -> Platform -> Application, parent-delegation)
        v
  +--------------------- JVM Runtime Data Areas ----------------------+
  |  Method Area / Metaspace ...... class metadata, static fields       |
  |  Heap ......................... objects (Collections, HashMap,     |
  |                                  ConcurrentHashMap entries live     |
  |                                  here; reclaimed by Garbage         |
  |                                  Collection)                       |
  |  Thread Stacks ................ frames, locals (threads run via    |
  |                                  ExecutorService; coordinate via    |
  |                                  synchronized / ReentrantLock)      |
  |  PC Registers, Native Stacks                                       |
  +----------------------------------------------------------------+
        |  interpreted first; hot methods promoted by
  JIT Compiler   (C1 client / C2 server, tiered compilation)
        v
  Native machine code   (executed directly by a CPU core)
        |  cross-thread visibility and ordering governed by
  Java Memory Model (JLS Chapter 17)
        -- the rulebook underneath synchronized, ReentrantLock, volatile,
           final, ConcurrentHashMap, ExecutorService, and CompletableFuture
```

**Chapter map:**

1. **JVM** — the machine that loads and executes bytecode.
2. **Garbage Collection** — automatic heap reclamation and pause-time
   trade-offs.
3. **Collections** — the standard, interface-driven data-structure library.
4. **HashMap Internals** — how the most-used Collection actually works.
5. **ConcurrentHashMap** — HashMap's thread-safe, fine-grained-locked sibling.
6. **ExecutorService** — managed thread pools and task queues.
7. **CompletableFuture** — composable, non-blocking async pipelines.
8. **synchronized** — Java's built-in intrinsic-lock primitive.
9. **ReentrantLock** — an explicit, more flexible alternative built on AQS.
10. **Java Memory Model** — the happens-before rules underlying chapters 5–9.

Suggested reading paths: **Beginner** — JVM → Garbage Collection →
Collections → HashMap Internals. **Intermediate** — ConcurrentHashMap →
ExecutorService → CompletableFuture. **Advanced** — synchronized →
ReentrantLock → Java Memory Model.

---

# JVM

## What is it?
The Java Virtual Machine (JVM) is the abstract computing machine, defined
by the Java Virtual Machine Specification (JVMS), that loads, verifies, and
executes Java bytecode — either the spec itself, or, colloquially, an
implementation such as HotSpot or Eclipse OpenJ9.

## Why do we need it?
A native binary is tied to one OS/CPU ABI. Java compiles source to bytecode
once, and a platform-specific JVM runs that bytecode everywhere, giving
portability, memory safety, and one place to plug in adaptive optimization.

## What problem does it solve?
It solves platform portability and manual-memory-management bugs, and
resolves fast startup (interpretation) versus high throughput (native
compilation) by doing both: interpret cold code and compile only hot paths.

## History (only if useful)
Sun's original JVM (1996) was a pure interpreter, giving early Java a poor
performance reputation. HotSpot, default since J2SE 1.3 (2000), introduced
adaptive tiered JIT compilation and remains the reference JVM today.

## How does it work?
**Beginner:** `javac` compiles `.java` source to `.class` bytecode; `java
Main` starts a JVM that executes that bytecode method by method.
**Intermediate:** classes load lazily via delegating class loaders and are
verified, then interpreted; a profiler counts invocations, and once a
method crosses a threshold, a JIT compiler produces native code for it.
**Advanced:** HotSpot tiers this — C1 produces quick, modest native code
early; C2 later recompiles hot methods with profile-guided optimizations
and can deoptimize back to the interpreter if a speculative assumption
proves wrong.

## Internal Working
A running JVM owns shared runtime data areas — the **Heap** (objects) and
**Method Area/Metaspace** (class metadata) — plus, per thread, a **JVM
stack**, a **PC register**, and a **native method stack** (JVMS §2.5).
Class loading has three phases: Loading, Linking (verify, prepare,
resolve), and Initialization, delegated bottom-up from Bootstrap to
Platform to Application loader.
**Spec vs. implementation:** the JVMS mandates these areas, phases, and
verification, but leaves the JIT algorithm, Metaspace layout, and GC choice
to the implementation — two conformant JVMs can differ enormously here.

## Step-by-Step Example
```java
public class Sum {
    public static void main(String[] args) {
        int total = 0;
        for (int i = 0; i < 1_000_000; i++) total += square(i);
        System.out.println(total);
    }
    static int square(int x) { return x * x; }
}
```
`javac` emits bytecode; the Application loader loads `Sum`; `main` is
interpreted while `square` calls increment an invocation counter; once that
crosses HotSpot's threshold, C1 compiles `square`, and sustained looping
may trigger a later C2 recompilation that inlines it entirely.

## Visualization
```text
 Sum.java --javac--> Sum.class (bytecode)
             [ClassLoader: Bootstrap -> Platform -> Application]
   +----------------- JVM process -----------------+
   |  Heap            Method Area / Metaspace       |
   |  Thread: main -> JVM stack [main][square]      |
   +-------------------------------------------------+
        interpret (cold) | JIT-compile (hot) -> native machine code
```

## Real-World Usage
Teams fix `-Xms`/`-Xmx` equal to avoid resize pauses, rely on tiered
compilation to balance warm-up against throughput, and profile slow startup
with Java Flight Recorder — a pain point for short-lived JVMs that
motivated Class Data Sharing and GraalVM Native Image.

## Advantages
- True portability across OS/CPU without recompilation.
- Adaptive JIT optimization based on real runtime profiles.

## Disadvantages
- Warm-up cost before peak performance is reached.
- Extra per-object and per-process memory overhead versus native code.

## Trade-offs
Interpretation trades peak speed for instant startup; JIT compilation
trades startup latency and memory for throughput; HotSpot's tiered design
compromises between C1's fast-but-modest code and C2's slow-but-excellent
code.

## Alternatives
GraalVM Native Image ahead-of-time compiles to a native executable for
instant startup and lower memory, at the cost of profile-guided runtime
optimization. Other managed runtimes (.NET CLR, V8) make similar
interpret-then-JIT trade-offs.

## Performance Analysis
Steady-state JIT-compiled throughput is often comparable to or better than
naive native code, since the JIT sees real branch/type profiles. Cold-start
latency is the JVM's weakest spot, driving CDS and Native Image adoption.

## Complexity Analysis (if applicable)
Class loading is O(1) amortized per class via loader caching. Non-virtual
dispatch is O(1); interface dispatch is optimized with inline caches. JIT
compilation is a one-time background cost, not charged to steady-state
execution.

## Implementation
```java
public final class Warmup {
    public static void main(String[] args) {
        long best = Long.MAX_VALUE;
        for (int round = 0; round < 5; round++) {
            long start = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < 20_000_000; i++) sum += fib(20);
            long ms = (System.nanoTime() - start) / 1_000_000;
            best = Math.min(best, ms);
            System.out.println("round " + round + ": " + ms + " ms");
        }
        System.out.println("fastest: " + best + " ms");
    }
    static int fib(int n) { return n < 2 ? n : fib(n - 1) + fib(n - 2); }
}
// Run with -XX:+PrintCompilation to watch fib move from interpreted
// to C1 to (if hot enough) C2-compiled native code mid-run.
```

## Common Mistakes
- Benchmarking with one run and concluding "Java is slow" before the JIT
  ever gets a chance to compile the hot path.
- Setting `-Xms` far below `-Xmx`, forcing repeated resize pauses.

## Common Interview Questions
Q: What are the main runtime data areas of the JVM?

Answer:
Heap and Method Area/Metaspace are shared across threads; each thread has
its own JVM stack, PC register, and native stack, holding local variables
and partial results for in-progress calls.

Q: What is the difference between JIT compilation and interpretation?

Answer:
Interpretation runs bytecode instruction-by-instruction with no lasting
output, so it starts instantly but re-pays dispatch cost every time; JIT
compilation translates a method to native code once, so later calls run at
native speed.

Q: How does class loading work in the JVM?

Answer:
A class loader delegates to its parent first, loading the class itself only
if no ancestor already has; the JVM then verifies the bytecode, prepares
static fields, and runs static initializers exactly once.

## Follow-up Interview Questions
Q: Why does the JVM use tiered compilation instead of one optimizing compiler?

Answer:
C2 is slow to run and only pays off for hot methods, so tiered compilation
ships a "good enough" C1 version fast and reserves C2 for methods profiling
proves are worth the extra compile time.

Q: What happens if a JIT-compiled method's optimistic assumption is wrong?

Answer:
The JVM deoptimizes: it discards that native code, falls back to the
interpreter to re-profile, and may recompile later with a safer assumption.

## How the Interviewer Thinks
This chapter checks whether a candidate understands the interpret-profile-
compile pipeline rather than treating Java performance as a black box, and
correctly separates what the JVMS mandates from what HotSpot happens to do.

## Production Engineering Perspective
Production tuning starts with `-Xms == -Xmx`, bounding Metaspace to catch
classloader leaks, and profiling with Java Flight Recorder instead of
guessing. Container-aware JVMs read cgroup limits automatically, but teams
still cap heap/threads explicitly to leave headroom for off-heap memory.

## Common Misconceptions
- "Java is always slower than C++" — false; steady-state JIT-compiled Java
  is often competitive, though C++ usually wins on startup and footprint.
- "The JVM only interprets bytecode" — false since HotSpot 1.3; most hot
  production paths run as JIT-compiled native code.

## Revision Notes
- JVM = spec (JVMS) + runtime data areas + class loader delegation +
  interpreter/JIT execution.
- Tiered compilation: interpret -> C1 (fast, modest) -> C2 (slow, excellent).
- Deoptimization is the safety valve for speculative JIT optimizations.
- Cold-start weakness drives CDS and GraalVM Native Image adoption.

## Summary
The JVM turns bytecode into running behavior: it loads classes through
delegated class loaders, executes them first via interpretation, and
progressively promotes hot methods to optimized native code through tiered
JIT compilation, while the JVMS specifies required guarantees and leaves
the algorithms to the implementation.

---

# Garbage Collection

## What is it?
Garbage collection (GC) is the JVM's automatic reclamation of heap memory
for objects no longer reachable from any GC root, removing the need for
manual `free`/`delete`.

## Why do we need it?
Manual memory management causes use-after-free and leak bugs that are hard
to reproduce. GC eliminates use-after-free and narrows leaks to a more
analyzable problem: objects unintentionally still reachable.

## What problem does it solve?
It solves safe, automatic reclamation of memory whose lifetime cannot be
known statically, without hand-tracked reference counting for every object.

## History (only if useful)
CMS was deprecated in JDK 9 (JEP 291) and removed in JDK 14 (JEP 363). G1
became default in JDK 9 (JEP 248). ZGC (JDK 11) and Shenandoah (JDK 12)
target sub-millisecond pauses; Generational ZGC became default in JDK 23.

## How does it work?
**Beginner:** the JVM periodically finds objects no thread can reach
anymore and frees their memory automatically; application code never calls
`free`.
**Intermediate:** most collectors exploit the generational hypothesis —
most objects die young — by collecting a small young generation frequently
and a larger old generation rarely, promoting long-lived survivors.
**Advanced:** modern low-pause collectors (G1, ZGC, Shenandoah) trace
concurrently with running application threads using tri-color marking and
load/store barriers, compacting via region-based relocation to keep
stop-the-world pauses down to milliseconds even on huge heaps.

## Internal Working
Collectors trace reachability from GC roots using tri-color marking, then
sweep or compact. Generational collectors use a card table to record
old-to-young references cheaply, so a young collection need not scan the
whole old generation.
**Spec vs. implementation:** the JLS never names a GC algorithm; it only
implies reclamation happens once an object is unreachable. Generational
layout, G1/ZGC/Shenandoah, and every tuning flag are HotSpot choices.

## Step-by-Step Example
```java
void demo() {
    byte[] buf = new byte[1024];  // allocated in Eden
    process(buf);
    buf = null;                   // now unreachable
}                                 // next young GC reclaims it
```
Allocation happens in Eden via a fast bump pointer; nulling the reference
removes the last root; the next young collection finds the array
unreachable and reclaims Eden by copying survivors elsewhere.

## Visualization
```text
 Heap
 +------------- Young Gen -------------+------- Old Gen -------+
 | Eden | Survivor S0 | Survivor S1     | Tenured (promoted)    |
 +------+-------------+-----------------+-----------------------+
   new objects   survive a few GCs      survive many young GCs
```

## Real-World Usage
Latency-sensitive services pick ZGC or Shenandoah for sub-millisecond
pauses; throughput batch jobs prefer G1 or Parallel GC. Teams monitor GC
logs and alert on pause-time percentiles, since rising promotion rate often
signals a leak before an `OutOfMemoryError`.

## Advantages
- Eliminates use-after-free and double-free bugs entirely.
- Modern collectors scale to huge heaps with low pause times.

## Disadvantages
- Pauses, however small, remain nondeterministic from the app's view.
- Logical leaks (unintentionally retained references) are not prevented.

## Trade-offs
Throughput collectors maximize application CPU time at the cost of longer,
less frequent pauses; low-latency collectors minimize pause time by doing
more work concurrently, costing some throughput; G1 offers a middle ground.

## Alternatives
Manual management (C/C++) trades safety for zero GC overhead. Reference
counting (Swift, CPython) reclaims deterministically but needs cycle
detection. Rust's borrow checker reclaims at compile time with no GC.

## Performance Analysis
Young collections cost roughly O(live young objects), not heap size, since
dead space is skipped. Old/full collections scale with live old-gen size,
making rising old-gen occupancy the real health signal, not raw heap size.

## Complexity Analysis (if applicable)
A young collection is O(live objects in that generation); a full collection
is O(total live heap) — the pause operators fear most on huge heaps.

## Implementation
```java
public class LeakDemo {
    static final List<byte[]> cache = new ArrayList<>(); // static root!
    public static void main(String[] args) {
        for (int i = 0; i < 100_000; i++) cache.add(new byte[1024]);
        // java -Xmx64m -Xlog:gc LeakDemo shows old-gen occupancy
        // climbing every cycle and never falling: a logical leak.
    }
}
```

## Common Mistakes
- Holding references in long-lived static collections and blaming the GC.
- Tuning heap size or GC choice before ever reading GC logs or profiles.

## Common Interview Questions
Q: What is the generational hypothesis and why does it matter for GC design?

Answer:
Most objects die shortly after allocation, so it pays to split the heap
into a small young generation collected often and cheaply, and a large old
generation collected rarely — far faster than scanning the whole heap
uniformly every time.

Q: What is the difference between a minor GC and a major/full GC?

Answer:
A minor GC collects only the young generation and is fast since live data
there is typically small. A major/full GC collects the old generation
(often the whole heap) and is far more expensive.

Q: How does G1 achieve more predictable pause times than older collectors?

Answer:
G1 divides the heap into fixed-size regions, tracks expected reclaimable
garbage per region, and each pause collects only the regions that fit a
configured pause-time goal, prioritizing the ones with the most garbage.

## Follow-up Interview Questions
Q: Why can't the JVM avoid all pauses by doing everything concurrently?

Answer:
Some phases, such as a final consistency check before relocating objects,
are unsafe while application threads mutate references, so even
"concurrent" collectors like ZGC still use a short stop-the-world pause.

Q: What does steadily rising old-generation occupancy across many GC cycles usually indicate?

Answer:
A memory leak: something (a growing cache, an unbounded queue) keeps
retaining references to otherwise-dead objects, so each full GC reclaims
less until the app eventually throws `OutOfMemoryError`.

## How the Interviewer Thinks
This chapter tests whether a candidate reasons about GC as a throughput
versus latency versus footprint trade-off rather than reciting collector
names, and separates "GC happens" (a reachability guarantee) from "which
algorithm runs" (a pure implementation choice).

## Production Engineering Perspective
Production teams graph pause-time percentiles, allocation rate, and
promotion rate alongside request latency, size heaps from measured
live-data-after-full-GC plus headroom, and validate collector choice
under production-like load rather than assuming from documentation alone.

## Common Misconceptions
- "GC means Java never leaks memory" — false; unintentionally retained
  references leak just as surely as forgetting to `free` in C.
- "`System.gc()` forces an immediate full collection" — false; it is only
  a suggestion the JVM may defer or ignore.

## Revision Notes
- Generational hypothesis -> young/old split -> minor GC cheap, major GC
  expensive.
- Collector families: throughput (Parallel), balanced (G1, default since
  JDK 9), low-latency (ZGC/Shenandoah, Generational ZGC default since JDK 23).
- The JLS guarantees reclamation-when-unreachable; it never mandates an
  algorithm.

## Summary
Garbage collection automatically reclaims heap memory for unreachable
objects, exploiting the generational hypothesis to make common-case
collection cheap; the specific algorithm is a HotSpot implementation choice
trading throughput, pause time, and memory footprint, not a language
guarantee.

---

# Collections

## What is it?
The Java Collections Framework (`java.util`) is a unified set of interfaces
(`Collection`, `List`, `Set`, `Queue`, `Map`) and implementations
(`ArrayList`, `HashMap`, `TreeMap`, ...), plus algorithms that operate on
them polymorphically.

## Why do we need it?
Before it existed, codebases reinvented lists, sets, and maps with
inconsistent APIs. A standard framework gives interoperable structures so
code reasons by interface rather than each concrete class's quirks.

## What problem does it solve?
It solves the need for reusable, type-safe, interoperable data structures
with documented performance, so code can be written against
`List<T>`/`Map<K,V>` and swap implementations without changing call sites.

## History (only if useful)
Java 1.0's legacy classes (`Vector`, `Hashtable`) synchronized every method
by default, hurting single-threaded performance. JDK 1.2 (1998) introduced
the modern, unsynchronized-by-default framework; generics (Java 5) added
compile-time type safety; Java 8 added default methods and Streams.

## How does it work?
**Beginner:** pick the interface matching the data's shape — ordered
(`List`), unique (`Set`), key-value (`Map`) — then pick an implementation
for its performance profile.
**Intermediate:** `ArrayList` backs a `List` with a resizable array (fast
random access); `LinkedList` uses linked nodes (O(1) insert at a known
node); `HashSet`/`HashMap` hash for O(1) average lookup; `TreeMap`/
`TreeSet` use a red-black tree for O(log n) sorted operations.
**Advanced:** iterators are fail-fast (a `modCount` check), throwing
`ConcurrentModificationException` as a best-effort debugging aid, not a
concurrency guarantee, while `List.of` guards against accidental mutation.

## Internal Working
`ArrayList` grows its backing array (roughly 1.5x) on overflow and copies
elements. `HashSet` is a thin wrapper over a `HashMap`. `TreeMap`/`TreeSet`
keep sorted order via `Comparable`/`Comparator` in a self-balancing tree.
**Spec vs. implementation:** the `Collection`/`List`/`Map`/`Set` contracts
are part of the Java SE API; the exact `ArrayList` growth factor and
`HashMap` bucketing scheme are reference-JDK details, not guarantees.

## Step-by-Step Example
```java
List<String> names = new ArrayList<>();
names.add("Ann"); names.add("Bo");
names.add(0, "Cy");        // ["Cy","Ann","Bo"] -- shifts elements right
names.remove("Ann");       // ["Cy","Bo"]
Collections.sort(names);   // ["Bo","Cy"]
```
Indexed `add` shifts trailing elements via `System.arraycopy`; `remove`
shifts remaining elements left; `sort` uses a tuned merge/insertion hybrid.

## Visualization
```text
 List<String> (ArrayList)        Map<String,Integer> (HashMap)
 index:0    1                     buckets:
 +----+----+----+---+ cap 4       [1] -> ("age",30) -> ("bmi",22)  (chain)
 |"Bo"|"Cy"|    |   |             [3] -> ("id",7)
 +----+----+----+---+ size 2
```

## Real-World Usage
`ArrayList`/`HashMap` default nearly every in-memory shaping and caching
task. `ArrayDeque` replaces legacy `Stack`/`LinkedList` for stack/queue use.
`EnumMap`/`EnumSet` suit finite, enum-keyed lookups in hot paths.

## Advantages
- One consistent, documented API across many implementations.
- Interfaces let call sites depend on behavior, easing future swaps.

## Disadvantages
- Autoboxing (`List<Integer>`) adds per-element overhead over primitives.
- Wrong implementation choice can silently degrade O(1) to O(n).

## Trade-offs
`ArrayList` trades slower middle insertion for fast, cache-friendly random
access; `LinkedList` trades that for O(1) insertion at a known node.
`HashMap` trades ordering for O(1) average lookup; `TreeMap` trades speed
for guaranteed sorted order.

## Alternatives
Primitive arrays avoid boxing but sacrifice resizing and shared algorithms.
Libraries like Eclipse Collections/fastutil offer primitive-specialized
collections. Immutable libraries (Guava, Vavr) trade mutation convenience
for structural-sharing safety.

## Performance Analysis
`ArrayList.get` is O(1); middle insert/remove is O(n) from shifting.
`HashMap` is O(1) average, O(log n) worst case since Java 8's treeification.
`TreeMap` is always O(log n).

## Complexity Analysis (if applicable)
| Operation | ArrayList | LinkedList | HashMap | TreeMap |
|---|---|---|---|---|
| get | O(1) | O(n) | O(1) avg | O(log n) |
| add at end | O(1) amortized | O(1) | O(1) avg | O(log n) |
| add/remove at front | O(n) | O(1) | n/a | n/a |

## Implementation
```java
Map<String, List<Integer>> scores = new HashMap<>();
scores.computeIfAbsent("Ann", k -> new ArrayList<>()).add(92);
scores.computeIfAbsent("Ann", k -> new ArrayList<>()).add(88);
for (var e : scores.entrySet()) {
    double avg = e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
    System.out.printf("%s: %.1f%n", e.getKey(), avg);
}
```

## Common Mistakes
- Defaulting to `LinkedList` "because insertion is O(1)" without checking
  whether the access pattern is actually index-heavy.
- Mutating a `List` while iterating with a for-each loop instead of using
  `Iterator.remove()` or `removeIf`.

## Common Interview Questions
Q: When would you choose ArrayList over LinkedList, and vice versa?

Answer:
Choose `ArrayList` when random access or cache-friendly iteration dominates;
choose `LinkedList` only when inserting/removing at a known node is
frequent, and even then `ArrayDeque` usually beats it for stack/queue use.

Q: What does fail-fast mean for Java's iterators, and what is it not?

Answer:
An iterator detects structural modification via a version check and throws
`ConcurrentModificationException` as a debugging aid; it is not a
concurrency guarantee and must never be relied on for thread safety.

Q: Why must equals and hashCode be overridden together?

Answer:
Equal objects must have equal hash codes, and hash-based collections use
`hashCode()` to pick a bucket before calling `equals()`. Overriding only
`equals` can put "equal" objects in different buckets, breaking lookups.

## Follow-up Interview Questions
Q: Why is List.of() immutable while Arrays.asList() is only partially mutable?

Answer:
`List.of()` returns a dedicated immutable implementation. `Arrays.asList()`
wraps the given array directly, so `set` writes through, but `add`/`remove`
are unsupported since the backing array cannot resize.

Q: How does Collections.synchronizedList differ from CopyOnWriteArrayList?

Answer:
`synchronizedList` wraps every call in one lock, so iteration still needs
external synchronization. `CopyOnWriteArrayList` copies its backing array
on mutation, giving lock-free, snapshot-consistent iteration for read-heavy
workloads.

## How the Interviewer Thinks
This chapter checks whether a candidate picks data structures from measured
access patterns and documented complexity rather than folklore, and knows
the `equals`/`hashCode` contract, since bugs here are common in real code.

## Production Engineering Perspective
Production code favors `ArrayList`/`HashMap`/`ArrayDeque` by default,
reaching for specialized structures only with a measured reason; immutable
collections (`List.of`, Guava `ImmutableMap`) suit shared configuration.

## Common Misconceptions
- "HashMap iteration order is basically insertion order" — false; it
  depends on hashing and can change across resizes; use `LinkedHashMap`.
- "Synchronized collections are fully thread-safe for compound operations"
  — false; check-then-act sequences still need external locking.

## Revision Notes
- Pick by interface/shape first, implementation second (by access pattern).
- `ArrayList`: O(1) index access; `LinkedList`: O(1) at a known node.
- `HashMap`/`HashSet`: O(1) average; `TreeMap`/`TreeSet`: O(log n) sorted.

## Summary
The Collections Framework standardizes list/set/map/queue behavior behind
shared interfaces while letting implementations specialize for different
performance profiles; choosing the right implementation for a workload's
actual access pattern is the practical skill this chapter tests.

---

# HashMap Internals

## What is it?
`HashMap<K,V>` is `java.util`'s general-purpose hash-table `Map`: an array
of buckets, each holding a linked list (or, since Java 8, occasionally a
red-black tree) of entries whose keys hash to that bucket, giving average
O(1) get/put.

## Why do we need it?
Arrays give O(1) access only by a small dense integer index; real keys are
arbitrary objects. `HashMap` gives the same O(1)-average access by
converting a key into an array index via its hash.

## What problem does it solve?
It solves fast key-based lookup, insertion, and deletion for arbitrary key
types without sorted keys, trading strict ordering for near-constant access.

## History (only if useful)
Java 8 changed collision handling from plain linked lists to red-black
trees once a bucket's chain grows past a threshold, bounding worst-case
get/put at O(log n) instead of O(n), and made resizing split each bucket
via one extra hash bit instead of full rehashing.

## How does it work?
**Beginner:** `put` computes `key.hashCode()`, picks a bucket, and stores
the entry; `get` recomputes the hash, goes to the same bucket, and scans
for a key that `equals()` the lookup key.
**Intermediate:** the raw hash is spread (`h ^ (h >>> 16)`) to mix high bits
into low bits before masking with `(capacity - 1)`, since capacity is a
power of two and a naive mask would otherwise ignore high hash bits.
**Advanced:** a bucket converts to a red-black tree once it exceeds
`TREEIFY_THRESHOLD` (8) entries and the table has at least
`MIN_TREEIFY_CAPACITY` (64) buckets; it reverts below `UNTREEIFY_THRESHOLD`
(6), typically during a resize split.

## Internal Working
`HashMap` doubles capacity once `size > capacity * loadFactor` (default
0.75). Since Java 8, resize splits each old bucket's chain into "low" and
"high" chains using one extra hash bit, without rehashing from scratch.
Null keys are allowed (hard-coded to bucket 0); null values are allowed
anywhere.
**Spec vs. implementation:** the `Map` contract only guarantees `get`
returns the value for an `equals`-equal key and leaves iteration order
unspecified. Bucket count, load factor, treeification thresholds, and the
resize-split algorithm are reference-implementation details.

## Step-by-Step Example
```java
Map<String, Integer> m = new HashMap<>(); // capacity 16
m.put("apple", 1);
m.put("grape", 2);   // may share apple's bucket -> chained
m.get("apple");      // same hash -> same bucket -> equals() scan -> 1
```
Hash is computed and spread; bucket index = `spreadHash & (capacity - 1)`;
an empty bucket inserts directly, an occupied one is walked comparing hash
then `equals()`; once `size` exceeds `threshold` the table resizes.

## Visualization
```text
 capacity = 16
 index: ... 2 ...
            [Node("grape",2)] -> [Node("apple",1)]   (collision chain)
            both hash to bucket 2; equals() disambiguates on get
```

## Real-World Usage
`HashMap` underlies caches, request-scoped lookups, and grouping results
(`Collectors.groupingBy`) across most Java services whenever key order does
not matter and keys hash well.

## Advantages
- O(1) average get/put/remove regardless of map size.
- Treeified worst case (O(log n)) protects against pathological collisions.

## Disadvantages
- Unordered iteration surprises engineers expecting insertion order.
- Not thread-safe; concurrent modification can corrupt internal structure.

## Trade-offs
A higher load factor trades more collisions for lower memory overhead; a
lower one trades memory for speed via earlier resizing. Pre-sizing trades a
larger initial allocation for avoiding resize pauses during warm-up.

## Alternatives
`LinkedHashMap` adds predictable iteration order at small overhead.
`TreeMap` gives sorted iteration at O(log n) per operation.
`ConcurrentHashMap` gives the same hash-table shape with thread safety
(next chapter).

## Performance Analysis
With a well-distributed hash and default load factor, operations are O(1)
average; a single resize touching millions of entries is a visible latency
spike, so pre-sizing (`new HashMap<>(expectedSize)`) matters on hot paths.

## Complexity Analysis (if applicable)
Average: O(1) get/put/remove. Worst case pre-Java-8: O(n). Worst case
Java 8+ (treeified): O(log n). Resize: O(n) for that call, amortized O(1)
per put over the map's lifetime.

## Implementation
```java
public final class SimpleHashMap<K, V> {
    @SuppressWarnings("unchecked")
    private Node<K, V>[] table = new Node[16];
    public void put(K key, V value) {
        int idx = spread(key.hashCode()) & (table.length - 1);
        for (Node<K, V> n = table[idx]; n != null; n = n.next) {
            if (n.key.equals(key)) { n.value = value; return; }
        }
        table[idx] = new Node<>(key, value, table[idx]);
    }
    public V get(K key) {
        int idx = spread(key.hashCode()) & (table.length - 1);
        for (Node<K, V> n = table[idx]; n != null; n = n.next)
            if (n.key.equals(key)) return n.value;
        return null;
    }
    private static int spread(int h) { return h ^ (h >>> 16); }
    private static final class Node<K, V> {
        final K key; V value; final Node<K, V> next;
        Node(K k, V v, Node<K, V> n) { key = k; value = v; next = n; }
    }
}
```

## Common Mistakes
- Mutating a key object's fields after insertion, "losing" the entry
  because its hash no longer matches its bucket.
- Relying on `HashMap` iteration order being stable across versions/resizes.

## Common Interview Questions
Q: Why does HashMap spread a key's hashCode before using it as a bucket index?

Answer:
Capacity is always a power of two, so masking with `(capacity - 1)` only
looks at low bits; spreading folds high bits into low bits first, improving
bucket distribution for typical hash functions.

Q: What happens internally when a HashMap resizes?

Answer:
The table doubles and every bucket's chain splits in two based on one extra
significant bit of the spread hash, so entries move to the same index or
index-plus-old-capacity, avoiding a full rehash from scratch.

Q: When does a HashMap bucket convert to a tree, and why?

Answer:
Once a bucket holds more than 8 entries and the table has at least 64
buckets, it converts to a red-black tree, bounding that bucket's worst-case
lookup at O(log n) instead of O(n).

## Follow-up Interview Questions
Q: Why is it dangerous to mutate an object used as a HashMap key after insertion?

Answer:
The entry lives in the bucket its hash determined at insertion time; if a
mutable field used in `hashCode()` changes afterward, a later `get` looks
in the wrong bucket and cannot find the entry, even though it is still
present in memory.

Q: Why does HashMap allow a null key but ConcurrentHashMap does not?

Answer:
`HashMap` hard-codes the null key to bucket 0, fine for single-threaded or
externally-synchronized use. `ConcurrentHashMap` forbids null because a
null return from `get` would be ambiguous between "absent" and "mapped to
null" under concurrent modification.

## How the Interviewer Thinks
This chapter probes whether a candidate understands `HashMap` beyond
"it's fast" — the hash-spreading rationale, the treeification safety net,
and the resize-splitting trick — and separates the documented `Map`
contract from HotSpot-specific mechanics.

## Production Engineering Perspective
Teams pre-size `HashMap`s to a known or estimated final size to avoid
mid-request resize pauses, keep key classes immutable, and stay alert to
hash-flooding risk on untrusted-input keys, mitigated by hash randomization
and treeification.

## Common Misconceptions
- "HashMap preserves insertion order" — false; use `LinkedHashMap` for that.
- "A HashMap with N entries always has N buckets" — false; bucket count is
  a power of two sized independently, governed by load factor.

## Revision Notes
- Bucket index = spread(hashCode) & (capacity - 1).
- Resize doubles capacity, splits chains via one extra hash bit.
- Treeify above 8 entries per bucket (capacity >= 64); untreeify at 6.

## Summary
`HashMap` achieves average O(1) operations by hashing keys into buckets,
spreading hash bits for better distribution, resizing via an efficient
bit-based split, and treeifying long collision chains to cap worst-case
behavior — all HotSpot implementation choices layered on the simpler `Map`
interface contract.

---

# ConcurrentHashMap

## What is it?
`ConcurrentHashMap<K,V>` is `java.util.concurrent`'s thread-safe hash-table
`Map`, built for high concurrent read/write throughput without locking the
entire map per operation.

## Why do we need it?
`HashMap` is not thread-safe; `Collections.synchronizedMap` is safe but
serializes every operation on one lock. `ConcurrentHashMap` gives thread
safety while letting unrelated operations proceed in parallel.

## What problem does it solve?
It solves safe, scalable concurrent access to a shared map from many
threads without external synchronization, and without one global lock
becoming the bottleneck.

## History (only if useful)
Introduced in Java 5 (JSR-166) using 16 lock-striped `Segment`s. Java 8
removed segments, switching to CAS for an empty bin's first node and
`synchronized` only on that bin's head node otherwise, plus tree bins for
long chains.

## How does it work?
**Beginner:** multiple threads read and write different parts of the map
simultaneously without external locks, and each operation is atomic.
**Intermediate:** `put` uses CAS to install the first node in an empty bin
lock-free; if occupied, it synchronizes only on that bin's head node, so
unrelated bins never contend.
**Advanced:** size is tracked via a striped counter-cell array (the idea
`LongAdder` generalizes) rather than one shared counter, and resizing lets
multiple threads cooperatively help transfer bins to the new table.

## Internal Working
Each bin's head node is effectively its own lock, so contention scales with
threads hitting the *same* bin, not the whole map. Iterators are weakly
consistent: never throwing `ConcurrentModificationException`, reflecting
the map's state at some point during iteration.
**Spec vs. implementation:** `ConcurrentMap` (JSR-166) guarantees atomicity
for `putIfAbsent`/`computeIfAbsent`/`replace` and no corruption under
concurrent access; it never mandates per-bin locking or CAS — those are
HotSpot's implementation choices.

## Step-by-Step Example
```java
ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
counts.putIfAbsent("visits", 0);
counts.compute("visits", (k, v) -> v + 1); // atomic read-modify-write
```
`putIfAbsent` atomically inserts only if absent; `compute` holds that bin's
lock for the whole read-modify-write; two threads on different keys proceed
fully in parallel, only same-key calls serialize.

## Visualization
```text
 table (array of bins)
 [0] Node A  (synchronized on A while modifying this bin)
 [1] Node B  <- thread T2 can modify this bin concurrently with T1 on bin 0
 [2] empty -> CAS installs new head, no lock needed
```

## Real-World Usage
`ConcurrentHashMap` backs shared caches, metrics registries, and
deduplication sets in multi-threaded services; `computeIfAbsent` is the
idiomatic way to implement a lazily-populated concurrent cache.

## Advantages
- High concurrent throughput; contention limited to same-bin threads.
- Atomic compound operations close common check-then-act races.

## Disadvantages
- No null keys or values (removes an absent-vs-null ambiguity).
- `size()` is a best-effort estimate under heavy concurrent mutation.

## Trade-offs
Fine-grained per-bin locking trades a small constant overhead for far
better scalability than one global lock — worthwhile when different keys
are contended concurrently, wasted otherwise.

## Alternatives
`Collections.synchronizedMap` is simpler but serializes on one lock.
Copy-on-write structures suit read-heavy, write-rare data better. External
caches (Caffeine, Redis) add eviction and cross-process sharing.

## Performance Analysis
Reads are largely lock-free and scale near-linearly with reader threads.
Write throughput scales well when writes spread across many bins; hammering
one key still serializes on that bin's lock regardless of thread count.

## Complexity Analysis (if applicable)
Same asymptotic profile as `HashMap`: O(1) average get/put/remove, O(log n)
worst case for a treeified bin.

## Implementation
```java
import java.util.concurrent.ConcurrentHashMap;

public final class HitCounter {
    private final ConcurrentHashMap<String, Long> hits = new ConcurrentHashMap<>();
    public void recordHit(String path) { hits.merge(path, 1L, Long::sum); }
    public long hitsFor(String path) { return hits.getOrDefault(path, 0L); }
}
```

## Common Mistakes
- Writing `if (!map.containsKey(k)) map.put(k, v)` instead of
  `putIfAbsent`/`computeIfAbsent`, reintroducing a race.
- Treating `size()` as a perfectly consistent snapshot under mutation.

## Common Interview Questions
Q: How does ConcurrentHashMap achieve better concurrency than a synchronized HashMap?

Answer:
It uses CAS to install empty bins lock-free and synchronizes only on an
occupied bin's head node, so threads on different bins never block each
other, unlike a single global lock serializing every operation.

Q: Are compound operations like computeIfAbsent atomic in ConcurrentHashMap?

Answer:
Yes; the whole read-check-write sequence for a key runs while holding that
bin's lock (or via CAS), so no other thread can interleave a partial update
for the same key.

Q: Why doesn't ConcurrentHashMap allow null keys or values?

Answer:
If `get` could return null both for "absent" and "mapped to null," a caller
would have no race-free way to distinguish them, so nulls are forbidden
entirely.

## Follow-up Interview Questions
Q: How does its iterator behave if the map is modified during iteration?

Answer:
It is weakly consistent: guaranteed not to throw
`ConcurrentModificationException`, reflecting the map's state at some point
at or after iterator creation — a deliberate relaxation of `HashMap`'s
fail-fast behavior.

Q: How is size() implemented efficiently under high concurrency?

Answer:
Updates spread across a striped array of counter cells instead of one
shared counter, and `size()` sums them, trading a slightly approximate read
for far less write contention — the idea `LongAdder` generalizes.## How the Interviewer Thinks
Interviewers check whether a candidate can explain *why* it scales
(fine-grained locking plus CAS) rather than naming it "the thread-safe
HashMap," and whether they know its atomic methods eliminate check-then-act
races a plain synchronized map still permits.

## Production Engineering Perspective
Production caches default to `ConcurrentHashMap` with `computeIfAbsent`/
`merge`; teams watch for "hot key" bottlenecks where one bin's lock becomes
a de facto serialization point, and avoid slow work inside compute lambdas.

## Common Misconceptions
- "ConcurrentHashMap has no locks at all" — false; it minimizes locking to
  per-bin synchronization plus CAS.
- "size() is always exact" — false under concurrent mutation.

## Revision Notes
- Java 8+: CAS for empty bins, synchronized on bin head node otherwise.
- Compound methods (`putIfAbsent`, `computeIfAbsent`, `merge`) are atomic.
- No null keys/values; weakly consistent iterators; striped size counters.

## Summary
`ConcurrentHashMap` scales by confining locking to individual bins (CAS for
the lock-free common case), offers atomic compound operations that close
check-then-act races, and relaxes some guarantees (null keys, exact size)
for high real-world concurrent throughput.

---

# ExecutorService

## What is it?
`ExecutorService` is the `java.util.concurrent` interface for managing a
pool of worker threads and a queue of submitted tasks, decoupling what work
runs from which thread runs it and when.

## Why do we need it?
A raw `new Thread(...)` per task is expensive and gives no built-in way to
bound concurrency, collect results, or handle failures uniformly.

## What problem does it solve?
It solves running many short-lived tasks concurrently without per-task
thread-creation cost or unbounded thread growth, with a uniform way to
submit work, retrieve results, and shut down cleanly.

## History (only if useful)
Introduced in Java 5 (JSR-166, 2004), replacing hand-rolled thread pools.
Java 21 finalized virtual threads (JEP 444), enabling one cheap virtual
thread per task via `Executors.newVirtualThreadPerTaskExecutor()`.

## How does it work?
**Beginner:** submit a task with `submit(...)`; the executor runs it on a
managed thread and returns a `Future` for the result.
**Intermediate:** `ThreadPoolExecutor` keeps `corePoolSize` threads alive,
queues excess tasks, and grows to `maximumPoolSize` only once the queue is
full, applying a `RejectedExecutionHandler` beyond that.
**Advanced:** idle threads beyond `corePoolSize` terminate after
`keepAliveTime`; the queue type fundamentally changes backpressure — an
unbounded queue means `maximumPoolSize` is effectively never reached.

## Internal Working
`execute(task)` follows a strict order: start a new thread if under
`corePoolSize`; else try to enqueue; if the queue rejects, grow up to
`maximumPoolSize`; if that also fails, invoke the rejection handler.
**Spec vs. implementation:** `ExecutorService` only guarantees submitted
tasks eventually run and lifecycle methods behave as documented; the
pooling/queueing algorithm belongs to `ThreadPoolExecutor`, one
implementation among possible conformant ones.

## Step-by-Step Example
```java
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> future = pool.submit(() -> 40 + 2);
System.out.println(future.get());   // blocks until done: 42
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
```
`submit` enqueues the task; a worker runs it and stores the result in the
`Future`; `get()` blocks until available; `shutdown` stops new submissions.

## Visualization
```text
 caller --submit--> [ queue: t1, t2, t3 ] -> worker1 worker2 worker3
                     each worker: take -> run -> Future.set(result)
```

## Real-World Usage
Servers and batch pipelines size bounded pools to CPU cores (CPU-bound) or
much larger (I/O-bound) instead of one thread per request;
`ScheduledExecutorService` runs periodic housekeeping on a managed schedule.

## Advantages
- Reuses threads, amortizing expensive OS thread creation.
- Uniform result/error handling via `Future`, plus clean shutdown.

## Disadvantages
- Many tuning knobs that are easy to misconfigure.
- Blocking calls in tasks can starve the whole pool.

## Trade-offs
A small fixed pool trades peak throughput for predictable resource usage; a
cached pool trades predictability for bursty-load adaptability. Bounded
queues surface backpressure early; unbounded queues never reject but can
grow latency without limit.

## Alternatives
Virtual threads (JEP 444) give each task its own cheap thread, removing
most pool-sizing questions for I/O-bound work. `CompletableFuture`
pipelines restructure I/O-bound work to avoid blocking a pool thread.

## Performance Analysis
CPU-bound throughput peaks near one thread per core; more adds
context-switch overhead. I/O-bound work needs a much larger pool (or
virtual threads) since threads spend most time blocked.

## Complexity Analysis (if applicable)
Submission/dispatch is O(1) amortized; overall throughput is bounded by
`min(pool size, cores)` for CPU-bound work or by external I/O latency.

## Implementation
```java
ExecutorService pool = new ThreadPoolExecutor(
    4, 8, 60, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy());
for (int i = 0; i < 20; i++) {
    int id = i;
    pool.submit(() -> process(id));
}
pool.shutdown();
```

## Common Mistakes
- Using `newCachedThreadPool` (unbounded growth) under high load, hiding
  overload instead of applying real backpressure.
- Blocking I/O inside tasks on a small CPU-sized pool, starving other work.

## Common Interview Questions
Q: What is the difference between corePoolSize and maximumPoolSize?

Answer:
`corePoolSize` threads stay alive even when idle; `maximumPoolSize` is the
hard growth cap, reached only once the queue is full.

Q: What happens when a bounded queue and maximumPoolSize are both exhausted?

Answer:
The executor invokes its `RejectedExecutionHandler`; the default
`AbortPolicy` throws `RejectedExecutionException`, while `CallerRunsPolicy`
runs the task on the submitting thread as simple backpressure.

Q: Why is Executors.newCachedThreadPool() risky in production?

Answer:
It has an unbounded `maximumPoolSize` and a `SynchronousQueue`, so
sustained load can create unbounded threads and exhaust memory; a custom
bounded `ThreadPoolExecutor` is safer.

## Follow-up Interview Questions
Q: How should pool size differ for CPU-bound versus I/O-bound tasks?

Answer:
CPU-bound work saturates near one thread per core; I/O-bound work needs a
larger pool (or virtual threads) so the CPU has other runnable work while
some threads wait.

Q: How do virtual threads change ExecutorService usage patterns?

Answer:
Each task gets its own lightweight virtual thread instead of sharing a
small pool of expensive OS threads, so blocking I/O per task becomes cheap
and most pool-sizing tuning for I/O-bound work disappears.

## How the Interviewer Thinks
This checks whether a candidate reasons about a pool as a bounded resource
with explicit queueing and rejection semantics, connecting sizing decisions
to CPU-bound versus I/O-bound characteristics.

## Production Engineering Perspective
Production pools are explicitly bounded with a sized queue and a deliberate
rejection policy, monitored for queue depth and rejection rate as an early
overload signal.

## Common Misconceptions
- "More threads always mean more throughput" — false past the point of
  available cores for CPU-bound work.
- "Executors.newCachedThreadPool is always a safe default" — false; it can
  grow threads without bound under load.

## Revision Notes
- Core threads persist; extra threads grow only once the queue is full, up
  to max, then rejection.
- Queue type controls backpressure behavior as much as pool size does.
- CPU-bound: size near core count. I/O-bound: size larger or use virtual
  threads.

## Summary
`ExecutorService` decouples task submission from thread management,
reusing a bounded pool of threads and applying explicit queueing and
rejection policies; correct sizing depends on whether work is CPU-bound or
I/O-bound, and virtual threads increasingly remove that tuning burden.

---

# CompletableFuture

## What is it?
`CompletableFuture<T>` is `java.util.concurrent`'s composable asynchronous
result container: a `Future` that can be explicitly completed, chained,
combined, and recovered from failure via callbacks, without blocking a
thread to wait for the result.

## Why do we need it?
A plain `Future` only supports blocking `get()`, with no built-in way to
chain "when this finishes, do that" or combine two async results.
`CompletableFuture` gives declarative composition instead of manual thread
coordination.

## What problem does it solve?
It solves building non-blocking, multi-stage asynchronous pipelines
without nested callbacks or threads blocked waiting on each other.

## History (only if useful)
Introduced in Java 8 (2014), implementing `CompletionStage` to standardize
asynchronous composition across libraries; Java 9 added timeout support
(`orTimeout`, `completeOnTimeout`).

## How does it work?
**Beginner:** `supplyAsync` runs work in the background and returns a
`CompletableFuture`; `thenApply` transforms its eventual result once ready.
**Intermediate:** `thenCompose` chains a step returning another
`CompletableFuture` (flattening nested futures); `thenCombine` joins two
independent futures; `exceptionally`/`handle` recover from failures.
**Advanced:** each stage without an explicit `Executor` runs on
`ForkJoinPool.commonPool()` by default, so CPU-heavy or blocking stages
should pass an explicit pool to avoid starving that shared, JVM-wide pool.

## Internal Working
A `CompletableFuture` holds a result field and a linked list of dependent
actions; completing it triggers those dependents, which may run on a
specified executor or the completing thread.
**Spec vs. implementation:** the `CompletionStage`/`Future` contracts are
part of the Java SE API; which thread runs a `then*` callback when no
executor is given is an implementation detail of the reference class.

## Step-by-Step Example
```java
CompletableFuture<String> result = CompletableFuture
    .supplyAsync(() -> fetchUser(1))
    .thenApply(user -> user.name().toUpperCase())
    .exceptionally(ex -> "UNKNOWN");
System.out.println(result.join());
```
`supplyAsync` runs `fetchUser` on the common pool; `thenApply` transforms
the result once ready; if either stage throws, `exceptionally` supplies a
fallback instead of propagating the exception.

## Visualization
```text
 supplyAsync(fetchUser) --> thenApply(toUpperCase) --> result
        |  (throws)                                       ^
        +-------------------- exceptionally ---------------+
```

## Real-World Usage
Service layers fan out several independent calls (`thenCombine`, `allOf`)
and merge results without blocking a request thread per call, common in
API gateways calling multiple downstream services concurrently.

## Advantages
- Non-blocking composition avoids one thread per pending call.
- Rich combinators (`thenCombine`, `allOf`, `anyOf`) express real pipelines.

## Disadvantages
- Chains can become hard to read and debug versus straight-line code.
- Default common-pool execution can silently starve unrelated async work.

## Trade-offs
Async composition trades straight-line readability for non-blocking
scalability; using the shared common pool trades explicit resource control
for convenience, fine for short CPU-only stages, risky for blocking I/O.

## Alternatives
Reactive libraries (Project Reactor, RxJava) offer richer operators and
backpressure for streams of values, not just single results. Virtual
threads let simple blocking code scale similarly without the composition
API at all.

## Performance Analysis
Non-blocking composition avoids tying up a pool thread per pending
operation, letting a small pool serve many concurrent pending pipelines;
mixing blocking calls into common-pool stages undoes that benefit entirely.

## Complexity Analysis (if applicable)
Each `then*` call is O(1) to register; overall pipeline latency is the
critical-path sum/max of its stages, not proportional to thread count.

## Implementation
```java
CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> priceOf("A"));
CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> priceOf("B"));
CompletableFuture<Integer> total = a.thenCombine(b, Integer::sum);
System.out.println(total.join());
```

## Common Mistakes
- Calling blocking I/O inside a stage running on the shared common pool.
- Using `thenApply` when the mapper returns a `CompletableFuture`,
  producing nested futures instead of using `thenCompose`.

## Common Interview Questions
Q: What is the difference between thenApply and thenCompose?

Answer:
`thenApply` transforms a result with a plain function; `thenCompose` chains
a function that itself returns a `CompletableFuture`, flattening the result
instead of nesting one future inside another.

Q: What thread runs a CompletableFuture callback if no executor is given?

Answer:
By default, `ForkJoinPool.commonPool()`, or the thread that completed the
previous stage if that stage was already finished when the callback was
attached.

Q: How does exceptionally differ from handle?

Answer:
`exceptionally` only runs on failure, supplying a fallback value; `handle`
always runs, receiving both the result and the exception (one will be
null), reacting to success and failure uniformly.

## Follow-up Interview Questions
Q: Why is it risky to run blocking calls on CompletableFuture's default executor?

Answer:
The default is the shared, JVM-wide `ForkJoinPool.commonPool()`; a blocking
call there occupies a thread other unrelated async work depends on,
potentially starving the whole application's async pipelines.

Q: How would you combine results from an arbitrary list of CompletableFutures?

Answer:
`CompletableFuture.allOf(futures)` completes once every future in the list
completes, letting you then collect each already-available `join()` result.

## How the Interviewer Thinks
This checks whether a candidate can design a non-blocking pipeline
correctly — using `thenCompose` versus `thenApply`, handling errors
explicitly, recognizing the shared-pool starvation risk — rather than only
knowing the method names exist.

## Production Engineering Perspective
Production pipelines pass explicit, purpose-sized executors to async
stages doing I/O or CPU-heavy work, always attach failure handling, and set
timeouts (`orTimeout`) so one slow downstream call cannot hang a pipeline.

## Common Misconceptions
- "CompletableFuture callbacks always run asynchronously" — false; an
  already-completed stage's callback may run on the calling thread.
- "get() and join() behave identically" — false; `get()` throws a checked
  `ExecutionException`, `join()` throws an unchecked `CompletionException`.

## Revision Notes
- `thenApply` transforms; `thenCompose` flattens; `thenCombine` joins two.
- Default execution uses the shared common pool; pass executors explicitly
  for blocking/heavy stages.
- Always attach `exceptionally`/`handle` for error paths.

## Summary
`CompletableFuture` composes asynchronous work declaratively through
chaining, combining, and error-handling stages, avoiding blocked threads
per pending operation, provided stages are given appropriate executors and
explicit failure handling instead of relying on defaults.

---

# synchronized

## What is it?
`synchronized` is Java's built-in keyword for mutual exclusion: it acquires
an object's intrinsic lock (monitor) before entering a block or method and
releases it on exit, even if an exception is thrown.

## Why do we need it?
Without exclusion, concurrent threads reading and writing shared mutable
state can interleave arbitrarily, producing lost updates and torn reads.
`synchronized` gives an in-language, exception-safe way to serialize access.

## What problem does it solve?
It solves both mutual exclusion (only one thread executes the critical
section at a time) and cross-thread visibility (changes made inside a
synchronized block become visible to the next thread that synchronizes on
the same monitor).

## History (only if useful)
Biased locking, an optimization assuming a lock is mostly used by one
thread, was disabled by default and deprecated in JDK 15 (JEP 374) and
removed in JDK 18. JEP 491 (JDK 24) removed virtual-thread "pinning" on
`synchronized`, so blocking inside a synchronized block no longer blocks
the whole carrier platform thread.

## How does it work?
**Beginner:** `synchronized(lock) { ... }` or a `synchronized` method
ensures only one thread runs that code on that lock at a time; other
threads wait.
**Intermediate:** the lock is reentrant — a thread already holding it can
re-enter another synchronized block on the same object without deadlocking
itself — and it is released automatically even if the block throws.
**Advanced:** HotSpot escalates locks from lock-free, to lightweight
(CAS-based, uncontended), to heavyweight (OS mutex, contended, with an
associated wait queue) only as contention actually appears, and the JIT can
eliminate locks entirely via escape analysis if an object provably never
leaves one thread.

## Internal Working
Bytecode uses `monitorenter`/`monitorexit` instructions (or, for
synchronized methods, an `ACC_SYNCHRONIZED` flag); `wait`/`notify`/
`notifyAll` on the same object coordinate threads waiting for a condition.
**Spec vs. implementation:** the JLS guarantees reentrancy, mutual
exclusion, and happens-before ordering on monitor exit/entry (JLS §17.4);
lock-eliding optimizations and lightweight/heavyweight escalation are
HotSpot implementation details.

## Step-by-Step Example
```java
class Counter {
    private int count = 0;
    synchronized void increment() { count++; }
    synchronized int get() { return count; }
}
```
Thread A calls `increment()`, acquiring `this`'s monitor; a concurrent
`increment()` from thread B blocks until A exits; `get()` synchronizes on
the same monitor, so it observes A's completed increment, not a partial one.

## Visualization
```text
 Thread A --acquire(this)--> [critical section: count++] --release-->
 Thread B ------------------------- waits ---------------------------> acquire
```

## Real-World Usage
Simple, low-contention shared counters and caches still use `synchronized`
for its simplicity; high-contention or advanced use cases (timeouts,
multiple conditions) move to `ReentrantLock` or concurrent collections.

## Advantages
- Built into the language: no imports, automatic release on exception.
- Reentrant by default, avoiding self-deadlock on nested calls.

## Disadvantages
- No tryLock, timeout, or interruptible acquisition.
- Only one implicit condition (`wait`/`notifyAll`) per monitor.

## Trade-offs
`synchronized` trades flexibility for simplicity and safety-by-construction
(cannot forget to unlock); explicit locks trade that simplicity for
finer-grained control over timeouts, fairness, and multiple conditions.

## Alternatives
`ReentrantLock` offers `tryLock`, timeouts, and multiple `Condition`
objects. `java.util.concurrent.atomic` classes avoid locking entirely for
single-variable updates via CAS.

## Performance Analysis
Uncontended `synchronized` is cheap (CAS-based lightweight locking); under
real contention it escalates to an OS-mutex-backed heavyweight lock, far
more expensive, so contention level matters more than the keyword itself.

## Complexity Analysis (if applicable)
Lock acquisition/release is O(1); throughput under contention is bounded by
critical-section length and thread count, not any inherent keyword cost.

## Implementation
```java
public final class BankAccount {
    private long balanceCents;
    public synchronized void deposit(long cents) { balanceCents += cents; }
    public synchronized boolean withdraw(long cents) {
        if (cents > balanceCents) return false;
        balanceCents -= cents;
        return true;
    }
}
```

## Common Mistakes
- Synchronizing on a mutable or non-private field, letting outside code
  lock on (and interfere with) the same monitor unexpectedly.
- Holding a lock across a slow or blocking I/O call, serializing unrelated
  work far longer than the actual critical section requires.

## Common Interview Questions
Q: What two guarantees does synchronized provide?

Answer:
Mutual exclusion (only one thread runs the critical section at a time) and
visibility (a happens-before edge from monitor exit to the next monitor
entry on the same object, per the Java Memory Model).

Q: What is the difference between synchronizing on an instance versus a class?

Answer:
Synchronizing on an instance locks per-object, so different instances do
not contend; synchronizing on a class literal (or a static synchronized
method) locks once for all instances, using the shared `Class` object.

Q: Why is synchronized reentrant?

Answer:
The monitor tracks both an owning thread and a hold count, so a thread
already holding the lock can re-enter another synchronized block on the
same object without blocking on itself.

## Follow-up Interview Questions
Q: How does lock escalation work in HotSpot?

Answer:
A lock starts lightweight (CAS-based, cheap) when uncontended, and only
escalates to a heavyweight, OS-mutex-backed lock with a real wait queue
once genuine contention is detected, keeping the common case fast.

Q: Why did JEP 491 matter for virtual threads and synchronized?

Answer:
Before JDK 24, a virtual thread blocking inside a synchronized block pinned
its carrier platform thread, preventing that carrier from running other
virtual threads; JEP 491 removed that pinning.

## How the Interviewer Thinks
This checks whether a candidate knows `synchronized` provides both
exclusion and visibility (not just exclusion), understands reentrancy, and
is aware of practical limitations that motivate `ReentrantLock`.

## Production Engineering Perspective
Production code keeps synchronized blocks as short as possible, avoids
locking on publicly-mutable or shared objects, and profiles for lock
contention before assuming a `ReentrantLock` rewrite is needed.

## Common Misconceptions
- "synchronized only prevents interleaving, not visibility" — false; it
  also establishes happens-before visibility per the JMM.
- "synchronized methods lock the whole class" — false except for static
  synchronized methods; instance methods lock `this`, per instance.

## Revision Notes
- Guarantees: mutual exclusion + happens-before visibility, reentrant.
- Instance methods lock `this`; static methods lock the `Class` object.
- Lock escalates from lightweight to heavyweight only under contention.

## Summary
`synchronized` is Java's built-in, exception-safe, reentrant mutual
exclusion and visibility primitive; it trades explicit control (timeouts,
tryLock, multiple conditions) for simplicity and safety-by-construction,
and modern JVMs optimize its uncontended case aggressively.

---

# ReentrantLock

## What is it?
`ReentrantLock` (`java.util.concurrent.locks`) is an explicit, reentrant
mutual-exclusion lock offering the same locking semantics as `synchronized`
plus optional fairness, timed/interruptible acquisition, and multiple
wait-conditions.

## Why do we need it?
`synchronized` cannot time out, cannot be interrupted while waiting, and
supports only one implicit wait-condition per monitor. `ReentrantLock`
addresses cases needing more control than the built-in keyword offers.

## What problem does it solve?
It solves mutual exclusion scenarios that need `tryLock` (avoid indefinite
blocking), `lockInterruptibly` (cancellable waiting), fairness ordering, or
several independent wait-conditions on one lock.

## History (only if useful)
Introduced in Java 5 (JSR-166) built on `AbstractQueuedSynchronizer` (AQS),
the same framework `Semaphore`, `CountDownLatch`, and `ReentrantReadWriteLock`
are built on, standardizing a reusable queued-lock primitive.

## How does it work?
**Beginner:** call `lock()` before a critical section and `unlock()` in a
`finally` block afterward, exactly mirroring `synchronized` but explicit.
**Intermediate:** `tryLock()`/`tryLock(timeout)` attempt acquisition without
blocking forever; `lockInterruptibly()` allows a waiting thread to be
interrupted instead of waiting indefinitely.
**Advanced:** AQS maintains an atomic integer state (hold count) and a CLH
(Craig, Landin, Hagersten) queue of waiting threads; a fair lock grants
access strictly in queue order, while an unfair (default) lock lets a newly
arriving thread race queued threads for a possible immediate acquisition.

## Internal Working
`lock()` attempts a CAS on AQS's state field; on success the calling thread
owns the lock (incrementing hold count on reentry); on failure it is
enqueued in the CLH queue and parked until unparked by the releasing thread.
**Spec vs. implementation:** the `Lock`/`Condition` interfaces (JSR-166)
guarantee documented mutual-exclusion and signaling semantics; AQS's
specific CAS-plus-CLH-queue mechanism is one influential implementation
other `Lock` implementations are not required to copy.

## Step-by-Step Example
```java
private final ReentrantLock lock = new ReentrantLock();
void transfer() {
    lock.lock();
    try { /* critical section */ }
    finally { lock.unlock(); }
}
```
`lock()` acquires or blocks/queues if held elsewhere; the `finally`
guarantees `unlock()` runs even if the critical section throws; a queued
thread is unparked once the lock releases and re-attempts acquisition.

## Visualization
```text
 Thread A: lock() -> [critical section] -> unlock()
 Thread B: lock() -> queued (CLH node) -> parked -> unparked -> acquires
```

## Real-World Usage
Libraries needing timed acquisition, cancellable locking, or multiple
independent conditions (e.g., a bounded buffer with "not full"/"not empty"
conditions) use `ReentrantLock` over `synchronized`.

## Advantages
- `tryLock`/timeouts avoid indefinite blocking under contention.
- Multiple `Condition` objects per lock, versus one implicit wait-set.

## Disadvantages
- Must remember to `unlock()` in `finally`; no compiler-enforced release.
- Misuse (forgetting unlock, wrong lock object) causes real production bugs.

## Trade-offs
`ReentrantLock` trades `synchronized`'s can't-forget-to-unlock safety for
explicit, finer-grained control; fairness trades throughput for reduced
thread starvation risk.

## Alternatives
`synchronized` remains simpler and sufficient for most cases.
`ReentrantReadWriteLock` allows concurrent readers with exclusive writers.
`StampedLock` adds an optimistic-read mode for even less reader overhead.

## Performance Analysis
Uncontended acquisition is a cheap CAS, comparable to `synchronized`'s
lightweight path; under contention, both eventually park threads, but
`ReentrantLock`'s explicit queue and optional fairness give more control.

## Complexity Analysis (if applicable)
Lock/unlock are O(1); queuing and unparking a waiting thread are O(1);
fairness adds a strict-order check costing a small constant overhead.

## Implementation
```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public final class RateLimiter {
    private final ReentrantLock lock = new ReentrantLock();
    public boolean tryProceed() {
        try {
            if (!lock.tryLock(50, TimeUnit.MILLISECONDS)) return false;
            try { return true; } finally { lock.unlock(); }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
```

## Common Mistakes
- Calling `unlock()` outside a `finally` block, leaking the lock if an
  exception is thrown.
- Using the default unfair lock when strict starvation avoidance is
  actually required, or fair locking where its throughput cost is not.

## Common Interview Questions
Q: What can ReentrantLock do that synchronized cannot?

Answer:
It supports non-blocking (`tryLock`) and timed acquisition, interruptible
waiting, configurable fairness, and multiple independent `Condition`
wait-sets per lock, none of which `synchronized` exposes.

Q: What is the difference between a fair and an unfair ReentrantLock?

Answer:
A fair lock grants access strictly in request order, avoiding starvation
at some throughput cost; the default unfair lock lets a newly arriving
thread race already-queued threads, usually yielding higher throughput.

Q: What is AbstractQueuedSynchronizer's role in ReentrantLock?

Answer:
AQS supplies the reusable machinery — an atomic state field and a queue of
waiting threads — that `ReentrantLock` (and `Semaphore`, `CountDownLatch`)
build their acquire/release semantics on top of.

## Follow-up Interview Questions
Q: Why must unlock() always be called in a finally block?

Answer:
Unlike `synchronized`, which releases its monitor automatically on any
exit path, `ReentrantLock` only releases when `unlock()` executes; without
`finally`, an exception would leave the lock held forever.

Q: When would you choose ReentrantReadWriteLock over ReentrantLock?

Answer:
When a shared resource is read far more often than written,
`ReentrantReadWriteLock` lets multiple readers proceed concurrently while
still giving writers exclusive access, improving throughput.

## How the Interviewer Thinks
This checks whether a candidate can justify choosing `ReentrantLock` over
`synchronized` for a concrete need (timeout, interruptibility, fairness,
multiple conditions) rather than treating it as a strictly better default.

## Production Engineering Perspective
Production code reserves `ReentrantLock` for cases genuinely needing its
extra capabilities, always pairs `lock()` with a `finally { unlock() }`,
and monitors lock-wait time under load to detect contention hot spots.

## Common Misconceptions
- "ReentrantLock is always faster than synchronized" — false; both are
  comparably cheap when uncontended, and neither is uniformly faster.
- "Fair locks are always the safer default" — false; fairness meaningfully
  reduces throughput and is only worth it when starvation is a real risk.

## Revision Notes
- Built on AQS: CAS-based state plus a CLH queue of waiting threads.
- Adds `tryLock`, timeouts, `lockInterruptibly`, fairness, multiple
  `Condition`s over `synchronized`.
- Always `lock()` then `try { ... } finally { unlock(); }`.

## Summary
`ReentrantLock` provides the same mutual-exclusion guarantee as
`synchronized` with explicit control over timeouts, interruptibility,
fairness, and multiple wait-conditions, at the cost of manual `unlock()`
discipline, built on the reusable `AbstractQueuedSynchronizer` framework.

---

# Java Memory Model

## What is it?
The Java Memory Model (JMM), defined in JLS Chapter 17, is the formal
specification of which writes by one thread are guaranteed visible to
reads by another, and which reorderings a compiler, JIT, or CPU may perform.

## Why do we need it?
Compilers and CPUs reorder and cache memory operations for performance;
without a contract, code correct on one thread could observe stale or
reordered values on another. The JMM specifies exactly what is guaranteed,
independent of hardware.

## What problem does it solve?
It solves the ambiguity of "what will thread B see after thread A writes
this?" by defining happens-before, a partial order guaranteeing visibility
only where the program establishes it explicitly.

## History (only if useful)
The original 1997 JLS memory model was found unsound (some "reasonable"
compiler optimizations violated programmer expectations); JSR 133 (2004,
alongside Java 5) replaced it with the current happens-before model.

## How does it work?
**Beginner:** without synchronization, one thread has no guarantee it will
ever see another thread's writes at all, not just "see them late."
**Intermediate:** the JMM defines happens-before edges — program order
within a thread, monitor unlock before a later lock on the same monitor,
a `volatile` write before a later read of that field, thread start before
its run, and a thread's actions before another observes its termination
via `join`.
**Advanced:** happens-before is transitive but not total — two actions with
no happens-before path between them are unordered and may be reordered
relative to each other, which is precisely what "data race" means formally.

## Internal Working
If two accesses to the same variable are not ordered by happens-before and
at least one is a write, that is a data race; the JMM does not define the
resulting behavior for racy programs beyond a few narrow exceptions.
`volatile` writes/reads establish happens-before directly without a lock;
`final` fields, once properly constructed, are guaranteed visible to any
thread that only ever sees the fully-constructed reference.
**Spec vs. implementation:** happens-before, the guarantees on `volatile`
and `final`, and the definition of a data race are JLS guarantees every
conforming JVM must uphold; the specific memory-barrier CPU instructions
HotSpot emits to implement those guarantees on a given architecture are
pure implementation detail.

## Step-by-Step Example
```java
class Flag {
    private volatile boolean ready = false;
    private int data;
    void writer() { data = 42; ready = true; }        // thread A
    void reader() { if (ready) System.out.println(data); } // thread B
}
```
`data = 42` happens-before `ready = true` by program order in thread A;
`ready = true` happens-before thread B's read since `ready` is `volatile`;
transitivity guarantees thread B, upon seeing `ready == true`, also sees
`data == 42`, never a stale value.

## Visualization
```text
 Thread A: data=42 --po--> ready=true(volatile write)
                                  |
                          happens-before (volatile)
                                  v
 Thread B:                ready read (volatile) --po--> print(data)
 Transitivity: data=42 happens-before print(data). No race.
```

## Real-World Usage
Every higher-level concurrency utility (`synchronized`, `ReentrantLock`,
`ConcurrentHashMap`, `ExecutorService`, `CompletableFuture`) is correct
specifically because its internals establish happens-before edges; the JMM
is why "submit a task, get a correct result back" needs no manual fences.

## Advantages
- Gives a hardware-independent, precise contract for concurrent correctness.
- Lets compilers and CPUs optimize aggressively where no ordering is required.

## Disadvantages
- Genuinely subtle; many correct-looking racy programs are undefined.
- Debugging visibility bugs is hard since they may not reproduce reliably.

## Trade-offs
Strong, always-synchronized memory ordering would be simple to reason about
but would forbid reordering optimizations essential to modern hardware and
compiler performance; happens-before is the deliberate middle ground.

## Alternatives
Sequential consistency (every thread sees all operations in one global
order) is simpler to reason about but matches no real hardware and would
forbid most useful optimizations. C++11's `std::memory_order` offers
similar happens-before concepts with finer-grained, more error-prone control.

## Performance Analysis
Establishing happens-before (via `volatile`, locks) has a real cost — memory
fences prevent certain reorderings and can force cache synchronization — so
correct code avoids over-synchronizing thread-confined data.

## Complexity Analysis (if applicable)
Not applicable in the algorithmic sense; the relevant "complexity" is
reasoning complexity — verifying a happens-before path exists between every
racy-looking pair of accesses across all threads.

## Implementation
```java
public final class SafePublication {
    private final int[] data;                 // final field
    public SafePublication(int[] source) {
        this.data = source.clone();           // safely constructed
    }
    public int[] data() { return data; }       // visible once published
    // Any thread that obtains a SafePublication reference after full
    // construction sees a fully-initialized `data` array, guaranteed
    // by the JMM's final-field semantics -- no lock required.
}
```

## Common Mistakes
- Reading and writing a shared non-volatile, non-locked variable from
  multiple threads and assuming updates "eventually" become visible.
- Publishing a partially-constructed object reference to another thread
  (e.g., via a non-final field or `this`-escape in a constructor).

## Common Interview Questions
Q: What is happens-before and why does it matter?

Answer:
It is the JMM's partial ordering guaranteeing that if action A
happens-before action B, A's effects are visible to B. It matters because,
absent such an edge, another thread has no guarantee of ever observing a
write at all.

Q: What guarantees does volatile provide?

Answer:
A `volatile` write happens-before every subsequent `volatile` read of the
same field, guaranteeing visibility and preventing reordering across it; it
does not make compound operations like `count++` atomic.

Q: What is a data race, formally, under the JMM?

Answer:
Two accesses to the same variable, from different threads, with no
happens-before ordering between them, where at least one is a write; the
JMM leaves the behavior of such a program largely undefined.

## Follow-up Interview Questions
Q: Why can a correct-looking program without synchronization still misbehave?

Answer:
Without a happens-before edge, the compiler, JIT, and CPU are free to
reorder or cache operations for performance, so a thread may see a stale
value or, for unsafe publication, even a partially-constructed object.

Q: How does the JMM relate to a CPU's cache-coherence protocol such as MESI?

Answer:
MESI keeps CPU caches coherent at the hardware level but does not itself
define program-level ordering guarantees; the JMM sits above that, and
HotSpot emits memory-fence instructions the CPU honors to implement the
JMM's happens-before guarantees on top of that coherence protocol.

## How the Interviewer Thinks
This chapter is often the real depth check of a Java concurrency
interview: it tests whether a candidate reasons from happens-before rather
than vague intuitions like "volatile makes things thread-safe," and whether
they connect this model back to why `synchronized` and `ReentrantLock`
are actually correct.

## Production Engineering Perspective
Production teams rely on well-tested concurrency utilities specifically so
individual engineers do not need to reason about raw happens-before edges;
when a custom lock-free structure is genuinely required, it is reviewed
with the JMM's guarantees explicitly in mind and stress-tested.

## Common Misconceptions
- "volatile makes an operation atomic" — false; it guarantees visibility
  and ordering only, not atomicity of compound read-modify-write operations.
- "If a race 'usually works' in testing, it is safe" — false; the JMM
  permits behavior that may only appear under specific optimizations,
  hardware, or load.

## Revision Notes
- Happens-before is the JMM's core guarantee: program order, monitor
  unlock-then-lock, volatile write-then-read, thread start/join.
- Data race = unordered concurrent access to the same variable with a
  write involved; behavior is largely undefined.
- `volatile`: visibility and ordering, not atomicity. `final`: safe
  publication without synchronization once construction completes.
- Every concurrency utility in this part is correct because it establishes
  happens-before edges internally.

## Summary
The Java Memory Model formally defines which cross-thread memory effects
are guaranteed visible and in what order, via the happens-before relation,
giving compilers and hardware room to optimize while still letting
engineers reason precisely about concurrent correctness — it is the
foundational contract every other concurrency primitive in this part is
built on top of.
