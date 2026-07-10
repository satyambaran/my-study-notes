# DSA Interview — Last Minute Cheat Sheet

## Structure
Each section follows: pattern name → when to use → key idea → skeleton approach.
Common questions show: problem type → core pattern → critical decision → time/space.

## Purpose
Rapid revision for DSA interviews. Assumes basic data structure knowledge. Covers patterns, approaches, and popular question types.

---

## 1. Complexity — One-Line Triggers

```
O(1)         : HashMap lookup, array index access, stack push/pop
O(log n)     : Binary search, balanced BST ops, heap insert/extract
O(n)         : Single pass, two pointers, sliding window, linear scan
O(n log n)   : Sorting (merge/quick/heap), sort + binary search
O(n²)        : Nested loops, brute force pairs, DP on 2D grid
O(2ⁿ)        : Subsets, recursive brute force without memoization
O(n!)         : Permutations, brute force all orderings

Amortized    : "Expensive rarely, cheap usually" — ArrayList resize, Union-Find with path compression
```

**Space complexity triggers:**
```
O(1) extra    : In-place sort, two pointers, bit manipulation
O(n)          : HashMap, recursion stack, DP 1D array, queue/stack
O(n²)         : 2D DP table, adjacency matrix
```

---

## 2. Algorithmic Patterns

### Sliding Window
**Trigger:** contiguous subarray/substring, max/min window, "at most K distinct"
```
Fixed window: maintain window of size k, slide right, update result
Variable window: expand right until invalid, shrink left until valid again

int left = 0;
for (int right = 0; right < n; right++) {
    add arr[right] to window state;
    while (window is invalid) {
        remove arr[left] from window state;
        left++;
    }
    update result with current window;
}
// Time: O(n) — each element enters and leaves window once
```

### Two Pointers
**Trigger:** sorted array, pair sum, remove duplicates, palindrome, merge sorted
```
Opposite ends: left=0, right=n-1, move inward based on condition
Same direction: slow/fast for cycle detection, in-place removal

// Pair sum in sorted array
while (left < right) {
    int sum = arr[left] + arr[right];
    if (sum == target) return;
    else if (sum < target) left++;
    else right--;
}
// Time: O(n)
```

### Fast & Slow Pointers
**Trigger:** cycle detection, middle of linked list, happy number
```
slow moves 1 step, fast moves 2 steps
If cycle: they meet inside cycle
Middle: when fast reaches end, slow is at middle

// Floyd's cycle detection
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) return true; // cycle found
}
```

### Binary Search
**Trigger:** sorted array, monotonic condition, "min/max that satisfies X", search space reduction
```
Classic: find target in sorted array
On answer: binary search on the answer space (min/max optimization)

// Template — handles all cases
int lo = 0, hi = n - 1;
while (lo <= hi) {
    int mid = lo + (hi - lo) / 2;   // avoid overflow
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) lo = mid + 1;
    else hi = mid - 1;
}
return -1; // or lo for insertion point

// Binary search on answer (e.g., "minimum capacity to ship in D days")
int lo = min_possible, hi = max_possible;
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (canAchieve(mid)) hi = mid;      // try smaller
    else lo = mid + 1;                   // need bigger
}
return lo;
// Time: O(n log(search_space))
```

### BFS (Breadth-First Search)
**Trigger:** shortest path (unweighted), level-order, minimum steps, nearest X
```
Queue-based, processes level by level
Guaranteed shortest path in unweighted graphs

Queue<Node> q = new LinkedList<>();
Set<Node> visited = new HashSet<>();
q.offer(start); visited.add(start);
int level = 0;
while (!q.isEmpty()) {
    int size = q.size(); // process one level
    for (int i = 0; i < size; i++) {
        Node curr = q.poll();
        for (Node neighbor : curr.neighbors) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                q.offer(neighbor);
            }
        }
    }
    level++;
}
// Time: O(V + E)
```

