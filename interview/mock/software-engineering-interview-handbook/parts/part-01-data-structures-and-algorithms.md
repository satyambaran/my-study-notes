# Part I: Data Structures & Algorithms

## Introduction
Data structures and algorithms (DS&A) are the shared vocabulary of
software interviews: they test whether you can model a problem, reason
about correctness, and predict how a solution scales. This part covers 23
core topics from first principles — each chapter defines a structure,
explains its purpose, walks through its mechanics, and answers the
interview questions it most commonly produces.

## Learning Roadmap
1. **Foundations** — Complexity Analysis, Arrays, Strings, Hashing.
2. **Array techniques** — Two Pointers, Sliding Window, Prefix Sum,
   Binary Search.
3. **Linear structures** — Linked Lists, Stack, Queue, Heap.
4. **Hierarchical structures** — Trees, Binary Search Trees, Trie.
5. **Relationships** — Graphs, Union Find.
6. **Paradigms** — Greedy, Backtracking, Dynamic Programming.
7. **Range structures** — Segment Tree, Fenwick Tree.
8. **Bit Manipulation** — low-level tricks used throughout.

Read in order first; use each chapter later as a standalone reference.
Every chapter shares the same structure so answers are easy to find.

---

# Complexity Analysis

## What is it?
A framework for describing how an algorithm's time and memory usage grow
as input size grows, using Big-O notation.

## Why do we need it?
Runtime in seconds depends on hardware; complexity describes growth rate
independent of machine speed.

## What problem does it solve?
It lets engineers compare algorithms and predict scaling before writing
or running any code.

## History (only if useful)
Big-O notation originates in 19th-century number theory, popularized in
computing by Donald Knuth in the 1970s.

## How does it work?
Count how many basic operations execute as a function of input size n,
then keep only the fastest-growing term.

## Internal Working
Constants and lower-order terms are dropped because they matter less and
less as n grows large; O(n² + n) simplifies to O(n²).

## Step-by-Step Example
A loop nested inside another loop, each running n times:
```
for i in 0..n: for j in 0..n: doWork()
Total operations = n * n = n^2 -> O(n^2)
```

## Visualization
```
O(1) < O(log n) < O(n) < O(n log n) < O(n^2) < O(2^n)
(flat)  (slow rise)      (linear)              (explosive)
```

## Real-World Usage
Choosing between algorithms for search, sorting, and query systems at
scale; capacity planning for expected data growth.

## Advantages
Gives a hardware-independent, provable way to compare algorithm scaling.

## Disadvantages
Hides constant factors, so a "worse" Big-O can be faster for small n.

## Trade-offs
Time and space can often be traded, e.g. caching avoids recomputation.

## Alternatives
Empirical benchmarking when constants matter more than asymptotic growth,
e.g. for small, fixed input sizes.

## Performance Analysis
Best, average, and worst case can differ sharply; quicksort is O(n log n)
average but O(n²) worst case.

## Complexity Analysis (if applicable)
| Notation | Meaning |
|---|---|
| O (Big-O) | Upper bound (worst case) |
| Ω (Omega) | Lower bound (best case) |
| Θ (Theta) | Tight bound (both match) |

## Implementation
```java
public final class ComplexityDemo {
    public static int linearSearch(int[] arr, int target) { // O(n)
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }

    public static int binarySearch(int[] sorted, int target) { // O(log n)
        int lo = 0, hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (sorted[mid] == target) return mid;
            if (sorted[mid] < target) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
}
```

## Common Mistakes
Ignoring space complexity; assuming average case always applies instead
of checking worst case.

## Common Interview Questions

Q: What is the time complexity of nested loops over the same input?

Answer:
O(n²) if both loops run n times independently, since work is done for
every pair of elements.

Q: What's the difference between O(n) and O(log n)?

Answer:
O(n) touches every element once; O(log n) halves the search space each
step, as in binary search.

## Follow-up Interview Questions

Q: How do you analyze the complexity of a recursive function?

Answer:
Write a recurrence (e.g. T(n) = 2T(n/2) + O(n)) and solve it via the
Master Theorem.

Q: When is a higher Big-O algorithm actually faster in practice?

Answer:
When constants dominate at small n; insertion sort can beat quicksort on
tiny arrays.

## How the Interviewer Thinks
They check whether you state complexity unprompted and justify it, not
just guess a label.

## Production Engineering Perspective
Systems choosing O(n²) algorithms on unexpectedly large input is a
classic cause of production slowdowns and timeouts.

## Common Misconceptions
"Big-O measures exact runtime" — it measures growth rate, not seconds.
"Lower Big-O is always faster" — only for large enough n.

## Revision Notes
- Big-O = worst case upper bound; drop constants and lower-order terms.
- Best/average/worst case can differ significantly.
- Time and space can often be traded against each other.

## Summary
Complexity analysis gives a hardware-independent way to predict how an
algorithm scales, and is the lens every other chapter is viewed through.

---

# Arrays

## What is it?
A fixed-size, contiguous block of memory storing elements of the same
type, accessed by index.

## Why do we need it?
Contiguous memory gives O(1) index access and excellent cache locality,
unlike scattered structures.

## What problem does it solve?
It solves storing and accessing an ordered collection efficiently by
position.

## History (only if useful)
Arrays date to the earliest programming languages, mirroring how memory
itself is physically addressed.

## How does it work?
Element i's address is computed as `base + i * elementSize`, giving
direct O(1) access.

## Internal Working
Java arrays are fixed-length; `ArrayList` wraps a backing array and
reallocates a larger one (typically 1.5x) when it fills up.

## Step-by-Step Example
Inserting 99 at index 1 in `[1,2,3]` (size 4 capacity):
```
Shift right from index 3: [1,_,2,3] -> [1,99,2,3]
```

## Visualization
```
Index:  0   1   2   3
Value: [10][20][30][40]
```

## Real-World Usage
Image pixel buffers, matrices, and any fixed-size lookup table needing
O(1) access.

## Advantages
O(1) random access; excellent cache locality from contiguous memory.

## Disadvantages
O(n) insert/delete in the middle; fixed arrays can't grow.

## Trade-offs
`ArrayList` trades some memory (unused capacity) for amortized O(1)
append.

## Alternatives
A linked list when frequent middle insertion/deletion outweighs the need
for random access.

## Performance Analysis
Amortized doubling keeps average append at O(1), though any single
resize costs O(n).

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| Access by index | O(1) |
| Insert/delete at end | O(1) amortized |
| Insert/delete in middle | O(n) |

## Implementation
```java
public final class DynamicArray {
    private int[] data = new int[4];
    private int size = 0;

    public void add(int value) {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2); // O(n) resize
        }
        data[size++] = value;
    }

    public void insertAt(int index, int value) {
        add(0); // grow if needed
        for (int i = size - 1; i > index; i--) data[i] = data[i - 1];
        data[index] = value;
    }
}
```

## Common Mistakes
Off-by-one errors at array bounds; assuming append is always O(1) instead
of amortized.

## Common Interview Questions

Q: Why is array access O(1) but linked list access O(n)?

Answer:
An array computes any element's address directly from its index; a
linked list must walk node by node from the head.

Q: How does a dynamic array like `ArrayList` grow?

Answer:
When full, it allocates a new, larger array (commonly 1.5-2x) and copies
existing elements over — O(n) for that resize, O(1) amortized overall.

## Follow-up Interview Questions

Q: How would you rotate an array in place by k positions?

Answer:
Reverse the whole array, then reverse the first k and remaining n-k
elements separately — O(n) time, O(1) extra space.

Q: How do you remove duplicates from a sorted array in place?

Answer:
Use a slow pointer for the next unique position and a fast pointer to
scan, copying only new values forward — O(n) time, O(1) space.

## How the Interviewer Thinks
They check whether you know amortized append is O(1), not true O(1), and
can reason about shifting cost for middle operations.

## Production Engineering Perspective
Pre-sizing a collection when the final size is known avoids repeated
resize-and-copy overhead in hot paths.

## Common Misconceptions
"Arrays and ArrayLists are the same" — arrays are fixed-size; ArrayLists
resize dynamically. "Append is always O(1)" — it's amortized, not
worst-case.

## Revision Notes
- O(1) access via direct address computation; O(n) middle insert/delete.
- Dynamic arrays grow by doubling, giving amortized O(1) append.
- Rotate and dedupe-in-place are common two-pointer-style follow-ups.

## Summary
Arrays give the fastest possible random access via contiguous memory, at
the cost of expensive middle mutations and fixed capacity unless wrapped
in a dynamic array.

---

# Strings

## What is it?
An immutable (in Java) sequence of characters, often backed internally by
a byte or char array.

## Why do we need it?
Text is everywhere in software; strings need specialized operations like
search, comparison, and concatenation.

## What problem does it solve?
It solves representing and manipulating textual data with predictable
memory and comparison semantics.

## History (only if useful)
Java strings became immutable partly for security and to safely support
string interning and hashing.

## How does it work?
Characters are stored contiguously; most operations (substring,
concatenation) create a new string rather than mutating in place.

## Internal Working
Repeated concatenation in a loop is O(n²) because each `+` allocates a
new string; `StringBuilder` mutates an internal buffer for O(n) total.

## Step-by-Step Example
Reversing "cat" with two pointers:
```
c a t -> swap ends: t a c (pointers meet at middle)
```

## Visualization
```
Index:  0   1   2
Char:  'c' 'a' 't'
```

## Real-World Usage
Parsing, validation, text search, and template rendering.

## Advantages
Immutability makes strings safe to share across threads and cache (e.g.,
in the string pool).

## Disadvantages
Immutability makes naive repeated concatenation expensive.

## Trade-offs
Immutable strings cost more per mutation but avoid defensive copying and
synchronization elsewhere.

## Alternatives
`StringBuilder`/`StringBuffer` for heavy in-place-style mutation; `char[]`
for direct low-level access.

## Performance Analysis
`StringBuilder` amortizes append to O(1) via a resizable buffer, just like
`ArrayList`.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| charAt / length | O(1) |
| concatenation (n times) | O(n²) naive, O(n) with StringBuilder |
| substring | O(n) |

## Implementation
```java
public final class StringOps {
    public static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left++) != s.charAt(right--)) return false;
        }
        return true;
    }

    public static String reverseWithBuilder(String s) {
        return new StringBuilder(s).reverse().toString();
    }
}
```

## Common Mistakes
Using `+` inside a loop instead of `StringBuilder`; comparing strings with
`==` instead of `.equals()`.

## Common Interview Questions

Q: Why are Java strings immutable?

Answer:
For thread safety, safe hashing/caching (string pool), and security when
strings are used as keys or credentials.

