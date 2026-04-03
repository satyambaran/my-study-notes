/*
=== RESOURCES ===
  OS Notes:         https://leetcode.com/discuss/interview-question/operating-system/6067720/operating-system-last-minutes-notes
  Java Concurrency: https://github.com/LeonardoZ/java-concurrency-patterns
  LLD Reference:    https://github.com/ashishps1/awesome-low-level-design
  Backend Map:      https://coggle.it/diagram/ZtMDf8rvLIwlbc-0/t/backend-engineering/44ac2c05e0c7fcdd9920723c49ec128e5a65bcc430a145638eb01f5c64a884a1
  Flipkart SDE-2:   https://leetcode.com/discuss/interview-experience/5650840/flipkart-sde-2-bengaluru-july-2024-offer

  Target: flexport, inmobi, Brevan Howard, myntra, c2fo, slice, phonepe, oracle, google, msft
	uber, stripe, coinbase, sprinklr, cred, cisco, razorpay, groww, ConcentricAI, kotak
	zepto, dream11, agoda, druva, walmart, flipkart

=== INTERVIEW MINDSET ===

	@ Quick checks
	Remember:
		* You gonna get stuck, thats the whole point of interview
		* Need to solve those with showing the thinking steps
		* Turn the interview in a conversation
	
	DSA Interview:
		Listen the questions
		` Reframe it and repeat
		` Ask all the details about the inputs
			Whether it's int, float, string, positive, negative
			In case of class and function Implementation
				Ask about read/write ratio
				Sparseness of binary string
				Whether to handle incorrect data, or just throw the exception
		` If you are assuming something, lets say book pages as index, then tell
		Think and tell about the solution first
		Clarifying questions if any
		` Tell time/space complexity before start Coding
	Process: Listen → Reframe → Clarify inputs (type, range, nulls, edge cases)
			→ State assumptions → Approach + complexity → Code → Test cases → Dry run
	For class/system design: ask read/write ratio, sparseness, error handling preference.

=== DSA APPROACHES ===

# Dynamic Programming
  When: answer is easy if you knew the answer to a smaller sub-problem. 
  Identify: optimal substructure + overlapping subproblems.

  Top-Down (Memoization): recurse + cache
	Recursively break it down to smaller subproblems and store the results to use it later whenever it's required again
	~ Definitely solves the smaller problems first, but it's WHILE solving the bigger problems
	Avoid redundant repetitive calculations using Memoization
    - Fibonacci, Climbing Stairs, 0/1 Knapsack
    - Mountain Range: https://cses.fi/problemset/result/16683972/
  Bottom-Up (Tabulation): fill table iteratively, small → large
	Build up the solution from smallest unit first iteratively by solving all the subproblems first
	~ Need to solve the sub-problems first to solve the bigger problem later
    - Longest Common Subsequence (LCS), Coin Change, Edit Distance

  Tricks:
    - 2D → 1D space optimization:  https://leetcode.com/problems/maximum-number-of-points-with-cost/solutions/5647834/
    - 2-directional DP:            https://leetcode.com/problems/candy/submissions/1462619749/
    - 3D DP — 
	  Burst Balloons: https://leetcode.com/problems/burst-balloons/submissions/1467194807/
      Merge Stones: https://leetcode.com/problems/minimum-cost-to-merge-stones/
    - DP + Bitmask: when n is small (~20), enumerate all subsets
		When we need to check all the combinations (e.g. Min XOR all pairs, TSP)

  Bellman-Ford: 
	Shortest path from a single source node to all other nodes 
  	SSSP with negative edges, O(VE). V-1 relaxations; Vth run detects negative cycle.

  Eulerian Circuit (visit every edge exactly once, return to start):
    - All node degrees must be even (undirected)
    - Hierholzer's: DFS; when no more edges from node, push to result
      dfs(node): while graph[node] not empty → pick & mark edge visited, dfs(neighbor); result.add(node)
	The objective is to visit every edge of the graph exactly once, starting and ending at the same vertex (in this case, the post office at crossing 1)
	:= 
		maintain a degree, check if all degree is even or not
		dfs(0)
		cout<< (result.size()==n+1) ?"Yes":"No";

		dfs(int node) {
			while (!graph[node].isEmpty()) {
				int neighbor = graph[node].remove(graph[node].size() - 1);
				long edgeId = getEdgeId(node, neighbor);
				if (visitedEdges.get(edgeId)) continue;
				visitedEdges.put(edgeId, true);
				dfs(neighbor);
			}
			result.add(node);
		}
  TSP (visit every vertex exactly once and return to start, minimize cost): DP + bitmask, O(2^n * n^2)
    https://www.geeksforgeeks.org/travelling-salesman-problem-using-dynamic-programming/

# Greedy
  Locally optimal choice → global optimum. Often sort first.
  - Activity Selection: Maximum number of activities that don’t overlap
  - Fractional Knapsack: Maximize profit by selecting fractional items
  - Huffman Coding
  - Buy & Sell Stock:      https://leetcode.com/problems/best-time-to-buy-and-sell-stock/submissions/1339882022/
  - Frog Jump II (tricky — greedy not obvious)(skipped greedy): https://leetcode.com/problems/frog-jump-ii/submissions/1468106726/

# Greedy + Heap (Priority Queue)
  Always process the min/max element next
  - Kth Largest Elements in an array
  - Top K Frequent Elements
  - Merge K Sorted Lists
  - Dijkstra's algorithm
  - Tough: https://pastebin.com/xxmHUvkx

# Prefix Sum
  O(n) preprocess → O(1) range queries.
  To calculate the sum of elements between any two indices
  - Subarray Sum Equals K
  - Range Sum Query: Efficiently calculate the sum of elements in a range
  - Max Sum Rectangle (2D): Find the maximum sum of a rectangle in a matrixhttps://www.geeksforgeeks.org/maximum-sum-rectangle-in-a-2d-matrix-dp-27/
  - XOR prefix: for XOR-range queries (xorPrefix[r] ^ xorPrefix[l-1])

# Two Pointers
  Same/opposite direction moving pointers on sorted or linear structures.
  - Merge Two Sorted Arrays
  - Container with Most Water
  - Two Sum: Find two numbers that add up to a specific target from an array (sort and 2-pointers) HashSet
  - Longest Substring Without Repeating Characters: Find the longest substring without repeating characters
  - Two Sum IV (On BST): 
	` BST Iterator
  	https://leetcode.com/problems/two-sum-iv-input-is-a-bst/submissions/1462653829/
  * Kadane's (max subarray, variable window):
    Fix start, extend end; reset start = end+1 when condition breaks.
    - Maximum Subarray: Find the contiguous subarray with the largest sum
	- Gas Station: https://leetcode.com/problems/gas-station/submissions/1401255598/

  * Sliding Window (fixed-size window, L and R stay distance k apart):
    - Maximum Sum Subarray of size k: Find the subarray with the maximum sum
	- Permutation in String: https://leetcode.com/problems/permutation-in-string/submissions/1462653829/

  * Rolling Hash (detect repeated substrings of fixed length):
    - Repeated DNA Sequences: https://leetcode.com/problems/repeated-dna-sequences/submissions/1386917764/

# Divide and Conquer
  Split → solve halves → merge. Recurrence: T(n) = aT(n/b) + f(n).
  - Merge Sort (stable, O(n log n))
  - Quick Sort (in-place, avg O(n log n))
  - Count Inversions (merge sort variant)

# Meet in the Middle
  Split the problem into two halves, solve each half, and then combine the results

  When 2^(n/2) is feasible but 2^n is not. Split, solve each half, combine.
  - Subset Sum, 4-Sum
  - LargestBST: https://www.geeksforgeeks.org/find-the-largest-subtree-in-a-tree-that-is-also-a-bst/

# Monotonic Stack
  Maintain strictly increasing or decreasing elements in a stack.
  When: next/previous greater/smaller element for each index.
  - Next Greater Element, Daily Temperatures
  - Largest Rectangle in Histogram: Find the largest rectangle in a histogramhttps://leetcode.com/problems/largest-rectangle-in-histogram/submissions/1378082792/
  - Car Fleet: https://leetcode.com/problems/car-fleet/

# Monotonic Deque
  Sliding window max/min in O(n). Deque stores indices in decreasing value order.
  Insert at back (pop smaller), evict front if out of window, front = current window max.
  Need to find the maximum or minimum element in a sliding window
  - Sliding Window Maximum: https://leetcode.com/problems/sliding-window-maximum/submissions/1414797294/
# Binary Search
  On sorted array, or when "is X a valid answer?" is easier than "find X directly".
			Find an element in a sorted array
			If checking whether a number is its solution is easier than finding the answer
				then decide the limits and use BS to get the answer
  - Rotated Sorted Array: https://leetcode.com/problems/search-in-rotated-sorted-array/submissions/1468114517/
  - Pattern: lo=minAnswer, hi=maxAnswer; binary search the answer space
  - Template: while(lo<=hi){ mid=(lo+hi)/2; if(valid(mid)) {ans=mid; hi=mid-1;} else lo=mid+1; }

# Backtracking
  Explore all possible solutions; undo state (backtrack) on dead ends.
  - Repetition allowed → pass same idx; no repetition → pass idx+1
  - Always reset any state changes/data before returning
  - Permutations:    https://leetcode.com/problems/permutations/submissions/1462664083/
  - N-Queens Problem: Place N queens on an NxN chessboard
  - Sudoku Solver,  Word Search
  - Combination Sum: Find all combinations of numbers that sum to a target
# DFS
  Explore branch fully (as far as possible along each branch) before backtracking.
  - Graph Traversal: Visit all nodes in a graph
  - Path finding: maintain parent of each node
  - Cycle (undirected): track parent; cycle if visited[neighbor != parent]
  - Cycle (directed):   track recursion stack (white/gray/black coloring)
  - Connected Components, Path Finding (track parent map for path reconstruction)
  - Round Trip II: maintain chain of DFS-visited vertices

  Strongly Connected Components — Kosaraju's:
	have adjList and it's transpose
    Pass 1: DFS on original graph → push node to stack on finish `foreach i if(!vis[i]) dfs1(i, adj)`
    dfs1(node, adj):     if vis → return; vis=1; for neigh: dfs1(neigh); stack.push(node)
    Pass 2: Reset visited; DFS on transposed graph in reverse finish order → each run = 1 SCC
    dfs2(node, k, adj2): if vis → return; vis=1; comp[node]=k; for neigh: dfs2(neigh)
    if any comp[i]==-1 → graph not strongly connected

# BFS
  Level-by-level: Explore all neighbors at the present level before moving on to nodes at the next depth level
  Optimal for unweighted shortest paths.
  - Snapshot queue.size() before each level (clean Level Order Traversal)
  - Shortest Path in Binary Matrix
  - Path tracing: store 'came from' node for each visited node

  Minimum Depth of Binary Tree: Find the minimum depth of a binary tree.
  ~ Level Order Traversal: Traverse a binary tree level by level
	Store length of queue at each level, before start processing it
  Multi-source BFS: enqueue all sources at start (e.g. 0-1 matrix distances, Monsters-CSES)

  Dijkstra (BFS + min-heap): SSSP, non-negative weights, O((V+E) log V)
  	Shortest path from a single source node to all other nodes in a graph with non-negative edge weights
  Floyd-Warshall: all-pairs shortest path, O(V^3). dp[i][j] = min over intermediate k.
	Shortest paths between all pairs of nodes in a weighted graph
	Planet queries (Binary jump)
		Given a transporter of each planet. Each planet has a teleporter to another planet (or the planet itself). 
			1. Determine if you start from a node, in k steps where you'll reach
			2. minimum steps to reach from a to b
		vector<vector<int>> v(n, vector<int>(30));
		for(i=0;i<n;i++) cin >> v[i][0];
		for(i=1;i<30;i++) for(j=0;j<n;j++) v[j][i] = v[v[j][i - 1]][i - 1];
		for (int i = 0; i < query.size(); i++) {
			for (int j = 0; j < 30; j++) {
				if (b & (1 << j)) {
					a = v[a][j];
				}
			}
		}
  Binary Lifting (functional graph — find node after k steps):
    Precompute v[j][i] = node reached from j after 2^i steps
    for i in 1..LOG: for j in 0..n-1: v[j][i] = v[v[j][i-1]][i-1]
    Query: decompose k in binary, apply jumps → O(log k)

# Union-Find (Disjoint Set)
  foreach i in 0..n-1: par[i]=i initially
  find(x):    if par[x]!=x → par[x]=find(par[x]); return par[x]   // path compression
  union(x,y): par[find(y)] = find(x)                               // union by rank for O(a(n))
  Complexity: O(a(n)) ~= O(1) per op (a = inverse Ackermann, <= 4 for any practical n)
			Kruskal’s Algorithm: Find the Minimum Spanning Tree of a graph.
  - Kruskal's MST: 
  	Sort edges by weight in non-decreasing order, 
	Pick the smallest edge and greedily add if no cycle (use union-find check). 
	Repeat until there are (V-1) edges in the spanning tree.
  - Number of Islands, Connected Components, Redundant Connection
  Worst case Time Complexity:
				The worst-case time complexity of the Union-Find (Disjoint Set) algorithm depends on how the data structure is implemented. Specifically, the complexity is determined by the techniques used to optimize the operations: path compression and union by rank/size.

				1. Basic Union-Find (No Optimization)

					•	Union Operation: ￼
					•	In the worst case, one tree is added as a child of another without considering their sizes or depths, leading to highly unbalanced trees.
					•	Find Operation: ￼
					•	Without optimization, the find operation might traverse the entire height of an unbalanced tree.

				2. Optimized Union-Find (with Path Compression and Union by Rank/Size)

					•	Union Operation: ￼
					•	Union by rank/size ensures smaller trees are attached to larger trees, keeping the tree height low.
					•	Find Operation: ￼
					•	Path compression flattens the tree structure, making subsequent find operations faster.

				Here:
					•	is the inverse Ackermann function, which grows extremely slowly.
					•	For all practical purposes, ￼ for any reasonable value of ￼, even for values as large as ￼.

				Overall Complexity (Optimized Implementation)

				For ￼ operations (unions or finds) on ￼ elements:
					•	Time Complexity: ￼

				Why is ￼ So Small?

				The Ackermann function, ￼, grows extremely fast, so its inverse grows extremely slowly. The inverse Ackermann function, ￼, is defined as the smallest ￼ such that:

				￼

				For practical scenarios:
					•	If ￼, then ￼.

				This means that for all real-world applications, the operations are nearly constant time.

				Conclusion

					•	Worst-case Time Complexity (Optimized): ￼ per operation.
					•	Practical Time Complexity: ￼ per operation due to the small value of ￼.
# Topological Sort
  Linear ordering of DAG(directed acyclic graph) nodes. Cycle detection: output size < n → cycle.
  When solving one problem leads to solve other problems
  Maintain InDegree of each node
  - Kahn's (BFS): track in-degrees, enqueue 0-in-degree, reduce neighbors on dequeue
  - DFS: post-order reverse = topo order
  - Course Schedule: If you can finish all courses given the prerequisites    https://leetcode.com/problems/course-schedule/
  - Alien Dictionary: Determine the order of characters in an alien language 
  - Job Scheduling with dependencies

# Tree
  Traversals: Pre/In/Post (DFS), Level-order (BFS)
  - Validate BST:        https://leetcode.com/problems/validate-binary-search-tree/submissions/1448909189/
  - Max Sum BST in Tree: https://leetcode.com/problems/maximum-sum-bst-in-binary-tree/submissions/1466798048/
  - Lowest Common Ancestor(LCA): recurse; return node if it matches p or q; LCA = node where both halves return non-null
  - Diameter: at each node, update global max with (left_depth + right_depth)

  AVL Tree (strictly balanced, |balance factor| <= 1):
    insert: recurse, update height, compute balance = depth(L) - depth(R)
    if |balance| > 1: rotate (LL→right rotate, RR→left, LR→left-right, RL→right-left)
    Rotation: (y.left=x; x.right=z) <=> (x.right=y; y.left=z)

	All nodes should be balanced, do shifting as many at required
	Node insert(node, key){
		if(node==null) return new Node(key);
		if(node.val==key) return node;
		if(key<node.val) node.left=insert(node.left,key)
		else node.right=insert(node.right, key)
		node.height = 1+max(depth(node.left), depth(node.right))
		int balance = depth(node.left)-depth(node.right)
		if(balance>1||balance<-1){
			(node=y) y->left=x; x->right=z;     <==>    x->right=y; y->left=z;  (node=x)
		}
		return node;
	}

  Red-Black Tree: root + null nodes always black. Looser than AVL → faster inserts.

# Trie (Prefix Tree)
  Node = { children[26], isEnd }. Insert/search O(L) per word.
  - Autocomplete, spell check, longest common prefix
  - Implement Trie: https://leetcode.com/problems/implement-trie-prefix-tree/
  - Word Search II:  https://leetcode.com/problems/word-search-ii/
  - XOR Trie: store binary digits of numbers → find max XOR pair in O(n log maxVal)

# Bit Manipulation
  1's Complement: ~n
  2's Complement: (~n)+1 = -n
  i & -i → isolates lowest set bit  (20→4, 22→2, 19→1)
  i-1 := flips all the bits right of right set bit(including that set bit)
  i & (i-1) → clears lowest set bit (Brian Kernighan)
  i - (i & (i - 1)) =  i & -i

  a ^ a = 0, a ^ 0 = a → XOR cancels duplicates

  - Single Number (XOR all):      https://leetcode.com/problems/single-number/
  - Count set bits: while(n){ n&=(n-1); count++; }
  - Next power of 2:              
  		k--; k|=k>>1; k|=k>>2; k|=k>>4; k|=k>>8; k|=k>>16; k++
  - Lowest set bit position:      n - (n&(n-1))
  - Check/Set/Clear/Toggle bit k: n&(1<<k), n|(1<<k), n&~(1<<k), n^(1<<k)

# Fenwick Tree (Binary Indexed Tree)
  Point update + prefix sum, O(log n). 1-indexed.
  Update(i, val): while i<=n { fen[i]+=val; i+=i&-i; }
  SumTillIdx(i):  sum=0; while i>0  { sum+=fen[i]; i-=i&-i; }
  - Range Sum, Frequency Tables, Count Inversions
  - Prefix Sum Queries, Range Sum Queries, Frequency Tables
  - Find lower_bound on BIT: https://www.youtube.com/watch?v=nuUspQ7ORXE
  https://codeforces.com/predownloaded/13/45/1345c040329da04363d61ef44be495950fc9ac55.gif
# Segment Tree
			Interval scheduling, Range-based statistics, 
			Used when you need to efficiently perform range queries and updates on an array.
			Both update and read can be done in O(log n)
			e.g.
				Sum of given range for each query 
					(SegmentTreeNode { int start, end, value; SegmentTreeNode left, right;})
					Populate segment tree: populate(int[] nums, int start, int end)
					Update segment tree: update(SegmentTreeNode node, int index, int value)
					Read Segment tree: rangeQuery(SegmentTreeNode node, int left, int right)
						check whether current node's start and end is  overlapping totally(value), partially(sum of each child) or partially(0)
  Interval scheduling
  Efficiently perform Range queries + point/range updates, O(log n). Build O(n).
  Node: { start, end, value, left, right }
  populate(int[] nums, node, start, end): if leaf → node.value=nums[start]; else recurse left/right, then node.value=merge(left.value, right.value)
  update(node, index, val): if leaf → update value; else recurse left/right based on index, then node.value = merge(left.value, right.value)
  rangeQuery(node, left, right): full overlap → value; no overlap → 0; partial → recurse both children
  Lazy propagation: defer range updates for O(log n) range-update + range-query.
  - Range Sum/Min/Max, Count Elements in Range, Interval Scheduling

=== MISC PATTERNS ===
  Intervals: sort by start → merge if overlap (curr.end >= next.start), else append
    - Merge Intervals, Meeting Rooms, Minimum Platforms

  LIS — O(n log n): maintain increasing 'tails' array; binary search for position
    - Min Removals for Mountain Array: https://leetcode.com/problems/minimum-number-of-removals-to-make-mountain-array/submissions/1401205256/

  Number of strictly increasing/decreasing trio: for each middle index, count smaller-left * larger-right.

  KMP (pattern matching O(n+m)): build LPS failure function array first
    - First Occurrence: https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/submissions/1362847216/

  Manacher's (longest palindromic substring, O(n)):
    - https://leetcode.com/problems/longest-palindromic-substring/submissions/740439346/

  Cycle Sort (O(n) time, O(1) space): place each element at its correct index (Swap in the list itself)
    - First Missing Positive: https://leetcode.com/problems/first-missing-positive/description/
	

  Linked List:
    - Nth from end: advance fast ptr n steps ahead, then move both until fast hits end
    - Floyd's Cycle: slow + fast ptr; meet → cycle. Reset one to head → meet again = cycle start
	- Detect Cycle, Determine Length of the Cycle, Break the Cycle in the LinkedList
    - 2D matrix → LL: https://www.geeksforgeeks.org/construct-a-linked-list-from-2d-matrix-iterative-approach/

  Lower Bound: https://leetcode.com/problems/contains-duplicate-iii/submissions/1460979384/
  Celebrity Problem: https://www.geeksforgeeks.org/the-celebrity-problem/
  Find single repeated number: negate nums[abs(nums[i])]; any index still positive = answer
  FiveTran trick: sort(nums); nums[i]-=i; find median.

=== DS DESIGN ===
  Min Stack: store (value, currentMin) pairs; peek min in O(1)
    https://leetcode.com/problems/min-stack/description/

  LRU Cache: HashMap<key, Node> + DoublyLinkedList (MRU at head, LRU at tail)
    get/put O(1): move accessed node to head; evict tail on overflow

  LFU Cache: HashMap<key, Node> + HashMap<freq, LinkedHashSet<key>> + minFreq pointer
    On access: move key to freq+1 bucket, update minFreq accordingly
    https://leetcode.com/problems/lfu-cache/submissions/1384768686/

  getMinElement:
    O(log n): AVL tree keyed on values + HashMap<key, treeNode>
    O(1):     min-heap + HashMap<key, heapIndex> for direct access

=== COMPLEXITY QUICK REF ===
  Sorting (comparison):  O(n log n)      | BFS/DFS:          O(V+E)
  Dijkstra:              O((V+E) log V)  | Bellman-Ford:     O(VE)
  Floyd-Warshall:        O(V^3)          | Kruskal:          O(E log E)
  Binary Search:         O(log n)        | Union-Find:       O(a(n)) ~= O(1)
  Segment/Fenwick:       O(log n)        | Build Seg Tree:   O(n)
  KMP / Z-function:      O(n+m)          | Manacher:         O(n)
  LIS:                   O(n log n)      | TSP (DP+bitmask): O(2^n * n^2)

	
	Data Structures:
		Array, Vector, List
		Stack, Queue, Priority_Queue, DeQueue
		Set, MultiSet, Unordered_Set, Map, Multimap, Unordered_Map,
		Tuple, Pair, String, StringStream, shared_ptr, unique_ptr, atomic
		Linked Lists, 
		Binary Trees & Binary Search Trees
		Self-balancing Trees (AVL Trees, Red-Black Trees, Splay Trees, B Tree, B+ Tree)
		Minimum Spanning Trees
		Disjoint Set Union
		Trie

		Maps & Hash Tables
		# Graph
			3 basic ways to represent a graph in memory (objects and pointers, matrix, and adjacency list)
		# Trees
			binary trees, n-ary trees, and trie-trees, self balancing tree(AVL, red-black)
		Heaps
		Tries
		Segment Trees
		Fenwick Trees

		reverse
tree, dp, binary search, oops, virtual, nodejs eventloop, acid, cap, solid, two pointer, sliding window, transaction DB, 

	https://coggle.it/diagram/ZtMDf8rvLIwlbc-0/t/backend-engineering/44ac2c05e0c7fcdd9920723c49ec128e5a65bcc430a145638eb01f5c64a884a1

	https://leetcode.com/discuss/interview-experience/5650840/flipkart-sde-2-bengaluru-july-2024-offer

*/

