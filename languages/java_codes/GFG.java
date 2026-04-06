
import java.util.*;
import java.util.Map.Entry;

class code {
    public static void main(String[] args) {
        System.out.println("null");
    }
}

class Solution {
    public int lllll(int[][] routes, int source, int target) {
        if (source == target)
            return 0;
        int ans = 1;
        HashMap<Integer, List<Route>> routeBelongTo = new HashMap<>();
        for (int i = 0; i < routes.length; i++) {
            Route cur = new Route();
            for (int j = 0; j < routes[i].length; j++) {

                // List<Integer> neigh = adjList.getOrDefault(routes[i][j], new ArrayList<>());
                // neigh.add(routes[i][(j + 1) % routes[i].length]);
                // adjList.put(routes[i][j], neigh);

                routeBelongTo.computeIfAbsent(routes[i][j], k -> new ArrayList<>())
                        .add(cur);
                cur.stations.add(routes[i][j]);
            }

        }
        Queue<Route> q = new ArrayDeque<>();
        Iterator<Route> it = routeBelongTo.getOrDefault(source, new ArrayList<>()).iterator();
        while (it.hasNext()) {
            Route cur = it.next();
            cur.vis = true;
            q.offer(cur);
        }
        while (!q.isEmpty()) {
            int len = q.size();
            System.out.printf("len %d\n", len);
            while (len-- > 0) {
                Route curNode = q.poll();
                System.out.printf("curNode %d\n", curNode);
                for (Integer nextNode : curNode.stations) {
                    if (nextNode == target)
                        return ans;
                    for (Route r : routeBelongTo.get(nextNode)) {
                        if (!r.vis) {
                            r.vis = true;
                            q.offer(r);
                        }
                    }
                }
            }
            System.out.println();
            ans++;
        }
        return -1;
    }
}

class Route {
    // public List<Route> neighs;
    public List<Integer> stations;
    public boolean vis;

    Route() {
        // neighs = new ArrayList<>();
        stations = new ArrayList<>();
        vis = false;
    }

    public boolean validateStackSequences(int[] pushed, int[] popped) {
        int i = 0, j = 0, n = pushed.length;
        if (n == 0)
            return false;
        Stack<Integer> st = new Stack<>();
        while (j < n) {
            if (!st.isEmpty() && st.peek() == popped[j]) {
                st.pop();
                j++;
            } else {
                if (i == n)
                    return false;
                st.push(pushed[i++]);
            }
        }
        return st.isEmpty();
    }

    public int minTime(int n, int[][] edges, List<Boolean> hasApple) {
        List<Integer>[] adjList = (List<Integer>[]) new List[n];
        for (int i = 0; i < edges.length; i++) {
            if (adjList[edges[i][0]] == null)
                adjList[edges[i][0]] = new ArrayList<>();
            adjList[edges[i][0]].add(edges[i][1]);
        }
        dfs(0, adjList, hasApple);
        return 0;

    }

    int dfs(int node, List<Integer>[] adjList, List<Boolean> hasApple) {
        int ans = -1;
        for (int i = 0; i < adjList[node].size(); i++) {
            ans = Math.max(dfs(adjList[node].get(i), adjList, hasApple), ans);
        }
        if (ans == -1) {
            if (hasApple.get(node))
                return 0;
            else
                return -1;
        }
        return ans + 2;
    }

    public boolean isInterleave(String s, String t, String u) {
        int n = u.length(), a = s.length(), b = t.length();
        if (n != a + b)
            return false;
        boolean[][] valid = new boolean[a + 1][n + 1];
        valid[0][0] = true;
        for (int i = 1; i <= b; i++) {
            if (t.charAt(i - 1) == u.charAt(i - 1))
                valid[0][i] = true;
            else
                break;
        }
        for (int i = 1; i <= a; i++) {
            for (int j = 1; j <= n; j++) {
                if (valid[i - 1][j - 1])
                    if (s.charAt(i - 1) == u.charAt(j - 1)
                            || (j - i - 1 >= 0 && t.charAt(j - i - 1) == u.charAt(j - 1))) {
                        valid[i][j] = true;
                    }
            }
        }
        return valid[a][n];
    }

    public List<Integer> kClosestValues(TreeNode root, int target, int k) {
        // Max Heap to store the closest values
        Deque<Integer> q = new ArrayDeque<>();

        // In-order traversal helper function
        inOrderTraversal(root, target, k, q);

        // Extract result from the max heap
        return new ArrayList<>(q);
    }

    void inOrderTraversal(TreeNode root, int target, int k, Deque<Integer> q) {
        if (root == null)
            return;

        inOrderTraversal(root.left, target, k, q);
        if (q.size() < k) {
            q.offerLast(root.val);
        } else if (Math.abs(q.peekFirst() - target) > Math.abs(root.val - target)) {
            q.offerLast(root.val);
            q.pollFirst();
        }
        inOrderTraversal(root.right, target, k, q);
        Stack<Integer> st = new Stack<>();
        st.pop();
    }

