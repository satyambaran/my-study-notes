# DSA Interview Cheat Sheet

A standalone, rapid-recall companion for coding rounds — no other file
required. It skips first-principles explanations (see
`parts/part-01-data-structures-and-algorithms.md` for those) and packs
complexity, operation-cost tables, pattern cues, clean Java templates,
edge cases, and "avoid this when" guidance throughout.

**Legend:** `n`/`m` input sizes, `V`/`E` graph vertices/edges, `L` key
length, `k` window/heap/answer size, `α(n)` inverse Ackermann (effectively
constant). Snippets assume `import java.util.*;` (plus
`import java.util.function.*;` for `IntPredicate`). Helper classes
(`ListNode`, `TreeNode`, `Trie`, `DSU`, `Fenwick`, `SegmentTree`) are
defined once per section and reused later in it.

## Table of Contents

1. [How to Use This Sheet](#how-to-use-this-sheet)
2. [Complexity Reference](#complexity-reference)
3. [Data Structure Operation Costs](#data-structure-operation-costs)
4. [Pattern Recognition Map](#pattern-recognition-map)
5. [Arrays and Strings](#arrays-and-strings)
6. [Hashing](#hashing)
7. [Two Pointers](#two-pointers)
8. [Sliding Window](#sliding-window)
9. [Prefix Sums](#prefix-sums)
10. [Binary Search Variants](#binary-search-variants)
11. [Linked Lists](#linked-lists)
12. [Stacks and Queues](#stacks-and-queues)
13. [Heaps](#heaps)
14. [Trees, BST and Trie](#trees-bst-and-trie)
15. [Graphs](#graphs)
16. [Union-Find](#union-find)
17. [Greedy](#greedy)
18. [Backtracking](#backtracking)
19. [Dynamic Programming](#dynamic-programming)
20. [Segment Tree and Fenwick Tree](#segment-tree-and-fenwick-tree)
21. [Bit Manipulation](#bit-manipulation)
22. [Debugging Checklist](#debugging-checklist)
23. [Rapid-Fire Q&A](#rapid-fire-qa)
24. [One-Page Recall](#one-page-recall)

---

## How to Use This Sheet

- Skim the **Pattern Recognition Map** first; match a keyword to a row,
  then jump to that section.
- Every pattern section shares one shape: **Recognize It**, **Template**,
  **Edge Cases**, **Avoid When** — read in that order under time pressure.
- Templates are minimal: copy, rename, adapt. Clarity beats
  micro-optimization.
- Before coding, state a target complexity from the **Complexity
  Reference** table matched to the input size.

---

## Complexity Reference

**Growth order (fastest to slowest):**
`O(1) < O(log n) < O(√n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2ⁿ) < O(n!)`

**Input size → safe complexity** (rule of thumb for a ~10⁸ operations/second
budget and a ~1-2 second limit):

| Constraint on `n` | Safe complexity |
|---|---|
| n ≤ 10-12 | O(n!), O(2ⁿ·n) |
| n ≤ 20-25 | O(2ⁿ) |
| n ≤ 100-500 | O(n³) |
| n ≤ 2,000-10,000 | O(n²) |
| n ≤ 10⁵-10⁶ | O(n log n) |
| n ≤ 10⁷-10⁸ | O(n) or O(n·α(n)) |
| n > 10⁸, or huge value ranges | O(log n) or O(1) |

**Master theorem** for `T(n) = a·T(n/b) + f(n)` — compare `f(n)` to `n^(log_b a)`:

- `f(n)` grows slower → `T(n) = Θ(n^(log_b a))`
- `f(n)` grows equally fast → `T(n) = Θ(n^(log_b a) · log n)`
- `f(n)` grows faster (regularity condition) → `T(n) = Θ(f(n))`

Example: merge sort `T(n) = 2T(n/2) + O(n)` → `O(n log n)`.

**Amortized cost notes:**

- Dynamic array append: O(1) amortized — doubling bounds total copy cost
  to O(n) across n inserts.
- `HashMap` get/put: O(1) average; Java 8+ treeifies buckets over 8
  entries, capping worst case at O(log n).
- Union-Find with path compression + union by rank: O(α(n)), practically
  O(1).
- `StringBuilder.append`: O(1) amortized vs. `String +=` in a loop: O(n²)
  total.

---

## Data Structure Operation Costs

**Linear structures:**

| Structure | Access | Search | Insert | Delete | Notes |
|---|---|---|---|---|---|
| Array / ArrayList | O(1) | O(n) | O(n), append O(1)* | O(n) | Contiguous, cache-friendly |
| Singly/Doubly Linked List | O(n) | O(n) | O(1) at known node | O(1) at known node | O(n) to find the node; doubly adds `prev` |
| Stack (ArrayDeque) | O(n) | O(n) | O(1) push | O(1) pop | LIFO |
| Queue (ArrayDeque) | O(n) | O(n) | O(1) offer | O(1) poll | FIFO |
| Deque (ArrayDeque) | O(1) ends | O(n) | O(1) either end* | O(1) either end* | Amortized |

\* amortized due to periodic resizing.

**Associative and ordered structures:**

| Structure | Access | Search | Insert | Delete | Notes |
|---|---|---|---|---|---|
| HashMap / HashSet | — | O(1) avg, O(log n) worst | O(1) avg | O(1) avg | Unordered; needs `hashCode`/`equals` |
| LinkedHashMap | — | O(1) avg | O(1) avg | O(1) avg | Preserves insertion/access order |
| TreeMap / TreeSet | — | O(log n) | O(log n) | O(log n) | Sorted; red-black tree |
| PriorityQueue (heap) | O(1) peek | O(n) | O(log n) | O(log n) | Binary heap, array-backed |

**Advanced structures:**

| Structure | Build | Query | Update | Notes |
|---|---|---|---|---|
| Trie | O(total chars) | O(L) | O(L) | `L` = key length |
| Union-Find | O(n) | O(α(n)) find | O(α(n)) union | Near O(1) amortized |
| Segment Tree | O(n) | O(log n) | O(log n) point, O(log n) range w/ lazy | Any associative combiner |
| Fenwick Tree (BIT) | O(n log n) naive, O(n) linear build | O(log n) | O(log n) | Half a segment tree's memory |
| Balanced BST (AVL/RB) | O(n log n) | O(log n) | O(log n) | Guaranteed worst case unlike a plain BST |

---

## Pattern Recognition Map

| Cue in the problem statement | Likely pattern |
|---|---|
| "sorted array", find a pair/triplet summing to a target | Two pointers |
| "contiguous subarray/substring", longest/shortest/max under a constraint | Sliding window |
| "number of subarrays with sum equal to k", static range-sum queries | Prefix sums |
| "search in a sorted/rotated array", "smallest x such that…" | Binary search (on answer) |
| "kth largest/smallest", "top k frequent" | Heap (or quickselect) |
| "next greater/smaller element", histogram area | Monotonic stack |
| "detect a cycle" in a linked list, "find the middle node" | Fast/slow pointers |
| "all subsets/permutations/combinations", "generate every valid…" | Backtracking |
| count ways / reachability / partition with overlapping subproblems | Dynamic programming |
| shortest path, unweighted or equal-cost graph/grid | BFS |
| shortest path, weighted, non-negative edges | Dijkstra |
| shortest path, negative edges allowed | Bellman-Ford |
| connectivity, provinces/islands, undirected-graph cycle | Union-Find |
| "course schedule", dependency resolution | Topological sort |
| range sum/min/max queries on a **mutable** array | Fenwick/segment tree |
| prefix matching, autocomplete, "word dictionary" | Trie |
| "meeting rooms", "merge intervals", interval scheduling | Greedy (sort by start/end) |
| count set bits, XOR tricks, "single number", subset masks | Bit manipulation |
| frequency counting, duplicates, grouping by a derived key | Hashing |

---

## Arrays and Strings

**Recognize it when:** the problem is index-driven, in-place, or needs
rearranging/partitioning a fixed collection.

```java
// Reverse a[lo..hi] in place — O(n), O(1) space
static void reverse(int[] a, int lo, int hi) {
    while (lo < hi) {
        int tmp = a[lo]; a[lo++] = a[hi]; a[hi--] = tmp;
    }
}

// Rotate right by k via 3-reversal trick — O(n), O(1)
static void rotateRight(int[] nums, int k) {
    int n = nums.length;
    k %= n;
    reverse(nums, 0, n - 1);
    reverse(nums, 0, k - 1);
    reverse(nums, k, n - 1);
}

// Dutch National Flag: sort 0/1/2 in one pass — O(n), O(1)
static void sortColors(int[] a) {
    int low = 0, mid = 0, high = a.length - 1;
    while (mid <= high) {
        if (a[mid] == 0) { int t = a[low]; a[low] = a[mid]; a[mid] = t; low++; mid++; }
        else if (a[mid] == 1) mid++;
        else { int t = a[mid]; a[mid] = a[high]; a[high] = t; high--; }
    }
}
```

**Edge cases:** empty array, single element, `k == 0`/`k == n` rotation,
all-duplicate values, negatives, sum overflow — use `long` past
`2^31 - 1`.

**Avoid when:** reaching for nested `O(n²)` loops for pairs/frequencies —
usually hashing or two pointers instead. Also avoid `String` concatenation
in a loop; use `StringBuilder`.

---

## Hashing

**Recognize it when:** you need O(1) average lookup, frequency counting, or
membership checks.

```java
// Frequency map
static Map<Character, Integer> freq(String s) {
    Map<Character, Integer> f = new HashMap<>();
    for (char c : s.toCharArray()) f.merge(c, 1, Integer::sum);
    return f;
}

// Two Sum — O(n) time/space
static int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> idx = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];
        if (idx.containsKey(need)) return new int[]{idx.get(need), i};
        idx.put(nums[i], i);
    }
    return new int[]{-1, -1};
}

// Group anagrams by a sorted-key
static Map<String, List<String>> groupAnagrams(String[] words) {
    Map<String, List<String>> groups = new HashMap<>();
    for (String w : words) {
        char[] chars = w.toCharArray();
        Arrays.sort(chars);
        groups.computeIfAbsent(new String(chars), key -> new ArrayList<>()).add(w);
    }
    return groups;
}
```

**Edge cases:** custom key objects must override `equals()` **and**
`hashCode()` together; mutating a key after insertion corrupts the map;
`HashMap` permits one null key, `Hashtable`/`ConcurrentHashMap` do not.

**Avoid when:** you need sorted/range access (`TreeMap`), insertion order
(`LinkedHashMap`), or small dense integer keys, where a plain array beats
hashing overhead.

---

## Two Pointers

**Recognize it when:** the input is sorted (or sortable) and you need a
pair/triplet or in-place partition.

```java
// Opposite-direction: pair sums to target in a sorted array — O(n)
static boolean pairSum(int[] sorted, int target) {
    int i = 0, j = sorted.length - 1;
    while (i < j) {
        int sum = sorted[i] + sorted[j];
        if (sum == target) return true;
        if (sum < target) i++; else j--;
    }
    return false;
}

// Fast/slow: dedup a sorted array in place — O(n)
static int dedup(int[] a) {
    if (a.length == 0) return 0;
    int slow = 0;
    for (int fast = 1; fast < a.length; fast++) {
        if (a[fast] != a[slow]) a[++slow] = a[fast];
    }
    return slow + 1;
}
```

**Edge cases:** array must be sorted for the sum-target variant;
duplicates that must still yield distinct pairs; empty/single-element
input; `i >= j` as the stop condition.

**Avoid when:** sorting would destroy required order/index information
(use hashing instead), or the problem needs every combination/pair, not a
single-pass answer (a backtracking/DP signal).

---

## Sliding Window

**Recognize it when:** you need a contiguous subarray/substring that is
longest/shortest/optimal under a constraint checkable as the window grows
or shrinks.

```
[ l ......... r ]                 window covers a[l..r]
      grow r -> include a[r]
      shrink l -> while window invalid, drop a[l], l++
```

```java
// Fixed window: max sum of size k — O(n)
static int maxSumFixed(int[] a, int k) {
    int windowSum = 0;
    for (int i = 0; i < k; i++) windowSum += a[i];
    int best = windowSum;
    for (int i = k; i < a.length; i++) {
        windowSum += a[i] - a[i - k];
        best = Math.max(best, windowSum);
    }
    return best;
}

// Variable window: longest substring without repeats
static int lengthOfLongestSubstring(String s) {
    int[] last = new int[128];
    Arrays.fill(last, -1);
    int best = 0, left = 0;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (last[c] >= left) left = last[c] + 1;
        last[c] = right;
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

**Edge cases:** `k` larger than the array, empty string, all identical
characters, non-ASCII input (use a `HashMap<Character, Integer>` instead of
`int[128]`).

**Avoid when:** selections can be non-contiguous (DP/backtracking
territory), or the constraint is non-monotonic — an added element could
re-validate a previously invalid window.

---

## Prefix Sums

**Recognize it when:** you need many range-sum queries on a **static**
array, or count subarrays matching a target sum.

```java
// Prefix sums; sum[l..r] = p[r + 1] - p[l]
static int[] prefix(int[] a) {
    int[] p = new int[a.length + 1];
    for (int i = 0; i < a.length; i++) p[i + 1] = p[i] + a[i];
    return p;
}

// Count subarrays with sum == k — prefix sum + hashmap, O(n)
static int subarraySumEqualsK(int[] a, int k) {
    Map<Integer, Integer> count = new HashMap<>();
    count.put(0, 1); // empty prefix
    int sum = 0, total = 0;
    for (int x : a) {
        sum += x;
        total += count.getOrDefault(sum - k, 0);
        count.merge(sum, 1, Integer::sum);
    }
    return total;
}

// 2D prefix sum over a matrix, via inclusion-exclusion
static int[][] prefix2D(int[][] m) {
    int rows = m.length, cols = m[0].length;
    int[][] p = new int[rows + 1][cols + 1];
    for (int r = 0; r < rows; r++)
        for (int c = 0; c < cols; c++)
            p[r + 1][c + 1] = m[r][c] + p[r][c + 1] + p[r + 1][c] - p[r][c];
    return p;
}
```

**Edge cases:** negatives are fine, but sums can overflow `int` — use
`long` for large ranges; `count.put(0, 1)` is required so a subarray
starting at index 0 counts.

**Avoid when:** the array mutates frequently — a prefix array needs a
full O(n) rebuild per update; use a Fenwick/segment tree for O(log n)
updates instead.

---

## Binary Search Variants

**Recognize it when:** the search space is monotonic — sorted data, or an
answer space where "feasible" flips exactly once.

```java
// Classic search — O(log n)
static int binarySearch(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] == target) return mid;
        if (a[mid] < target) lo = mid + 1; else hi = mid - 1;
    }
    return -1;
}

// Lower bound: first index with a[i] >= target
static int lowerBound(int[] a, int target) {
    int lo = 0, hi = a.length;
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] < target) lo = mid + 1; else hi = mid;
    }
    return lo;
}

// Search on the answer: smallest x with feasible(x) true
static int minimalFeasible(int lo, int hi, java.util.function.IntPredicate feasible) {
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (feasible.test(mid)) hi = mid; else lo = mid + 1;
    }
    return lo;
}

// Search a rotated sorted array (no duplicates) — O(log n)
static int searchRotated(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] == target) return mid;
        if (a[lo] <= a[mid]) {
            if (a[lo] <= target && target < a[mid]) hi = mid - 1; else lo = mid + 1;
        } else {
            if (a[mid] < target && target <= a[hi]) lo = mid + 1; else hi = mid - 1;
        }
    }
    return -1;
}
```

**Edge cases:** empty array (`lo > hi` immediately); duplicates in a
rotated array break the sorted-half check when `a[lo] == a[mid] == a[hi]`
— shrink both ends by one instead (O(n) worst case); always use
`lo + (hi - lo) / 2`; on "search on the answer," use `hi = mid`, not
`mid - 1`, on the feasible branch.

**Avoid when:** data is unsorted and cannot be sorted, the dataset is
tiny (linear scan is simpler), or you need every occurrence (use the
lower/upper bound pair, not a single `binarySearch` call).

---

## Linked Lists

**Recognize it when:** the input is a linked list, or you need O(1)
insert/delete at a known position without shifting.

```java
static class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

// Reverse a linked list iteratively — O(n), O(1)
static ListNode reverse(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}

// Floyd's cycle detection — O(n), O(1)
static boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}

// Merge two sorted lists using a dummy head
static ListNode mergeTwoLists(ListNode a, ListNode b) {
    ListNode dummy = new ListNode(0), tail = dummy;
    while (a != null && b != null) {
        if (a.val <= b.val) { tail.next = a; a = a.next; }
        else { tail.next = b; b = b.next; }
        tail = tail.next;
    }
    tail.next = (a != null) ? a : b;
    return dummy.next;
}
```

**Edge cases:** empty list, single node, cycle at the head, merging when
one list is empty, needing the node *before* a target — keep a `prev`
pointer or use a dummy head.

**Avoid when:** you need random/indexed access (`get(i)` is O(n) vs O(1) on
an array), or cache locality matters in a hot inner loop.

---

## Stacks and Queues

**Recognize it when:** you need LIFO undo/matching (stack), FIFO order
(queue), or "next greater/smaller" problems (monotonic stack).

```java
Deque<Integer> stack = new ArrayDeque<>(); // push() / pop() / peek()
Deque<Integer> queue = new ArrayDeque<>(); // offer() / poll() / peek()

// Monotonic stack: next greater element for every index
static int[] nextGreater(int[] a) {
    int[] res = new int[a.length];
    Arrays.fill(res, -1);
    Deque<Integer> idxStack = new ArrayDeque<>(); // indices, values decreasing
    for (int i = 0; i < a.length; i++) {
        while (!idxStack.isEmpty() && a[idxStack.peek()] < a[i]) res[idxStack.pop()] = a[i];
        idxStack.push(i);
    }
    return res;
}

// Min stack: O(1) getMin via an auxiliary stack
static class MinStack {
    Deque<Integer> data = new ArrayDeque<>(), mins = new ArrayDeque<>();
    void push(int x) {
        data.push(x);
        mins.push(mins.isEmpty() ? x : Math.min(x, mins.peek()));
    }
    int pop() { mins.pop(); return data.pop(); }
    int getMin() { return mins.peek(); }
}
```

**Edge cases:** popping an empty `ArrayDeque` throws
`NoSuchElementException` — check `isEmpty()` first; monotonic direction
must match "next greater" vs. "next smaller"; `ArrayDeque` disallows
`null`.

**Avoid when:** you need O(1) access to arbitrary middle elements (use an
array/list) or thread safety (`ConcurrentLinkedDeque`). Prefer
`ArrayDeque` over `LinkedList` as a general-purpose stack/queue.

---

## Heaps

**Recognize it when:** you repeatedly need the current min/max from a
changing collection — "kth largest," "top k frequent," merged sorted
sources, running median.

```
Array-backed binary heap: parent i, children 2i+1 and 2i+2 (0-indexed)
```

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();                        // default: min-heap
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder()); // max-heap

// Kth largest via a size-k min-heap — O(n log k)
static int kthLargest(int[] a, int k) {
    PriorityQueue<Integer> heap = new PriorityQueue<>();
    for (int x : a) {
        heap.offer(x);
        if (heap.size() > k) heap.poll();
    }
    return heap.peek();
}

// Merge K sorted lists — O(n log k)
static ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((x, y) -> x.val - y.val);
    for (ListNode l : lists) if (l != null) pq.offer(l);
    ListNode dummy = new ListNode(0), tail = dummy;
    while (!pq.isEmpty()) {
        ListNode min = pq.poll();
        tail.next = min; tail = tail.next;
        if (min.next != null) pq.offer(min.next);
    }
    return dummy.next;
}

// Running median via two heaps — O(log n) insert
static class MedianFinder {
    PriorityQueue<Integer> lo = new PriorityQueue<>(Collections.reverseOrder()); // max-heap, lower half
    PriorityQueue<Integer> hi = new PriorityQueue<>();                           // min-heap, upper half
    void addNum(int n) {
        lo.offer(n);
        hi.offer(lo.poll());
        if (hi.size() > lo.size()) lo.offer(hi.poll());
    }
    double findMedian() {
        return lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0;
    }
}
```

**Edge cases:** ties break arbitrarily (`PriorityQueue` isn't stable);
custom objects need a `Comparator`/`Comparable`; `peek()`/`poll()` on an
empty heap return `null`, which NPEs on unboxing.

**Avoid when:** you need the full sorted order once — just sort (lower
constant factor than n inserts plus polls). Also avoid `decreaseKey`;
Java's heap only supports remove + reinsert at O(n).

---

## Trees, BST and Trie

**Recognize it when:** data is hierarchical, needs ordered traversal,
range queries over sorted keys (BST), or prefix-based string lookups
(trie).

```java
static class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

// Recursive inorder traversal
static void inorder(TreeNode node, List<Integer> out) {
    if (node == null) return;
    inorder(node.left, out);
    out.add(node.val);
    inorder(node.right, out);
}

// Iterative level-order traversal (BFS)
static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) return levels;
    Deque<TreeNode> q = new ArrayDeque<>();
    q.offer(root);
    while (!q.isEmpty()) {
        int size = q.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode n = q.poll();
            level.add(n.val);
            if (n.left != null) q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        levels.add(level);
    }
    return levels;
}

// BST insert
static TreeNode insertBST(TreeNode node, int val) {
    if (node == null) return new TreeNode(val);
    if (val < node.val) node.left = insertBST(node.left, val);
    else if (val > node.val) node.right = insertBST(node.right, val);
    return node;
}

// Trie: assumes lowercase a-z keys
static class Trie {
    Trie[] children = new Trie[26];
    boolean isWord;
    void insert(String w) {
        Trie cur = this;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) cur.children[i] = new Trie();
            cur = cur.children[i];
        }
        cur.isWord = true;
    }
    boolean search(String w) { Trie node = find(w); return node != null && node.isWord; }
    boolean startsWith(String prefix) { return find(prefix) != null; }
    private Trie find(String s) {
        Trie cur = this;
        for (char c : s.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) return null;
            cur = cur.children[i];
        }
        return cur;
    }
}
```

**Edge cases:** `null` root; a skewed tree has O(n) height, risking stack
overflow; duplicate BST keys need a convention (left, right, or count); a
trie assumes a fixed alphabet — widen `children` or use
`HashMap<Character, Trie>` for sparser ones.

**Avoid when:** you need a *guaranteed* O(log n) worst case — a hand-rolled
BST can degrade to O(n); use `TreeMap`/`TreeSet` instead. Avoid a trie for
sparse keys; 26+ pointers per node costs memory, so `HashMap<String, V>`
is often better.

---

## Graphs

**Recognize it when:** entities have relationships — reachability,
shortest paths, connectivity, dependency ordering.

| Representation | Space | Check edge (u,v) | Iterate neighbors of u |
|---|---|---|---|
| Adjacency list | O(V+E) | O(degree(u)) | O(degree(u)) |
| Adjacency matrix | O(V²) | O(1) | O(V) |

```java
// Adjacency list setup
List<List<Integer>> graph = new ArrayList<>();
for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
graph.get(u).add(v); // add both directions for an undirected edge

// BFS shortest path, unweighted graph — O(V + E)
static int[] bfs(List<List<Integer>> graph, int src, int n) {
    int[] dist = new int[n];
    Arrays.fill(dist, -1);
    Deque<Integer> q = new ArrayDeque<>();
    dist[src] = 0;
    q.offer(src);
    while (!q.isEmpty()) {
        int u = q.poll();
        for (int v : graph.get(u)) {
            if (dist[v] == -1) { dist[v] = dist[u] + 1; q.offer(v); }
        }
    }
    return dist;
}

// Recursive DFS with a visited array — O(V + E)
static void dfs(List<List<Integer>> graph, int u, boolean[] visited) {
    visited[u] = true;
    for (int v : graph.get(u)) if (!visited[v]) dfs(graph, v, visited);
}

// Dijkstra, non-negative weights — O((V + E) log V)
static int[] dijkstra(List<List<int[]>> graph, int src, int n) { // edge = {neighbor, weight}
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]); // {node, dist}
    pq.offer(new int[]{src, 0});
    while (!pq.isEmpty()) {
        int[] cur = pq.poll();
        int u = cur[0], d = cur[1];
        if (d > dist[u]) continue; // stale entry, skip
        for (int[] edge : graph.get(u)) {
            int v = edge[0], w = edge[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    return dist;
}

// Topological sort, Kahn's algorithm — O(V + E)
static List<Integer> topoSort(List<List<Integer>> graph, int n) {
    int[] indeg = new int[n];
    for (List<Integer> adj : graph) for (int v : adj) indeg[v]++;
    Deque<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < n; i++) if (indeg[i] == 0) q.offer(i);
    List<Integer> order = new ArrayList<>();
    while (!q.isEmpty()) {
        int u = q.poll();
        order.add(u);
        for (int v : graph.get(u)) if (--indeg[v] == 0) q.offer(v);
    }
    return order.size() == n ? order : new ArrayList<>(); // fewer than n => a cycle exists
}
```

**Edge cases:** disconnected components (loop over all nodes as a source);
self-loops and parallel edges; negative weights break Dijkstra; insert
both directions for undirected edges; skip stale Dijkstra entries
(`if (d > dist[u]) continue;`); deep graphs risk stack overflow
recursively.

**Avoid when:** BFS on a weighted graph (it counts edges, not weight); DFS
for shortest path (finds *a* path, not the shortest); Dijkstra with
negative edges (use Bellman-Ford); recursive DFS on very deep graphs.

---

## Union-Find

**Recognize it when:** you need to group elements into connected
components and ask "are these two connected?" or merge sets — Kruskal's
MST, cycle detection, dynamic connectivity.

```java
static class DSU {
    int[] parent, rank;
    DSU(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }
    boolean union(int x, int y) {
        int rx = find(x), ry = find(y);
        if (rx == ry) return false; // already connected -> this edge would form a cycle
        if (rank[rx] < rank[ry]) { int t = rx; rx = ry; ry = t; }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) rank[rx]++;
        return true;
    }
}
```

**Edge cases:** `union(x, x)` is a no-op; always call `find`, never read
`parent[x]` directly; `union`'s boolean return detects a cycle while
building a spanning tree (Kruskal); path compression + union by rank give
near-O(1) cost.

**Avoid when:** the problem needs to *split* sets — DSU only merges, never
un-merges, and does not support dynamic edge deletion; use offline
processing (queries in reverse) instead.

---

## Greedy

**Recognize it when:** a locally optimal choice, repeated, provably yields
a global optimum — interval scheduling, canonical coin systems, Huffman
coding.

```java
// Max number of non-overlapping intervals — O(n log n)
static int maxNonOverlapping(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[1] - b[1]); // sort by end time
    int count = 0, lastEnd = Integer.MIN_VALUE;
    for (int[] iv : intervals) {
        if (iv[0] >= lastEnd) { count++; lastEnd = iv[1]; }
    }
    return count;
}
```

**Edge cases:** whether touching endpoints (`iv[0] == lastEnd`) count as
overlapping depends on inclusive/exclusive interval convention — clarify
with the interviewer; tie-breaking when intervals share an end time;
empty input.

**Avoid when:** you cannot construct an exchange-argument proof (assume an
optimal answer differs from the greedy choice, then show swapping it in
cannot make things worse) — 0/1 knapsack's greedy-by-density heuristic
fails, needing DP instead.

---

## Backtracking

**Recognize it when:** the problem asks you to enumerate subsets,
permutations, combinations, or valid configs (N-Queens, Sudoku, word
search) on small enough input.

```java
// Choose -> explore -> un-choose (here: permutations)
static void backtrack(List<Integer> path, boolean[] used, int[] nums, List<List<Integer>> results) {
    if (path.size() == nums.length) {
        results.add(new ArrayList<>(path)); // copy — path keeps mutating
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true;
        path.add(nums[i]);
        backtrack(path, used, nums, results);
        path.remove(path.size() - 1); // undo
        used[i] = false;
    }
}

// Subsets via bitmask — for n <= ~20
static List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    int n = nums.length;
    for (int mask = 0; mask < (1 << n); mask++) {
        List<Integer> subset = new ArrayList<>();
        for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) subset.add(nums[i]);
        res.add(subset);
    }
    return res;
}

// Combination sum with pruning (cand sorted ascending)
static void combinationSum(int[] cand, int remain, int start, List<Integer> path, List<List<Integer>> res) {
    if (remain == 0) { res.add(new ArrayList<>(path)); return; }
    for (int i = start; i < cand.length && cand[i] <= remain; i++) {
        path.add(cand[i]);
        combinationSum(cand, remain - cand[i], i, path, res); // reuse i: unbounded reuse allowed
        path.remove(path.size() - 1);
    }
}
```

**Edge cases:** duplicates create duplicate results unless sorted first
with a skip on `i > start && nums[i] == nums[i - 1]`; always add a
**copy** of `path` (`new ArrayList<>(path)`), never the reference; `i`
vs. `i + 1` in the recursive call controls whether reuse is allowed.

**Avoid when:** the search space (`2^n` or `n!`) is infeasible without
heavy pruning — check the input-size table first. Also avoid it when
subproblems overlap with optimal substructure — that's a DP signal, and
re-exploring the same state wastes time memoization would save.

---

## Dynamic Programming

**Recognize it when:** the problem asks for an optimal value, count of
ways, or feasibility, and naive recursion revisits subproblems
(overlapping subproblems + optimal substructure).

```java
// Top-down memoization template
static Map<Integer, Long> memo = new HashMap<>();
static long fib(int n) {
    if (n <= 1) return n;
    if (memo.containsKey(n)) return memo.get(n);
    long res = fib(n - 1) + fib(n - 2);
    memo.put(n, res);
    return res;
}

// 0/1 knapsack — dp[w] = max value at capacity w
static int knapsack01(int[] weights, int[] values, int capacity) {
    int[] dp = new int[capacity + 1];
    for (int i = 0; i < weights.length; i++) {
        for (int w = capacity; w >= weights[i]; w--) // reverse: each item used at most once
            dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
    }
    return dp[capacity];
}

// LIS via patience sorting — O(n log n)
static int lengthOfLIS(int[] nums) {
    int[] tails = new int[nums.length];
    int size = 0;
    for (int x : nums) {
        int lo = 0, hi = size;
        while (lo < hi) { // lower_bound of x in tails[0..size)
            int mid = lo + (hi - lo) / 2;
            if (tails[mid] < x) lo = mid + 1; else hi = mid;
        }
        tails[lo] = x;
        if (lo == size) size++;
    }
    return size;
}

// Longest Common Subsequence — O(n * m)
static int lcs(String a, String b) {
    int n = a.length(), m = b.length();
    int[][] dp = new int[n + 1][m + 1];
    for (int i = 1; i <= n; i++)
        for (int j = 1; j <= m; j++)
            dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                ? dp[i - 1][j - 1] + 1
                : Math.max(dp[i - 1][j], dp[i][j - 1]);
    return dp[n][m];
}
```

**Common recurrences:**

| Problem | State | Recurrence |
|---|---|---|
| 0/1 Knapsack | `dp[i][w]` | `max(dp[i-1][w], dp[i-1][w-wt]+val)` |
| Unbounded Knapsack / Coin Change (min coins) | `dp[amount]` | `min(dp[amount], dp[amount-coin]+1)` |
| Longest Increasing Subsequence | `dp[i]` | `max(dp[j]+1)` for `j<i`, `nums[j]<nums[i]` |
| Longest Common Subsequence | `dp[i][j]` | match: `dp[i-1][j-1]+1`; else `max(dp[i-1][j], dp[i][j-1])` |
| Edit Distance | `dp[i][j]` | match: `dp[i-1][j-1]`; else `1+min(insert, delete, replace)` |
| Matrix Chain / interval DP | `dp[i][j]` | `min over k of dp[i][k]+dp[k+1][j]+cost(i,k,j)` |

**Edge cases:** define base cases (`dp[0]`, empty string, zero capacity);
init `+∞`/`-∞` for min/max, `0` for counting; watch overflow (use
`long`/modulo); iteration order matters — 0/1 knapsack goes capacity
**descending**, unbounded **ascending**.

**Avoid when:** there are no overlapping subproblems (plain
divide-and-conquer or greedy is simpler), the state space is exponential
and unbounded (seek a different formulation), or a simpler
two-pointer/prefix-sum solution already matches the complexity.

---

## Segment Tree and Fenwick Tree

**Recognize it when:** you need range queries (sum/min/max) on an array
updated repeatedly — static arrays should use a prefix sum instead.

```java
// Fenwick Tree (BIT) — 1-indexed, point update + prefix sum
static class Fenwick {
    int[] tree;
    Fenwick(int n) { tree = new int[n + 1]; }
    void update(int i, int delta) { // i is 1-indexed
        for (; i < tree.length; i += i & (-i)) tree[i] += delta;
    }
    int prefixSum(int i) {
        int sum = 0;
        for (; i > 0; i -= i & (-i)) sum += tree[i];
        return sum;
    }
    int rangeSum(int l, int r) { return prefixSum(r) - prefixSum(l - 1); } // 1-indexed, inclusive
}

// Segment Tree — sum query + point update, O(log n)
static class SegmentTree {
    int[] tree;
    int n;
    SegmentTree(int[] a) {
        n = a.length;
        tree = new int[4 * n]; // safe upper bound
        build(a, 1, 0, n - 1);
    }
    void build(int[] a, int node, int lo, int hi) {
        if (lo == hi) { tree[node] = a[lo]; return; }
        int mid = lo + (hi - lo) / 2;
        build(a, 2 * node, lo, mid);
        build(a, 2 * node + 1, mid + 1, hi);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }
    void update(int node, int lo, int hi, int idx, int val) {
        if (lo == hi) { tree[node] = val; return; }
        int mid = lo + (hi - lo) / 2;
        if (idx <= mid) update(2 * node, lo, mid, idx, val);
        else update(2 * node + 1, mid + 1, hi, idx, val);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }
    int query(int node, int lo, int hi, int l, int r) {
        if (r < lo || hi < l) return 0;               // no overlap
        if (l <= lo && hi <= r) return tree[node];     // total overlap
        int mid = lo + (hi - lo) / 2;                  // partial overlap
        return query(2 * node, lo, mid, l, r) + query(2 * node + 1, mid + 1, hi, l, r);
    }
}
```

**Edge cases:** Fenwick index `0` is unused — translate a 0-indexed input
index `i` to `i + 1`; negative deltas work like positive ones; `4n` is a
safe segment-tree array bound; all three overlap branches in `query` must
be handled; range **updates** need lazy propagation, a separate template.

**Avoid when:** the array never changes — a plain prefix-sum array is
simpler, O(1) queries, no `log n` overhead. Also skip it when updates are
rare enough that an O(n) rebuild fits the time limit.

---

## Bit Manipulation

**Recognize it when:** the problem mentions XOR, subsets encoded as
integers, parity, or asks for O(1) space tricks on a fixed-width value.

| Operation | Expression |
|---|---|
| Check bit `i` | `(n >> i) & 1` |
| Set bit `i` | `n \| (1 << i)` |
| Clear bit `i` | `n & ~(1 << i)` |
| Toggle bit `i` | `n ^ (1 << i)` |
| Lowest set bit | `n & (-n)` |
| Clear lowest set bit | `n & (n - 1)` |
| Count set bits | `Integer.bitCount(n)` |
| Is a power of two | `n > 0 && (n & (n - 1)) == 0` |
| Swap without a temp | `a ^= b; b ^= a; a ^= b;` |

```java
// Single number: all dupes appear twice except one — XOR cancels pairs
static int singleNumber(int[] nums) {
    int result = 0;
    for (int x : nums) result ^= x;
    return result;
}

// Count set bits — Kernighan's algorithm, O(popcount)
static int countBits(int n) {
    int count = 0;
    while (n != 0) { n &= (n - 1); count++; }
    return count;
}
```

**Edge cases:** Java uses two's complement — `>>` sign-extends negatives,
use `>>>` for a logical shift; shift amounts on an `int` mask to the low 5
bits (`1 << 32 == 1 << 0`); left-shifting near bit 31 flips the sign bit;
the XOR-swap trick zeroes `a`/`b` if they're the same variable.

**Avoid when:** readability matters more than the last bit of performance
— prefer clear boolean/arithmetic logic unless O(1)-space bit tricks are
specifically needed. Clarify 32-bit vs. 64-bit intent; use `long`/`1L << i`
once `i` can reach 31+.

---

## Debugging Checklist

**Before coding:**

- [ ] Restated the problem; confirmed input/output format.
- [ ] Asked constraints: size, negatives, duplicates, sorted or not.
- [ ] Picked a target complexity matching constraints (input-size table
      above).
- [ ] Named edge cases up front: empty, single element, all-equal.

**While coding:**

- [ ] Loop bounds: `<` vs `<=`, inclusive vs exclusive.
- [ ] Overflow: `long` for sums/products that could exceed `2^31 - 1`.
- [ ] `mid = lo + (hi - lo) / 2`, never `(lo + hi) / 2`.
- [ ] Null checks before dereferencing (list/tree nodes, `map.get()`).
- [ ] Not mutating a collection while iterating it
      (`ConcurrentModificationException`) — use the `Iterator`'s own
      `remove()`.
- [ ] `==` vs `.equals()` for `Integer`/`String` — autoboxing caches only
      `-128..127`.
- [ ] Defaults: `int[]` is `0`; `Integer[]`/`List<Integer>` is `null`.
- [ ] Recursion has a correct, reachable base case.

**After the first draft:**

- [ ] Dry-run the smallest non-trivial example by hand.
- [ ] Trace an edge case: empty, single element, duplicates, negatives.
- [ ] Re-check actual complexity against the stated constraints.
- [ ] Look for an accidental nested loop or a rebuild inside a loop.
- [ ] In backtracking, confirm state is undone on every path
      (`path.remove(...)`, `used[i] = false`).

**Common Java pitfalls:**

- [ ] `Arrays.asList(arr)` is fixed-size — `add()`/`remove()` throws.
- [ ] Unboxing a `null` `Integer`/`Boolean` throws `NullPointerException`.
- [ ] `String.split()` treats its argument as a regex — escape `.`, `|`.
- [ ] Java's `%` can return a negative value; use `((x % m) + m) % m`.
- [ ] `new int[n][m]` is zero-filled; a jagged `new int[n][]` needs each
      row allocated.
- [ ] Prefer `StringBuilder` over `String` concatenation in a loop.

---

## Rapid-Fire Q&A

### Complexity

Q: Why can recursive DFS stack-overflow when iterative DFS doesn't?

Answer:
Recursion depth is bounded by the JVM's fixed thread stack; iterative
DFS uses the heap.

### Arrays and Strings

Q: Why is string concatenation with `+=` inside a loop O(n²) in Java?

Answer:
Strings are immutable, so each `+=` copies both operands; n iterations
cost O(n²). Use `StringBuilder`.

### Hashing

Q: Why must equal objects produce equal `hashCode()` values in Java?

Answer:
`HashMap`/`HashSet` pick a bucket from `hashCode()`; mismatched hashes
break `get()` for equal objects.

### Two Pointers

Q: Can the two-pointer pair-sum technique work on unsorted data?

Answer:
No — it moves pointers by comparing the sum to the target, which
requires sorted data first.

### Sliding Window

Q: How do you decide when to shrink a sliding window?

Answer:
Grow the right edge every step; shrink the left edge while the window
is invalid.

### Prefix Sums

Q: Do negative numbers break the prefix-sum + hashmap technique for counting subarrays with a target sum?

Answer:
No — the `sum - k` lookup is sign-agnostic; only overflow risk changes,
fixed with `long`.

### Binary Search

Q: Why write `mid = lo + (hi - lo) / 2` instead of `(lo + hi) / 2`?

Answer:
`lo + hi` can overflow near `Integer.MAX_VALUE`; the subtraction form is
bounded.

### Linked Lists

Q: Why do linked-list problems commonly use a dummy head node?

Answer:
It removes special-casing for edits to the real head, since the dummy
always precedes "first."

### Stacks and Queues

Q: Why is a monotonic stack O(n) overall despite an inner `while` loop?

Answer:
Each element is pushed and popped at most once, bounding total work to
2n.

### Heaps

Q: Why does finding the kth largest with a size-k min-heap cost O(n log k), not O(n log n)?

Answer:
The heap never exceeds k elements, so each op is O(log k) — cheaper
than sorting when k ≪ n.

### Trees, BST and Trie

Q: Why can binary search tree operations degrade from O(log n) to O(n)?

Answer:
Sorted inserts build a skewed, list-shaped tree of height O(n);
self-balancing trees bound it to O(log n).

### Graphs

Q: Why does Dijkstra's algorithm fail with negative edge weights?

Answer:
It finalizes a distance once popped from the queue; a later negative
edge can violate that — use Bellman-Ford.

### Union-Find

Q: Why combine path compression with union by rank in Union-Find?

Answer:
Either alone gives roughly O(log n) amortized; combined they give
O(α(n)) — effectively constant.

### Greedy

Q: How can you tell a greedy strategy is correct before coding it?

Answer:
Show an exchange argument: swapping toward the greedy choice must never
worsen an optimal solution.

### Backtracking

Q: How do you avoid duplicate results when backtracking over input with repeats?

Answer:
Sort the input, then skip a candidate equal to the unused previous one
at the same depth.

### Dynamic Programming

Q: How do you cut a DP's space from O(n²) to O(n)?

Answer:
If `dp[i][j]` only needs the previous row, keep one 1D array and update
it carefully.

### Segment Tree and Fenwick Tree

Q: When would you reach for a Fenwick tree instead of a segment tree?

Answer:
For prefix-invertible aggregates like sum or XOR with point updates —
shorter code, half the memory.

### Bit Manipulation

Q: What does `n & (n - 1)` compute, and why is it useful?

Answer:
It clears the lowest set bit — used to count set bits or test for a
power of two.

---

## One-Page Recall

| Pattern | Time | Space | Do not use when |
|---|---|---|---|
| Two Pointers | O(n) | O(1) | Unsortable data; need all combinations |
| Sliding Window | O(n) | O(1)/O(k) | Non-contiguous selection or non-monotonic constraint |
| Prefix Sum | O(n) build, O(1) query | O(n) | Array mutates often |
| Binary Search | O(log n) | O(1) | Not sorted or monotonic |
| Hashing | O(1) avg | O(n) | Need ordering, ranges, or worst-case guarantees |
| Monotonic Stack | O(n) | O(n) | Need random access to the middle |
| Heap | O(log n) per op | O(n) | Need full sorted order once — just sort |
| Union-Find | O(α(n)) | O(n) | Need to split or disconnect sets |
| Greedy | varies | O(1)-O(n) | No exchange-argument proof exists |
| Backtracking | exponential | O(depth) | Search space too large; subproblems overlap |
| Dynamic Programming | varies | varies | No overlapping subproblems; unbounded state space |
| Segment/Fenwick Tree | O(log n) per op | O(n) | Data is static |
| Bit Manipulation | O(1)/O(bits) | O(1) | Readability matters more than micro-performance |

Match the prompt to a row, confirm "do not use when" doesn't apply, then
write the template.