// ============================================================
// QUESTION BANK — grouped by topic, with key insights
// ============================================================

// --- DS DESIGN ---
"https://leetcode.com/problems/lfu-cache/submissions/1384768686/",         // HashMap<key,Node> + HashMap<freq,LinkedHashSet> + minFreq; on access: move to freq+1 bucket
"https://leetcode.com/problems/my-calendar-ii/submissions/1384734461/",    // TreeMap: +1 at start, -1 at end; running prefix sum > 2 = triple booking

// --- INTERVALS / SCHEDULING ---
"https://leetcode.com/problems/meeting-rooms-ii?envType=problem-list-v2&envId=prefix-sum",
 // start and end time can be very big, it's better to sort and then use only particular start time and end time only not the whole time
// sort start & end arrays separately; min-heap of end times; heap size = rooms needed
"https://leetcode.com/problems/maximum-number-of-events-that-can-be-attended/submissions/1374309703/",
// greedy: for each day, add available events to min-heap (by end); attend soonest-ending
"https://leetcode.com/problems/maximum-number-of-events-that-can-be-attended-ii/submissions/1414849490/",
// DP[i][k] = max value attending k events from i; binary search for next non-overlapping event
"https://leetcode.com/problems/minimum-number-of-taps-to-open-to-water-a-garden/submissions/1012841173/",
// reduce to Jump Game II; each tap covers [i-r, i+r]; build maxReach[] then greedy BFS levels

