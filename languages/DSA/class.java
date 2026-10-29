import java.util.*;

class Solution {
    int ans = 0;

    int func(List<Integer> nums, int target, int idx) {
        if (target == 0)
            return 0;
        if (idx == nums.size()) {
            return Integer.MIN_VALUE;
        }

        printNumbers(nums, target, idx);
        return Math.max(func(nums, target, idx + 1), 1 + func(nums, target - nums.get(idx), idx + 1));
    }

    public int lengthOfLongestSubsequence(List<Integer> nums, int target) {
        int n = nums.size();
        int[] dp = new int[target + 1];
        for (int i = 0; i < n; i++) {
            for (int j = target; j >= nums.get(i); j--) {
                dp[i] = Math.max(1 + dp[j - nums.get(i)], dp[i]);
            }
        }
        return dp[target];
    }

    public int minimumVisitedCells(int[][] grid) {
        int n = grid.length, m = grid[0].length;
        if (n == 0 || m == 0)
            return 0;
        boolean[][] vis = new boolean[n][m];
        int[] rows = new int[n], cols = new int[m];
        Queue<int[]> q = new ArrayDeque<>();
        for (int ans = 1; !q.isEmpty(); ans++) {
            for (int i = q.size(); i > 0; i--) {
                int[] cur = q.poll();
                int x = cur[0], y = cur[1];
                if (x == n - 1 && y == m - 1)
                    return ans;
                for (int j = Math.max(y + 1, rows[x]); j <= grid[x][y] + y && j < m; j++) {
                    vis[x][j] = true;
                    q.offer(new int[] { x, j });
                }
                // while (rows[x] < m && vis[x][rows[x]] == true) {
                // rows[x]++;
                // }
                rows[x] = Math.max(rows[x], Math.min(m - 1, grid[x][y] + y));
                for (int j = Math.max(x + 1, cols[y]); j <= grid[x][y] + x && j < n; j++) {
                    vis[j][y] = true;
                    q.offer(new int[] { j, y });
                }
                cols[y] = Math.max(cols[y], Math.min(n - 1, grid[x][y] + x));
            }
        }
        return -1;
    }

    public int minimumVisitedCells(String ans) {
        return -1;
    }

    public void printNumbers(Object... objs) {
        for (Object obj : objs) {
            System.out.println(obj);
        }
    }

    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        int sum = 0, ans = Integer.MAX_VALUE;
        Deque<Integer[]> dq = new ArrayDeque<>();
        dq.offer(new Integer[] { 0, -1 });
        for (int i = 0; i < n; i++) {
            sum += nums[i];
            while (!dq.isEmpty() && sum - dq.peekFirst()[0] >= k) {
                ans = Math.min(ans, i - dq.pollFirst()[0] + 1);
                // dq.pollFirst();
            }

            while (!dq.isEmpty() && dq.peekLast()[0] >= sum) {
                dq.pollLast();
            }
            dq.offer(new Integer[] { sum, i });
        }
        return ans == Integer.MAX_VALUE ? -1 : ans;
    }

}