Q: How do you check if two strings are anagrams?

Answer:
Sort both and compare, or count character frequencies in a fixed-size
array and compare counts — O(n) with counting.

## Follow-up Interview Questions

Q: How would you find the longest palindromic substring?

Answer:
Expand around each possible center (odd and even length) checking
symmetry — O(n²) time, O(1) space.

Q: How does `StringBuilder` avoid the O(n²) cost of repeated concatenation?

Answer:
It maintains a resizable internal char buffer and appends in-place,
amortizing to O(1) per append like a dynamic array.

## How the Interviewer Thinks
They check whether you avoid `+` concatenation in loops and reach for
two-pointer or frequency-count techniques by default.

## Production Engineering Perspective
Building large strings via repeated `+` in a request-handling hot path is
a common, easily-missed source of latency regressions.

## Common Misconceptions
"`==` compares string content" — it compares references; use `.equals()`.
"Substrings are always cheap" — modern Java substring copies the backing
array, costing O(n).

## Revision Notes
- Java strings are immutable; use `StringBuilder` for repeated mutation.
- `==` compares reference identity, `.equals()` compares content.
- Anagram and palindrome checks are the most common string patterns.

## Summary
Strings trade mutation cost for safety and shareability, and knowing when
to switch to `StringBuilder` or two-pointer techniques is the core
interview skill.

---

# Hashing

## What is it?
A technique mapping keys to array indices via a hash function, giving
average O(1) insert, lookup, and delete.

## Why do we need it?
Arrays give O(1) access only by numeric index; hashing gives near-O(1)
access by arbitrary key.

## What problem does it solve?
It solves fast key-based lookup, counting, and deduplication without
scanning the whole collection.

## History (only if useful)
Hash tables date to 1953 IBM research; open addressing and chaining were
developed shortly after to handle collisions.

## How does it work?
A hash function converts a key into an array index; collisions (two keys
mapping to the same index) are resolved via chaining or probing.

## Internal Working
Java's `HashMap` uses an array of buckets, each holding a linked list (or
balanced tree once a bucket exceeds 8 entries) for collision resolution.

## Step-by-Step Example
Inserting "cat" (hash 3) and "dog" (hash 3) into an 8-bucket table:
```
bucket[3] = "cat" -> "dog" (chained on collision)
```

## Visualization
```
Buckets: [0][1][2][3: cat->dog][4][5][6][7]
```

## Real-World Usage
Caches, database indexes, deduplication, and frequency counting.

## Advantages
Average O(1) insert/lookup/delete regardless of collection size.

## Disadvantages
Worst-case O(n) if many keys collide; no ordering guarantee (`HashMap`).

## Trade-offs
`HashMap` gives speed without order; `TreeMap`/`LinkedHashMap` add
ordering at a performance cost.

## Alternatives
A `TreeMap` when sorted iteration matters; a `LinkedHashMap` when
insertion order matters.

## Performance Analysis
A good hash function with load factor kept below ~0.75 keeps operations
close to O(1) by minimizing collisions.

## Complexity Analysis (if applicable)
| Operation | Average | Worst Case |
|---|---|---|
| Insert / lookup / delete | O(1) | O(n) |

## Implementation
```java
public final class FrequencyCounter {
    public static Map<Character, Integer> countChars(String s) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : s.toCharArray()) {
            counts.merge(c, 1, Integer::sum); // O(1) average per char
        }
        return counts;
    }
}
```

## Common Mistakes
Using a mutable object as a hash key; overriding `equals()` without
overriding `hashCode()`.

## Common Interview Questions

Q: How does a HashMap handle collisions?

Answer:
It chains colliding entries in a linked list per bucket, converting to a
balanced tree if a bucket grows past 8 entries.

Q: Why must equal objects have equal hash codes?

Answer:
Because HashMap uses the hash code to pick a bucket first; mismatched
hash codes would let equal keys land in different buckets and never be
found.

## Follow-up Interview Questions

Q: How would you design a hash function for a custom object?

Answer:
Combine the hash codes of all fields used in `equals()`, e.g. via
`Objects.hash(field1, field2)`, keeping it consistent with equality.

Q: How do you detect if an array contains duplicates in O(n)?

Answer:
Insert elements into a `HashSet` one at a time; if `add()` returns false,
a duplicate was found.

## How the Interviewer Thinks
They check whether you know the equals/hashCode contract and can reason
about load factor and collision impact.

## Production Engineering Perspective
A poor or attacker-influenced hash function enabling many collisions is a
known denial-of-service vector (hash flooding).

## Common Misconceptions
"HashMap preserves insertion order" — it doesn't; use `LinkedHashMap` for
that. "Hashing guarantees O(1)" — only on average with a good hash
function.

## Revision Notes
- Average O(1), worst case O(n) with heavy collisions.
- `equals()` and `hashCode()` must stay consistent.
- Use `HashSet` for O(1) membership/duplicate checks.

## Summary
Hashing trades a small chance of collision-driven slowdown for average
O(1) key-based access, making it the default structure for lookups,
counting, and deduplication.

---

# Two Pointers

## What is it?
A technique using two index pointers moving through a structure to solve
problems in one pass instead of nested loops.

## Why do we need it?
A brute-force nested loop over pairs costs O(n²); two pointers often
reduce this to O(n).

## What problem does it solve?
It solves pair-finding, partitioning, and comparison problems on sorted or
linear data efficiently.

## History (only if useful)
An informal pattern refined through decades of algorithm-competition and
interview practice rather than a single named origin.

## How does it work?
Two indices move toward each other (or in the same direction) based on a
comparison, narrowing the search space each step.

## Internal Working
On a sorted array, moving the low pointer up increases the sum and the
high pointer down decreases it, eliminating one candidate per step.

## Step-by-Step Example
Two-sum on sorted `[1,3,5,7]`, target 8:
```
left=1,right=7 sum=8 -> match
```

## Visualization
```
[1, 3, 5, 7]
 ^L       ^R  -> sum=8, found
```

## Real-World Usage
Two-sum on sorted data, palindrome checks, and merging sorted sequences.

## Advantages
Reduces O(n²) brute force to O(n) with O(1) extra space.

## Disadvantages
Usually requires sorted (or otherwise structured) input to work
correctly.

## Trade-offs
Sorting first costs O(n log n) but then enables an O(n) two-pointer pass,
still better than O(n²) brute force.

## Alternatives
A hash set for unsorted two-sum in O(n) time and O(n) space instead of
sorting.

## Performance Analysis
Each pointer moves at most n times total, so the whole pass is O(n)
despite looking like nested logic.

## Complexity Analysis (if applicable)
| Approach | Time | Space |
|---|---|---|
| Brute force pairs | O(n²) | O(1) |
| Two pointers (sorted) | O(n) | O(1) |

## Implementation
```java
public final class TwoSumSorted {
    public static int[] twoSum(int[] sorted, int target) {
        int left = 0, right = sorted.length - 1;
        while (left < right) {
            int sum = sorted[left] + sorted[right];
            if (sum == target) return new int[]{left, right};
            if (sum < target) left++; else right--;
        }
        return new int[]{-1, -1};
    }
}
```

## Common Mistakes
Applying two pointers to unsorted data without a sort or hash set first;
off-by-one errors when pointers should meet or cross.

## Common Interview Questions

Q: When should you use two pointers instead of a hash set for two-sum?

Answer:
When the array is already sorted (or sorting is acceptable), two pointers
use O(1) space versus a hash set's O(n).

Q: How do you check if a string is a palindrome using two pointers?

Answer:
Move one pointer from the start and one from the end, comparing
characters and stopping if they differ or the pointers meet.

## Follow-up Interview Questions

Q: How would you find three numbers summing to zero (3Sum)?

Answer:
Sort the array, fix one number, then two-pointer the rest — O(n²),
skipping duplicates to avoid repeat triplets.

Q: How do you merge two sorted arrays in place using two pointers?

Answer:
Fill from the back using three pointers so overwriting never destroys
unread data.

## How the Interviewer Thinks
They check whether you recognize sorted-input problems as two-pointer
candidates instead of defaulting to nested loops.

## Production Engineering Perspective
Merge-based database query engines rely on this same two-pointer merge
pattern for streaming sorted data.

## Common Misconceptions
"Two pointers always move toward each other" — fast/slow variants move
in the same direction.

## Revision Notes
- Requires sorted (or specially structured) input; O(n) time, O(1) space.
- Opposite-direction for pair-sum; same-direction for fast/slow patterns.
- 3Sum = sort + fix one + two-pointer on the rest.

## Summary
Two pointers exploit sorted or structured input to collapse an O(n²)
brute force into a single O(n) pass, one of the highest-leverage patterns
in interviews.

---

# Sliding Window

## What is it?
A technique that maintains a contiguous subrange (window) over an array
or string, expanding and shrinking it instead of recomputing from
scratch.

## Why do we need it?
Recomputing a result for every possible subrange costs O(n²) or worse;
sliding the window reuses prior work.

## What problem does it solve?
It solves finding an optimal contiguous subarray/substring under a size or
condition constraint.

## History (only if useful)
Grew out of streaming and network buffering concepts (TCP's sliding
window) applied to array/string problems.

## How does it work?
Expand the window's right edge to include new elements; shrink the left
edge when a constraint is violated.

## Internal Working
Each element enters and leaves the window at most once, so total work
across the whole pass is O(n) instead of O(n) per window position.

## Step-by-Step Example
Max sum of window size 2 in `[1,3,2,5]`:
```
[1,3]=4 -> slide -> [3,2]=5 -> slide -> [2,5]=7 (max)
```

## Visualization
```
[1  3][2  5]     window slides right, one element at a time
```

## Real-World Usage
Network flow control, rate limiting, and substring/subarray optimization
problems.

## Advantages
Reduces O(n²) or worse brute force to O(n) by reusing overlapping work.

## Disadvantages
Only applies when the constraint is monotonic (window can meaningfully
grow/shrink); doesn't fit all subarray problems.

## Trade-offs
A fixed-size window is simpler; a variable-size window handles more
problems but needs careful shrink-condition logic.

## Alternatives
Prefix sums when non-contiguous or fully precomputed range sums suffice
instead of a moving window.

## Performance Analysis
Because each pointer only moves forward, total pointer movement across
the whole run is bounded by 2n, giving O(n).

## Complexity Analysis (if applicable)
| Approach | Time |
|---|---|
| Brute force (all subarrays) | O(n²) or O(n³) |
| Sliding window | O(n) |

## Implementation
```java
public final class LongestSubstringNoRepeat {
    public static int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int left = 0, maxLen = 0;
        for (int right = 0; right < s.length(); right++) {
            while (window.contains(s.charAt(right))) {
                window.remove(s.charAt(left++)); // shrink until valid
            }
            window.add(s.charAt(right));
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }
}
```