// --- GRAPH ---
"https://leetcode.com/problems/cheapest-flights-within-k-stops/submissions/1360366239/",
// k times level-wise Dijkstra but put all the candidates of the for loop in the queue
"https://leetcode.com/problems/second-minimum-time-to-reach-destination/submissions/1335506772/",
// BFS; maintain dist1 and dist2 per node; answer = dist2[target]; handle odd/even signal timing
"https://cses.fi/problemset/result/4307529/",
// Bellman-Ford for max score: store edge cost as negative (minimize = maximize); Vth pass marks neg-cycle reachable nodes as -INF
	//? c should be stored -ve of what we recieved, as it tries to minimise the score
	/*
        if (ans[a] == INF) continue;
        ans[b] = min(ans[b], c + ans[a]);
    */
	/**
        if (ans[a] == INF) continue;
        if (ans[b] > c + ans[a]) {
                ans[b] = NINF;
        } 
    */
"https://leetcode.com/problems/shortest-path-visiting-all-nodes/submissions/1161056037/",
// BFS + bitmask; state = (node, visitedMask); shortest path guaranteed by BFS layers
"https://wentao-shao.gitbook.io/leetcode/toposort/1136.parallel-courses",
// topo sort; answer = longest path in DAG (critical path); use BFS in-degree approach
"https://leetcode.com/problems/get-the-maximum-score/submissions/896173308/",
// two pointers on both sorted arrays; merge path at common values; always accumulate max side

