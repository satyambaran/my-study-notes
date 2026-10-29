# Java Collections Framework — Quick Reference Cheat Sheet

## Structure
Each section follows: collection name → internal data structure → when to use → commented method signatures grouped logically.
Variants are compared with key distinctions. Thread-safe alternatives noted inline.

## Purpose
Rapid revision reference for Java Collections Framework — List, Set, Queue, Deque, Map, and all major implementations. Assumes working Java knowledge. Covers internals, complexity, and interview-relevant details.

---

## 1. Collection Hierarchy — Mental Map

```
Iterable<T>
 └── Collection<T>
      ├── List<T>           — ordered, indexed, duplicates allowed
      │    ├── ArrayList
      │    ├── LinkedList    (also Deque)
      │    ├── Vector        (legacy synchronized)
      │    │    └── Stack    (legacy, use Deque)
      │    └── CopyOnWriteArrayList (concurrent)
      │
      ├── Set<T>            — no duplicates
      │    ├── HashSet
      │    ├── LinkedHashSet — insertion order
      │    ├── TreeSet       — sorted (NavigableSet)
      │    ├── EnumSet       — bitfield for enums
      │    └── CopyOnWriteArraySet (concurrent)
      │
      └── Queue<T>          — FIFO (usually)
           ├── PriorityQueue — min-heap
           ├── ArrayDeque    — resizable circular array (also Deque)
           ├── LinkedList    (also List, Deque)
           └── BlockingQueue (concurrent — see concurrency sheet)
                ├── ArrayBlockingQueue
                ├── LinkedBlockingQueue
                ├── PriorityBlockingQueue
                ├── SynchronousQueue
                └── DelayQueue

Map<K,V>                    — NOT a Collection, but part of the framework
 ├── HashMap
 ├── LinkedHashMap           — insertion/access order
 ├── TreeMap                 — sorted (NavigableMap)
 ├── EnumMap                 — enum keys, array-backed
 ├── WeakHashMap             — weak-ref keys (GC-friendly cache)
 ├── IdentityHashMap         — reference equality (==) not equals()
 ├── Hashtable               (legacy synchronized)
 └── ConcurrentHashMap       (concurrent)
```

---

## 2. Common Collection<T> Methods (inherited by List, Set, Queue)

```java
// --- Size & emptiness ---
// int size(); boolean isEmpty();

// --- Contains ---
// boolean contains(Object o);           // uses equals()
// boolean containsAll(Collection<?> c);

// --- Add ---
// boolean add(E e);                     // true if collection changed
// boolean addAll(Collection<? extends E> c);

// --- Remove ---
// boolean remove(Object o);             // removes first occurrence (List), the element (Set)
// boolean removeAll(Collection<?> c);   // remove all elements in c
// boolean retainAll(Collection<?> c);   // keep only elements in c (set intersection)
// void clear();
// boolean removeIf(Predicate<? super E> filter); // Java 8+

// --- Conversion ---
// Object[] toArray();
// T[] toArray(T[] a);                   // e.g., list.toArray(new String[0])
// T[] toArray(IntFunction<T[]> gen);    // Java 11+ e.g., list.toArray(String[]::new)

// --- Iteration ---
// Iterator<E> iterator();
// Spliterator<E> spliterator();
// void forEach(Consumer<? super E> action); // Java 8+

// --- Stream ---
// Stream<E> stream();
// Stream<E> parallelStream();
```

---

## 3. List<T> — Ordered, Indexed, Duplicates Allowed

### Common List Methods (beyond Collection)
```java
// --- Positional access ---
// E get(int index);
// E set(int index, E element);          // returns old element
// void add(int index, E element);       // shifts right
// E remove(int index);                  // shifts left, returns removed
// boolean addAll(int index, Collection<? extends E> c);

// --- Search ---
// int indexOf(Object o);               // first occurrence, -1 if absent
// int lastIndexOf(Object o);

// --- Range view ---
// List<E> subList(int fromInc, int toExc); // backed view — changes reflect in original
//   ⚠ Structural modification of original after subList → ConcurrentModificationException

// --- Sort ---
// void sort(Comparator<? super E> c);  // Java 8+ (TimSort, stable, O(n log n))
// Collections.sort(list);              // natural order
// Collections.sort(list, comparator);

// --- Factory methods (Java 9+, immutable) ---
// List.of();                            // empty
// List.of(e1); List.of(e1, e2, ...);   // up to 10 overloads, then varargs
// List.copyOf(collection);             // Java 10+
//   ⚠ All return UNMODIFIABLE lists — add/set/remove throw UnsupportedOperationException
//   ⚠ No nulls allowed — throws NPE

// --- Other immutable wrappers ---
// Collections.unmodifiableList(list);   // unmodifiable VIEW of mutable list
// Collections.singletonList(e);         // immutable single-element
// Collections.emptyList();              // immutable empty
```

---

