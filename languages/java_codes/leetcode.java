import java.util.*;

public class leetcode {
    public static void main(String[] args) {
        // [3,9,20,null,null,15,7]
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(9);
        root.right = new TreeNode(20);
        root.right.left = new TreeNode(15);
        root.right.right = new TreeNode(7);
        System.out.println(zigzagLevelOrder(root));
    }

    public int[] maxSlidingWindow(int[] nums, int k) {
        if (k == nums.length)
            return new int[] { Arrays.stream(nums).max().getAsInt() };
        Deque<Integer> dq = new ArrayDeque<>();
        dq.addLast(nums[0]);
        int n = nums.length;
        int[] ans = new int[n - k + 1];
        for (int i = 0; i < k; i++) {
            while (!dq.isEmpty() && nums[dq.peekFirst()] <= nums[i]) {
                dq.pollFirst();
            }
            dq.addFirst(i);
        }
        for (int i = k; i < n; i++) {
            ans[i - k] = dq.peekLast();
            while (!dq.isEmpty() && nums[dq.peekFirst()] <= nums[i]) {
                dq.pollFirst();
            }
            dq.addFirst(i);
            while (i - k + 1 > dq.peekLast()) {
                dq.pollLast();
            }
        }
        ans[n - k] = nums[dq.peekLast()];
        return ans;
    }

    public int largestRectangleArea(int[] heights) {
        Stack<int[]> st = new Stack<>();
        int n = heights.length, ans = 0, startsFrom;
        st.add(new int[] { 0, 0 });// val-idx, starts-from
        for (int i = 1; i < n; i++) {
            startsFrom = i;
            while (!st.isEmpty() && heights[st.peek()[0]] > heights[i]) {
                int[] cur = st.pop();
                startsFrom = cur[1];
                ans = Math.max(ans, (i - startsFrom + 1) * heights[cur[0]]);
            }
            st.add(new int[] { i, startsFrom });
        }
        while (!st.isEmpty()) {
            int[] cur = st.pop();
            startsFrom = cur[1];
            ans = Math.max(ans, (n - startsFrom) * heights[cur[0]]);
        }
        return ans;
    }

    public boolean findTarget(TreeNode root, int k) {
        TreeNode cur = root;
        Stack<TreeNode> left = new Stack<>();
        Stack<TreeNode> right = new Stack<>();
        // Stack<TreeNode> right = new ArrayDeque<>();
        while (cur != null) {
            left.add(cur);
            cur = cur.left;
        }
        cur = root;
        while (cur != null) {
            right.add(cur);
            cur = cur.right;
        }
        int sum = 0;
        while (!left.isEmpty() && !right.isEmpty()) {
            TreeNode ln = left.peek();
            TreeNode rn = right.peek();
            sum = ln.val + rn.val;
            if (ln == rn)
                break;
            if (sum == k) {
                return true;
            } else if (sum < k) {
                // move left
                cur = left.pop();
                cur = cur.right;
                while (cur != null) {
                    left.add(cur);
                    cur = cur.left;
                }
            } else {
                // move right
                cur = right.pop();
                cur = cur.left;
                while (cur != null) {
                    right.add(cur);
                    cur = cur.right;
                }
            }
        }
        return false;
    }

