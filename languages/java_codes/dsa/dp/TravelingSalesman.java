/**
 * TRAVELING SALESMAN PROBLEM (TSP) — Bitmask DP
 * ================================================
 *
 * PROBLEM: Given n cities and distances between each pair, find the shortest
 * route that visits every city exactly once and returns to the starting city.
 *
 * APPROACH: Bitmask Dynamic Programming
 * - State: dp[i][mask] = minimum cost to reach city i, having visited the
 *   cities represented by the bitmask `mask`.
 * - Transition: For each city j in mask (j != i, j != start),
 *   dp[i][mask] = min(dp[j][mask without i] + dist[j][i])
 * - Base case: dp[i][{start, i}] = dist[start][i]
 *
 * BITMASK EXPLAINED:
 * - mask = 0b1101 means cities 0, 2, 3 have been visited (bits 0, 2, 3 are set).
 * - mask | (1 << j) sets bit j (add city j to visited set).
 * - mask & ~(1 << i) clears bit i (remove city i from visited set).
 * - mask & (1 << j) checks if city j is in the visited set.
 *
 * TIME: O(n^2 * 2^n) — n cities, 2^n subsets, O(n) transitions per state.
 * SPACE: O(n * 2^n) for the memoization table.
 *
 * NOTE: TSP is NP-hard. This DP is exact but only practical for n <= ~20.
 * For larger n, use approximation algorithms (nearest neighbor, 2-opt, etc.)
 */
public class TravelingSalesman {

    static final int N = 4;         // Number of cities
    static final int INF = 1000000;

    // dist[i][j] = distance from city i to city j (1-indexed)
    static int[][] dist = {
        {0,  0,  0,  0,  0},
        {0,  0, 10, 15, 20},
        {0, 10,  0, 25, 25},
        {0, 15, 25,  0, 30},
        {0, 20, 25, 30,  0},
    };

    static int[][] memo = new int[N + 1][1 << (N + 1)];

    /**
     * Returns min cost to complete the tour, currently at city i,
     * having visited the cities in `mask`.
     */
    static int solve(int i, int mask) {
        // Base case: only city i and city 1 are in the mask
        // -> we've visited everyone else, return direct distance to start
        if (mask == ((1 << i) | (1 << 1)))
            return dist[1][i];

        if (memo[i][mask] != 0)
            return memo[i][mask];

        int result = INF;

        for (int j = 1; j <= N; j++) {
            // j is in mask, j is not i, j is not the start city
            if ((mask & (1 << j)) != 0 && j != i && j != 1) {
                // Remove city i from mask, solve from city j
                result = Math.min(result, solve(j, mask & ~(1 << i)) + dist[j][i]);
            }
        }

        return memo[i][mask] = result;
    }

    public static void main(String[] args) {
        int fullMask = (1 << (N + 1)) - 1; // All cities visited
        int answer = INF;

        // Try ending at each city, then returning to city 1
        for (int i = 1; i <= N; i++) {
            answer = Math.min(answer, solve(i, fullMask) + dist[i][1]);
        }

        System.out.println("Minimum tour cost: " + answer); // Expected: 80
    }
}