### 3a. ArrayList<T>
**Internal:** Resizable array (`Object[] elementData`)
**Use:** Default list. Random access, iteration, append-heavy workloads.
> Initial capacity = 10 (on first add, not constructor). Grows by **50%** (`newCapacity = oldCapacity + (oldCapacity >> 1)`). `System.arraycopy` for shifts. Not synchronized — use `Collections.synchronizedList()` or `CopyOnWriteArrayList` for concurrency.

```java
// new ArrayList<>();                    // default capacity (lazy init, actually 0 until first add → 10)
// new ArrayList<>(initialCapacity);     // pre-size to avoid resizing
// new ArrayList<>(collection);          // copy from another collection

// ensureCapacity(minCapacity);          // pre-allocate if you know size
// trimToSize();                         // shrink backing array to current size (save memory)
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `get(i)` / `set(i, e)` | O(1) | Direct array index |
| `add(e)` (append) | **Amortized O(1)** | Occasional O(n) resize + copy |
| `add(i, e)` (insert) | O(n) | Shifts elements right |
| `remove(i)` | O(n) | Shifts elements left |
| `contains(o)` / `indexOf(o)` | O(n) | Linear scan |
| `size()` / `isEmpty()` | O(1) | |

**Interview notes:**
```
Q: Why ArrayList over LinkedList almost always?
A: CPU cache locality — contiguous memory. LinkedList has pointer chasing (cache misses) + 
   ~40 bytes overhead per node (prev + next + item + object header). Even O(n) shifts in 
   ArrayList are fast due to System.arraycopy (native memcpy).

Q: ArrayList vs Array?
A: ArrayList = dynamic size, generic, autoboxing for primitives (overhead).
   Array = fixed size, supports primitives directly, faster for primitives.

Q: How does remove(int) vs remove(Object) work?
A: remove(5) removes index 5. remove(Integer.valueOf(5)) removes the element 5.
   Autoboxing trap: list.remove(5) on List<Integer> removes by INDEX, not value.
```

---

### 3b. LinkedList<T>
**Internal:** Doubly-linked list (`Node { E item; Node prev; Node next; }`)
**Use:** Frequent insert/remove at head/tail, use as Deque. **Rarely better than ArrayList.**
> Implements both `List<E>` and `Deque<E>`. Each node is a separate heap object — cache-unfriendly. No random access — `get(i)` traverses from head or tail (whichever is closer). Good for: queue/deque usage, iterator-based removal (O(1) unlink after finding node).

```java
// new LinkedList<>();
// new LinkedList<>(collection);

// --- Deque methods (head/tail O(1)) ---
// addFirst(e); addLast(e); offerFirst(e); offerLast(e);
// removeFirst(); removeLast(); pollFirst(); pollLast();
// getFirst(); getLast(); peekFirst(); peekLast();
// push(e); pop(); // stack behavior (head)

// --- List + Deque ---
// descendingIterator(); // reverse order iteration
// listIterator(index); // bidirectional iterator from position
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `addFirst(e)` / `addLast(e)` | O(1) | Direct pointer manipulation |
| `removeFirst()` / `removeLast()` | O(1) | |
| `get(i)` / `set(i, e)` | **O(n)** | Traverse from nearest end |
| `add(i, e)` / `remove(i)` | **O(n)** | O(n) to find + O(1) to unlink |
| `contains(o)` | O(n) | Linear scan |
| `size()` | O(1) | Cached |

---

### 3c. Vector<T> (Legacy)
**Internal:** Resizable array (like ArrayList)
**Use:** **Don't.** Legacy — every method is `synchronized`. Use ArrayList + external sync if needed.
> Grows by **100%** (doubles) vs ArrayList's 50%. Thread-safe but coarse-grained — single lock for all ops.

```java
// All ArrayList methods + synchronized
// Stack<E> extends Vector<E> — legacy, use ArrayDeque instead
```

---

## 4. Set<T> — No Duplicates

### Common Set Notes
```
- add(e) returns false if already present (checked via equals() + hashCode())
- No positional access (no get(i)) — unordered by default
- Iteration order depends on implementation
```

### Factory methods (Java 9+, immutable)
```java
// Set.of();                             // empty
// Set.of(e1, e2, e3);                  // no duplicates allowed — throws IllegalArgumentException
// Set.copyOf(collection);              // Java 10+, deduplicates
//   ⚠ Unmodifiable, no nulls
```

---

### 4a. HashSet<T>
**Internal:** Backed by `HashMap<E, PRESENT>` where PRESENT is a dummy static Object
**Use:** Default set. Fast add/remove/contains. No ordering.
> Same hashing as HashMap — `hashCode()` → spread → bucket index. Rehashes when `size > capacity * loadFactor` (default 0.75). Initial capacity 16.

```java
// new HashSet<>();
// new HashSet<>(initialCapacity);
// new HashSet<>(initialCapacity, loadFactor);
// new HashSet<>(collection);
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `add(e)` / `remove(o)` / `contains(o)` | **O(1)** average | O(n) worst case (hash collision) |
| Iteration | O(capacity + size) | Traverses all buckets |

---

### 4b. LinkedHashSet<T>
**Internal:** `LinkedHashMap` — hash table + doubly-linked list threading all entries
**Use:** Set with **insertion-order** iteration. Slightly slower than HashSet.
> Each entry has `before`/`after` pointers maintaining a doubly-linked list across all entries. Iteration is O(size) not O(capacity) — better than HashSet for sparse tables.

```java
// new LinkedHashSet<>();
// new LinkedHashSet<>(initialCapacity);
// new LinkedHashSet<>(initialCapacity, loadFactor);
// new LinkedHashSet<>(collection);