    class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public int maxRectSum(int mat[][]) {
        if (mat.length == 0 || mat[0].length == 0)
            return 0;
        int l = 0, r = 0, n = mat.length, m = mat[0].length, sum = 0, maxSum = Integer.MIN_VALUE;
        int[] pf = new int[n];
        while (l < m) {
            r = l;
            for (int i = 0; i < n; i++)
                pf[i] = 0;
            while (r < m) {
                for (int i = 0; i < n; i++) {
                    pf[i] += mat[i][r];
                }
                sum = 0;
                for (int cur : pf) {
                    sum += cur;
                    maxSum = Math.max(sum, maxSum);
                    if (sum < 0)
                        sum = 0;
                }
                r++;
            }
            l++;
        }
        return maxSum;
    }

    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>((x, y) -> x.val - y.val);
        for (int i = 0; i < lists.length; i++) {
            pq.offer(lists[i]);
        }
        ListNode ans = new ListNode(-1);
        ListNode temp = ans;
        while (pq.size() > 1) {
            temp.next = pq.poll();
            temp = temp.next;
            if (temp.next != null) {
                pq.offer(temp.next);
            }
        }
        if (pq.size() == 1)
            temp.next = pq.poll();
        return ans.next;
    }

    long[][][] memo;

    public long maximumProfit(int[] prices, int k) {
        int n = prices.length;
        memo = new long[n][k + 1][3];
        for (long[][] a : memo)
            for (long[] b : a)
                Arrays.fill(b, Long.MIN_VALUE);
        return dfs(prices, 0, k, 0);
    }

    int dfs(int[] prices, int idx, int trnsLeft, int hold) {
        if (idx >= prices.length || trnsLeft == 0)
            return 0;
        if (memo[idx][trnsLeft][hold] != Long.MIN_VALUE)
            return (int) memo[idx][trnsLeft][hold];
        int res = dfs(prices, idx + 1, trnsLeft, hold); // skip
        if (hold == 0) {
            // buy
            int buy = -prices[idx] + dfs(prices, idx + 1, trnsLeft, 1);
            res = Math.max(res, buy);
            // sell
            int sell = prices[idx] + dfs(prices, idx + 1, trnsLeft, 2);
            res = Math.max(res, sell);
        } else if (hold == 1) {
            // sell (have bought already)
            int sell = prices[idx] + dfs(prices, idx + 1, trnsLeft - 1, 0);
            res = Math.max(res, sell);
        } else if (hold == 2) {
            // buy (have sold already)
            int buy = -prices[idx] + dfs(prices, idx + 1, trnsLeft - 1, 0);
            res = Math.max(res, buy);
        }
        memo[idx][trnsLeft][hold] = res;
        return res;
    }

    static List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> ans = new ArrayList<>();
        if (root == null)
            return ans;
        Deque<TreeNode> dq = new ArrayDeque<TreeNode>();
        dq.offer(root);
        int level = 0;
        while (!dq.isEmpty()) {
            int len = dq.size();
            List<Integer> temp = new ArrayList<>();
            while (len-- > 0) {
                TreeNode cur = (level % 2 == 1) ? dq.pollLast() : dq.pollFirst();
                System.out.printf("%d ", cur.val);
                temp.add(cur.val);
                if (level % 2 == 1) {
                    dq.addFirst(cur.right);
                    dq.addFirst(cur.left);
                } else {
                    dq.addLast(cur.left);
                    dq.addLast(cur.right);
                }
            }
            System.out.println();
            level++;
            ans.add(temp);
        }
        return ans;
    }
}
// 1
// 2 3
// 4 5 6 7
// 8 9 10 11 12 13 14 15
// 2 3 4 5 6 7 8 9 23 33 43 54 65 34
// 556 12 34 12 23 32 23 33 43 54 65 34

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode() {
    }

    TreeNode(int val) {
        this.val = val;
    }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}