### DFS (Depth-First Search)
**Trigger:** paths, connectivity, cycle detection, topological sort, backtracking, island counting
```
Stack-based (or recursion), goes deep before wide
Use for: all paths, connected components, tree traversals

void dfs(Node node, Set<Node> visited) {
    visited.add(node);
    // process node
    for (Node neighbor : node.neighbors) {
        if (!visited.contains(neighbor)) {
            dfs(neighbor, visited);
        }
    }
}
// Time: O(V + E)
```

### Backtracking
**Trigger:** "generate all", permutations, combinations, subsets, N-Queens, Sudoku, word search
```
Try a choice → recurse → undo the choice (backtrack)

void backtrack(state, choices, result) {
    if (base case) { result.add(copy of state); return; }
    for (choice : choices) {
        if (isValid(choice)) {
            state.add(choice);        // make choice
            backtrack(state, remaining_choices, result);
            state.removeLast();       // undo choice
        }
    }
}
// Pruning: skip invalid branches early to reduce time
```

### Dynamic Programming
**Trigger:** "count ways", "min/max cost", "can you achieve X", overlapping subproblems, optimal substructure
```
1. Define state: dp[i] = answer for subproblem i
2. Recurrence: dp[i] = f(dp[i-1], dp[i-2], ...)
3. Base case: dp[0] = ...
4. Order: fill smallest subproblems first (bottom-up) or use memo (top-down)
5. Optimize: if dp[i] depends only on dp[i-1], dp[i-2] → use 2 variables

// Top-down (memoization)
Map<State, Integer> memo = new HashMap<>();
int solve(state) {
    if (base case) return ...;
    if (memo.containsKey(state)) return memo.get(state);
    int result = /* recurrence */;
    memo.put(state, result);
    return result;
}

// Bottom-up (tabulation)
int[] dp = new int[n + 1];
dp[0] = base; dp[1] = base;
for (int i = 2; i <= n; i++)
    dp[i] = dp[i-1] + dp[i-2]; // example: Fibonacci
```

### Greedy
**Trigger:** locally optimal → globally optimal, interval scheduling, Huffman, activity selection
```
Make the best choice at each step, never revisit

Key proof obligation: greedy choice property + optimal substructure
Common greedy strategies:
  - Sort by end time (interval scheduling)
  - Sort by ratio (fractional knapsack)
  - Always pick smallest/largest available
// If greedy doesn't work → try DP
```