// --- TREES ---
"https://leetcode.com/problems/all-nodes-distance-k-in-binary-tree/submissions/740268232/",
// convert tree to undirected graph (map child→parent); BFS from target node with dist k
"https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/submissions/1339987255/",
// recurse; return node if it's p or q; LCA = node where both halves return non-null
"https://leetcode.com/problems/house-robber-iii/submissions/529176938/",
// tree DP; each node returns pair (robThisNode, skipThisNode); propagate both choices up
"https://leetcode.com/problems/recover-binary-search-tree/submissions/742379443/",
// in-order traversal; find two swapped nodes: first = prev>curr, second = last such curr; swap vals
"https://leetcode.com/problems/maximum-product-of-splitted-binary-tree/submissions/427710013/",
// DFS for subtree sums; answer = max(sum * (total-sum)); compute total first
"https://leetcode.com/problems/distribute-coins-in-binary-tree/submissions/1339960049/",
// post-order; excess at node = coins-1; moves = sum of |excess| across all nodes
"https://leetcode.com/problems/step-by-step-directions-from-a-binary-tree-node-to-another/description/",
// get root→p and root→q paths; strip common prefix; p-remaining = 'U's, then q-remaining
"https://leetcode.com/problems/insufficient-nodes-in-root-to-leaf-paths/submissions/750529036/",
// post-order; prune node if max root-to-leaf sum through it < limit
"https://leetcode.com/problems/validate-binary-search-tree/submissions/755417043/",
// pass (min,max) bounds down; in-order traversal must be strictly increasing
"https://leetcode.com/problems/longest-univalue-path/submissions/747201640/",
// DFS; return longest arm with same val as parent; global max = left_arm + right_arm at each node
"https://leetcode.com/problems/diameter-of-binary-tree/submissions/894151232/",
// post-order; diameter at node = left_depth + right_depth; update global max
"https://leetcode.com/problems/balanced-binary-tree/submissions/637920669/", 
// return -1 to signal imbalance upward; balanced = no node has |left_depth - right_depth| > 1
"https://leetcode.com/problems/binary-tree-right-side-view/submissions/417061945/",
// BFS level order; last node of each level = right side view
"https://leetcode.com/problems/construct-string-from-binary-tree/submissions/748846255/",
// pre-order; always include left paren if right child exists; omit right-only empty parens
"https://leetcode.com/problems/binary-tree-paths/submissions/",
// DFS; pass path string down; on leaf, add to result; backtrack by string immutability
"https://leetcode.com/problems/add-one-row-to-tree/submissions/427713066/",
// BFS to depth d-1; new node's children = old node's left/right children
"https://leetcode.com/problems/house-robber-iii/submissions/529176938/",
// tree DP (see above)
"https://leetcode.com/problems/invert-binary-tree/submissions/725480243/",
// swap left & right at every node recursively or iteratively with BFS
"https://leetcode.com/problems/convert-sorted-array-to-binary-search-tree/submissions/894146690/",
// mid = root; recursively build on left half and right half → balanced BST

