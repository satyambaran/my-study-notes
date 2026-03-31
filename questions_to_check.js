/*
https://leetcode.com/discuss/interview-question/operating-system/6067720/operating-system-last-minutes-notes
https://github.com/LeonardoZ/java-concurrency-patterns
flexport, inmobi, Brevan Howard, myntra, c2fo, slice, phonepe, oracle, google, msft, uber, stripe, coinbase, sprinklr, cred, cisco, razorpay, groww, ConcentricAI, kotak, zepto, dream11, agoda, druva, walmart, flipkart,

https://github.com/ashishps1/awesome-low-level-design?tab=readme-ov-file

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
		Code
		Write test cases
		Dry run

	DSA approaches:
		# Dynamic Programming (DP):
			When answer is easily gettable if you knew the answer of smaller question
			* Typical
				2-directional: 
					https://leetcode.com/problems/candy/submissions/1462619749/
			* Top-Down with Memoization (similar to recursion)
				Recursively break it down to smaller subproblems and store the results to use it later whenver it's required again
				~ Definitely solves the smaller problems first, but it's WHILE solving the bigger problems
				Avoid redundant repeatative calculations using Memoization
				e.g. Fibonacci numbers, Climbing Stairs, 0/1 Knapsack
				`Mountain Range: https://cses.fi/problemset/result/16683972/
			* Bottom-Up (Tabulation)
				Build up the solution from smallest unit first iteratively by solving all the subproblems first
				~ Need to solve the sub-problems first to solve the bigger problem later
				e.g. Longest Common Subsequence (LCS), Coin Change, Edit Distance
			* 3D dp:
				` https://leetcode.com/problems/burst-balloons/submissions/1467194807/
				https://leetcode.com/problems/minimum-cost-to-merge-stones/
			* DP with Bitmasking
				Check if n is small
				When we need to check all the combinations
					e.g.
						Minimum XOR of all pairs
			* Bellman-Ford Algorithm
				Shortest path from a single source node to all other nodes 
			* Eulerian Circuit (Mail Delivery Problem): 
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
			* Travelling Salesman Problem (TSP): 
				The objective is to visit every vertex exactly once and return to the starting vertex, while minimizing the total distance or cost of the tour
				https://www.geeksforgeeks.org/travelling-salesman-problem-using-dynamic-programming/
			* Use 1D instead of 2D
				https://leetcode.com/problems/maximum-number-of-points-with-cost/solutions/5647834/simple-easy-java-c-solution-beats-100-time-complexity-detailed-explaination/
		# Greedy:
			Make the locally optimal choice at each step, hoping to find the global optimum
			Sometimes require sorting of the array
			e.g. 
				Activity Selection: Maximum number of activities that don’t overlap
				Fractional Knapsack: Maximize profit by selecting fractional items
				Huffman Coding: Used in data compression
				Buy and sell stock: https://leetcode.com/problems/best-time-to-buy-and-sell-stock/submissions/1339882022/
				skipped greedy: https://leetcode.com/problems/frog-jump-ii/submissions/1468106726/
		# Greedy with Heap (Priority Queue)
			Use a heap to always get the minimum or maximum element efficiently
			e.g.
				Kth Largest Element in an Array
				Top K Frequent Elements
				Merge K Sorted Lists
				Dijkstra’s Algorithm
				tough: https://pastebin.com/xxmHUvkx
		# Prefix Sum
			To calculate the sum of elements between any two indices
			e.g.
				Subarray Sum Equals K
				Range Sum Query: Efficiently calculate the sum of elements in a range
				Maximum Sum Rectangle: Find the maximum sum of a rectangle in a matrix
					https://www.geeksforgeeks.org/maximum-sum-rectangle-in-a-2d-matrix-dp-27/
		# Two Pointers:
			* On BST:
				` BST Iterator
				https://leetcode.com/problems/two-sum-iv-input-is-a-bst/submissions/1462653829/
			* Two pointers(same or opposite direction)
				e.g. 
					Merge Two Sorted Arrays
					Container with Most Water
					Two Sum: 
						Find two numbers that add up to a specific target from an array (sort and 2-pointers)
						HashSet
					Longest Substring Without Repeating Characters: Find the longest substring without repeating characters
			* Kadane’s Algorithm:
				Start stays fix, end increases
				Once condition breaks, start = end + 1
					e.g. 
						Maximum Subarray: Find the contiguous subarray with the largest sum
						Gas Station: https://leetcode.com/problems/gas-station/submissions/1401255598/
			* Sliding Window:
				Distance of left and right pointers stays same
				e.g.
					Maximum Sum Subarray: Find the subarray with the maximum sum
					Permutation in String: Find if one string is a permutation of a substring in another string
			* Rolling Hash:
				To find repeated strings of given length
				e.g.
					Repeated Dna sequences: https://leetcode.com/problems/repeated-dna-sequences/submissions/1386917764/
		# Divide and Conquer
			Recursive
			Break problems into smaller problems and later merge the results at each step
			e.g.
				Merge Sort: Sort array by dividing it into two parts
				Quick Sort: Sort an array using a pivot
		# Meet in the Middle
			Split the problem into two halves, solve each half, and then combine the results
			e.g.
				Subset Sum Problem: Subset with a sum equal to a given target
				LargestBST: https://www.geeksforgeeks.org/find-the-largest-subtree-in-a-tree-that-is-also-a-bst/
		# Monotonic Stack
			Use a stack to maintain a sequence of elements that is either strictly increasing or decreasing
			Need to find the (next or previous) (greater or smaller element) for each element
			e.g.
				Next Greater Element
				Largest Rectangle in Histogram: Find the largest rectangle in a histogram
					https://leetcode.com/problems/largest-rectangle-in-histogram/submissions/1378082792/
				https://leetcode.com/problems/car-fleet/
		# Monotonic Queue
			Need to find the maximum or minimum element in a sliding window
			e.g.
				Sliding Window Maximum: https://leetcode.com/problems/sliding-window-maximum/submissions/1414797294/
					DeQue will be in decresing order always
					Will insert from front, check & delete from front, get the max value from back
		# Binary Search
			Find an element in a sorted array
			If checking whether a number is it's solution is easier then finding the answer
				then decide the limits and use BS to get the answer
			https://leetcode.com/problems/search-in-rotated-sorted-array/submissions/1468114517/
		# Backtracking
			Explore all possible solutions by selecting one at a time, use other when this doesn't lead to a solution
				(If repeatition is allowed use idx else idx+1 in the next function call), 
				(Reset the data if you have changed anything before calling)
			e.g.
				Permutations: https://leetcode.com/problems/permutations/submissions/1462664083/
				N-Queens Problem: Place N queens on an NxN chessboard
				Sudoku Solver
				Combination Sum: Find all combinations of numbers that sum to a target
		# Depth-First Search (DFS)
			Explore as far as possible along each branch before going back
			Usage:
				Graph Traversal: Visit all nodes in a graph
				Path finding: maintain parent of each node
				Connected Components: Find all connected components in a graph
				Round Trip II: maintain chain of dfs visited vertex
				Cycle Detection in Graph: Detect if a graph contains a cycle.
					Detect cycle in an undirected graph
						keep track of parent, if(visited[neigh except parent]==1) cycle detected in dfs
					Detect cycle in an directed graph
						Keep track of the visited nodes that are in the current dfs recursion stack
				Strongly Connected Components:
					have adjList and it's transpose
					foreach i if(!vis[i]) dfs(i, adj)
					Arrays.fill(vis, false);
					// a vector will be generated by this, use that for dfs2
					for(i=v.size()-1;i>-1;i--) if(!vis[v[i]]) dfs2(v[i], k++, adj2)
					foreach i:= if(comp[i]==-1) return -1;//not a SCC. failed to reach this node

					dfs(i, adj):= if(vis[i])return; vis[i]=1;  for(neigh:adj[i])dfs(i); V.push_BACK(i);
					dfs2(i,k,adj2):= if(vis[i])return; vis[i]=1; comp[i] = k; for(neigh:adj2[i])dfs2(i);

		# Breadth-First Search (BFS)
			Explore all neighbors at the present level before moving on to nodes at the next depth level
			e.g.
				Shortest Path in a Binary Matrix: Find the shortest path from top-left to bottom-right in a binary matrix
				Path direction:
					maintian 'where did you came from' for each node
				Minimum Depth of Binary Tree: Find the minimum depth of a binary tree.
				~ Level Order Traversal: Traverse a binary tree level by level
					Store length of queue at each level, before start processing it
			* Multi-source bfs
				e.g.
					Monsters from cses
			* Dijkstra’s Algorithm (BFS + greedy by heap)
				Shortest path from a single source node to all other nodes in a graph with non-negative edge weights
			* Floyd-Warshall
				Shortest paths between all pairs of nodes in a weighted graph
			* Planet queries (Binary jump)
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
		# Union-Find (Disjoint Set)
			Implementation:
				Initially: foreach i par[i]=i
				Union:	union(x,y) := parent[find(y)]=find(x)
				Find: find(x):= if(parent[x]!=x) return find(parent[x]) else return x;
			Kruskal’s Algorithm: Find the Minimum Spanning Tree of a graph.
				Sort all the edges in non-decreasing order of their weight. 
				Pick the smallest edge. Check if it forms a cycle with the spanning tree formed so far. If the cycle is not formed, include this edge. Else, discard it. 
				Repeat step#2 until there are (V-1) edges in the spanning tree.
			Number of Islands: Find the number of islands in a grid
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
		# Topological Sorting
			Linear ordering of vertices in a directed acyclic graph (DAG)
			When solving one problem leads to solve other problems
			Maintain InDegree of each node
			e.g.
				Course Schedule: If you can finish all courses given the prerequisites
				Alien Dictionary: Determine the order of characters in an alien language
				Job Scheduling: Order of jobs with dependencies
		# Tree 
			Pre-order, In-order, Post-order, Level-order(BFS)
			e.g.
				https://leetcode.com/problems/validate-binary-search-tree/submissions/1448909189/
				https://leetcode.com/problems/maximum-sum-bst-in-binary-tree/submissions/1466798048/
				Lowest Common Ancestor
				Diameter of the tree
			Self balancing tree:
				AVL:
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
				Red-Black:
					Root and null nodes are always black
		# Bit Manipulation
			1's Complement: ~n
			2's Complement: (~n)+1 = -n
			i & -i
				isolates the rightmost set bit (the lowest 1) 20:4, 22:2, 18:2, 19:1
			i - (i & (i - 1))
				same as i & -i
				i-1 := flips all the bits right of right set bit(including that set bit)
				i&(i-1) := the all bits same as before the rightmost set bit

			e.g.
				Single Number:
				Next Power of Two:
					k = 66; k--; k|=(k>>1);k|=(k>>2);k|=(k>>4);k|=(k>>8);k|=(k>>16); k++;
				Brian Kernighan’s Algorithm: n & (n-1)
					Counting Set Bits:
						int countSetBits(int n) {
							int count = 0;
							while (n > 0) {
								n = n & (n - 1); // Removes the lowest set bit
								count++;
							}
							return count;
						}
					Position of the Lowest Set Bit:
						n = n-(n&(n-1)) := 8->8, 15->1, 16->16, 10->2 
		# Binary Indexed Tree (Fenwick Tree)
			e.g.
				Prefix Sum Queries, Range Sum Queries, Frequency Tables
				find lower_bound := https://www.youtube.com/watch?v=nuUspQ7ORXE
			Update(id, add) := while(i<n){fen[i]+=add; i+=(i&-i);} add the rightmost set bit
			SumTillIdx(idx) := sum=0; while(idx>0){sum+=fen[idx]; idx -= (idx&(-idx))};
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
		# Other Random ones
			* Number of strictly increasing/decreasing trio
				take middle index, get number of greater and smaller each side
			* Longest increasing subsequence 
				https://leetcode.com/problems/minimum-number-of-removals-to-make-mountain-array/submissions/1401205256/
			* Knuth-Morris-Pratt (KMP) Algorithm
				https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/submissions/1362847216/
			* Manacher’s Algorithm
				For finding palindromes in string
				https://leetcode.com/problems/longest-palindromic-substring/submissions/740439346/
			* Swap in the list itself:
				If constant space complexity is required
				e.g.
					https://leetcode.com/problems/first-missing-positive/description/
			* Cycle sort:
				Keep placing element at correct places
				e.g.
					https://leetcode.com/problems/first-missing-positive/description/
			* Linked List
				Find nth node from end
					Move the fast pointer `n` steps ahead. Now, move both fast and slow pointers one step at a time until fast reaches the end of the list.
				Detect Cycle, Determine Length of the Cycle, Break the Cycle in the LinkedList
				https://www.geeksforgeeks.org/construct-a-linked-list-from-2d-matrix-iterative-approach/
			* Lower Bound
				https://leetcode.com/problems/contains-duplicate-iii/submissions/1460979384/
			* Celebrity-problem
				https://www.geeksforgeeks.org/the-celebrity-problem/
			* FiveTran:
				sort(nums); num[i]-=i; find median;
			* Payu:
				Only one number is repeated, find that:
					negate the value at that index
		# DS design
			Min Stack: https://leetcode.com/problems/min-stack/description/
			LRU, LFU, 
			getMinElement
				in O(log n): selfBalancing Tree Node(on node.val)(AVL), hashMap<key, Node>
				in O(1): heap, with idx of each Node(key, val) in hashMap
			
sum((a_i)+(a_i+1 - 1)+(a_i+2 - 2)....(a_i+k - k))
	
	Data Structures:
		Array, Vector, List, Stack, Queue, Priority_Queue, DeQueue, Set, MultiSet, Unordered_Set, Map, Multimap, Unordered_Map,
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

let obj = [
	"https://leetcode.com/problems/lfu-cache/submissions/1384768686/",
	"https://leetcode.com/problems/my-calendar-ii/submissions/1384734461/",
	"https://leetcode.com/problems/meeting-rooms-ii?envType=problem-list-v2&envId=prefix-sum", // start and end time can be very big, it's better to sort and then use only particular start time and end time only not the whole time
	"https://leetcode.com/problems/maximum-number-of-events-that-can-be-attended-ii/submissions/1414849490/",
	"https://leetcode.com/problems/maximum-number-of-events-that-can-be-attended/submissions/1374309703/",
	"https://leetcode.com/problems/construct-string-with-minimum-cost/submissions/1362434976/",
	"https://leetcode.com/problems/unique-binary-search-trees-ii/submissions/1365152652/",

	"https://leetcode.com/problems/top-k-frequent-elements/submissions/1360335922/",
	"https://www.lintcode.com/problem/874/", // dp+dfs
	"https://leetcode.com/problems/cheapest-flights-within-k-stops/submissions/1360366239/",
	// k times Dijkstra but put all the candidate in the queue
	"https://leetcode.com/problems/second-minimum-time-to-reach-destination/submissions/1335506772/",
	// maintain two times to reach each node
	"https://leetcode.com/problems/median-of-two-sorted-arrays/",
	"https://leetcode.com/problems/top-k-frequent-elements/solutions/5179176/hashmap-stack-o-n-c/",
	"https://leetcode.com/problems/koko-eating-bananas/submissions/1313346886/",
	"https://leetcode.com/discuss/interview-experience/5426325/Flipkart-or-SDE-2-or-Backend-or-June-2024",
	"https://leetcode.com/problems/cherry-pickup-ii/description/",
	"https://leetcode.com/problems/create-maximum-number/submissions/",
	"https://leetcode.com/discuss/interview-question/4820505/Google-question/",
	"https://leetcode.com/discuss/interview-question/4314794/GOOGLE-SDE-2-CODING-ROUND-1%3A-Find-the-in-compatible-pair-of-unit-tests/",

	"https://www.codechef.com/viewsolution/1052100771",
	"https://www.youtube.com/watch?v=jgQjes7MgTM",
	"https://nodejs.org/en/guides/event-loop-timers-and-nexttick",
	"https://www.geeksforgeeks.org/how-single-threaded-handles-concurrency-when-multiple-i-o-operations-happening-in-node-js/",
	"https://dev.to/arealesramirez/is-node-js-single-threaded-or-multi-threaded-and-why-ab1",
	"https://dev.to/integridsolutions/understanding-node-js-single-threaded-server-side-language-3eed",

	"Parallelism can only be achieved in multicore environments",
	"Concurrency can be on Single-threaded server as well using event loop",
	"Redis operations are atomic is simply a consequence of the single-threaded event loop. The interesting point is atomicity is provided at no extra cost (it does not require synchronization). It can be exploited by the user to implement optimistic locking and other patterns without paying for the synchronization overhead.",

	"https://intersog.com/blog/how-to-write-a-custom-url-shortener-using-golang-and-redis/",

	"http://www.tutorialspoint.com/cplusplus/cpp_templates.htm",
	"https://codeforces.com/edu/course/2",
	"https://www.youtube.com/watch?v=wXvljefXyEo&ab_channel=AsliEngineeringbyArpitBhayani",
	"https://www.educative.io/blog/how-to-prepare-system-design-interview",
	"https://afteracademy.com/blog/what-happens-when-you-type-a-url-in-the-web-browser/",

	"https://www.youtube.com/watch?v=SsPSJvH2mew&ab_channel=TheCodeSkool",


	// ! "questions folder",
	"https://leetcode.com/problems/contains-duplicate-iii/",
	"abbbbbbccccccda, remove all the occurences of bc, use stack method",
	"https://leetcode.com/problems/shortest-subarray-with-sum-at-least-k/submissions/1187524694/",

	"https://leetcode.com/problems/frog-jump/submissions/1003004221/",
	"https://leetcode.com/problems/maximum-number-of-jumps-to-reach-the-last-index/submissions/1339188686/",
	"https://leetcode.com/problems/search-a-2d-matrix/submissions/1004892084/",
	"https://leetcode.com/problems/partition-array-for-maximum-sum/submissions/1006949582/",
	"https://leetcode.com/problems/minimum-number-of-taps-to-open-to-water-a-garden/submissions/1012841173/",

	"https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/submissions/740455761/", // kmp algorithm
	"https://leetcode.com/problems/longest-palindromic-substring/submissions/740439346/", // mirror
	"https://leetcode.com/problems/minimum-remove-to-make-valid-parentheses/",
	"https://leetcode.com/problems/longest-substring-without-repeating-characters/submissions/510182271/",
	"https://leetcode.com/problems/3sum/submissions/740444035/",
	"https://leetcode.com/problems/longest-valid-parentheses", // both side are necessary because of (() and it'll return 0
	"https://leetcode.com/problems/sudoku-solver", // backtrack
	"https://leetcode.com/problems/combination-sum-ii/submissions/741639722/",
	"https://leetcode.com/problems/maximum-subarray/submissions/742064253/",
	"https://leetcode.com/problems/jump-game-ii/submissions/887630259/",
	"https://leetcode.com/problems/jump-game-v/submissions/1012385835/",
	"https://leetcode.com/problems/jump-game-vi/submissions/1339229703/",
	"https://leetcode.com/problems/jump-game-vii/submissions/1003571893/",

	//?string questions
	// string.replace(pos, len, str);
	//size_t k=bigstr.find(str), k==string::npos
	"https://leetcode.com/problems/find-common-characters/submissions/635828266/",
	"https://leetcode.com/problems/construct-string-from-binary-tree/submissions/748846255/", //!string+tree
	"https://leetcode.com/problems/step-by-step-directions-from-a-binary-tree-node-to-another/description/", //! get path from root to each node and remove wahts matching
	"https://leetcode.com/problems/binary-tree-paths/submissions/", //! let loose all dfs case and store
	"https://leetcode.com/problems/rotate-string/submissions/741102428/", //! merge string and find the string between 0, 2n-2 https://leetcode.com/problems/repeated-substring-pattern/description/
	"https://leetcode.com/problems/remove-all-occurrences-of-a-substring/",
	"https://leetcode.com/problems/palindromic-substrings/",
	"https://leetcode.com/problems/number-of-substrings-containing-all-three-characters/",

	//!

	"https://leetcode.com/problems/minimum-ascii-delete-sum-for-two-strings/",
	"https://leetcode.com/problems/longest-palindromic-subsequence/submissions/725186242/",
	"https://leetcode.com/problems/longest-string-chain/submissions/742326594/",
	"https://leetcode.com/problems/longest-common-subsequence/submissions/720620054/",
	"https://leetcode.com/problems/smallest-subsequence-of-distinct-characters/submissions/515740380/",
	"https://leetcode.com/problems/vowels-of-all-substrings/submissions/725417738/", //! total possible substrings using ith char = (i-0+1)*(n-1-i+1)
	"https://leetcode.com/problems/destination-city/submissions/893476174/",
	"https://leetcode.com/problems/maximum-product-of-the-length-of-two-palindromic-subsequences/submissions/725337960/",
	"https://leetcode.com/problems/number-of-ways-to-select-buildings/submissions/725430129/",
	"https://leetcode.com/problems/find-all-anagrams-in-a-string/submissions/551625518/",
	"https://leetcode.com/problems/smallest-string-starting-from-leaf/submissions/631139660/",
	//! let loose all string, and check which is smallest
	"https://leetcode.com/problems/longest-palindrome-by-concatenating-two-letter-words/submissions",
	"https://leetcode.com/problems/restore-ip-addresses/submissions/414532333/",
	"https://leetcode.com/problems/word-break/submissions/551639526/",
	"https://leetcode.com/problems/minimum-number-of-food-buckets-to-feed-the-hamsters/submissions/742085465/",
	"https://leetcode.com/problems/longest-palindromic-substring/submissions/740439346/",
	"https://leetcode.com/problems/longest-substring-without-repeating-characters/submissions/510182271/", //! two pointer
	"https://leetcode.com/problems/interleaving-string/submissions/510555426/", //!`
	"https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/submissions/740455761/", //! kmp
	"https://leetcode.com/problems/shortest-common-supersequence/submissions/725441958/", //! very very `
	"https://leetcode.com/problems/word-ladder/submissions/554002228/", //! fixed length queue process, to get to next step
	"https://www.hackerrank.com/challenges/real-estate-broker/submissions/code/343435427",
	//! very `, notice not breaking once found and second ones order is kept that purposely so that increasing j follows that
	//! Since our outer loop is on clients(hence not repeating) so we need to maintain houses assigned already
	//! we need to push most costly house to clients which agreeable to them
	//! first we have already filled high area demand, thats why we can do it

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
	// https://www.geeksforgeeks.org/variants-of-binary-search/
	"https://leetcode.com/problems/split-array-largest-sum/",
	"https://leetcode.com/problems/count-negative-numbers-in-a-sorted-matrix/submissions/789849857/",
	"https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/submissions/789874463/",
	"https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/submissions/789882721/",
	"https://leetcode.com/problems/find-the-smallest-divisor-given-a-threshold/submissions/788907798/",
	"https://leetcode.com/problems/longest-increasing-subsequence/submissions/787935818/",
	"https://leetcode.com/problems/russian-doll-envelopes/submissions/644809838/",

	//tree
	"https://leetcode.com/problems/invert-binary-tree/submissions/725480243/",
	"https://leetcode.com/problems/convert-sorted-array-to-binary-search-tree/submissions/894146690/",
	"https://leetcode.com/problems/diameter-of-binary-tree/submissions/894151232/",
	"https://leetcode.com/problems/balanced-binary-tree/submissions/637920669/", //! balanced binary tree means none of the node with left and right depth diff > 1
	"https://leetcode.com/problems/validate-binary-search-tree/submissions/755417043/",
	"https://leetcode.com/problems/longest-univalue-path/submissions/747201640/",
	"https://leetcode.com/problems/all-nodes-distance-k-in-binary-tree/submissions/740268232/", //! very `
	"https://leetcode.com/problems/binary-tree-right-side-view/submissions/417061945/",
	"https://leetcode.com/problems/add-one-row-to-tree/submissions/427713066/",
	"https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/submissions/1339987255/", //! `
	"https://leetcode.com/problems/house-robber-iii/submissions/529176938/",
	"https://leetcode.com/problems/insufficient-nodes-in-root-to-leaf-paths/submissions/750529036/",
	"https://leetcode.com/problems/recover-binary-search-tree/submissions/742379443/",
	"https://leetcode.com/problems/maximum-product-of-splitted-binary-tree/submissions/427710013/", //!`
	"https://leetcode.com/problems/distribute-coins-in-binary-tree/submissions/1339960049/", //!very `

	//graph

	"https://cses.fi/problemset/result/4307529/", //!bellman ford, loop through n times both of the time
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
	"https://leetcode.com/problems/get-the-maximum-score/submissions/896173308/",

	// vector < int > v, u = { 1, 2, 3};
	// v=u; works
	// v={u.begin(), u.end()}; works// after either of two above, v==u true;

	"https://leetcode.com/problems/reverse-nodes-in-k-group/submissions/",
	"https://leetcode.com/problems/best-time-to-buy-and-sell-stock-iv/", //process k times
	"https://leetcode.com/problems/trapping-rain-water-ii/",
	"https://leetcode.com/problems/maximum-value-of-k-coins-from-piles/submissions/",
	// increase pile one by one, and keep best k. (break down k to z and k-z. k-z from previous piles and z from current piles)
	"https://leetcode.com/problems/coin-change-ii/",
	"https://leetcode.com/problems/number-of-ways-to-earn-points/submissions/",
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
	"https://leetcode.com/problems/stickers-to-spell-word/submissions/",

	"https://leetcode.com/problems/shortest-path-visiting-all-nodes/submissions/1161056037/",
	"https://wentao-shao.gitbook.io/leetcode/toposort/1136.parallel-courses",
	"https://leetcode.com/problems/painting-the-walls/submissions/1162570133/",
];

//option is equivalent to command + D
// to move line up and down option+arrow
// command + L to highlight the line

let topics = ["http range requests", "http-redirect"];

/*
package main
import "fmt"
 

//  * Complete the 'solution' function below.
//  *
//  * The function is expected to return an INTEGER.
//  * The function accepts STRING a as parameter.


func solution(array []int, k int) int {
	totalSubArray := 0
	len := len(array)
	if len==0 {
		return 0
	}
	freqMap:=map[int]int{}
	freqMap[0]=1
	for i:=1;i<len;i++{
		array[i] = array[i]+array[i-1]
	}
	// 1 3 6
	for i:=0;i<len;i++{
		if val,ok:=freqMap[array[i]-k]; ok{
			totalSubArray += val
		}
		freqMap[array[i]]++
	}

	return totalSubArray
}


func main() {
	array:=[]int{1, 2, 3}
	k:=3
	fmt.Println(solution(array, k))
}


Given an array of integers and an integer k, determine the total number of continuous subarrays whose sum equals to k

2
array:= 4 1 -1 2 2
prefix_sum:= 4 5 4 6 8


Constraints:

The length of the input array can be up to 10^4.
The array can contain both positive and negative integers.




Example:

Input: nums = [1, 1, 1], k = 2

Output: 2




Input: nums = [1, 2, 3], k = 3

Output: 2



wg:=sync.waitGroup
wg.Add(1)

go func(){
	defer wg.Done()
}()
wg.Wait()

slice:=[]int{}
len(slice) != cap(slice)

val, err := function()
if err!=nil{

}

0, error.New("j")
0, fmt.Errorf("error raised by fucntion %w", err)


panic()
recover()


thread vs goroutine

package main
import (
	"fmt"
	"math"
)

 * Complete the 'solution' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts STRING a as parameter.

func solution(prices []float64) float64 {
	len:=len(prices)
	if len<2{
		return 0
	}
	minPrice:=prices[0]
	// maxPrice:=price[0]
	maxProfit:=0.0
	for i:=1;i<len;i++{
		if maxProfit < prices[i]-minPrice{
			maxProfit = prices[i]-minPrice
		}
		minPrice = math.Min(minPrice, prices[i])
	}
	return maxProfit
}


func main() {
	prices:=[]float64{7,6,4,3,1}

	fmt.Println(solution(prices))
}


You are given an array prices where prices[i] is the price of a given stock on the ith day.

You want to maximize your profit by choosing a single day to buy one stock and choosing a different day in the future to sell that stock.

Return the maximum profit you can achieve from this transaction. If you cannot achieve any profit, return 0.

 

Example 1:

Input: prices = [7,1,5,3,6,4]

Output: 5

Explanation: Buy on day 2 (price = 1) and sell on day 5 (price = 6), profit = 6-1 = 5.

Note that buying on day 2 and selling on day 1 is not allowed because you must buy before you sell.

Example 2:

Input: prices = [7,6,4,3,1]

Output: 0

Explanation: In this case, no transactions are done and the max profit = 0.

 

Constraints:

1 <= prices.length <= 105
0 <= prices[i] <= 104


*/