// Iteration order = insertion order (first add wins; re-add doesn't change position)
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `add` / `remove` / `contains` | **O(1)** average | Same as HashSet + constant overhead for linked list maintenance |
| Iteration | **O(size)** | Better than HashSet's O(capacity) |

---

### 4c. TreeSet<T>
**Internal:** Backed by `TreeMap<E, PRESENT>` — Red-Black tree (self-balancing BST)
**Use:** Sorted set, range queries, floor/ceiling ops.
> Elements must be `Comparable` OR provide `Comparator` at construction. Maintains elements in **sorted order**. Implements `NavigableSet` → rich navigation API.

```java
// new TreeSet<>();                      // natural ordering (Comparable)
// new TreeSet<>(comparator);           // custom ordering
// new TreeSet<>(collection);           // sorts on construction
// new TreeSet<>(sortedSet);            // preserves comparator

// --- NavigableSet methods ---
// E first(); E last();                 // min/max
// E lower(e);                          // greatest element strictly less than e (null if none)
// E floor(e);                          // greatest element ≤ e
// E ceiling(e);                        // smallest element ≥ e
// E higher(e);                         // smallest element strictly greater than e
// E pollFirst(); E pollLast();         // remove and return min/max

// --- Range views (backed views — changes reflect) ---
// SortedSet<E> headSet(toExc);                  // elements < to
// SortedSet<E> tailSet(fromInc);                // elements ≥ from
// SortedSet<E> subSet(fromInc, toExc);          // [from, to)
// NavigableSet<E> headSet(to, inclusive);        // flexible bounds
// NavigableSet<E> tailSet(from, inclusive);
// NavigableSet<E> subSet(from, fromInc, to, toInc);

// NavigableSet<E> descendingSet();              // reverse order view
// Iterator<E> descendingIterator();
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `add` / `remove` / `contains` | **O(log n)** | Red-Black tree |
| `first()` / `last()` | O(log n) | Leftmost / rightmost node |
| `floor` / `ceiling` / `lower` / `higher` | O(log n) | |
| Iteration (in order) | O(n) | In-order traversal |

---

### 4d. EnumSet<E extends Enum<E>>
**Internal:** Bit vector (single `long` for ≤64 constants, `long[]` for more)
**Use:** Set of enum constants. Fastest possible Set — everything is bit manipulation.
> No constructors — use factory methods. Internally uses `RegularEnumSet` (≤64 enum values, single long) or `JumboEnumSet` (>64 values, long array). `allOf()` is a single bit mask. Iteration is in enum declaration order.

```java
// EnumSet.allOf(DayOfWeek.class);      // all enum constants
// EnumSet.noneOf(DayOfWeek.class);     // empty set of that enum type
// EnumSet.of(MON, TUE, WED);          // specific constants
// EnumSet.range(MON, FRI);            // MON through FRI inclusive
// EnumSet.complementOf(set);          // all NOT in set
// EnumSet.copyOf(collection);
```

| Operation | Time Complexity | Notes |
|---|---|---|
| All operations | **O(1)** | Bit manipulation on long(s) |
| Iteration | O(n) | Number of enum constants |

**Interview note:** Always prefer `EnumSet` over `HashSet<MyEnum>` — dramatically faster and uses less memory.

---

## 5. Queue<T> — FIFO (typically)

### Queue Interface Methods
```java
// --- Two method groups: throws exception vs returns special value ---
//
//              Throws          Returns null/false
// Insert:     add(e)          offer(e)
// Remove:     remove()        poll()
// Examine:    element()       peek()
//
// add/remove/element throw if capacity full / empty
// offer/poll/peek return false/null instead
```

---

### 5a. PriorityQueue<T>
**Internal:** Binary min-heap (array-based: `Object[] queue`)
**Use:** Process elements by priority (scheduling, Dijkstra, top-K problems)
> Not FIFO — dequeues **smallest** element (natural order or Comparator). Array-backed heap: parent at `i`, children at `2i+1`, `2i+2`. Grows by 50% if < 64 capacity, else by 100%. **Not thread-safe** — use `PriorityBlockingQueue` for concurrency.

```java
// new PriorityQueue<>();                // natural ordering (min-heap)
// new PriorityQueue<>(initialCapacity);
// new PriorityQueue<>(comparator);      // custom ordering
// new PriorityQueue<>(initialCapacity, comparator);
// new PriorityQueue<>(collection);      // heapify in O(n)

// --- Max-heap trick ---
// new PriorityQueue<>(Comparator.reverseOrder());
// new PriorityQueue<>((a, b) -> b - a);  // ⚠ overflow risk with large ints, prefer compareTo