// --- STRINGS ---
// C++ API: s.replace(pos,len,str);  s.find(sub) == string::npos means not found
"https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/submissions/740455761/",
// KMP: build LPS (failure function) array; avoid redundant recomparisons; O(n+m)
"https://leetcode.com/problems/longest-palindromic-substring/submissions/740439346/",
// Manacher's: expand around 2n-1 centers; mirror property avoids recomputation; O(n)
"https://leetcode.com/problems/shortest-common-supersequence/submissions/725441958/",
// find LCS first; reconstruct SCS by merging both strings using LCS as skeleton
"https://leetcode.com/problems/word-ladder/submissions/554002228/",
// BFS; each word = node; try all 1-char mutations; use word set for O(1) lookup
//! fixed length queue process, to get to next step
"https://leetcode.com/problems/construct-string-with-minimum-cost/submissions/1362434976/",
// Aho-Corasick / Trie + DP; dp[i] = min cost to build s[0..i]; match all dict words at pos i
"https://leetcode.com/problems/interleaving-string/submissions/510555426/",
// 2D DP; dp[i][j] = can interleave s1[0..i] + s2[0..j] to form s3 prefix
"https://leetcode.com/problems/rotate-string/submissions/741102428/",
// s+s contains all rotations as substrings; check if goal is a substring of s+s
"https://leetcode.com/problems/remove-all-occurrences-of-a-substring/",
// simulate with stack; when tail of stack matches pattern, pop those chars
"https://leetcode.com/problems/number-of-substrings-containing-all-three-characters/",
// sliding window; for each r, track last position of a/b/c; ans += min(last_a,last_b,last_c)+1
"https://leetcode.com/problems/vowels-of-all-substrings/submissions/725417738/",
// contribution: vowel at index i appears in (i-0+1)*(n-1-i+1) substrings total
"https://leetcode.com/problems/smallest-subsequence-of-distinct-characters/submissions/515740380/",
// monotonic stack; only pop if char appears again later; greedy lex order
"https://leetcode.com/problems/word-break/submissions/551639526/",
// DP; dp[i] = s[0..i-1] segmentable; check all dict words ending at i
"https://leetcode.com/problems/restore-ip-addresses/submissions/414532333/",
// backtracking; 4 parts, each 0-255, no leading zeros; prune early
"https://leetcode.com/problems/palindromic-substrings/",
// expand around 2n-1 centers; count each valid expansion; O(n^2)
"https://leetcode.com/problems/find-all-anagrams-in-a-string/submissions/551625518/",
// sliding window of size |p|; compare char frequency maps; O(n)
"https://leetcode.com/problems/longest-palindrome-by-concatenating-two-letter-words/submissions",
// pair "ab"+"ba" words; one unpaired same-char word (e.g. "cc") can go in the middle
"https://leetcode.com/problems/longest-palindromic-subsequence/submissions/725186242/",
// DP; dp[i][j] = LPS of s[i..j]; if s[i]==s[j]: dp[i][j] = dp[i+1][j-1]+2
"https://leetcode.com/problems/minimum-ascii-delete-sum-for-two-strings/",
// LCS variant; minimize ASCII sum of deleted chars; dp[i][j] for s1[0..i], s2[0..j]
"https://leetcode.com/problems/longest-string-chain/submissions/742326594/",
// sort by word length; dp[word] = longest chain ending at word; try removing each char
"https://leetcode.com/problems/longest-common-subsequence/submissions/720620054/",
// classic DP; dp[i][j] = LCS of s1[0..i], s2[0..j]; O(mn)
"https://leetcode.com/problems/smallest-string-starting-from-leaf/submissions/631139660/",
// DFS collecting path chars; reverse at leaf; compare lexicographically; keep global min
// "abbbbbbccccccda → remove all 'bc' occurrences" — stack approach: push chars; when top+new == 'bc', pop top

