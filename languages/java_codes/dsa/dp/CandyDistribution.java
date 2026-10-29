/**
 * CANDY DISTRIBUTION (LeetCode 135)
 * ====================================
 *
 * PROBLEM: N children stand in a line, each with a rating. Distribute candies such that:
 *   1. Every child gets at least 1 candy.
 *   2. A child with a higher rating than a neighbor gets more candies than that neighbor.
 * Return the minimum total candies needed.
 *
 * APPROACH: Two-pass greedy.
 *   Pass 1 (left to right): If rating[i] > rating[i-1], give more than left neighbor.
 *   Pass 2 (right to left): If rating[i] > rating[i+1], ensure more than right neighbor.
 *   Take max of both passes for each child.
 *
 * WHY TWO PASSES?
 * - A single left pass handles increasing sequences but not decreasing ones.
 * - The right pass fixes decreasing sequences.
 * - Taking max ensures BOTH neighbor constraints are satisfied.
 *
 * EXAMPLE: ratings = [1, 0, 2]
 *   Left pass:  [1, 1, 2]  (0 < 1, so reset to 1; 2 > 0, so 1+1=2)
 *   Right pass: [2, 1, 2]  (1 > 0, so max(1, 1+1)=2)
 *   Total: 2 + 1 + 2 = 5
 *
 * TIME: O(n) — two linear passes.
 * SPACE: O(n) — array for candy counts.
 */
public class CandyDistribution {

    public int candy(int[] ratings) {
        int n = ratings.length;
        int[] candies = new int[n];

        // Pass 1: Left to right — handle increasing ratings
        candies[0] = 1;
        for (int i = 1; i < n; i++) {
            candies[i] = (ratings[i] > ratings[i - 1]) ? candies[i - 1] + 1 : 1;
        }

        // Pass 2: Right to left — handle decreasing ratings
        for (int i = n - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candies[i] = Math.max(candies[i], candies[i + 1] + 1);
            }
        }

        // Sum up total candies
        int total = 0;
        for (int c : candies) total += c;
        return total;
    }

    public static void main(String[] args) {
        CandyDistribution cd = new CandyDistribution();

        System.out.println(cd.candy(new int[]{1, 0, 2}));       // 5
        System.out.println(cd.candy(new int[]{1, 2, 2}));       // 4
        System.out.println(cd.candy(new int[]{1, 3, 2, 2, 1})); // 7
    }
}