// All Queue methods: offer, poll, peek, add, remove, element
// No efficient contains(o) or remove(o) — both O(n), linear scan
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `offer(e)` / `add(e)` | **O(log n)** | Sift up |
| `poll()` / `remove()` | **O(log n)** | Sift down |
| `peek()` | **O(1)** | Just return queue[0] |
| `remove(Object)` | **O(n)** | Linear scan + sift |
| `contains(o)` | **O(n)** | Linear scan |

**Interview patterns:**
```
Top K largest:  min-heap of size K → poll when size > K → heap has K largest
Top K smallest: max-heap of size K → poll when size > K → heap has K smallest
Merge K sorted lists: min-heap of K list heads → poll min, add its next
Running median: max-heap (lower half) + min-heap (upper half), balance sizes
```

---

### 5b. ArrayDeque<T>
**Internal:** Resizable circular array (`Object[] elements`, head/tail indices)
**Use:** **Default choice for Stack AND Queue.** Faster than LinkedList for both.
> Circular buffer: `head` and `tail` wrap around using bitmask (capacity is always power of 2). No null elements allowed. Doubles capacity when full. No random access (not a List). **Preferred over Stack class and LinkedList for stack/queue usage.**

```java
// new ArrayDeque<>();                   // default capacity 16
// new ArrayDeque<>(numElements);        // initial capacity (rounded up to power of 2)
// new ArrayDeque<>(collection);

// --- As Stack (LIFO) — use these instead of Stack class ---
// push(e);                              // addFirst(e) — O(1)
// pop();                                // removeFirst() — O(1)
// peek();                               // peekFirst() — O(1)

// --- As Queue (FIFO) ---
// offer(e);                             // offerLast(e) — O(1)
// poll();                               // pollFirst() — O(1)
// peek();                               // peekFirst() — O(1)

// --- Full Deque API ---
// addFirst(e); addLast(e); offerFirst(e); offerLast(e);
// removeFirst(); removeLast(); pollFirst(); pollLast();
// getFirst(); getLast(); peekFirst(); peekLast();

// --- Bulk ---
// removeFirstOccurrence(o); removeLastOccurrence(o); // O(n) scan
// descendingIterator();
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `push` / `pop` / `peek` | **O(1)** | Amortized, circular array |
| `offerFirst` / `offerLast` | **O(1)** amortized | Occasional O(n) resize |
| `pollFirst` / `pollLast` | **O(1)** | |
| `contains(o)` / `remove(o)` | O(n) | Linear scan |
| `size()` | O(1) | |

**Interview note:**
```
Q: Why ArrayDeque over Stack?
A: Stack extends Vector (synchronized = slow). ArrayDeque is unsynchronized + circular array = fast.

Q: Why ArrayDeque over LinkedList for queue?
A: Cache locality (contiguous array) + no per-node object overhead (40 bytes/node in LinkedList).
   ArrayDeque is faster for both stack and queue operations in benchmarks.

Q: Can ArrayDeque hold null?
A: No — null is used as sentinel for empty slots. LinkedList can hold nulls.
```

---

## 6. Deque<T> — Double-Ended Queue

```java
// Deque extends Queue — supports both ends
//
// --- Method summary (Deque-specific) ---
//              First Element (Head)                    Last Element (Tail)
//              Throws       Returns null       Throws       Returns null
// Insert:     addFirst(e)  offerFirst(e)      addLast(e)   offerLast(e)
// Remove:     removeFirst() pollFirst()       removeLast()  pollLast()
// Examine:    getFirst()    peekFirst()       getLast()     peekLast()
//
// --- As Stack ---
// push(e)  ↔ addFirst(e)
// pop()    ↔ removeFirst()
// peek()   ↔ peekFirst()
//
// --- As Queue ---
// offer(e) ↔ offerLast(e)
// poll()   ↔ pollFirst()
// peek()   ↔ peekFirst()
//
// Implementations: ArrayDeque (default), LinkedList, ConcurrentLinkedDeque
```

---

## 7. Map<K, V> — Key-Value Pairs

### Common Map Methods
```java
// --- Basic ops ---
// V put(K key, V value);               // returns previous value (null if new key)
// V get(Object key);                   // null if absent
// V getOrDefault(Object key, V defaultValue); // Java 8+
// V remove(Object key);               // returns removed value
// boolean remove(Object key, Object value); // remove only if mapped to value
// boolean containsKey(Object key);
// boolean containsValue(Object value); // O(n) — scans all values
// int size(); boolean isEmpty();
// void clear();

// --- Bulk ---
// void putAll(Map<? extends K, ? extends V> m);

// --- Views ---
// Set<K> keySet();                     // backed view — removal reflects in map
// Collection<V> values();              // backed view
// Set<Map.Entry<K,V>> entrySet();     // backed view — most efficient iteration