// --- DP ---
"https://leetcode.com/problems/cherry-pickup-ii/description/",
// 3D DP (row, col1, col2); both robots move simultaneously row by row; 7 move combos per step
"https://leetcode.com/problems/unique-binary-search-trees-ii/submissions/1365152652/",
// recursion: for root i, left subtrees = generate(1,i-1), right = generate(i+1,n); combine all pairs
"https://leetcode.com/problems/partition-array-for-maximum-sum/submissions/1006949582/",
// DP; dp[i] = max sum for arr[0..i]; try all partitions of last k elements, keep max of partition
"https://leetcode.com/problems/painting-the-walls/submissions/1162570133/",
// paid painter costs time proportional to painted walls, free painter covers remaining; 0/1 knapsack
"https://leetcode.com/problems/coin-change-ii/",
// iterate coins outer, amounts inner (L→R) → unique combos not permutations
// for bounded/limited items: iterate amounts R→L to avoid reuse
"https://leetcode.com/problems/number-of-ways-to-earn-points/submissions/",
// 3D knapsack; dp[type][count][score]; add each problem type one by one
"https://leetcode.com/problems/stickers-to-spell-word/submissions/",
// BFS/DP + bitmask; state = which chars of target still needed; O(2^n * stickers)
"https://leetcode.com/problems/maximum-value-of-k-coins-from-piles/submissions/",
	// increase pile one by one, and keep best k. (break down k to z and k-z. k-z from previous piles and z from current piles)
