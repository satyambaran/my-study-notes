import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class leetcode {
    public static void main(String[] args) {

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
            if (nx >= 0 && ny >= 0 && nx < n && ny < m && matrix[nx][ny] > matrix[x][y] && (dis[nx][ny] == 0
                    || dis[nx][ny] < 1 + dis[x][y])) {
                ans = Math.max(ans, 1 + dfs(nx, ny, matrix, dis));
            }
        }
        dis[x][y] = ans;
        return ans;
    }

    public int longestIncreasingPath(int[][] matrix) {
        int n = matrix.length, m = matrix[0].length, ans = 0;
        int[][] dir = new int[n][m];
        int[][] dis = new int[n][m];
        // PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> x[0] - y[0]);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                ans = Math.max(ans, dfs(i, j, matrix, dis));
            }
        }

        return ans;
    }
}