// --- Java 8+ Compute methods ---
// V putIfAbsent(K key, V value);       // put only if key absent or mapped to null
// V replace(K key, V value);           // replace only if key present
// boolean replace(K key, V oldValue, V newValue); // CAS-style
// V compute(K key, BiFunction<K, V, V> remappingFn);     // always runs fn
// V computeIfAbsent(K key, Function<K, V> mappingFn);    // runs fn only if absent
// V computeIfPresent(K key, BiFunction<K, V, V> remappingFn);
// V merge(K key, V value, BiFunction<V, V, V> remappingFn);
//   // if absent → put(key, value); if present → put(key, fn(oldVal, value))
//   // if fn returns null → remove key

// --- Iteration ---
// void forEach(BiConsumer<K, V> action); // Java 8+
// map.entrySet().forEach(e -> use(e.getKey(), e.getValue()));

// --- Factory methods (Java 9+, immutable) ---
// Map.of();                             // empty
// Map.of(k1, v1, k2, v2, ...);        // up to 10 key-value pairs
// Map.ofEntries(Map.entry(k1,v1), Map.entry(k2,v2), ...); // any number
// Map.copyOf(map);                     // Java 10+
//   ⚠ Unmodifiable, no null keys or values

// --- Utility ---
// Collections.unmodifiableMap(map);    // unmodifiable view
// Collections.synchronizedMap(map);    // synchronized wrapper
// Collections.singletonMap(k, v);     // immutable single entry
// Collections.emptyMap();
```

---

### 7a. HashMap<K, V>
**Internal:** Array of buckets (`Node<K,V>[] table`). Each bucket is a linked list → tree (Red-Black) when chain length ≥ 8 (TREEIFY_THRESHOLD), untreeifies when ≤ 6.
**Use:** Default map. Fast O(1) average get/put. No ordering.
> Default capacity = 16, load factor = 0.75. Rehash doubles capacity when `size > capacity * loadFactor`. Hash spreading: `(h = key.hashCode()) ^ (h >>> 16)` — mixes high bits into low bits. Bucket index = `hash & (capacity - 1)` (bitwise AND since capacity is power of 2). **Not thread-safe.**

```java
// new HashMap<>();
// new HashMap<>(initialCapacity);
// new HashMap<>(initialCapacity, loadFactor);
// new HashMap<>(map);                   // copy constructor
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `get(k)` / `put(k,v)` / `remove(k)` | **O(1)** average | O(log n) in treeified bucket, O(n) worst (bad hash) |
| `containsKey(k)` | O(1) average | |
| `containsValue(v)` | **O(n)** | Full scan of all values |
| Iteration | O(capacity + size) | Traverses all buckets |

**Interview deep-dive:**
```
Q: What happens on hash collision?
A: Chaining — nodes added to linked list in bucket. Java 8+: if chain ≥ 8 AND table size ≥ 64, 
   bucket converts to Red-Black tree (O(log n) lookup). Untreeifies at ≤ 6.

Q: Why capacity is power of 2?
A: So bucket index = hash & (capacity - 1) — bitwise AND is faster than modulo.

Q: What if key is mutable and hashCode changes after put?
A: Entry is in wrong bucket — get() won't find it. NEVER use mutable keys.

Q: null key handling?
A: HashMap allows ONE null key — always stored in bucket 0.
   TreeMap does NOT allow null keys (Comparator can't compare null).

Q: HashMap vs Hashtable?
A: HashMap: unsynchronized, allows null key/value, fail-fast iterator.
   Hashtable: synchronized (slow), no nulls, legacy. Use ConcurrentHashMap instead.

Q: What is rehashing?
A: When size exceeds threshold (capacity * loadFactor), table doubles.
   All entries re-bucketed: new index = old index OR old index + old capacity.
```

---

### 7b. LinkedHashMap<K, V>
**Internal:** HashMap + doubly-linked list threading all entries (before/after pointers on each node)
**Use:** Insertion-order iteration. Access-order mode for **LRU cache**.
> Extends HashMap — same hash table performance. Additionally maintains a linked list in insertion order (default) or access order. Access-order mode: every `get()`/`put()` moves entry to tail — override `removeEldestEntry()` to auto-evict oldest = LRU cache.

```java
// new LinkedHashMap<>();                // insertion order
// new LinkedHashMap<>(capacity);
// new LinkedHashMap<>(capacity, loadFactor, accessOrder);
//   accessOrder = true → access-order (LRU); false → insertion-order (default)

// --- LRU Cache pattern ---
// class LRUCache<K,V> extends LinkedHashMap<K,V> {
//     private final int maxSize;
//     LRUCache(int maxSize) {
//         super(maxSize, 0.75f, true);   // accessOrder = true
//         this.maxSize = maxSize;
//     }
//     @Override
//     protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
//         return size() > maxSize;        // auto-evict when over capacity
//     }
// }
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `get` / `put` / `remove` | **O(1)** average | Same as HashMap + constant linked list overhead |
| Iteration | **O(size)** | Better than HashMap's O(capacity + size) |

---

### 7c. TreeMap<K, V>
**Internal:** Red-Black tree (self-balancing BST)
**Use:** Sorted map, range queries, floor/ceiling/subMap.
> Keys must be `Comparable` or provide `Comparator`. Implements `NavigableMap`. No null keys (can't compare). Guarantees O(log n) for all single-key ops.

```java
// new TreeMap<>();                      // natural key ordering
// new TreeMap<>(comparator);           // custom ordering
// new TreeMap<>(map);                  // copy + sort
// new TreeMap<>(sortedMap);            // preserves comparator