### Monotonic Stack / Queue
**Trigger:** "next greater element", "previous smaller", "max in sliding window", histogram area
```
Stack maintains monotonic order (increasing or decreasing)
Pop when current element breaks the monotonic property

// --- Monotonic Stack — Next Greater Element ---
// For each element, find the next element to its right that is strictly greater
Deque<Integer> stack = new ArrayDeque<>(); // stores indices
int[] result = new int[n];
Arrays.fill(result, -1);
for (int i = 0; i < n; i++) {
    while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) {
        result[stack.pop()] = arr[i]; // arr[i] is the answer for popped index
    }
    stack.push(i);
}
// Time: O(n) — each element pushed and popped at most once
//
// Trace: arr = [4, 2, 1, 5, 3]
// i=0: push 0             stack=[0]           result=[-1,-1,-1,-1,-1]
// i=1: 4<2? No, push 1    stack=[0,1]
// i=2: 2<1? No, push 2    stack=[0,1,2]
// i=3: 1<5? Yes→result[2]=5, 2<5? Yes→result[1]=5, 4<5? Yes→result[0]=5, push 3
//                          stack=[3]           result=[5,5,5,-1,-1]
// i=4: 5<3? No, push 4    stack=[3,4]         result=[5,5,5,-1,-1]
// Answer: [5, 5, 5, -1, -1] ✓
//
// Variants:
//   Next smaller: change < to > (maintain increasing stack)
//   Previous greater: iterate right to left

// --- Monotonic Deque — Max in Sliding Window of size k ---
// Deque stores indices in decreasing order of values. Front = index of window max.
Deque<Integer> deque = new ArrayDeque<>();
int[] maxResult = new int[n - k + 1];
for (int i = 0; i < n; i++) {
    // 1. Evict front if it's outside the window [i-k+1, i]
    if (!deque.isEmpty() && deque.peekFirst() <= i - k)
        deque.pollFirst();
    // 2. Remove from back all indices whose values are ≤ current
    //    (they can never be the max while current is in the window)
    while (!deque.isEmpty() && arr[deque.peekLast()] <= arr[i])
        deque.pollLast();
    // 3. Add current index to back
    deque.offerLast(i);
    // 4. Window is full once i >= k-1, record the max (front of deque)
    if (i >= k - 1)
        maxResult[i - k + 1] = arr[deque.peekFirst()];
}
// Time: O(n) — each element enters and leaves deque at most once
//
// Trace: arr = [1, 3, -1, -3, 5], k = 3
// i=0: evict? No. back empty. add 0.          deque=[0](val:[1])
// i=1: evict? 0<=−2? No. back 1<=3? Yes→rm 0. deque=[1](val:[3])
// i=2: evict? 1<=−1? No. back 3<=−1? No.      deque=[1,2](val:[3,−1])   → max=arr[1]=3  ✓ window[1,3,−1]
// i=3: evict? 1<=0?  No. back −1<=−3? No.     deque=[1,2,3](val:[3,−1,−3]) → max=arr[1]=3  ✓ window[3,−1,−3]
// i=4: evict? 1<=1?  Yes→rm 1.                deque=[2,3].
//      back −3<=5? Yes→rm 3. −1<=5? Yes→rm 2. deque=[4](val:[5])        → max=arr[4]=5  ✓ window[−1,−3,5]
// Answer: [3, 3, 5] ✓
//
// For MIN in window: change <= to >= (maintain increasing order, front = min)
```

### Prefix Sum / Difference Array
**Trigger:** range sum queries, subarray sum equals K, range updates
```
prefix[i] = sum(arr[0..i-1])
sum(l..r) = prefix[r+1] - prefix[l]

// Subarray sum equals K — prefix sum + HashMap
Map<Integer, Integer> map = new HashMap<>();
map.put(0, 1); int sum = 0, count = 0;
for (int num : arr) {
    sum += num;
    count += map.getOrDefault(sum - k, 0);
    map.merge(sum, 1, Integer::sum);
}
// Time: O(n)
```

### Union-Find (Disjoint Set)
**Trigger:** "are they connected?", connected components, cycle detection in undirected graph, Kruskal's MST
```
int[] parent, rank;
int find(int x) {
    if (parent[x] != x) parent[x] = find(parent[x]); // path compression
    return parent[x];
}
void union(int x, int y) {
    int px = find(x), py = find(y);
    if (px == py) return;
    if (rank[px] < rank[py]) parent[px] = py;         // union by rank
    else if (rank[px] > rank[py]) parent[py] = px;
    else { parent[py] = px; rank[px]++; }
}
// Time: O(α(n)) ≈ O(1) amortized per operation
```

### Topological Sort
**Trigger:** dependency ordering, course schedule, build order, DAG processing
```
// Kahn's algorithm (BFS-based)
int[] inDegree = new int[n];
Queue<Integer> q = new LinkedList<>();
for each edge (u → v): inDegree[v]++;
for (int i = 0; i < n; i++) if (inDegree[i] == 0) q.offer(i);
List<Integer> order = new ArrayList<>();
while (!q.isEmpty()) {
    int u = q.poll();
    order.add(u);
    for (int v : adj[u]) {
        if (--inDegree[v] == 0) q.offer(v);
    }
}
if (order.size() != n) → cycle exists (no valid ordering)
// Time: O(V + E)
```

