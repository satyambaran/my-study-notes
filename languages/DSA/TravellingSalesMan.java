import java.util.Arrays;

public class TravellingSalesMan {

    private static int[][] dp;
    private static int visitedAll;

    public static void main(String[] args) {
        int n = 4;
        int[][] dist = {
                { 0, 10, 15, 20 },
                { 10, 0, 35, 25 },
                { 15, 35, 0, 30 },
                { 20, 25, 30, 0 }
        };
        visitedAll = (1 << n) - 1;
        dp = new int[visitedAll + 1][n];
        for (int[] row : dp) {
            Arrays.fill(row, -1);
        }

        System.out.println(fun(0, 1, dist));
    }

    private static int fun(int node, int mask, int[][] dist) {
        int n = dist.length;
        if (mask == visitedAll) {
            return dist[node][0];
        }
        if (dp[mask][node] != -1) {
            return dp[mask][node];
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0) {
                min = Math.min(min, dist[node][i] + fun(i, mask | (1 << i), dist));
            }
        }
        return dp[mask][node] = min;
    }
}