// DP; dp[i][k] = max coins using first i piles taking k total; try 0..min(pileSize,k) from pile i
"https://leetcode.com/problems/best-time-to-buy-and-sell-stock-iv/",
//process k times
// DP with k transactions; dp[k][i] = max profit on day i with k txns; optimize with running max
"https://leetcode.com/problems/maximum-product-of-the-length-of-two-palindromic-subsequences/submissions/725337960/",
// bitmask DP (len <= 12); find palindrome subsets, pair two disjoint ones maximizing product

// --- ARRAYS / TWO POINTER ---
"https://leetcode.com/problems/shortest-subarray-with-sum-at-least-k/submissions/1187524694/",
// prefix sum + monotonic deque (increasing); pop front when sum condition met; handles negatives
"https://leetcode.com/problems/contains-duplicate-iii/",
// sliding window of size k + TreeSet (ordered); check if any val within valueDiff exists in window
"https://leetcode.com/problems/3sum/submissions/740444035/",
// sort; fix i, two-pointer l=i+1 r=n-1; skip consecutive duplicates for both i and pointers
"https://leetcode.com/problems/maximum-subarray/submissions/742064253/",
// Kadane's; maxEndingHere = max(num, maxEndingHere+num); reset on negative sum
"https://leetcode.com/problems/trapping-rain-water-ii/",
// min-heap BFS from boundary inward; water level = max boundary height seen; fill lower cells first
"https://leetcode.com/problems/reverse-nodes-in-k-group/submissions/",
// reverse k nodes iteratively; track prev-group tail to reconnect; verify k nodes exist before reversing

// --- BINARY SEARCH ---
// lower_bound = first element >= val | upper_bound = first element > val
// variants: https://www.geeksforgeeks.org/variants-of-binary-search/
"https://leetcode.com/problems/split-array-largest-sum/",
// BS on answer (min possible max-subarray-sum); validate with greedy: can we split into <= m parts?
"https://leetcode.com/problems/koko-eating-bananas/submissions/1313346886/",
// BS on speed; check: sum(ceil(pile/speed)) <= h hours
"https://leetcode.com/problems/find-the-smallest-divisor-given-a-threshold/submissions/788907798/",
// BS on divisor; validate: sum(ceil(n/d)) <= threshold
"https://leetcode.com/problems/count-negative-numbers-in-a-sorted-matrix/submissions/789849857/",
// staircase from top-right corner; each row sorted so walk left on negatives; O(m+n)
"https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/submissions/789874463/",
// BS on value; count elements <= mid using top-right staircase; O(n log(max-min))
"https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/submissions/789882721/",
// two pointers from both ends; sorted array guarantees O(n)
"https://leetcode.com/problems/median-of-two-sorted-arrays/",
// BS on smaller array; ensure left half has (m+n+1)/2 total elements; check cross-boundary condition
"https://leetcode.com/problems/search-a-2d-matrix/submissions/1004892084/",
// treat as 1D sorted array; index conversion: row=mid/cols, col=mid%cols
"https://leetcode.com/problems/longest-increasing-subsequence/submissions/787935818/",
// patience sort; tails[i] = smallest tail of LIS of length i+1; BS for insertion; O(n log n)
"https://leetcode.com/problems/russian-doll-envelopes/submissions/644809838/",
// sort by w asc, h desc; LIS on h only; desc h prevents same-width stacking

// --- JUMP GAMES ---
"https://leetcode.com/problems/jump-game-ii/submissions/887630259/",
// greedy BFS layers; track current range end + next max reach; jumps++ when end of level reached
"https://leetcode.com/problems/jump-game-v/submissions/1012385835/",
// DP + sort by height; can only jump to strictly lower height; dp[i] = max sequence starting at i
"https://leetcode.com/problems/jump-game-vi/submissions/1339229703/",
// DP + monotonic deque; dp[i] = max score reaching i; deque maintains max dp in window k
"https://leetcode.com/problems/jump-game-vii/submissions/1003571893/",
// BFS; maintain reach pointer to avoid revisiting; only enqueue unvisited cells in [i+minJ, i+maxJ]
"https://leetcode.com/problems/frog-jump/submissions/1003004221/",
// DP + HashMap; dp[stone] = set of valid k values; from stone s with jump k: try k-1,k,k+1
"https://leetcode.com/problems/maximum-number-of-jumps-to-reach-the-last-index/submissions/1339188686/",
// DP; dp[i] = max jumps to reach i; valid if |nums[j]-nums[i]| <= target

// --- MISC / INTERVIEW DISCUSSION ---
"https://leetcode.com/problems/top-k-frequent-elements/submissions/1360335922/",
// bucket sort by freq O(n); or min-heap of size k O(n log k)
"https://leetcode.com/problems/top-k-frequent-elements/solutions/5179176/hashmap-stack-o-n-c/",
// O(n) bucket sort detail
"https://leetcode.com/problems/minimum-number-of-food-buckets-to-feed-the-hamsters/submissions/742085465/",
// greedy; try placing bucket to the right first; if blocked, try left; O(n)
"https://leetcode.com/problems/number-of-ways-to-select-buildings/submissions/725430129/",
// prefix count of 0s and 1s; for each position: ways = count_opposite_left * count_opposite_right
"https://leetcode.com/problems/destination-city/submissions/893476174/",
// city not in any source = destination; use HashSet of all sources
"https://leetcode.com/problems/find-common-characters/submissions/635828266/",
// min frequency of each char across all words; chars with min > 0 are common
"https://leetcode.com/problems/longest-valid-parentheses",
// DP or stack; also scan L→R and R→L (both needed — "(()" returns 0 from one side only)
"https://leetcode.com/problems/minimum-remove-to-make-valid-parentheses/",
// stack of unmatched '(' indices; also mark unmatched ')' to remove; rebuild string
"https://leetcode.com/problems/combination-sum-ii/submissions/741639722/",
// backtrack + sort; skip duplicate elements at same recursion depth level
"https://leetcode.com/problems/create-maximum-number/submissions/",
// try all splits: i digits from arr1, k-i from arr2; merge two max-subsequences greedily
"https://leetcode.com/problems/longest-substring-without-repeating-characters/",
// two pointer/sliding window; HashMap tracks last seen index; extend right, shrink left on dup
"https://www.hackerrank.com/challenges/real-estate-broker/submissions/code/343435427",
// greedy: sort clients and houses; outer loop on clients (no repeat); for each client, pick cheapest house they accept; increasing j maintained by sorted order
	//! very `, notice not breaking once found and second ones order is kept that purposely so that increasing j follows that
	//! Since our outer loop is on clients(hence not repeating) so we need to maintain houses assigned already
	//! we need to push most costly house to clients which agreeable to them
	//! first we have already filled high area demand, thats why we can do it