### Trie (Prefix Tree)
**Trigger:** autocomplete, prefix matching, word search, longest common prefix, dictionary problems
```
class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEnd;
}
insert: walk/create nodes for each char, mark end
search: walk nodes, return isEnd at last char
startsWith: walk nodes, return true if path exists
// Time: O(L) per operation where L = word length
// Space: O(N × L) worst case
```

### Bit Manipulation
**Trigger:** single number, power of 2, counting bits, XOR tricks, subsets via bitmask
```
n & (n-1)        → removes lowest set bit (power of 2 check: n & (n-1) == 0)
n & (-n)         → isolates lowest set bit
n ^ n = 0        → XOR cancels duplicates (find single number)
n >> 1           → divide by 2
n << 1           → multiply by 2
(n >> i) & 1     → check if bit i is set
n | (1 << i)     → set bit i
n & ~(1 << i)    → clear bit i
n ^ (1 << i)     → toggle bit i
Integer.bitCount(n) → count set bits (popcount)
// Subset enumeration: for (int mask = 0; mask < (1 << n); mask++)
```

---

## 3. Pattern Selection Cheat

```
"subarray sum / max / min"            → Sliding Window or Prefix Sum
"pair with target sum (sorted)"       → Two Pointers
"pair with target sum (unsorted)"     → HashMap
"k-th largest / smallest"             → Heap (PriorityQueue) or Quickselect
"shortest path (unweighted)"          → BFS
"shortest path (weighted, no neg)"    → Dijkstra
"shortest path (negative edges)"      → Bellman-Ford
"all pairs shortest path"             → Floyd-Warshall
"connected components"                → DFS / BFS / Union-Find
"cycle detection (directed)"          → DFS with coloring (white/gray/black)
"cycle detection (undirected)"        → Union-Find or DFS with parent tracking
"dependency order"                    → Topological Sort
"generate all combinations"           → Backtracking
"count ways / min cost"               → DP
"interval merge / schedule"           → Sort + Greedy
"next greater / smaller"              → Monotonic Stack
"max in sliding window"               → Monotonic Deque
"prefix search / autocomplete"        → Trie
"find single / unique element"        → XOR (bit manipulation)
"median of stream"                    → Two Heaps (max + min)
"LRU Cache"                           → HashMap + Doubly Linked List
"merge K sorted lists"                → Min-Heap
"level order traversal"               → BFS with queue
"lowest common ancestor"              → DFS recursion on tree
"serialize / deserialize tree"        → BFS or preorder DFS
```

---

## 4. DSA Interview Framework (5-step)

```
1. CLARIFY     → constraints (n size? sorted? duplicates? negative?), edge cases (empty? single element?)
2. BRUTE FORCE → state the naive approach + complexity — "I can do O(n²) by checking all pairs"
3. OPTIMIZE    → identify the pattern — "since it's sorted, I can use two pointers for O(n)"
4. CODE        → write clean, correct code — good variable names, handle edge cases
5. TEST        → trace through example, edge cases (empty, single, all same, negative)
```

**During problem solving:**
```
- Talk through your thinking — silence is bad
- State time + space complexity BEFORE coding
- Start with the function signature
- Use helper methods for readability
- After coding: dry run with the given example
- Ask: "should I handle nulls / empty input?"
```

---

## 5. Data Structure Selection

```
Need fast lookup by key?                 → HashMap O(1)
Need sorted order + fast lookup?         → TreeMap O(log n)
Need fast insert/delete at both ends?    → Deque (ArrayDeque)
Need FIFO?                               → Queue (LinkedList / ArrayDeque)
Need LIFO?                               → Stack (ArrayDeque — avoid Stack class)
Need sorted + fast min/max extraction?   → PriorityQueue (Heap) O(log n)
Need fast contains check?               → HashSet O(1)
Need sorted unique elements?            → TreeSet O(log n)
Need indexed access + fast append?       → ArrayList O(1) amortized
Need fast insert/delete in middle?       → LinkedList O(1) at pointer (but O(n) find)
Need frequency count?                    → HashMap<T, Integer> or int[]
Need range sum queries?                  → Prefix sum array or Segment Tree
Need dynamic connectivity?              → Union-Find
```