## Common Mistakes
Forgetting to shrink the window when the constraint is violated; using
the wrong data structure to track window contents efficiently.

## Common Interview Questions

Q: How do you find the maximum sum subarray of a fixed size k?

Answer:
Compute the first window's sum, then slide by subtracting the outgoing
element and adding the incoming one — O(n) total.

Q: How does sliding window find the longest substring without repeating characters?

Answer:
Expand the right pointer, and shrink the left pointer past any repeated
character until the window is valid again.

## Follow-up Interview Questions

Q: How would you find the smallest subarray with a sum at least S?

Answer:
Expand right summing as you go, then shrink left greedily whenever the
target is met, tracking the minimum length seen.

Q: How do you adapt sliding window for "at most k distinct characters" problems?

Answer:
Track counts in a map; shrink the left edge whenever distinct count
exceeds k.

## How the Interviewer Thinks
They check whether you can identify the shrink condition correctly and
avoid recomputing the window from scratch each step.

## Production Engineering Perspective
Rate limiters ("N requests per minute") are a live sliding-window
computation over a stream of timestamps.

## Common Misconceptions
"Sliding window only works for fixed sizes" — variable-size windows are
equally common.

## Revision Notes
- Expand right, shrink left only when a constraint is violated; O(n)
  total.
- Fixed-size: slide by one in/one out. Variable-size: shrink until valid.
- Track window state with a count/frequency map or running sum.

## Summary
Sliding window reuses overlapping work across subranges to turn brute
force subarray/substring scans into a single O(n) pass, driven by an
expand-then-shrink loop.

---

# Prefix Sum

## What is it?
A precomputed array where each entry holds the cumulative sum of all
elements up to that index.

## Why do we need it?
Recomputing a range sum by looping every query costs O(n) per query;
prefix sums answer it in O(1) after O(n) preprocessing.

## What problem does it solve?
It solves fast repeated range-sum queries over a static (non-changing)
array.

## History (only if useful)
A basic cumulative-sum idea from numerical computing, widely adopted in
competitive programming for O(1) range queries.

## How does it work?
Build `prefix[i] = prefix[i-1] + arr[i-1]`; then `sum(l,r) = prefix[r+1] -
prefix[l]`.

## Internal Working
Subtracting the prefix sum just before the range from the prefix sum at
its end isolates exactly the elements in between, in constant time.

## Step-by-Step Example
`arr = [1,2,3,4]`, prefix = `[0,1,3,6,10]`:
```
sum(1,2) = prefix[3] - prefix[1] = 6 - 1 = 5  (matches 2+3)
```

## Visualization
```
arr:    [1][2][3][4]
prefix:[0][1][3][6][10]
```

## Real-World Usage
Range-sum dashboards, cumulative analytics, and image integral images for
fast region-sum lookups.

## Advantages
O(1) range-sum queries after a one-time O(n) build.

## Disadvantages
O(n) to rebuild after any update, making it a poor fit for frequently
changing arrays.

## Trade-offs
Prefix sums favor read-heavy, write-rare workloads; a Fenwick/segment tree
handles frequent updates better.

## Alternatives
A Fenwick tree or segment tree when the array is updated often alongside
range queries.

## Performance Analysis
Build is a single O(n) pass; every subsequent query is O(1) regardless of
range size.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| Build | O(n) |
| Range sum query | O(1) |
| Update | O(n) (rebuild) |

## Implementation
```java
public final class PrefixSum {
    private final int[] prefix;

    public PrefixSum(int[] arr) {
        prefix = new int[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            prefix[i + 1] = prefix[i] + arr[i];
        }
    }

    public int rangeSum(int left, int right) { // inclusive [left, right]
        return prefix[right + 1] - prefix[left];
    }
}
```

## Common Mistakes
Off-by-one errors between inclusive/exclusive bounds; forgetting to
rebuild after mutating the source array.

## Common Interview Questions

Q: How does a prefix sum array answer range-sum queries in O(1)?

Answer:
Each prefix entry stores the cumulative sum to that point, so subtracting
two prefix values isolates the sum of everything in between.

Q: What is the 2D equivalent of prefix sum used for?

Answer:
It answers rectangular region-sum queries in O(1) using inclusion-
exclusion over four corner values, common in image processing.

## Follow-up Interview Questions

Q: How would you find the number of subarrays summing to exactly k?

Answer:
Track prefix sums seen so far in a hash map; for each new prefix sum,
check how many times `prefixSum - k` occurred before — O(n).

Q: How do you handle updates efficiently if the array changes often?

Answer:
Switch to a Fenwick tree or segment tree, which supports both range
query and update in O(log n) instead of prefix sum's O(n) rebuild.

## How the Interviewer Thinks
They check whether you reach for prefix sums for repeated range queries
and recognize when updates make them the wrong tool.