"https://www.lintcode.com/problem/874/",
// DP + DFS: robot paths on grid

// --- NODE.JS / CONCURRENCY ---
"https://nodejs.org/en/guides/event-loop-timers-and-nexttick",          // official event loop phases: timers → I/O → idle → poll → check → close
"https://www.geeksforgeeks.org/how-single-threaded-handles-concurrency-when-multiple-i-o-operations-happening-in-node-js/",
"https://dev.to/arealesramirez/is-node-js-single-threaded-or-multi-threaded-and-why-ab1",
"https://dev.to/integridsolutions/understanding-node-js-single-threaded-server-side-language-3eed",
"https://www.youtube.com/watch?v=jgQjes7MgTM",
// Parallelism requires multicore; Concurrency is  achievable on single-thread via event loop
// Redis atomicity is a free consequence of single-threaded event loop — no locks needed; enables optimistic locking patterns
   "Redis operations are atomic is simply a consequence of the single-threaded event loop. The interesting point is atomicity is provided at no extra cost (it does not require synchronization). It can be exploited by the user to implement optimistic locking and other patterns without paying for the synchronization overhead.",
"https://intersog.com/blog/how-to-write-a-custom-url-shortener-using-golang-and-redis/",

// --- REFERENCE ---
"https://codeforces.com/edu/course/2",                                   // CP EDU: segment tree, DSU, suffix array courses
"https://www.youtube.com/watch?v=wXvljefXyEo", // system design deep dives
"https://www.educative.io/blog/how-to-prepare-system-design-interview",
"https://afteracademy.com/blog/what-happens-when-you-type-a-url-in-the-web-browser/",
"https://www.youtube.com/watch?v=SsPSJvH2mew&ab_channel=TheCodeSkool",
"http://www.tutorialspoint.com/cplusplus/cpp_templates.htm",
"https://leetcode.com/discuss/interview-experience/5426325/Flipkart-or-SDE-2-or-Backend-or-June-2024",
"https://leetcode.com/discuss/interview-question/4820505/Google-question/",
"https://leetcode.com/discuss/interview-question/4314794/GOOGLE-SDE-2-CODING-ROUND-1%3A-Find-the-in-compatible-pair-of-unit-tests/",
"https://www.codechef.com/viewsolution/1052100771",

	//?string questions
	// string.replace(pos, len, str);
	//size_t k=bigstr.find(str), k==string::npos

	// priority_queue<int>pq;4, 3, 1, -2
	// priority_queue<int, vector<int>, greater<int>> pq2; -2, 1, 3, 4

	//?binary search
	//lower_bound := first element which has a value not less than val
	//upper_bound := first element which has a value greater than val
	/*//?
        while (l <= r) {
            mid = l + (r - l) / 2; 
            if (grid[i][mid] < 0) {
                r = mid - 1; ans = mid;
            }
            else {
                l = mid + 1;
            }
        } 
    */

	// vector < int > v, u = { 1, 2, 3};
	// v=u; works
	// v={u.begin(), u.end()}; works// after either of two above, v==u true;

		/*
	5   [1,2,5]
	5, 221, 2111, 11111 (unique ways to create combination)
	5 (use one coin only once)
	5, 221, 212, 122, 2111, 1211, 1121, 1112, 11111 (number of ways to reach there)

    int change(int n, vector<int>& coins) {
        vector<int>dp(n+1,0);
        dp[0]=1;
        for(int i=0;i<coins.size();i++){
            for(int j=0;j<=n;j++){   //? this is for first case (for second case we need to use  j=n;j>-1;j-- )
                if(j>=coins[i]){
                    dp[j]+=dp[j-coins[i]];
                }
            }
        }
        return dp[n];
    }

	 j=n;j>-1;j--
		when we have limited items, then we use this way to avoid repeatition
	*/

//option is equivalent to command + D
// to move line up and down option+arrow
// command + L to highlight the line

// Subarray Sum Equals K: prefix sum + hashmap; freqMap[prefixSum-k] = count of valid subarrays
// "https://leetcode.com/problems/subarray-sum-equals-k/"
// Best Time to Buy and Sell Stock: track minPrice; maxProfit = max(price - minPrice)
// "https://leetcode.com/problems/best-time-to-buy-and-sell-stock/"

// --- GO NOTES ---

// goroutine basics: go func(){ defer wg.Done() }(); wg.Wait() — lightweight (~2KB vs ~1MB thread)
// panic/recover: recover() inside deferred func catches panics and prevents crash
// thread vs goroutine: goroutines use M:N threading (many goroutines on few OS threads), scheduled by Go runtime
// error handling: val, err := f(); if err != nil { ... }; fmt.Errorf("msg %w", err) wraps errors
// slice: len(s) = number of elements; cap(s) = underlying array capacity; len != cap is normal