// --- NavigableMap methods ---
// K firstKey(); K lastKey();           // min/max key
// Map.Entry<K,V> firstEntry(); lastEntry();
// Map.Entry<K,V> pollFirstEntry(); pollLastEntry(); // remove + return
// Map.Entry<K,V> lowerEntry(k);       // greatest key strictly < k
// Map.Entry<K,V> floorEntry(k);       // greatest key ≤ k
// Map.Entry<K,V> ceilingEntry(k);     // smallest key ≥ k
// Map.Entry<K,V> higherEntry(k);      // smallest key strictly > k
// K lowerKey(k); K floorKey(k); K ceilingKey(k); K higherKey(k);

// --- Range views (backed, changes reflect) ---
// SortedMap<K,V> headMap(toKeyExc);
// SortedMap<K,V> tailMap(fromKeyInc);
// SortedMap<K,V> subMap(fromKeyInc, toKeyExc);
// NavigableMap<K,V> headMap(toKey, inclusive);
// NavigableMap<K,V> tailMap(fromKey, inclusive);
// NavigableMap<K,V> subMap(fromKey, fromInc, toKey, toInc);

// NavigableMap<K,V> descendingMap();   // reverse order view
// NavigableSet<K> navigableKeySet();
// NavigableSet<K> descendingKeySet();
```

| Operation | Time Complexity | Notes |
|---|---|---|
| `get` / `put` / `remove` / `containsKey` | **O(log n)** | Red-Black tree |
| `firstKey` / `lastKey` | O(log n) | Leftmost / rightmost |
| `floor` / `ceiling` / `lower` / `higher` | O(log n) | |
| Iteration (sorted) | O(n) | In-order traversal |

---

### 7d. EnumMap<K extends Enum<K>, V>
**Internal:** Simple array indexed by `enum.ordinal()`. `Object[] vals` of size = number of enum constants.
**Use:** Map with enum keys. Fastest map — array indexing, no hashing.
> Maintains enum declaration order during iteration. Very compact — no Entry objects, just a flat array. All ops are constant time. Null values allowed, null keys NOT.

```java
// new EnumMap<>(DayOfWeek.class);      // must pass key class
// new EnumMap<>(existingEnumMap);
// new EnumMap<>(regularMap);           // copy from regular map (must have ≥1 entry for type inference)
```

| Operation | Time Complexity |
|---|---|
| All single-key ops | **O(1)** |
| Iteration | O(enum size) |

---

### 7e. WeakHashMap<K, V>
**Internal:** Like HashMap but keys are `WeakReference`. When key is GC'd, entry is auto-removed.
**Use:** Caches where entries should be GC'd when key is no longer referenced elsewhere.
> Uses a `ReferenceQueue` — GC enqueues cleared WeakReferences. On each map operation, stale entries are expunged. **Not thread-safe.** Null keys and values allowed.

```java
// new WeakHashMap<>();
// new WeakHashMap<>(initialCapacity);
// Entries vanish when key is garbage collected (no strong reference elsewhere)
```

---

### 7f. IdentityHashMap<K, V>
**Internal:** Linear-probing hash table using `System.identityHashCode()` and `==` instead of `equals()`
**Use:** Reference-equality semantics (serialization graphs, object canonicalization).
> Violates `Map` contract — uses `==` not `equals()`. Two keys that are `equals()` but not `==` are treated as different. Uses a flat `Object[]` alternating key-value (`[k0, v0, k1, v1, ...]`) — cache-friendly.

```java
// new IdentityHashMap<>();
// new IdentityHashMap<>(expectedMaxSize);
// Uses == for key comparison, NOT equals()
```

---

## 8. Specialized Utilities

### 8a. Collections Utility Class
```java
// --- Sorting ---
// Collections.sort(list);              // natural order
// Collections.sort(list, comparator);  // custom order
// Collections.reverse(list);
// Collections.shuffle(list);
// Collections.swap(list, i, j);
// Collections.rotate(list, distance);  // circular rotation

// --- Searching ---
// Collections.binarySearch(list, key); // list must be sorted; returns index or -(insertionPoint)-1
// Collections.binarySearch(list, key, comparator);
// Collections.frequency(collection, obj); // count occurrences
// Collections.disjoint(c1, c2);       // true if no common elements

// --- Min/Max ---
// Collections.min(collection);
// Collections.max(collection);
// Collections.min(collection, comparator);
// Collections.max(collection, comparator);

// --- Filling ---
// Collections.fill(list, obj);        // replace all elements
// Collections.nCopies(n, obj);        // immutable list of n copies (all same ref)
// Collections.replaceAll(list, oldVal, newVal);

// --- Wrappers ---
// Collections.unmodifiableList/Set/Map(x);    // read-only view (throws on mutation)
// Collections.synchronizedList/Set/Map(x);    // synchronized wrapper (mutex on every op)
// Collections.checkedList/Set/Map(x, type);   // runtime type-checked (catches raw-type pollution)