    public int nextGreaterElement(int n) {
        char arr[] = Integer.toString(n).toCharArray();
        int i = arr.length - 1;
        StringBuilder sb = new StringBuilder();
        while (i > 0 && arr[i - 1] > arr[i]) {
            i--;
        }
        if (i == 0)
            return -1;
        else {
            int pivot = i - 1;
            char temp = arr[i];
            arr[i] = arr[i - 1];
            arr[i - 1] = temp;
            for (int j = i; j < arr.length; j++) {

            }
        }
        // 21
        // 2 3 1
        // 2 4 3 2 1
        // 3 4 2 2 1
        // 3 1 2 2 4
    }
}

class TreeNode {
    int val;
    TreeNode left, right;

    TreeNode(int val) {
        this.val = val;
    }
}
/*
 * -5 -3 -2 1 2 4 5 6
 * 
 * |r-l|+|r+l|==target
 * 
 * if(r+l<0){ -2*l}
 * else 2*r
 * 
 * if(l<=0 && l+r<=0){-2*l}
 * else if(r>=l) 2*r
 * 
 * freq[target/2]*
 * 
 * 
 * -2 -1 1 2 4 4
 * 
 * 
 */

public class GFG {

    static int celebrity(int[][] matrix, int n) {
        // This function returns the celebrity
        // index 0-based (if any)

        int i = 0, j = n - 1;
        while (i < j) {
            if (matrix[j][i] == 1) // j knows i
                j--;
            else // j doesn't know i so i can't be celebrity
                i++;
        }
        // i points to our celebrity candidate
        int candidate = i;

        // Now, all that is left is to check that whether
        // the candidate is actually a celebrity i.e: he is
        // known by everyone but he knows no one
        for (i = 0; i < n; i++) {
            if (i != candidate) {
                if (matrix[i][candidate] == 0
                        || matrix[candidate][i] == 1)
                    return -1;
            }
        }
        // if we reach here this means that the candidate
        // is really a celebrity
        return candidate;
    }

    public static void main(String[] args) {
        int n = 4;
        int[][] matrix = {
                { 0, 0, 1, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 0 },
                { 0, 0, 1, 0 }
        };

        int celebIdx = celebrity(matrix, n);

        if (celebIdx == -1)
            System.out.println("No Celebrity");
        else {
            System.out.println("Celebrity ID " + celebIdx);
        }
    }

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int n1 = nums1.length, n2 = nums2.length;
        if (n1 > n2)
            return findMedianSortedArrays(nums2, nums1);
        int total = (n1 + n2);
        int leftSize = (total) / 2;
        int l = -1, r = n1 - 1;
        while (true) {
            int aidx = l + (r - l) / 2;
            int bidx = leftSize - (aidx + 1) - 1;
            // System.out.printf("idx %d %d\n", aidx, bidx);
            int aleft = aidx < 0 ? Integer.MIN_VALUE : nums1[aidx],
                    bleft = bidx < 0 ? Integer.MIN_VALUE : nums2[bidx],
                    aright = aidx + 1 >= n1 ? Integer.MAX_VALUE : nums1[aidx + 1],
                    bright = bidx + 1 >= n2 ? Integer.MAX_VALUE : nums2[bidx + 1];
            // System.out.printf("%d %d %d %d\n", aleft, aright, bleft, bright);
            if (aleft <= bright && bleft <= aright) {
                if (total % 2 == 1) {
                    return Math.min(aright, bright);
                } else {
                    return ((double) Math.min(aright, bright) + (double) Math.max(aleft, bleft)) / 2;
                }
            } else if (aleft > bright) {
                r = aidx - 1;
            } else {
                l = aidx + 1;
            }
        }
    }

    public boolean containsNearbyDuplicate(int[] nums, int k) {
        HashSet<Integer> hs = new HashSet<>();
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            if (i >= k)
                hs.remove(nums[i - k]);
            if (hs.contains(nums[i]))
                return true;
            hs.add(nums[i]);
        }
        return false;
    }

    public boolean containsNearbyAlmostDuplicate(int[] nums, int indexDiff, int valueDiff) {
        TreeMap<Integer, Integer> pq = new TreeMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (i > indexDiff && pq.get(nums[i - indexDiff - 1]) == i - indexDiff - 1) {
                pq.remove(nums[i - indexDiff - 1]);
            }
            Entry<Integer, Integer> entry = pq.ceilingEntry(nums[i] - diff);
            if (entry.getKey() <= nums[i] + diff) {
                return true;
            }
            pq.put(nums[i], i);
        }

        // for (int i = 0; i < indexDiff; i++) {
        // pq.offer(new Node(nums[i], i));
        // }
        return false;
    }
}

class Node implements Comparable<Node> {
    public int idx, val;

    Node(int _val, int _idx) {
        val = _val;
        idx = _idx;
    }

    @Override
    public int compareTo(Node other) {
        return other.val - val;

    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        Node obj = (Node) other;
        return obj.val == val;

    }

    @Override
    public int hashCode() {
        return val;

    }
}
/*
 * 
 * 2
 * 1 3
 * 
 * 
 * 
 * 0 1 2 3 4 --- 5 6 7 8 9 10 11 12
 * n1=5, n2=8, total=13, left=6
 * 6ele 6 6ele
 * 
 * 0 1 2 3 4 --- 5 6 7 8 9 10 11 12 13
 * n1=5, n2=9, total=14, left=7=(total+1)/2
 * 
 * 0 1 2 3 4 --- 5 6 7 8 9
 * n1=5, n2=5, total=10, left=5
 */