## Production Engineering Perspective
Precomputed cumulative sums back dashboard metrics (e.g., "total sales
between two dates") for O(1) reads on largely static historical data.

## Common Misconceptions
"Prefix sum works well with frequent updates" — every update forces an
O(n) rebuild.

## Revision Notes
- `prefix[i]` = cumulative sum up to index i-1; range sum via subtraction.
- O(n) build, O(1) query, O(n) update — great for static data only.
- Hash-map-of-prefix-sums finds subarrays summing to k in O(n).

## Summary
Prefix sums trade a one-time O(n) build for O(1) range-sum queries, ideal
for static arrays with many repeated queries but a poor fit once frequent
updates are involved.

---

# Binary Search

## What is it?
An algorithm that finds a target in a sorted collection by repeatedly
halving the search space.

## Why do we need it?
Linear search costs O(n); binary search exploits sorted order to find a
target in O(log n).

## What problem does it solve?
It solves fast lookup and "find the boundary" problems over sorted or
monotonic data.

## History (only if useful)
Described as early as 1946, but early implementations often had subtle
bugs in the midpoint calculation for decades.

## How does it work?
Compare the target to the middle element; discard the half that can't
contain the target, and repeat.

## Internal Working
Each comparison eliminates half the remaining candidates, so after
roughly log₂(n) steps only one candidate remains.

## Step-by-Step Example
Searching for 7 in `[1,3,5,7,9]`:
```
mid=5 (7>5, go right) -> [7,9], mid=7 -> found
```

## Visualization
```
[1, 3, 5, 7, 9]
       ^mid (5<7, search right half)
```

## Real-World Usage
Searching sorted datasets, database index lookups, and "find the
boundary" problems like version/commit bisection.

## Advantages
O(log n) search, dramatically faster than linear scan for large sorted
data.

## Disadvantages
Requires sorted (or monotonic) data; unsorted data must be sorted first
at O(n log n) cost.

## Trade-offs
Binary search needs sorted input, so it's best when data is sorted once
and queried many times.

## Alternatives
A hash set for O(1) average membership when order isn't otherwise needed.

## Performance Analysis
Halving the search space each step yields O(log n), far better than O(n)
even for moderately large inputs.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| Search in sorted array | O(log n) |
| Sort then search once | O(n log n) |

## Implementation
```java
public final class BinarySearch {
    public static int search(int[] sorted, int target) {
        int lo = 0, hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; // avoids overflow
            if (sorted[mid] == target) return mid;
            if (sorted[mid] < target) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
}
```

## Common Mistakes
Computing `mid = (lo + hi) / 2` (can overflow); off-by-one errors in the
loop condition or bound updates.

## Common Interview Questions

Q: Why use `lo + (hi - lo) / 2` instead of `(lo + hi) / 2`?

Answer:
`lo + hi` can overflow a 32-bit int for large arrays; the alternative
form avoids that overflow.

Q: How do you find the first occurrence of a target in a sorted array with duplicates?

Answer:
When a match is found, keep searching the left half and record the
match, narrowing until the boundary is found.

## Follow-up Interview Questions

Q: How do you search in a rotated sorted array?

Answer:
Determine which half is properly sorted by comparing endpoints to mid,
then decide which half the target could be in — O(log n).

Q: How would you find a peak element in an array using binary search?

Answer:
Compare the middle element to its neighbor; move toward the larger side,
since a peak is guaranteed to exist in that direction.

## How the Interviewer Thinks
They check whether you handle boundaries correctly and can adapt binary
search beyond plain "find the exact value".

## Production Engineering Perspective
Feature-flag and deployment bisection tools use binary search to find the
first commit that introduced a regression.

## Common Misconceptions
"Binary search only finds exact matches" — it also finds boundaries
(first/last occurrence, insertion point).

## Revision Notes
- Requires sorted/monotonic data; O(log n) via halving the search space.
- Use `lo + (hi - lo) / 2` to avoid overflow.
- Generalizes to boundary search, rotated arrays, and peak finding.

## Summary
Binary search exploits sorted or monotonic structure to cut the search
space in half each step, and its boundary-finding variants are as
important to know as the classic exact-match version.

---

# Linked Lists

## What is it?
A linear structure of nodes, each holding a value and a reference to the
next node.

## Why do we need it?
It gives O(1) insertion/deletion given a node reference, without the O(n)
shifting arrays require.

## What problem does it solve?
It solves building sequences that grow and shrink often without shifting
or resizing a contiguous block.

## History (only if useful)
Linked lists date to the late 1950s, used in Lisp for symbolic list
processing.

## How does it work?
Each node stores data plus a pointer to the next node; traversal follows
pointers until null.

## Internal Working
Singly linked lists only know "next", so deleting a node needs its
predecessor; doubly linked lists store "prev" too, giving O(1) deletion
given just the node.

## Step-by-Step Example
Inserting 15 between 10 and 20 in `10 -> 20 -> 30`:
```
newNode(15).next = 20; 10.next = 15
Result: 10 -> 15 -> 20 -> 30
```

## Visualization
```
Singly:  [10|*]->[20|*]->[30|null]
Doubly:  null<-[10]<->[20]<->[30]->null
```

## Real-World Usage
LRU cache implementations, undo/redo history, and browser navigation.

## Advantages
O(1) insert/delete given a node reference; no resizing/copying cost.

## Disadvantages
O(n) access/search; extra memory per node for pointers.

## Trade-offs
Trades random-access speed for cheap structural mutation — the opposite
of arrays.

## Alternatives
`ArrayDeque` for double-ended access with better cache locality; skip
lists for fast sorted search plus insertion.

## Performance Analysis
Array traversal is faster in practice than linked-list traversal despite
identical O(n) complexity, due to CPU cache prefetching.

## Complexity Analysis (if applicable)
| Operation | Singly | Doubly |
|---|---|---|
| Access by index | O(n) | O(n) |
| Delete given node reference | O(n) | O(1) |

## Implementation
```java
public final class SinglyLinkedList<T> {
    static class Node<T> { T value; Node<T> next; Node(T v) { value = v; } }
    private Node<T> head;

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        node.next = head;
        head = node;
    }

    public boolean hasCycle() { // Floyd's cycle detection
        Node<T> slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }
}
```

## Common Mistakes
Losing the head reference before rewiring; forgetting to handle an empty
list.

## Common Interview Questions

Q: How do you reverse a singly linked list?

Answer:
Walk with `prev`, `curr`, `next` pointers, rewiring `curr.next=prev` each
step — O(n) time, O(1) space.

Q: How do you detect a cycle in a linked list?

Answer:
Use Floyd's slow/fast pointers: if they meet, a cycle exists.

## Follow-up Interview Questions

Q: How would you find the start of the cycle, not just detect it?

Answer:
After slow and fast meet, reset one to the head and advance both one
step at a time; they meet again at the cycle's start.

Q: How do you merge two sorted linked lists?

Answer:
Use a dummy head, repeatedly appending the smaller current node, then
attach any remainder — O(n+m) time.

## How the Interviewer Thinks
They test whether you track the list state at each step and avoid losing
references mid-rewiring.

## Production Engineering Perspective
LRU caches pair a doubly linked list (O(1) reorder-to-front) with a hash
map (O(1) lookup) for O(1) recency and lookup combined.

## Common Misconceptions
"Linked lists are always better for insertion" — only given a direct node
reference. "Doubly always beats singly" — it costs extra memory per node.

## Revision Notes
- Singly: one direction; doubly: both, O(1) arbitrary deletion.
- Slow/fast pointers: cycle detection, middle-finding.
- Reversal template: `prev, curr, next` triple-pointer walk.

## Summary
Linked lists exchange random access for O(1) structural mutation given a
node reference, the backbone of LRU caches and insertion-heavy workloads.

---

# Stack

## What is it?
A LIFO structure supporting push (add to top) and pop (remove from top),
both O(1).

## Why do we need it?
Many problems are "undo the most recent thing first" — function calls,
matching brackets, backtracking.

## What problem does it solve?
It solves tracking nested or recursive structure using strict
last-in-first-out order.

## History (only if useful)
Formalized in 1950s-60s compiler and calculator design, directly shaping
CPU function-call handling.

## How does it work?
Elements are added and removed only from one end (the top); the last
pushed is always the first popped.

## Internal Working
`ArrayDeque` backs push/pop with a resizable circular array, giving O(1)
amortized operations without legacy synchronization overhead.

## Step-by-Step Example
Validating `"({[]})"`:
```
push ( { [ -> stack: (,{,[
']' pops '[', '}' pops '{', ')' pops '(' -> empty -> valid
```

## Visualization
```
push(3) push(7) push(9)   pop()->9   pop()->7
 [3]     [3,7]   [3,7,9]   [3,7]      [3]
```

## Real-World Usage
Function call stacks, undo/redo in editors, expression parsing, and
depth-first search.

## Advantages
O(1) push/pop/peek; models nested/recursive structure directly.

## Disadvantages
No random access; unbounded push exhausts memory.

## Trade-offs
Recursive solutions read naturally but risk overflow; an explicit stack
trades clarity for controlled memory.

## Alternatives
A `Deque` when both LIFO and FIFO access are needed; recursion when depth
is bounded.

## Performance Analysis
Array-backed stacks outperform linked-list-backed stacks in practice due
to cache locality.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| push / pop / peek | O(1) |
| search arbitrary element | O(n) |

## Implementation
```java
public final class ArrayStack<T> {
    private final List<T> data = new ArrayList<>();
    public void push(T value) { data.add(value); }
    public T pop() {
        if (data.isEmpty()) throw new NoSuchElementException();
        return data.remove(data.size() - 1); // O(1): removes from the end
    }
    public T peek() { return data.get(data.size() - 1); }
}
```

## Common Mistakes
Using legacy `java.util.Stack` instead of `ArrayDeque`; popping without
checking `isEmpty()`.

## Common Interview Questions

Q: How do you check for balanced parentheses using a stack?

Answer:
Push openers; on a closer, pop and check it matches, invalid if empty at
a closer or non-empty at the end.

Q: Why is `ArrayDeque` preferred over `java.util.Stack`?

Answer:
`java.util.Stack` extends the legacy synchronized `Vector`; `ArrayDeque`
is unsynchronized and faster.

## Follow-up Interview Questions

Q: How would you implement a queue using two stacks?

Answer:
Push onto an "in" stack; when dequeuing and "out" is empty, pop
everything from "in" onto "out", then pop from "out" — amortized O(1).

Q: How do you evaluate a postfix expression with a stack?

Answer:
Push operands; on an operator, pop the top two, apply it, push the
result back — O(n).

## How the Interviewer Thinks
They check whether you recognize the LIFO shape of a problem immediately
and handle empty-stack edge cases.

## Production Engineering Perspective
Deep unbounded recursion on attacker-controlled input is a classic
`StackOverflowError` denial-of-service vector.

## Common Misconceptions
"A stack and a call stack are unrelated" — the call stack is a stack.
"Stacks are only for parsing" — they're central to DFS too.

## Revision Notes
- LIFO; push/pop/peek all O(1); prefer `ArrayDeque`.
- Canonical uses: bracket matching, expression evaluation, DFS.
- Two-stack queue and monotonic stack are common follow-up twists.

## Summary
A stack's strict LIFO order fits nested or reversible structure, and its
common variants (monotonic stack, two-stack queue) cover most interview
stack questions.

---

# Queue

## What is it?
A FIFO structure supporting enqueue (add at the back) and dequeue (remove
from the front), both O(1).

## Why do we need it?
Many systems must process work in arrival order; a queue enforces that
ordering directly.

## What problem does it solve?
It solves fair, order-preserving processing of a stream of items and
level-by-level traversal (BFS).

## History (only if useful)
Formalizes "first come, first served", adopted via early OS job
scheduling and queueing theory.

## How does it work?
New elements are added at the tail; the oldest at the head is removed
first.

## Internal Working
`ArrayDeque` implements a queue via a circular array: head and tail
indices wrap around, avoiding the O(n) shifting a naive array queue
needs.

## Step-by-Step Example
```
enqueue(1): [1]
enqueue(2): [1,2]
dequeue() -> 1: [2]
```

## Visualization
```
Front                     Back
 [ 1 ][ 2 ][ 3 ]  <- enqueue(4) appends here
   ^ dequeue() removes here
```

## Real-World Usage
Task/job queues, print spoolers, CPU ready-queue scheduling, and BFS.

## Advantages
O(1) enqueue/dequeue with a circular-buffer implementation.

## Disadvantages
No random access to the middle; naive array-shift dequeue degrades to
O(n).

## Trade-offs
Bounded queues give backpressure at the cost of rejecting producers;
unbounded queues risk unbounded growth.

## Alternatives
A priority queue (heap) when "next to process" is priority-order, not
arrival-order.

## Performance Analysis
A circular-buffer queue gives true O(1) enqueue/dequeue; calling
`remove(0)` on an ArrayList-backed queue silently degrades to O(n).

## Complexity Analysis (if applicable)
| Operation | Circular Array | Naive remove(0) |
|---|---|---|
| enqueue | O(1) amortized | O(1) |
| dequeue | O(1) | O(n) |

## Implementation
```java
public final class CircularQueue<T> {
    private final Object[] data;
    private int head = 0, size = 0;
    public CircularQueue(int capacity) { data = new Object[capacity]; }

    public void enqueue(T value) {
        if (size == data.length) throw new IllegalStateException("full");
        data[(head + size) % data.length] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) throw new NoSuchElementException();
        T value = (T) data[head];
        head = (head + 1) % data.length;
        size--;
        return value;
    }
}
```

## Common Mistakes
Implementing dequeue with `ArrayList.remove(0)` (silently O(n));
forgetting modulo wraparound.

## Common Interview Questions

Q: How is a queue different from a stack?

Answer:
A queue is FIFO (arrival order); a stack is LIFO (most recent first),
changing BFS versus DFS order.

Q: How do you implement a queue using two stacks?

Answer:
Maintain an "in" stack for enqueue and an "out" stack for dequeue,
transferring only when "out" is empty — amortized O(1).

## Follow-up Interview Questions

Q: Why is a circular buffer preferred over a plain array for a fixed-size queue?

Answer:
A plain array must shift all elements after every dequeue (O(n)); a
circular buffer wraps indices via modulo, keeping both O(1).

Q: How would you implement a queue that also supports retrieving the maximum in O(1)?

Answer:
Pair it with a monotonic decreasing deque, popping smaller trailing
candidates on enqueue.

## How the Interviewer Thinks
They check whether you default to a queue for "arrival order" problems
and notice implementations that silently lose their O(1) guarantee.

## Production Engineering Perspective
Message brokers (Kafka, SQS) are FIFO-oriented, and bounded queue
capacity plus a rejection policy is a core reliability lever.

## Common Misconceptions
"A queue always processes strictly in arrival order at scale" —
partitioned queues often only guarantee order within a partition.

## Revision Notes
- FIFO; enqueue tail, dequeue head, both O(1) with the right structure.
- Use `ArrayDeque`, not `ArrayList.remove(0)`.
- Canonical uses: BFS, scheduling, producer-consumer buffering.

## Summary
A queue's FIFO ordering fits fair scheduling and level-order traversal,
and spotting a "queue-like" implementation that silently degrades to
O(n) is as important as the ideal design.

---

# Heap

## What is it?
A complete binary tree stored in an array, maintaining parent ≤ children
(min-heap) or ≥ (max-heap), giving O(log n) insert/removal and O(1) peek.

## Why do we need it?
Fully sorting data just to repeatedly grab the current minimum/maximum
wastes work; a heap keeps only the ordering invariant needed.

## What problem does it solve?
It solves efficient "always process the highest-priority item next"
workloads without fully sorting.

## History (only if useful)
Invented by J. W. J. Williams in 1964 as the basis for Heapsort.

## How does it work?
Insert and extract-min/max touch only one root-to-leaf path instead of
reordering everything.

## Internal Working
Stored in a flat array: children of index i are at 2i+1/2i+2, parent at
(i-1)/2. Insert appends and "sifts up"; extract moves the last element to
the root and "sifts down".

## Step-by-Step Example
Inserting 3 into min-heap `[5, 8, 6]`:
```
Append:        [5, 8, 6, 3]
Sift-up (3<8): [5, 3, 6, 8]
Sift-up (3<5): [3, 5, 6, 8]
```

## Visualization
```
Array: [3, 5, 6, 8]        3
Tree:                    /   \
                        5     6
```

## Real-World Usage
OS/thread schedulers, Dijkstra's and Prim's algorithms, and streaming
top-k dashboards.

## Advantages
O(log n) insert/extract with O(1) extra space beyond the array; O(1)
peek.

## Disadvantages
O(n) to search an arbitrary non-root element; no efficient sorted
traversal.

## Trade-offs
A heap gives fast access only to the current extreme value; a sorted
structure gives full ordering at higher insert cost.

## Alternatives
A balanced BST/`TreeMap` when both ordered iteration and priority
extraction are needed.

## Performance Analysis
Building a heap bottom-up is O(n), not O(n log n), since most nodes are
near the bottom and need few sift-down swaps.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| peek | O(1) |
| insert / extract | O(log n) |
| build heap from array | O(n) |

## Implementation
```java
public final class MinHeap {
    private final int[] data; private int size = 0;
    public MinHeap(int capacity) { data = new int[capacity]; }

    public void insert(int value) {
        int i = size++;
        data[i] = value;
        while (i > 0 && data[(i - 1) / 2] > data[i]) {
            int p = (i - 1) / 2, t = data[i]; data[i] = data[p]; data[p] = t;
            i = p;
        }
    }

    public int extractMin() {
        int min = data[0];
        data[0] = data[--size];
        siftDown(0);
        return min;
    }

    private void siftDown(int i) {
        int smallest = i, left = 2 * i + 1, right = 2 * i + 2;
        if (left < size && data[left] < data[smallest]) smallest = left;
        if (right < size && data[right] < data[smallest]) smallest = right;
        if (smallest != i) {
            int t = data[i]; data[i] = data[smallest]; data[smallest] = t;
            siftDown(smallest);
        }
    }
}
```
Prefer Java's built-in `PriorityQueue<>` (min-heap by default) in practice.

## Common Mistakes
Assuming a heap is fully sorted (only the root is guaranteed extreme);
forgetting `PriorityQueue` is a min-heap by default.

## Common Interview Questions

Q: How do you find the k largest elements in a stream efficiently?

Answer:
Maintain a min-heap of size k, popping the minimum whenever it exceeds k
— O(n log k) overall.

Q: Why is building a heap from an array O(n) instead of O(n log n)?

Answer:
Sift-down cost is proportional to a node's height, and most nodes sit
near the bottom with small height.

## Follow-up Interview Questions

Q: How would you find the median of a running data stream?

Answer:
Balance a max-heap (smaller half) and min-heap (larger half); the median
comes from their tops in O(log n) per insert.

Q: How does a heap compare to a balanced BST for a priority queue?

Answer:
Both give O(log n) insert, but a heap adds O(1) peek while a BST adds
sorted traversal.

## How the Interviewer Thinks
Heap questions test whether you recognize "top-k" or "running median" as
heap-shaped, and bound the heap size to k rather than n.

## Production Engineering Perspective
Task schedulers use a min-heap keyed by "next run time" so only the
single next-due task is ever inspected.

## Common Misconceptions
"A heap is a sorted array" — only parent-child order is guaranteed.
"Peek and pop cost the same" — peek is O(1); pop is O(log n).

## Revision Notes
- Array-backed complete binary tree; children 2i+1, 2i+2; parent (i-1)/2.
- Insert = append + sift-up; extract = replace root + sift-down.
- Build-heap is O(n); single insert/extract is O(log n).

## Summary
A heap sacrifices full ordering for a cheap, array-based way to always
access the current minimum or maximum in O(log n).

---

# Trees

## What is it?
A hierarchical structure of nodes where each node has one parent (except
the root) and zero or more children, with no cycles.

## Why do we need it?
Many relationships are hierarchical (file systems, org charts, parsed
expressions) and benefit from hierarchical decomposition.

## What problem does it solve?
It solves representing and navigating nested relationships, underlying
faster-than-linear search and range queries.

## History (only if useful)
Formalizes 18th-19th century graph theory concepts, central to computing
through compiler parse trees.

## How does it work?
Each node stores a value and child references; traversal visits nodes in
a defined order (pre/in/post/level-order).

## Internal Working
A binary tree restricts each node to at most two children. DFS uses the
call stack; BFS uses a queue; balance keeps height O(log n).

## Step-by-Step Example
Pre-order (root, left, right) of:
```
        1
       / \
      2   3
     / \
    4   5
```
Order: 1, 2, 4, 5, 3.

## Visualization
```
        1
       / \
      2   3
Level order (BFS): 1 | 2 3
```

## Real-World Usage
File systems, HTML/XML DOM trees, and compiler ASTs.

## Advantages
Models hierarchy naturally; balanced variants give O(log n) search/
insert/delete.

## Disadvantages
An unbalanced tree degrades to O(n); recursive traversal risks stack
overflow on deep trees.

## Trade-offs
Recursive traversal is simpler; iterative traversal avoids call-stack
depth limits at the cost of verbosity.

## Alternatives
A flat array with index math (heaps) when the shape is always complete; a
graph when relationships aren't strictly hierarchical.

## Performance Analysis
Because most tree algorithms cost O(height), keeping height at O(log n)
is the biggest performance lever.

## Complexity Analysis (if applicable)
| Operation | Balanced | Degenerate |
|---|---|---|
| Search / Insert | O(log n) | O(n) |

## Implementation
```java
public final class BinaryTree {
    static class Node { int value; Node left, right; Node(int v) { value = v; } }

    public static List<Integer> levelOrder(Node root) { // iterative BFS
        List<Integer> out = new ArrayList<>();
        if (root == null) return out;
        Deque<Node> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            out.add(node.value);
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
        return out;
    }
}
```

## Common Mistakes
Confusing pre/in/post-order traversal order; forgetting the null-check
base case in recursion.

## Common Interview Questions

Q: What is the difference between pre-order, in-order, and post-order traversal?

Answer:
Pre-order suits serializing; in-order yields sorted output for a BST;
post-order suits bottom-up deletion or evaluation.

Q: How do you compute the height of a binary tree?

Answer:
Recursively, `1 + max(height(left), height(right))`, with an empty
subtree as height 0.

## Follow-up Interview Questions

Q: How would you check if a binary tree is balanced?

Answer:
Compute height recursively but return early once any subtree is
unbalanced, running the whole check in one O(n) pass.

Q: How do you serialize and deserialize a binary tree?

Answer:
Use pre-order traversal with a sentinel for nulls; deserialize by reading
tokens in the same order, rebuilding recursively.

## How the Interviewer Thinks
Tree questions test recursive thinking: can you define a node's answer in
terms of its children without recomputing subtrees?

## Production Engineering Perspective
Deeply nested or attacker-influenced tree structures are a real stack-
overflow risk, so recursion is often converted to an explicit stack.

## Common Misconceptions
"All trees are binary trees" — a general tree allows any number of
children. "A BST and binary tree are the same" — a BST adds ordering.

## Revision Notes
- Pre/in/post/level-order: know when each is the natural choice.
- Most tree algorithms are O(height); balance keeps height O(log n).
- Solve for a node using answers from its children.

## Summary
Trees generalize linear structures into hierarchies, and their recursive
nature carries directly into BSTs, tries, heaps, and segment trees.

---

# Binary Search Trees

## What is it?
A binary tree where every node's left subtree holds only smaller values
and its right subtree only larger, giving O(log n) search/insert/delete
when balanced.

## Why do we need it?
A sorted array gives O(log n) search but O(n) insert/delete; a BST gives
O(log n) for all three when balanced.

## What problem does it solve?
It solves maintaining a dynamically-changing, ordered collection with
fast search, insert, delete, and sorted traversal together.

## History (only if useful)
BSTs emerged in the 1960s; worst-case degeneration under sorted insertion
motivated AVL trees (1962) and red-black trees (1972).

## How does it work?
To search, compare the target to the current node: go left if smaller,
right if larger, stop at a match or null.

## Internal Working
Deleting a node with two children requires its in-order successor
(smallest value in the right subtree); without rebalancing, sorted
inserts produce an O(n)-height chain.

## Step-by-Step Example
Inserting 5, 3, 8, 1, 4:
```
        5
       / \
      3   8
     / \
    1   4
```

## Visualization
```
Search for 4:  5 (4<5, left) -> 3 (4>3, right) -> 4 (found)
```

## Real-World Usage
`TreeMap`/`TreeSet` in Java, leaderboards, and scheduling by timestamp.

## Advantages
O(log n) search/insert/delete when balanced; in-order traversal yields
sorted output for free.

## Disadvantages
Degenerates to O(n) height without balancing; two-children deletion is
more complex than a hash table's operations.

## Trade-offs
A plain BST is simple but risks worst-case O(n); a self-balancing BST
guarantees O(log n) at the cost of rotation logic.

## Alternatives
A `HashMap`/`HashSet` when sorted order isn't needed; a sorted array with
binary search for static, read-heavy data.

## Performance Analysis
AVL trees bound height near 1.44·log₂(n) (faster lookups); red-black
trees allow up to ~2·log₂(n) (fewer rotations).

## Complexity Analysis (if applicable)
| Operation | Balanced | Unbalanced (worst case) |
|---|---|---|
| Search / Insert / Delete | O(log n) | O(n) |

## Implementation
```java
public final class BST {
    static class Node { int value; Node left, right; Node(int v) { value = v; } }

    public static Node insert(Node node, int value) {
        if (node == null) return new Node(value);
        if (value < node.value) node.left = insert(node.left, value);
        else if (value > node.value) node.right = insert(node.right, value);
        return node;
    }

    public static Node delete(Node node, int value) {
        if (node == null) return null;
        if (value < node.value) { node.left = delete(node.left, value); return node; }
        if (value > node.value) { node.right = delete(node.right, value); return node; }
        if (node.left == null) return node.right;
        if (node.right == null) return node.left;
        Node successor = node.right;
        while (successor.left != null) successor = successor.left;
        node.value = successor.value;
        node.right = delete(node.right, successor.value);
        return node;
    }
}
```

## Common Mistakes
Assuming BST operations are always O(log n) despite unbalanced input;
mishandling the two-children delete case.

## Common Interview Questions

Q: How do you validate whether a binary tree is a valid BST?

Answer:
Recursively pass down a valid `(min, max)` range per node, since a deep
node must respect every ancestor's bound.

Q: How do you find the kth smallest element in a BST?

Answer:
Do an in-order traversal and stop at the kth node — O(h + k) with an
iterative stack.

## Follow-up Interview Questions

Q: Why do sorted-order insertions degrade a plain BST to O(n)?

Answer:
Increasing inserts always attach as the rightmost child, forming an
O(n)-height chain without self-balancing.

Q: How do you find the lowest common ancestor of two nodes in a BST?

Answer:
Recurse toward whichever side contains both targets; the first split
point is the LCA.

## How the Interviewer Thinks
BST questions probe whether you use the ordering invariant directly
rather than falling back to a generic tree algorithm.

## Production Engineering Perspective
Database engines generalize the BST idea into B-trees/B+-trees with high
fan-out to minimize disk-page reads.

## Common Misconceptions
"A BST always gives O(log n)" — only true when the tree stays balanced.

## Revision Notes
- Left < node < right, recursively, for every node.
- In-order traversal of a BST is always sorted.
- Delete with two children: replace with in-order successor.

## Summary
A BST combines a sorted array's ordering with a linked structure's
mutation flexibility, but only delivers O(log n) when balanced.

---

# Trie

## What is it?
A tree where each edge represents one character and every root-to-node
path spells a prefix, giving O(L) insert/search/prefix-check.

## Why do we need it?
A hash set answers exact-membership in O(L) too, but can't efficiently
answer "does any stored string start with this prefix?"

## What problem does it solve?
It solves prefix-based search — autocomplete, spell-check, IP routing —
where the query concerns a prefix, not exact membership.

## History (only if useful)
The name comes from "retrieval", coined by Edward Fredkin in 1960.

## How does it work?
Each node holds child pointers plus a boolean marking "a word ends here";
insert/search walk one character at a time.

## Internal Working
Insert creates a child node for any missing character and marks
`isEndOfWord` at the end. Search follows the same path, returning false
immediately if an edge is missing.

## Step-by-Step Example
Inserting "cat" and "car":
```
root -c-> c -a-> ca -t-> cat*
                    -r-> car*   (reuses "c" and "ca")
```

## Visualization
```
        root
         |c
         c-a-> ca
             /    \
            t*    r*
```

## Real-World Usage
Autocomplete systems, spell-checkers, and IP routing (longest prefix
match).

## Advantages
O(L) search/insert/prefix-check regardless of key count n; shares memory
across common prefixes.

## Disadvantages
Higher memory overhead than a hash set for sparse alphabets or few shared
prefixes.

## Trade-offs
An array-per-node trie gives O(1) child lookup at the cost of memory; a
hash-map-per-node trie saves memory for sparse branching.

## Alternatives
A sorted list with binary search for prefix ranges; a hash set for
exact-match-only lookups.

## Performance Analysis
Trie operations are O(L), independent of n, whereas a naive prefix search
over a hash set costs O(n·L).

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| Insert / search key of length L | O(L) |
| Prefix check | O(L) |

## Implementation
```java
public final class Trie {
    static class Node { Node[] children = new Node[26]; boolean isEndOfWord; }
    private final Node root = new Node();

    public void insert(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (current.children[idx] == null) current.children[idx] = new Node();
            current = current.children[idx];
        }
        current.isEndOfWord = true;
    }

    public boolean search(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (current.children[idx] == null) return false;
            current = current.children[idx];
        }
        return current.isEndOfWord;
    }
}
```

## Common Mistakes
Forgetting to mark/check `isEndOfWord`; assuming a fixed 26-entry array
works for arbitrary Unicode input.

## Common Interview Questions

Q: How is a trie different from a hash set for storing strings?

Answer:
A hash set answers exact-membership in O(L); a trie additionally answers
prefix queries in O(L).

Q: How would you implement autocomplete using a trie?

Answer:
Walk to the node for the typed prefix, then DFS its subtree collecting
complete words at each `isEndOfWord` node.

## Follow-up Interview Questions

Q: How would you reduce a trie's memory footprint for large alphabets?

Answer:
Use a `HashMap<Character, Node>` instead of a fixed array, and/or
compress single-child chains into one edge (a radix trie).

Q: How do you support wildcard search in a trie?

Answer:
Use backtracking: at a wildcard, try every non-null child; at a literal,
follow only the matching child.

## How the Interviewer Thinks
They look for recognizing "prefix" as the trigger for a trie instead of a
hash set by default.

## Production Engineering Perspective
Router forwarding tables use trie-like structures for longest-prefix-
match routing, and search engines use them for autocomplete backends.

## Common Misconceptions
"A trie is always more memory-efficient than a hash set" — not for data
with little shared prefix structure.

## Revision Notes
- Each edge = one character; root-to-node path = a prefix.
- O(L) insert/search/prefix-check, independent of key count n.
- `isEndOfWord` distinguishes "prefix exists" from "word exists".

## Summary
A trie trades some memory for O(L) prefix-aware operations a hash set
cannot provide, the go-to structure for autocomplete and routing.

---

# Graphs

## What is it?
Vertices connected by edges, modeling pairwise relationships; edges may
be directed/undirected, weighted/unweighted.

## Why do we need it?
Trees can't model cycles or multiple paths; graphs generalize to any
network structure.

## What problem does it solve?
It solves representing networks — roads, social ties, dependencies —
that aren't strictly hierarchical.

## History (only if useful)
Graph theory began with Euler's 1736 Königsberg bridges solution.

## How does it work?
Represented as an adjacency list or matrix; traversed via BFS (queue) or
DFS (stack/recursion).

## Internal Working
BFS explores level-by-level, finding shortest paths in unweighted graphs;
DFS explores depth-first, useful for cycle detection and topological
sort.

## Step-by-Step Example
BFS from A in `A-B, A-C, B-D`:
```
Visit A -> queue[B,C] -> visit B -> queue[C,D] -> visit C,D
```

## Visualization
```
A --- B      DFS/BFS both explore
|     |      via these edges
C --- D
```

## Real-World Usage
Social networks, road/flight routing, dependency resolution, and
recommendations.

## Advantages
Models arbitrary relationships including cycles; BFS/DFS/Dijkstra cover
most connectivity problems.

## Disadvantages
Adjacency matrix costs O(V²) space; correctness is easy to get subtly
wrong.

## Trade-offs
Adjacency list saves space for sparse graphs; matrix gives O(1) edge
lookup at O(V²) space cost.

## Alternatives
A tree for strictly hierarchical data; a hash map for pairwise lookup
without traversal.

## Performance Analysis
BFS/DFS run in O(V+E); Dijkstra with a heap runs in O((V+E) log V).

## Complexity Analysis (if applicable)
| Algorithm | Time |
|---|---|
| BFS / DFS | O(V+E) |
| Dijkstra (heap) | O((V+E) log V) |

## Implementation
```java
public final class Graph {
    private final Map<Integer, List<Integer>> adj = new HashMap<>();
    public void addEdge(int u, int v) {
        adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
        adj.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
    }
    public List<Integer> bfs(int start) {
        List<Integer> order = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(start); visited.add(start);
        while (!queue.isEmpty()) {
            int node = queue.poll();
            order.add(node);
            for (int next : adj.getOrDefault(node, List.of())) {
                if (visited.add(next)) queue.add(next);
            }
        }
        return order;
    }
}
```

## Common Mistakes
Forgetting to mark nodes visited before enqueuing; not handling
disconnected components.

## Common Interview Questions

Q: When would you use BFS versus DFS?

Answer:
BFS for shortest path (unweighted) or level order; DFS for cycle
detection or topological sort.

Q: How does Dijkstra's algorithm find shortest paths?

Answer:
It greedily expands the closest unvisited vertex via a min-heap — O((V+E)
log V), fails with negative weights.

## Follow-up Interview Questions

Q: How would you detect a cycle in a directed graph?

Answer:
DFS while tracking the current recursion path; a back-edge into that
path means a cycle.

Q: How do you perform a topological sort?

Answer:
Kahn's algorithm: repeatedly remove in-degree-zero nodes, or use
DFS post-order reversed.

## How the Interviewer Thinks
They check you pick the right traversal for the question and handle
disconnected/cyclic graphs.

## Production Engineering Perspective
Build tools and package managers use topological sort and cycle detection
to catch circular dependencies.

## Common Misconceptions
"BFS always finds shortest path" — only unweighted graphs; weighted needs
Dijkstra/Bellman-Ford.

## Revision Notes
- Adjacency list for sparse graphs; matrix for dense/O(1) lookup.
- BFS = shortest path + level order; DFS = cycles + topological sort.
- Dijkstra needs non-negative weights.

## Summary
Graphs generalize trees to arbitrary relationships; picking BFS, DFS, or
a shortest-path algorithm based on the question is the key skill.

---

# Union Find

## What is it?
A structure tracking disjoint sets, supporting `find` and `union` near
O(1).

## Why do we need it?
Checking connectivity via graph traversal after every merge is too slow;
union-find answers both near-instantly.

## What problem does it solve?
It solves dynamic connectivity as unions occur over time.

## History (only if useful)
Path compression and union by rank (Tarjan) established its near-constant
bound in the 1970s.

## How does it work?
Each set is a tree; `find` walks to the root, `union` attaches one root
under another.

## Internal Working
Path compression repoints every visited node directly to the root; union
by rank attaches the smaller tree under the larger.

## Step-by-Step Example
```
union(1,2): parent[1]=2
union(2,3): parent[2]=3
find(1) -> compresses to parent[1]=3
```

## Visualization
```
Before: 1  2  3   After: 3
                        / \
                       2   1
```

## Real-World Usage
Kruskal's MST, cycle detection, and network connectivity queries.

## Advantages
Near-O(1) amortized `find`/`union` with both optimizations.

## Disadvantages
No `split` operation; stores only connectivity, not full edges.

## Trade-offs
Both optimizations together give the best bound; either alone still
works, just slower.

## Alternatives
DFS/BFS connectivity checks for a static, once-queried graph.

## Performance Analysis
With both optimizations, operations run in O(α(n)) amortized — near
constant for any n.

## Complexity Analysis (if applicable)
| Optimization | Time |
|---|---|
| Neither | O(n) worst case |
| Both | O(α(n)) amortized |

## Implementation
```java
public final class UnionFind {
    private final int[] parent, rank;
    public UnionFind(int n) {
        parent = new int[n]; rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }
    public void union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;
        if (rank[ra] == rank[rb]) rank[ra]++;
    }
}
```

## Common Mistakes
Skipping path compression or union by rank, degrading to O(n) chains.

## Common Interview Questions

Q: How does union-find detect a cycle in an undirected graph?

Answer:
Check if both endpoints already share a root before adding an edge; if
so, it creates a cycle.

Q: What is path compression and why does it matter?

Answer:
It repoints every node visited during `find` to the root, flattening
future lookups.

## Follow-up Interview Questions

Q: How is union-find used in Kruskal's MST algorithm?

Answer:
Sort edges by weight, adding each only if endpoints differ in `find`,
then union them.

Q: Why is the amortized complexity O(α(n)) rather than O(1)?

Answer:
The inverse Ackermann function grows so slowly it's under 5 for any
practical n, but isn't formally constant.

## How the Interviewer Thinks
They check you know both optimizations and connect union-find to
Kruskal's algorithm.

## Production Engineering Perspective
Distributed systems use union-find-like structures to track connected
components in changing network graphs.

## Common Misconceptions
"Union-find gives strict O(1)" — it's amortized O(α(n)), not truly
constant.

## Revision Notes
- `find` = walk to root (compression); `union` = attach smaller under
  larger (rank).
- Both optimizations together give O(α(n)) amortized.
- Classic uses: Kruskal's MST, cycle detection.

## Summary
Union-find answers connectivity and merge queries in near-constant
amortized time, the backbone of Kruskal's algorithm.

---

# Greedy Algorithms

## What is it?
A strategy making the locally optimal choice at each step, aiming for a
globally optimal solution.

## Why do we need it?
Exploring every combination is often exponential; greedy exploits
structure to be much faster.

## What problem does it solve?
It solves optimization problems with a provable greedy-choice property
and optimal substructure.

## History (only if useful)
Formalized via matroid theory and early algorithms (Kruskal 1956, Huffman
1952).

## How does it work?
Sort or prioritize choices by some criterion, then commit to each
best-looking option without reconsidering.

## Internal Working
Correctness requires proving the greedy-choice property (a locally
optimal first choice belongs to some global optimum) and optimal
substructure.

## Step-by-Step Example
Activity selection (sorted by end time) `[1,3],[2,4],[3,5]`:
```
Pick [1,3] (earliest end) -> next start>=3 -> pick [3,5]
```

## Visualization
```
[1---3]
   [2---4]
      [3---5]   Selected: [1-3],[3-5]
```

## Real-World Usage
Job scheduling, Huffman compression, and Kruskal's/Prim's MST.

## Advantages
Typically O(n log n), simple once the criterion is known.

## Disadvantages
Not universally correct — many problems need DP instead.

## Trade-offs
Greedy trades completeness for speed, valid only when proven safe.

## Alternatives
Dynamic programming when greedy fails to guarantee optimality.

## Performance Analysis
Most greedy algorithms are dominated by an initial O(n log n) sort.

## Complexity Analysis (if applicable)
| Problem | Time |
|---|---|
| Activity selection | O(n log n) |
| Huffman coding | O(n log n) |

## Implementation
```java
public final class ActivitySelection {
    public static int maxActivities(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[1]));
        int count = 0, lastEnd = Integer.MIN_VALUE;
        for (int[] interval : intervals) {
            if (interval[0] >= lastEnd) { count++; lastEnd = interval[1]; }
        }
        return count;
    }
}
```

## Common Mistakes
Applying greedy without proving correctness; sorting by the wrong key.

## Common Interview Questions

Q: Why does sorting by end time work for activity selection?

Answer:
Picking the activity that frees the resource soonest leaves the most
room for future choices.

Q: When does greedy fail to find the optimal solution?

Answer:
When no exchange argument holds, e.g. 0/1 knapsack, where DP is needed
instead.

## Follow-up Interview Questions

Q: How would you prove a greedy algorithm is correct?

Answer:
Use an exchange argument: show any optimum can include the greedy choice
without getting worse.

Q: How does Huffman coding use a greedy strategy?

Answer:
Repeatedly merge the two lowest-frequency nodes via a min-heap, so rare
symbols end up deepest.

## How the Interviewer Thinks
They probe whether you can justify correctness, not just propose a rule.

## Production Engineering Perspective
Load balancers use greedy heuristics (shortest job first) for low-latency
decisions.

## Common Misconceptions
"Greedy always works if simple" — simplicity doesn't imply correctness.

## Revision Notes
- Requires greedy-choice property + optimal substructure — prove it.
- Usually O(n log n) via an initial sort.
- Correct uses: activity selection, Huffman; failure: 0/1 knapsack.

## Summary
Greedy algorithms trade exhaustive guarantees for speed, valid only when
the greedy-choice property is proven for the problem.

---

# Backtracking

## What is it?
A trial-and-error search that builds a solution incrementally and
abandons a partial one once it can't succeed.

## Why do we need it?
Brute-force enumeration wastes time on paths already known to fail;
backtracking prunes them early.

## What problem does it solve?
It solves combinatorial search — permutations, subsets, constraint
satisfaction.

## History (only if useful)
The term was coined by D. H. Lehmer in the 1950s.

## How does it work?
Recursively choose an option, recurse, then undo it before trying the
next.

## Internal Working
Each recursive call is a decision point; "choose, explore, un-choose"
ensures shared state is restored between branches.

## Step-by-Step Example
Subsets of `[1,2]`:
```
[] -> choose 1 -> [1] -> choose 2 -> [1,2] -> backtrack -> []
   -> choose 2 -> [2] -> backtrack -> []
```

## Visualization
```
    []
   /  \
 [1]  [2]
  |
[1,2]
```

## Real-World Usage
Sudoku/N-Queens solvers and constraint-satisfaction scheduling.

## Advantages
Explores only promising paths via pruning.

## Disadvantages
Worst-case exponential when pruning is ineffective.

## Trade-offs
More pruning cuts runtime but adds per-node overhead.

## Alternatives
Dynamic programming when subproblems overlap.

## Performance Analysis
Runtime depends on branching factor and how early invalid branches are
pruned.

## Complexity Analysis (if applicable)
| Problem | Time |
|---|---|
| Subsets of n | O(2^n) |
| Permutations of n | O(n!) |

## Implementation
```java
public final class Subsets {
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(nums, 0, new ArrayList<>(), result);
        return result;
    }
    private static void backtrack(int[] nums, int start, List<Integer> path,
                                   List<List<Integer>> result) {
        result.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            backtrack(nums, i + 1, path, result);
            path.remove(path.size() - 1); // un-choose
        }
    }
}
```

## Common Mistakes
Forgetting to un-choose after recursing; storing a live reference instead
of a snapshot.

## Common Interview Questions

Q: How is backtracking different from plain brute force?

Answer:
It abandons a branch the moment a constraint is violated, unlike brute
force which builds every combination fully.

Q: How would you solve N-Queens with backtracking?

Answer:
Place queens row by row, checking conflicts before each placement, and
backtrack when no column is valid.

## Follow-up Interview Questions

Q: How can you speed up backtracking for Sudoku or N-Queens?

Answer:
Add constraint propagation (bitsets for used columns/diagonals) to reject
invalid placements in O(1).

Q: When does backtracking overlap with dynamic programming?

Answer:
When subproblems repeat and only the optimum matters, memoizing turns it
into polynomial DP.

## How the Interviewer Thinks
They check "choose, explore, un-choose" is implemented correctly, with
pruning, not just brute force.

## Production Engineering Perspective
Constraint solvers (e.g. dependency resolution) use backtracking with
heavy pruning among combinatorial options.

## Common Misconceptions
"Backtracking is always exponential" — effective pruning often makes it
fast in practice.

## Revision Notes
- Pattern: choose -> explore (recurse) -> un-choose.
- Always snapshot (copy), never store a live reference.
- Add pruning as early as possible to cut branches.

## Summary
Backtracking explores a solution space while pruning invalid branches
early, and "choose, explore, un-choose" underlies most such problems.

---

# Dynamic Programming

## What is it?
A technique solving problems by breaking them into overlapping
subproblems, solving each once, and reusing results.

## Why do we need it?
Naive recursion on overlapping subproblems recomputes the same values
exponentially many times.

## What problem does it solve?
It solves optimization/counting problems with optimal substructure and
overlapping subproblems.

## History (only if useful)
Coined by Richard Bellman in the 1950s.

## How does it work?
Define a recurrence relating a problem to smaller subproblems, computed
top-down (memoized) or bottom-up (tabulated).

## Internal Working
Top-down caches each subproblem's result keyed by its parameters;
bottom-up fills a table from base cases upward.

## Step-by-Step Example
Fibonacci bottom-up, `fib(5)`:
```
dp[0]=0, dp[1]=1
dp[2]=1, dp[3]=2, dp[4]=3, dp[5]=5
```

## Visualization
```
dp: [0, 1, 1, 2, 3, 5]
      each cell = sum of prior two
```

## Real-World Usage
Sequence alignment, edit distance, and route optimization.

## Advantages
Converts exponential brute force into polynomial time/space.

## Disadvantages
Requires identifying the correct state, which can be non-obvious.

## Trade-offs
Top-down is easier to derive; bottom-up avoids recursion overhead.

## Alternatives
Greedy when a valid greedy-choice property exists.

## Performance Analysis
Time is (distinct states) × (work per state); space often shrinks via a
rolling array.

## Complexity Analysis (if applicable)
| Problem | Time | Space |
|---|---|---|
| Fibonacci | O(n) | O(1) rolling |
| 0/1 Knapsack | O(n·W) | O(W) rolling |

## Implementation
```java
public final class Knapsack {
    public static int maxValue(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < weights.length; i++) {
            for (int w = capacity; w >= weights[i]; w--) { // reverse: no reuse
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }
        return dp[capacity];
    }
}
```

## Common Mistakes
Missing a needed state dimension; iterating capacity forward in 0/1
knapsack (allows item reuse).

## Common Interview Questions

Q: How do you decide between top-down and bottom-up DP?

Answer:
Top-down suits sparse recursive structure; bottom-up avoids recursion
overhead when every state is needed.

Q: What is the difference between optimal substructure and overlapping subproblems?

Answer:
Optimal substructure builds a solution from subproblem optima;
overlapping subproblems recur, making caching worthwhile.

## Follow-up Interview Questions

Q: How would you reduce the space complexity of a 2D DP solution?

Answer:
If a row only depends on the previous row, keep just that row instead of
the full table.

Q: How does memoization differ from tabulation in practice?

Answer:
Memoization computes only needed subproblems via a cache; tabulation
computes every state in fixed order.

## How the Interviewer Thinks
They check you define the state and recurrence explicitly, not just
pattern-match a memorized solution.

## Production Engineering Perspective
Databases use DP-like memoization for query plan selection, caching
partial plan costs.

## Common Misconceptions
"DP always needs a 2D table" — many problems reduce to O(1) or O(n) space
via rolling arrays.

## Revision Notes
- Requires optimal substructure + overlapping subproblems.
- Define state and recurrence first, then pick top-down or bottom-up.
- Watch loop direction in knapsack-style DP.

## Summary
Dynamic programming eliminates redundant recomputation via overlapping
subproblems and optimal substructure; defining the state correctly is
the real skill.

---

# Segment Tree

## What is it?
A binary tree over an array storing range aggregates, enabling O(log n)
range queries and updates.

## Why do we need it?
Prefix sums answer range-sum in O(1) but need O(n) per update; a segment
tree balances both to O(log n).

## What problem does it solve?
It solves range queries (sum, min, max) over an array that also needs
frequent updates.

## History (only if useful)
Emerged from 1970s computational geometry research.

## How does it work?
Each leaf is one array element; each internal node stores its children's
combined aggregate.

## Internal Working
A query recurses, combining only nodes whose range overlaps the query,
visiting O(log n) nodes; an update walks one root-to-leaf path.

## Step-by-Step Example
Sum segment tree over `[1,3,5,7]`:
```
          [16]
         /    \
       [4]     [12]
```

## Visualization
```
Query sum(1,2) -> combines node[3] and node[5] -> 8
```

## Real-World Usage
Range-sum dashboards and competitive-programming range queries.

## Advantages
O(log n) for both range query and update.

## Disadvantages
More complex than a prefix-sum array; uses ~4n space.

## Trade-offs
Segment trees balance query/update speed; rare updates favor a simpler
prefix-sum array.

## Alternatives
A Fenwick tree for simpler, more memory-efficient sum-style updates.

## Performance Analysis
Build is O(n); every query/update is O(log n).

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| Build | O(n) |
| Range query / point update | O(log n) |

## Implementation
```java
public final class SegmentTree {
    private final int[] tree; private final int n;
    public SegmentTree(int[] arr) {
        n = arr.length; tree = new int[4 * n];
        build(arr, 1, 0, n - 1);
    }
    private void build(int[] arr, int node, int lo, int hi) {
        if (lo == hi) { tree[node] = arr[lo]; return; }
        int mid = (lo + hi) / 2;
        build(arr, 2 * node, lo, mid);
        build(arr, 2 * node + 1, mid + 1, hi);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }
    public int query(int node, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return 0;
        if (l <= lo && hi <= r) return tree[node];
        int mid = (lo + hi) / 2;
        return query(2 * node, lo, mid, l, r) + query(2 * node + 1, mid + 1, hi, l, r);
    }
}
```

## Common Mistakes
Under-sizing the array (needs 4n); mixing inclusive/exclusive bounds.

## Common Interview Questions

Q: Why is a segment tree preferred over prefix sum when updates occur?

Answer:
Prefix sum needs O(n) to rebuild; a segment tree updates one path in
O(log n).

Q: How does a range query traverse the tree?

Answer:
It recurses, skipping no-overlap nodes, returning full-overlap nodes
directly, and combining partial ones — O(log n) nodes visited.

## Follow-up Interview Questions

Q: How would you support range updates efficiently?

Answer:
Add lazy propagation: store a pending update at a node and apply it only
when that subtree is next visited.

Q: How does a segment tree generalize beyond sums?

Answer:
Replace the combine step with any associative operation like min, max, or
GCD.

## How the Interviewer Thinks
They check you understand why segment trees beat brute force and prefix
sums, and which nodes a query visits.

## Production Engineering Perspective
Time-series systems use segment-tree-like structures for windowed
aggregate queries.

## Common Misconceptions
"Segment trees only support sums" — any associative operation works.

## Revision Notes
- Each node = aggregate of a range; leaves = single elements.
- Build O(n); query/update O(log n).
- Generalizes to any associative operation, not just sum.

## Summary
A segment tree gives balanced O(log n) range queries and updates via
precomputed aggregates over a binary partition of the array.

---

# Fenwick Tree

## What is it?
A Binary Indexed Tree (BIT); a compact array supporting O(log n)
prefix-sum queries and updates via bit tricks.

## Why do we need it?
A segment tree solves the same problem with more code and ~4n space; a
Fenwick tree needs just n+1 space.

## What problem does it solve?
It solves prefix-sum queries and point updates more simply than a segment
tree.

## History (only if useful)
Introduced by Peter Fenwick in 1994 for cumulative frequency tables.

## How does it work?
Each index stores a partial sum covering a range from its lowest set bit;
update/query jump via `i + (i&-i)` or `i - (i&-i)`.

## Internal Working
`i & -i` isolates the lowest set bit, giving that index's range size.
Update propagates upward; query accumulates downward.

## Step-by-Step Example
Updating index 3 (+5) in a size-8 tree:
```
i=3 -> update tree[3] -> i+=(3&-3)=1 -> i=4 -> update tree[4] -> i=8
```

## Visualization
```
Index:   1  2  3  4  5  6  7  8
Covers: [1][1-2][3][1-4][5][5-6][7][1-8]
```

## Real-World Usage
Cumulative frequency tables and counting inversions.

## Advantages
O(log n) update/query with only O(n) space, no tree nodes needed.

## Disadvantages
Only naturally supports invertible aggregates like sum.

## Trade-offs
Fenwick trees trade generality (segment trees support any op plus lazy
updates) for simplicity.

## Alternatives
A segment tree when min/max or range updates with lazy propagation are
needed.

## Performance Analysis
Update and prefix query both run in O(log n) using bitwise arithmetic
only.

## Complexity Analysis (if applicable)
| Operation | Time | Space |
|---|---|---|
| Update / prefix query | O(log n) | O(n) |

## Implementation
```java
public final class FenwickTree {
    private final int[] tree; private final int n;
    public FenwickTree(int size) { n = size; tree = new int[n + 1]; }

    public void update(int i, int delta) {
        for (; i <= n; i += i & (-i)) tree[i] += delta;
    }

    public int prefixSum(int i) {
        int sum = 0;
        for (; i > 0; i -= i & (-i)) sum += tree[i];
        return sum;
    }

    public int rangeSum(int l, int r) { return prefixSum(r) - prefixSum(l - 1); }
}
```

## Common Mistakes
Using 0-based indexing (requires 1-based); forgetting `rangeSum` needs
two calls.

## Common Interview Questions

Q: How does a Fenwick tree achieve O(log n) with just an array?

Answer:
Each index covers a range sized by its lowest set bit, so update/query
only touch O(log n) indices.

Q: How do you compute a range sum with a Fenwick tree?

Answer:
`prefixSum(r) - prefixSum(l - 1)`, since prefix sums are its native
operation.

## Follow-up Interview Questions

Q: How would you use a Fenwick tree to count inversions?

Answer:
Process elements by rank, querying counts less than the current element,
then updating — O(n log n) total.

Q: Why must Fenwick tree indices be 1-based?

Answer:
`i & -i` has no valid meaning at index 0, so 1-based indexing avoids that
undefined case.

## How the Interviewer Thinks
They check you understand the bit-trick mechanics, not just memorize the
code.

## Production Engineering Perspective
Analytics systems favor Fenwick trees for simple, memory-efficient
cumulative counters.

## Common Misconceptions
"Fenwick and segment trees are the same" — Fenwick only handles
invertible aggregates like sum.

## Revision Notes
- 1-based indexing is mandatory; `i & -i` isolates the lowest set bit.
- Update: `i += i & -i`; query: `i -= i & -i`.
- Simpler and more compact than a segment tree, but sum-only.

## Summary
A Fenwick tree uses a bit trick to match segment-tree performance with
far less code, at the cost of only supporting invertible aggregates.

---

# Bit Manipulation

## What is it?
Directly operating on binary bits using AND, OR, XOR, NOT, and shifts.

## Why do we need it?
Bitwise operations run in O(1) hardware time and can replace slower
arithmetic or extra memory.

## What problem does it solve?
It solves flag/boolean-set problems and numeric tricks more efficiently
than general structures.

## History (only if useful)
Rooted in Boolean algebra (George Boole, 1854).

## How does it work?
Each operator acts independently per bit position: AND needs both bits
set, OR needs either, XOR needs exactly one.

## Internal Working
`n & (n-1)` clears the lowest set bit; `n & -n` isolates it; XOR-ing a
number with itself yields 0, cancelling duplicates.

## Step-by-Step Example
Counting set bits in `13` (`1101`):
```
13 & 12 = 12, count=1
12 & 11 = 8,  count=2
8  & 7  = 0,  count=3
```

## Visualization
```
5=0101  3=0011
5&3=0001  5|3=0111  5^3=0110
```

## Real-World Usage
Permission bitmasks, hash mixing, and subnet masks.

## Advantages
O(1) per operation, minimal memory for boolean flags.

## Disadvantages
Bit tricks are harder to read than explicit boolean logic.

## Trade-offs
Bit tricks trade readability for speed and compactness.

## Alternatives
A `boolean[]` or `BitSet` when readability matters more than performance.

## Performance Analysis
Bitwise ops execute as single CPU instructions, faster than loop-based
equivalents.

## Complexity Analysis (if applicable)
| Operation | Time |
|---|---|
| AND / OR / XOR / shift | O(1) |
| Count set bits | O(set bits) |

## Implementation
```java
public final class BitTricks {
    public static int countSetBits(int n) {
        int count = 0;
        while (n != 0) { n &= (n - 1); count++; } // clears lowest set bit
        return count;
    }

    public static int singleNumber(int[] nums) { // all appear twice but one
        int result = 0;
        for (int num : nums) result ^= num;
        return result;
    }
}
```

## Common Mistakes
Using `^` when `&`/`|` was intended; forgetting `&` binds looser than
`==` in Java.

## Common Interview Questions

Q: How do you count the number of set bits in an integer?

Answer:
Apply `n &= (n-1)` repeatedly (Kernighan's trick) and count iterations
until n is 0.

Q: How do you find the single non-duplicate number when others appear twice?

Answer:
XOR all numbers; paired duplicates cancel to 0, leaving the unique value.

## Follow-up Interview Questions

Q: How do you check if a number is a power of two?

Answer:
`n > 0 && (n & (n-1)) == 0`, since a power of two has exactly one set
bit.

Q: How would you swap two integers without a temporary variable?

Answer:
`a ^= b; b ^= a; a ^= b;` — a curiosity, less readable than a temp
variable in practice.

## How the Interviewer Thinks
They check you know a few canonical bit tricks and can explain why they
work.

## Production Engineering Perspective
Feature-flag systems pack many booleans into one integer bitmask for
compact storage.

## Common Misconceptions
"`>>` and `>>>` are the same in Java" — `>>` is arithmetic, `>>>` is
logical (unsigned).

## Revision Notes
- AND/OR/XOR/shift all O(1); Kernighan's trick clears the lowest set bit.
- XOR cancels pairs — useful for "find the unique element".
- Java: `>>` arithmetic shift, `>>>` logical shift.

## Summary
Bit manipulation exploits O(1) hardware operations for flag storage and
tricks like counting set bits or finding a unique element via XOR.