---

## 6. Common DSA Questions — Approach Notes

### Arrays

**Two Sum**
```
Pattern: HashMap (complement lookup)
Map<Integer, Integer> map = new HashMap<>(); // val → index
for each num: if map.contains(target - num) → found; else map.put(num, i);
Time: O(n), Space: O(n)
```

**Three Sum**
```
Pattern: Sort + Two Pointers
Sort array. For each i, use two pointers (i+1, n-1) to find pairs summing to -arr[i]
Skip duplicates: if (i > 0 && arr[i] == arr[i-1]) continue;
Time: O(n²), Space: O(1) extra
```

**Maximum Subarray (Kadane's)**
```
Pattern: DP / Greedy
maxEndingHere = max(num, maxEndingHere + num);
maxSoFar = max(maxSoFar, maxEndingHere);
Time: O(n), Space: O(1)
Key: reset running sum when it goes negative
```

**Merge Intervals**
```
Pattern: Sort by start + linear merge
Sort by interval[0]. Merge if curr.start <= prev.end: prev.end = max(prev.end, curr.end)
Time: O(n log n), Space: O(n)
```

**Product of Array Except Self**
```
Pattern: Prefix + Suffix products
left[i] = product of all left of i; right[i] = product of all right of i
result[i] = left[i] × right[i]; Can use output array as left, single variable for right
Time: O(n), Space: O(1) extra (output not counted)
```

**Trapping Rain Water**
```
Pattern: Two Pointers or Prefix Max
water[i] = min(leftMax[i], rightMax[i]) - height[i]
Two pointer: maintain leftMax, rightMax, process smaller side
Time: O(n), Space: O(1) with two pointers
```

---

### Strings

**Valid Anagram**
```
Pattern: Frequency count (int[26] or HashMap)
Count chars in s1, decrement for s2, all counts should be 0
Time: O(n), Space: O(1) — fixed 26 chars
```

**Longest Substring Without Repeating Characters**
```
Pattern: Sliding Window + HashSet/HashMap
Expand right, if duplicate found → shrink left until removed
HashMap<Character, Integer> stores last index → jump left to max(left, lastIndex+1)
Time: O(n), Space: O(min(n, charset))
```

**Longest Palindromic Substring**
```
Pattern: Expand Around Center
For each center (and each gap between chars): expand while chars match
2n-1 centers total
Time: O(n²), Space: O(1)
// Manacher's for O(n) — rarely needed in interview
```

**Group Anagrams**
```
Pattern: HashMap with sorted-string key
Key = sorted chars of word; Value = list of words
Or: key = char frequency array as string "2#1#0#..." for O(n×k) vs O(n×k×log k)
Time: O(n × k log k), Space: O(n × k)
```

**KMP — Pattern Matching**
```
Pattern: Precompute LPS array (Longest Proper Prefix = Suffix), then match without re-scanning text
Time: O(n + m), Space: O(m)

// Build LPS: lps[i] = longest proper prefix of pattern[0..i] that is also a suffix
int[] lps = new int[m]; int len = 0, i = 1;
while (i < m) {
    if (pat[i] == pat[len]) lps[i++] = ++len;
    else if (len > 0) len = lps[len - 1];     // try shorter prefix, DON'T increment i
    else lps[i++] = 0;
}
// e.g. "ABABCABAB" → lps = [0,0,1,2,0,1,2,3,4]

// Search: i scans text, j scans pattern. On mismatch: j jumps to lps[j-1], i never goes back
int i = 0, j = 0;
while (i < n) {
    if (text[i] == pat[j]) { i++; j++; }
    if (j == m) { /* match at i-j */ j = lps[j - 1]; }
    else if (i < n && text[i] != pat[j]) {
        if (j > 0) j = lps[j - 1]; else i++;
    }
}
```

**Manacher's — Longest Palindromic Substring O(n)**
```
Pattern: Insert '#' between chars to handle even-length, track rightmost palindrome boundary
Time: O(n), Space: O(n)

// Preprocess: "abc" → "^#a#b#c#$" (sentinels avoid bounds checking)
char[] t = preprocess(s);
int[] p = new int[t.length]; // p[i] = palindrome radius at center i
int c = 0, r = 0;            // center and right edge of rightmost known palindrome

for (int i = 1; i < t.length - 1; i++) {
    int mirror = 2 * c - i;
    if (i < r) p[i] = Math.min(r - i, p[mirror]); // reuse mirror's radius
    while (t[i + p[i] + 1] == t[i - p[i] - 1]) p[i]++; // expand beyond known
    if (i + p[i] > r) { c = i; r = i + p[i]; }    // update rightmost palindrome
}
// Answer: max(p[i]). Convert index back: originalStart = (i - p[i]) / 2, length = p[i]
// Key: expand-around-center is O(n²); Manacher skips redundant expansions via mirror symmetry
```

---

### Linked Lists

**Reverse Linked List**
```
Pattern: Iterative with 3 pointers (prev, curr, next)
while (curr != null) { next = curr.next; curr.next = prev; prev = curr; curr = next; }
return prev; // new head
Time: O(n), Space: O(1)
```

**Detect Cycle**
```
Pattern: Fast & Slow Pointers (Floyd's)
slow=head, fast=head; move slow 1, fast 2
If they meet → cycle; To find start: reset one to head, move both 1 step
Time: O(n), Space: O(1)
```

**Merge Two Sorted Lists**
```
Pattern: Dummy head + iterate
dummy = new Node(0); tail = dummy;
Compare heads, append smaller, advance that pointer
Time: O(n + m), Space: O(1)
```

**LRU Cache**
```
Pattern: HashMap + Doubly Linked List
HashMap<key, Node> for O(1) lookup; DLL for O(1) insert/remove at ends
get: move to head (most recent); put: add to head, evict tail if over capacity
Time: O(1) for both get and put
```

---

### Trees

**Tree Traversals**
```
Inorder (L-Root-R): gives sorted order for BST
Preorder (Root-L-R): serialize tree, copy tree
Postorder (L-R-Root): delete tree, evaluate expression tree
Level order: BFS with queue, process by level
// Iterative inorder: stack + "go left as far as possible, pop, go right"
```

**Maximum Depth**
```
Pattern: DFS recursion
return (root == null) ? 0 : 1 + max(depth(left), depth(right));
Time: O(n), Space: O(h) — h = height
```

**Validate BST**
```
Pattern: DFS with min/max bounds
isValid(node, min, max): node.val must be in (min, max)
Recurse: left gets (min, node.val), right gets (node.val, max)
Or: inorder traversal should be strictly increasing
Time: O(n), Space: O(h)
```

**Lowest Common Ancestor (BST)**
```
If both values < node → go left; both > node → go right; else → current is LCA
Time: O(h)
```

**Lowest Common Ancestor (Binary Tree)**
```
Pattern: DFS recursion
If root is null or matches p or q → return root
Recurse left and right; if both non-null → root is LCA; else return non-null side
Time: O(n)
```

**Diameter of Binary Tree**
```
Pattern: DFS, track max(leftHeight + rightHeight) at each node
diameter = max(diameter, leftH + rightH);
return 1 + max(leftH, rightH); // return height to parent
Time: O(n)
```

**Serialize / Deserialize**
```
Pattern: Preorder DFS with "null" markers, or BFS level-order
Serialize: root,left,right with "X" for null
Deserialize: read values, recursively build left then right
```

---

### Graphs

**Number of Islands**
```
Pattern: DFS/BFS on grid, mark visited by setting '1' → '0'
For each unvisited '1': start DFS, count++
Time: O(m × n), Space: O(m × n) worst case stack
```

**Course Schedule (Cycle Detection)**
```
Pattern: Topological Sort (Kahn's BFS) or DFS with coloring
If topo sort processes all nodes → no cycle → can finish
DFS: white (unvisited), gray (in-progress), black (done) — gray→gray = cycle
Time: O(V + E)
```

**Dijkstra's Algorithm**
```
Pattern: BFS with PriorityQueue (min-heap)
dist[] initialized to INF; dist[src] = 0
Poll min-dist node, relax neighbors: if (dist[u] + w < dist[v]) update
PriorityQueue<int[]> pq — stores (distance, node)
Time: O((V + E) log V) with binary heap
⚠ Doesn't work with negative edges → use Bellman-Ford
```

**Clone Graph**
```
Pattern: DFS/BFS + HashMap<Original, Clone>
For each node: create clone, recursively clone neighbors
HashMap prevents revisiting (handles cycles)
Time: O(V + E)
```

---

### Dynamic Programming — Common Patterns

**0/1 Knapsack**
```
dp[i][w] = max value using items 0..i with capacity w
dp[i][w] = max(dp[i-1][w], dp[i-1][w-wt[i]] + val[i])  // skip or take
Space optimize: single row, iterate w from right to left
Time: O(n × W), Space: O(W)
```

**Longest Common Subsequence (LCS)**
```
dp[i][j] = LCS length of s1[0..i-1] and s2[0..j-1]
if s1[i-1] == s2[j-1]: dp[i][j] = dp[i-1][j-1] + 1
else: dp[i][j] = max(dp[i-1][j], dp[i][j-1])
Time: O(m × n), Space: O(m × n) → optimize to O(min(m,n))
```

**Longest Increasing Subsequence (LIS)**
```
O(n²): dp[i] = length of LIS ending at i; dp[i] = max(dp[j]+1) for j < i where arr[j] < arr[i]
O(n log n): maintain tails[] array, binary search for insertion point
Time: O(n log n) with patience sorting
```

**Coin Change**
```
dp[amount] = min coins to make amount
dp[0] = 0; for each coin: dp[a] = min(dp[a], dp[a - coin] + 1)
Time: O(amount × coins), Space: O(amount)
```

**House Robber**
```
dp[i] = max(dp[i-1], dp[i-2] + nums[i])  // skip or rob current
Can optimize to 2 variables (prev1, prev2)
Time: O(n), Space: O(1)
```

**Edit Distance**
```
dp[i][j] = min ops to convert s1[0..i-1] to s2[0..j-1]
if match: dp[i][j] = dp[i-1][j-1]
else: dp[i][j] = 1 + min(dp[i-1][j-1], dp[i-1][j], dp[i][j-1]) // replace, delete, insert
Time: O(m × n)
```

---

### Heap / Priority Queue

**Kth Largest Element**
```
Pattern: Min-heap of size K
Add elements, if size > k → poll. Top of heap = kth largest
Time: O(n log k)
Alternative: Quickselect — O(n) average, O(n²) worst
```

**Merge K Sorted Lists**
```
Pattern: Min-heap holding one node per list
Poll smallest, add its next to heap
Time: O(N log k) where N = total elements, k = lists
```

**Top K Frequent Elements**
```
Pattern: HashMap (frequency) + Min-heap of size K (or Bucket Sort for O(n))
Bucket sort: buckets[freq] = list of elements with that freq; iterate from high to low
Time: O(n) with bucket sort, O(n log k) with heap
```

**Median of Data Stream**
```
Pattern: Two heaps — maxHeap (lower half) + minHeap (upper half)
Balance: sizes differ by at most 1
Median: top of maxHeap (odd) or average of both tops (even)
Time: O(log n) per add, O(1) median
```

---

## 7. Common Edge Cases to Always Check

```
Empty input       : null, [], ""
Single element    : [1], "a"
All same          : [5,5,5,5], "aaaa"
Already sorted    : [1,2,3,4,5]
Reverse sorted    : [5,4,3,2,1]
Negative numbers  : [-1,-2,3], especially in sum/product problems
Integer overflow  : use long for products, mid = lo + (hi-lo)/2
Duplicates        : [1,1,2,2,3] — affects two pointers, binary search
Single node tree  : root with no children
Skewed tree       : all left or all right (height = n)
Disconnected graph: multiple components — iterate all nodes, not just node 0
Self-loops        : node pointing to itself
```

---

## 8. Quick Do's and Don'ts

```
DO:  state brute force first — then optimize ("I see O(n²), can I do O(n)?")
DO:  clarify constraints — n ≤ 10⁵ means O(n log n) is fine, O(n²) might TLE
DO:  use HashMap for O(1) lookup instead of nested loops
DO:  handle edge cases BEFORE the main logic (null checks, empty arrays)
DO:  dry-run your code on the given example before saying "done"

DON'T: jump to code without explaining approach
DON'T: use recursion without considering stack overflow (n > 10⁴ → use iterative)
DON'T: forget to mark visited in graph/grid traversals (infinite loop)
DON'T: modify input unless told you can (clone if needed)
DON'T: use int when result can overflow (e.g., factorial, large products → use long)
```

**Example — each DO/DON'T illustrated:**

```java
// ✗ DON'T: nested loop for lookup        ✓ DO: HashMap
for (int i : arr)                         // Set<Integer> set = new HashSet<>(list);
  for (int j : arr2)                      // for (int i : arr)
    if (i == j) found = true;             //   if (set.contains(i)) found = true;
// O(n²)                                  // O(n)

// ✗ DON'T: forget base case              ✓ DO: always handle null/empty
int maxDepth(TreeNode root) {             // int maxDepth(TreeNode root) {
  return 1 + max(maxDepth(root.left),     //   if (root == null) return 0;
             maxDepth(root.right));        //   return 1 + max(maxDepth(root.left),
}  // NPE when root is null               //              maxDepth(root.right));

// ✗ DON'T: int overflow                   ✓ DO: use long or prevent
int mid = (lo + hi) / 2;                  // int mid = lo + (hi - lo) / 2;
// overflows if lo + hi > 2³¹             // safe: no overflow possible

// ✗ DON'T: forget visited                 ✓ DO: mark before enqueue
q.offer(node);                            // visited.add(node);
// ... process ...                        // q.offer(node);
visited.add(node); // too late! dupes     // (mark BEFORE adding to queue, not after polling)
// already in queue

// ✗ DON'T: deep recursion on large n      ✓ DO: convert to iterative
// n=100,000 → StackOverflowError          // Use explicit stack or BFS queue
int fib(int n) { return fib(n-1)+fib(n-2); }  // → bottom-up DP with loop

// ✗ DON'T: mutate input silently          ✓ DO: ask or clone
Arrays.sort(nums);                         // "Can I modify the input array?"
// caller's array is now sorted            // int[] copy = nums.clone(); Arrays.sort(copy);
```

---

## 9. Complexity Cheat — "What fits in time?"

```
n ≤ 10       → O(n!) or O(2ⁿ)    — brute force, permutations fine
n ≤ 20       → O(2ⁿ)             — bitmask DP, subset enumeration
n ≤ 100      → O(n³)             — Floyd-Warshall, 3D DP
n ≤ 1,000    → O(n²)             — DP on pairs, brute force pairs
n ≤ 10⁵      → O(n log n)        — sorting, binary search, segment tree
n ≤ 10⁶      → O(n)              — single pass, two pointers, sliding window
n ≤ 10⁸      → O(n) barely       — simple loop, no allocation
n ≤ 10¹⁸     → O(log n)          — binary search, math, matrix exponentiation
```