// --- Singleton/Empty ---
// Collections.singletonList(e); singleton(e); singletonMap(k,v); // immutable 1-element
// Collections.emptyList(); emptySet(); emptyMap();               // immutable empty

// --- Thread-safe alternatives (preferred over synchronized wrappers) ---
// ConcurrentHashMap, CopyOnWriteArrayList, CopyOnWriteArraySet,
// ConcurrentLinkedQueue, ConcurrentLinkedDeque, ConcurrentSkipListMap/Set
```

---

### 8b. Arrays Utility Class
```java
// --- Sorting ---
// Arrays.sort(arr);                    // primitive: dual-pivot quicksort; Object: TimSort
// Arrays.sort(arr, fromInc, toExc);
// Arrays.sort(arr, comparator);        // Object arrays only
// Arrays.parallelSort(arr);            // parallel merge sort (large arrays, Java 8+)

// --- Searching ---
// Arrays.binarySearch(arr, key);       // must be sorted
// Arrays.binarySearch(arr, fromInc, toExc, key);

// --- Comparison ---
// Arrays.equals(a, b);                // element-wise equals (one dimension)
// Arrays.deepEquals(a, b);            // nested array comparison
// Arrays.compare(a, b);              // lexicographic, Java 9+
// Arrays.mismatch(a, b);             // first differing index, Java 9+

// --- Fill/Copy ---
// Arrays.fill(arr, val);
// Arrays.fill(arr, fromInc, toExc, val);
// Arrays.copyOf(arr, newLength);       // truncates or zero-pads
// Arrays.copyOfRange(arr, fromInc, toExc);

// --- Conversion ---
// Arrays.asList(arr);                 // fixed-size list backed by array
//   ⚠ asList(int[]) returns List<int[]> (one element) — use IntStream.of(arr).boxed() for List<Integer>
//   ⚠ Returned list is fixed-size — add/remove throws, but set() works (modifies original array)
// Arrays.stream(arr);                 // Stream or IntStream/LongStream/DoubleStream
// Arrays.stream(arr, fromInc, toExc);

// --- String ---
// Arrays.toString(arr);               // "[1, 2, 3]"
// Arrays.deepToString(arr);           // nested: "[[1, 2], [3, 4]]"

// --- Parallel ---
// Arrays.parallelPrefix(arr, op);     // cumulative reduction (prefix sum etc.)
// Arrays.parallelSetAll(arr, generator); // e.g., i -> i * 2
// Arrays.setAll(arr, generator);      // sequential version

// --- Hash ---
// Arrays.hashCode(arr);
// Arrays.deepHashCode(arr);
```

---

## 9. Iteration Patterns & Fail-Fast vs Fail-Safe

```java
// --- Fail-fast (most standard collections) ---
// Modification during iteration → ConcurrentModificationException
// Backed by modCount field — incremented on structural changes
// list.forEach(e -> list.remove(e));  // ✗ throws CME
// for (E e : list) { list.remove(e); } // ✗ throws CME
// Iterator<E> it = list.iterator();
// while (it.hasNext()) { E e = it.next(); it.remove(); } // ✓ safe removal via iterator

// --- Fail-safe (concurrent collections, copy-on-write) ---
// ConcurrentHashMap, CopyOnWriteArrayList → no CME
// Iterate over snapshot or segment — may not reflect concurrent modifications
// Weakly consistent — guaranteed not to throw CME