class Solution {
    public int longestConsecutive(int[] nums) {
        HashMap<Integer, Integer> mp = new HashMap<>();
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            Integer val = mp.get(nums[i] - 1);
            if (val == null) {
                mp.put(nums[i], 1);
            } else {
                mp.put(nums[i], val + 1);
                mp.remove(nums[i]);
            }
        }
        int ans = 0;
        for (Map.Entry<Integer, Integer> val : mp.entrySet()) {
            ans = Math.max(ans, val.getValue());
        }
        return ans;
    }

    private Map<String, List<TreeNode>> mp = new HashMap<>();

    private List<TreeNode> func(int l, int r) {
        if (l > r)
            return Collections.singletonList(null);
        if (l == r)
            return List.of(new TreeNode(l));
        StringBuilder sb = new StringBuilder();
        sb.append(l).append('-').append(r);
        String key = sb.toString();
        if (mp.containsKey(key)) {
            return mp.get(key);
        }
        for (int i = l; i <= r; i++) {
            List<TreeNode> left = func(l, i - 1);
            List<TreeNode> right = func(i + 1, r);
            for (TreeNode ln : left) {
                for (TreeNode rn : right) {
                    TreeNode node = new TreeNode(i);
                    node.left = ln;
                    node.right = rn;
                    mp.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
                }
            }
        }
        return mp.get(key);
    }

    public List<TreeNode> generateTrees(int n) {
        return func(1, n);
        // StringBuilder sb = new StringBuilder();
        // sb.append(1).append('-').append(n);
        // return mp.get(sb.toString());
    }

    public long getRangeSum(long[] prefixSum, int start, int end) {
        if (start > end)
            return 0;
        if (end == 0)
            return prefixSum[0];
        return prefixSum[end] - ((start > 0) ? prefixSum[start - 1] : 0);
    }

    public long[] maximumSegmentSum(int[] nums, int[] removeQueries) {
        long[] prefixSum = new long[nums.length];
        prefixSum[0] = nums[0];
        for (int i = 1; i < nums.length; i++) {
            prefixSum[i] = prefixSum[i - 1] + nums[i];
        }
        TreeMap<Integer, Integer> segments = new TreeMap<>(); // start -> end
        segments.put(0, nums.length - 1);
        TreeMap<Long, Integer> sums = new TreeMap<>(); // sum -> count
        long total = prefixSum[nums.length - 1];
        sums.put(total, 1);
        long[] ans = new long[removeQueries.length];
        for (int i = 0; i < removeQueries.length; i++) {
            int key = segments.floorKey(removeQueries[i]);
            long rangeSum = getRangeSum(prefixSum, key, segments.get(key));
            if (sums.get(rangeSum) == 1) {
                sums.remove(rangeSum);
            } else {
                sums.merge(rangeSum, -1, Integer::sum);
            }
            sums.merge(getRangeSum(prefixSum, key, removeQueries[i] - 1), 1, Integer::sum);
            sums.merge(getRangeSum(prefixSum, removeQueries[i] + 1, segments.get(key)), 1, Integer::sum);
            if (removeQueries[i] + 1 <= segments.get(key))
                segments.put(removeQueries[i] + 1, segments.get(key));
            segments.remove(key);
            if (key <= removeQueries[i] - 1)
                segments.put(key, removeQueries[i] - 1);
            ans[i] = sums.lastKey();
        }
        return ans;
    }

    public String largestMultipleOfThree(int[] digits) {
        StringBuilder sb = new StringBuilder();
        Arrays.sort(digits);
        int total = Arrays.stream(digits).sum();
        int rem = total % 3;
        int idx = -1;
        if (rem != 0)
            for (int i = 0; i < digits.length; i++) {
                if (digits[i] % 3 == rem) {
                    idx = i;
                    break;
                }
            }
        for (int i = digits.length - 1; i > -1; i--)
            if (i != idx)
                sb.append(digits[i]);
        return sb.toString();
    }

    int[] dx = new int[] { 0, 1, 0, -1 };
    int[] dy = new int[] { 1, 0, -1, 0 };

    private int dfs(int x, int y, int[][] matrix, int[][] dis) {
        if (dis[x][y] != 0)
            return dis[x][y];
        int n = matrix.length, m = matrix[0].length, ans = 0;
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && ny >= 0 && nx < n && ny < m && matrix[nx][ny] > matrix[x][y]
                    && (dis[nx][ny] == 0 || dis[nx][ny] < 1 + dis[x][y])) {
                ans = Math.max(ans, 1 + dfs(nx, ny, matrix, dis));
            }
        }
        dis[x][y] = ans;
        return ans;
    }

    public int longestIncreasingPath(int[][] matrix) {
        int n = matrix.length, m = matrix[0].length, ans = 0;
        int[][] dis = new int[n][m];
        // PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> x[0] - y[0]);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                ans = Math.max(ans, dfs(i, j, matrix, dis));
            }
        }
        return ans;
    }

    // int[] dxx = Arrays.of(0, 0, -1, 1);
    // int[] dyy = Arrays.of(-1, 1, 0, 0);

    // public int shortestPath(int[][] grid, int k) {
    // int n = grid.length, m = grid[0].length;
    // if (k >= n + m - 3)
    // return n + m - 2;
    // // we will process all the nodes and maintain a queue of nodes reachable in
    // // minimum state
    // int[][][] dis = new int[n][m][k];
    // for (int i = 0; i < n; i++)
    // for (int j = 0; j < m; j++)
    // Arrays.fill(dis[i][j], Integer.MAX_VALUE);
    // Queue<int[]> q = new ArrayDeque<>();
    // q.push(Arrays.of(0, 0, k));
    // // int[] base = dis[0][0];
    // // Arrays.fill(base, 0);
    // dis[0][0][k] = 0;
    // while (!q.isEmpty()) {
    // int[] cur = q.poll();
    // int x = cur[0], y = cur[1], rem = cur[2];
    // // if(dis[x])
    // for (int i = 0; i < 4; i++) {
    // int nx = x + dx[i], ny = y + dy[i];
    // if (grid[nx][ny] == 1) {
    // if (dis[nx][ny][rem - 1] > dis[x][y][rem] + 1) {
    // dis[nx][ny][rem - 1] = dis[x][y][rem] + 1;
    // q.push(Arrays.of(nx, ny, rem - 1));
    // }
    // } else {
    // if (dis[nx][ny][rem] > dis[x][y][rem] + 1) {
    // dis[nx][ny][rem] = dis[x][y][rem] + 1;
    // q.push(Arrays.of(nx, ny, rem));
    // }
    // }
    // }
    // }
    // int ans = Arrays.min(dis[n - 1][m - 1]);
    // return ans == Integer.MAX_VALUE ? -1 : ans;
    // }

    public int carFleet(int target, int[] position, int[] speed) {
        /*
         * x u. target
         * y v
         * 
         * pos = x + ut = y+vt <= target
         * 
         * t = (x-y)/(v-u)
         * 
         * (xv -xu + ux -uy)/(v-u) <= target
         * (xv-uy)/(v-u) <= t
         * 
         */
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> y[0] - x[0]);
        for (int i = 0; i < position.length; i++) {
            pq.add(new int[] { position[i], speed[i] });
        }
        int ans = 1;
        int curPos = pq.peek()[0], curSpeed = pq.peek()[1];
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            // is catchable
            if (cur[0] == curPos) {

            } else {
                if (cur[1] > curSpeed) {
                    // might be catchable
                    long dis1 = (long) curPos * cur[1] - (long) cur[0] * curSpeed;
                    long dis2 = (long) target * (cur[1] - curSpeed);
                    if (dis1 <= dis2) {
                        // catchable
                    } else {
                        // not catchable
                        curPos = cur[0];
                        curSpeed = cur[1];
                        ans++;
                    }
                } else {
                    // not catchable
                    curPos = cur[0];
                    curSpeed = cur[1];
                    ans++;
                }
            }
        }
        return ans;
    }
}
