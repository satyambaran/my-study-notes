import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class Solution {
    public static void main(String[] args) {
        for (int i = 16; i < 28; i++) {
            // if ((i & -i) != i - (i & (i - 1))) {
            System.out.printf("%d %d %d\n", i, (i & -i), i - (i & (i - 1)));
            // }
        }
        System.out.printf("null");
        // for (int i = -10000; i < 10000; i++) {
        // if ((i & i) != i - (i & (i - 1))) {
        // System.out.printf("%d %d %d", i, (i & -i), i - (i & (i - 1)));
        // }
        // }
        System.out.printf("null");
    }

    int maxSum = 0;

    public int maxSumBST(TreeNode root) {
        funtion(root);
        return maxSum;
    }

    int[] funtion(TreeNode root) {
        if (root == null)
            return new int[] { 1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE };
        // isBst, sum, min, max
        int[] left = funtion(root.left);
        int[] right = funtion(root.right);
        if (left[0] == 1 && right[0] == 1 && left[3] < root.val && right[2] > root.val) {
            maxSum = Math.max(maxSum, root.val + right[1] + left[1]);
            return new int[] { 1, root.val + right[1] + left[1],
                    (left[2] == Integer.MAX_VALUE) ? root.val : left[2],
                    (right[3] == Integer.MIN_VALUE) ? root.val : right[3] };
        }
        return new int[] { 0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE };
    }

    public List<Double> averageOfLevels(TreeNode root) {
        List<Double> ans = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        int len = q.size();
        while (len > 0) {
            int sum = 0, cnt = len;
            while (len-- > 0) {
                TreeNode cur = q.poll();
                sum += cur.val;
                if (cur.left != null)
                    q.offer(cur.left);
                if (cur.right != null)
                    q.offer(cur.right);
            }
            ans.add((double) sum / cnt);
            len = q.size();
        }
        return ans;
    }

    public int mergeStones(int[] nums, int k) {
        int n = nums.length;
        if ((n - 1) % (k - 1) != 0)
            return -1;
        int[] preSum = new int[n];
        preSum[0] = nums[0];
        for (int i = 1; i < n; i++) {
            preSum[i] = preSum[i - 1] + nums[i];
        }
        int[][] dp = new int[nums.length][nums.length];
        // dp[i][j]=
        // dp[i][j]=dp[i,t-1]+sum(k elements)+dp[t+k,j]
        /*
         * dp[i][j][t] = dp[i][j][t+1] + sum[i--j] - minsum of k consecutive elements
         */
        for (int l = nums.length - 1; l > -1; l--) {
            for (int r = l + k - 1; r < nums.length; r++) {
                dp[l][r] = Integer.MAX_VALUE;
                for (int i = l; i <= r - k; i++) {
                    int coin = preSum[i + k - 1] - preSum[i - 1];
                    coin += dp[l][i - 1] + dp[i + k][r];
                    dp[l][r] = Math.min(dp[l][r], coin);
                }
            }
        }
        return dp[0][nums.length - 1];
    }
    // dp = new int[nums.length][nums.length];
    // for (int i = 0; i < nums.length; i++)
    // for (int j = 0; j < nums.length; j++)
    // dp[i][j] = -1;
    // return dfs(0, nums.length - 1);
    // }

    int dfs(int l, int r, int[] nums) {
        if (l > r)
            return 0;
        if (dp[l][r] != -1)
            return dp[l][r];
        int coin = 0;
        for (int i = l; i <= r; i++) {
            coin = (i == 0 ? 1 : nums[i - 1]) * nums[i] * (r == nums.length - 1 ? 1 : nums[r + 1])
                    + dfs(l, i - 1, nums) + dfs(i + 1, r);
            dp[l][r] = Math.max(dp[l][r], coin);
        }
        return dp[l][r];
    }

    public int maxSelectedElements(int[] nums) {
        // 1 1 1 2 5
        // 0 -1 -2
        int ans = Integer.MIN_VALUE;
        Arrays.sort(nums);
        int[][] dp = new int[nums.length][2];
        dp[0][0] = 1;
        dp[0][1] = 1;
        for (int i = 1; i < n; i++) {
            if (nums[i] == nums[i - 1] + 1) {
                dp[i][0] = 1 + dp[i - 1][0];
                dp[i][1] = 1 + dp[i - 1][1];
            } else if (nums[i] == nums[i - 1] + 2) {
                dp[i][0] = 1;
                dp[i][1] = 1 + dp[i - 1][0];
            } else {
                dp[i][0] = 1;
                dp[i][1] = 1;
            }
            ans = Math.max(ans, dp[i][0]);
            ans = Math.max(ans, dp[i][1]);
        }
        return ans;
    }
}

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