// --- Safe removal patterns ---
// 1. iterator.remove()                  — standard
// 2. collection.removeIf(predicate)     — Java 8+, cleanest
// 3. CopyOnWriteArrayList.remove()      — safe during iteration (operates on snapshot)
// 4. ConcurrentHashMap.remove()         — weakly consistent, no CME
```

---

## 10. Quick Comparison Tables

### List Implementations

| Feature | ArrayList | LinkedList | Vector | CopyOnWriteArrayList |
|---|---|---|---|---|
| Internal | Dynamic array | Doubly-linked list | Dynamic array (synced) | Copy-on-write array |
| Random access | **O(1)** | O(n) | O(1) | O(1) |
| Insert/remove (middle) | O(n) | O(1)* (at iterator) | O(n) | O(n) + full copy |
| Insert/remove (ends) | O(1) amort | **O(1)** | O(1) amort | O(n) copy |
| Thread-safe | No | No | Yes (coarse) | Yes (read-optimized) |
| Null elements | Yes | Yes | Yes | Yes |
| Use case | General purpose | Deque, frequent head/tail ops | Legacy | Read-heavy concurrent |

### Set Implementations

| Feature | HashSet | LinkedHashSet | TreeSet | EnumSet |
|---|---|---|---|---|
| Internal | HashMap | LinkedHashMap | Red-Black tree | Bit vector |
| Order | None | **Insertion** | **Sorted** | Enum declaration |
| add/remove/contains | **O(1)** | O(1) | O(log n) | O(1) |
| Null | One null | One null | **No** | **No** |
| Use case | General purpose | Ordered iteration | Sorted, range ops | Enum flags |

### Map Implementations

| Feature | HashMap | LinkedHashMap | TreeMap | EnumMap | ConcurrentHashMap |
|---|---|---|---|---|---|
| Internal | Hash table | Hash + linked list | Red-Black tree | Array | Segmented hash |
| Order | None | **Insertion/Access** | **Sorted** | Enum decl | None |
| get/put | **O(1)** | O(1) | O(log n) | O(1) | O(1) |
| Null key | One | One | **No** | **No** | **No** |
| Null value | Yes | Yes | Yes | Yes | **No** |
| Thread-safe | No | No | No | No | **Yes** |
| Use case | General | LRU cache | Sorted, ranges | Enum keys | Concurrent |

### Queue/Deque Implementations

| Feature | ArrayDeque | LinkedList | PriorityQueue |
|---|---|---|---|
| Internal | Circular array | Doubly-linked list | Binary heap (array) |
| Order | FIFO/LIFO | FIFO/LIFO | **Priority (min-heap)** |
| offer/poll | **O(1)** | O(1) | O(log n) |
| peek | O(1) | O(1) | O(1) |
| Null | **No** | Yes | **No** |
| Random access | No | O(n) | No |
| Use case | **Default stack/queue** | Queue with nulls | Priority processing |

---

## 11. Interview Cheat Patterns

```
"Need O(1) lookup"                     → HashSet / HashMap
"Need sorted order"                    → TreeSet / TreeMap
"Need insertion order"                 → LinkedHashSet / LinkedHashMap
"Need stack"                           → ArrayDeque (push/pop)
"Need queue"                           → ArrayDeque (offer/poll)
"Need priority processing"            → PriorityQueue
"Need LRU cache"                       → LinkedHashMap(accessOrder=true) + removeEldestEntry
"Need concurrent map"                  → ConcurrentHashMap
"Need concurrent list (read-heavy)"   → CopyOnWriteArrayList
"Need enum flags/sets"                → EnumSet
"Need enum map"                       → EnumMap
"Need frequency count"                → HashMap<T, Integer> + merge(key, 1, Integer::sum)
"Need grouping"                       → Collectors.groupingBy()
"Need top K elements"                 → PriorityQueue of size K
"Need dedup + preserve order"         → LinkedHashSet
"Need bi-directional map"             → no built-in — use two HashMaps or Guava BiMap
"Need multimap (key → many values)"   → Map<K, List<V>> + computeIfAbsent(k, k -> new ArrayList<>())
"Need range queries on keys"          → TreeMap (subMap, headMap, tailMap)
"Need thread-safe queue"              → ConcurrentLinkedQueue / BlockingQueue impls
```

---

## 12. Common Traps & Gotchas

```java
// 1. Arrays.asList() returns fixed-size list
List<Integer> list = Arrays.asList(1, 2, 3);
list.set(0, 10);   // ✓ works — modifies underlying array
list.add(4);        // ✗ UnsupportedOperationException
// Fix: new ArrayList<>(Arrays.asList(1, 2, 3));

// 2. Arrays.asList(primitiveArray) wraps the ARRAY, not elements
int[] arr = {1, 2, 3};
List<int[]> wrong = Arrays.asList(arr);     // List of one int[] element!
List<Integer> right = IntStream.of(arr).boxed().collect(Collectors.toList());

// 3. List.remove() overload ambiguity with Integer
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));
nums.remove(1);                // removes INDEX 1 → [1, 3]
nums.remove(Integer.valueOf(1)); // removes VALUE 1 → [2, 3]

// 4. HashMap key mutation
Map<List<Integer>, String> map = new HashMap<>();
List<Integer> key = new ArrayList<>(List.of(1, 2));
map.put(key, "value");
key.add(3);                    // hashCode changed → can't find entry anymore
map.get(key);                  // null! Entry is orphaned in wrong bucket

// 5. ConcurrentModificationException
for (String s : list) {
    if (s.equals("remove")) list.remove(s);  // ✗ CME
}
list.removeIf(s -> s.equals("remove"));      // ✓ safe

// 6. TreeSet/TreeMap requires Comparable or Comparator
new TreeSet<>().add(new Object()); // ✗ ClassCastException at runtime

// 7. Collections.unmodifiableList() is a VIEW, not a copy
List<String> original = new ArrayList<>(List.of("a", "b"));
List<String> unmod = Collections.unmodifiableList(original);
original.add("c");            // unmod now also has "c"!
// Use List.copyOf(original) for true immutable copy

// 8. HashSet relies on equals() + hashCode() contract
// If you override equals() but not hashCode() → duplicate entries in HashSet/HashMap
// Rule: equal objects MUST have same hashCode. Same hashCode does NOT mean equal.

// 9. PriorityQueue iteration order ≠ sorted order
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(3, 1, 2));
for (int x : pq) { }          // NOT guaranteed sorted — only poll() gives sorted order
// Heap property: parent ≤ children, but siblings are unordered

// 10. subList() is a live view
List<String> sub = list.subList(1, 3);
list.add("new");               // ✗ sub is now invalid → CME on sub operations
```
