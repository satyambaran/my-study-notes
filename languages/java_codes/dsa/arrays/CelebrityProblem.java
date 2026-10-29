/**
 * CELEBRITY PROBLEM (Two-Pointer Elimination)
 * =============================================
 *
 * PROBLEM: In a group of n people, a "celebrity" is someone who:
 *   - Is known by everyone else.
 *   - Knows nobody.
 * Given matrix[i][j] = 1 if person i knows person j, find the celebrity (or -1).
 *
 * APPROACH: Two-pointer elimination + verification.
 *
 * STEP 1 — ELIMINATION (O(n)):
 *   - Start with i=0 (leftmost) and j=n-1 (rightmost).
 *   - If matrix[j][i] == 1 (j knows i): j can't be celebrity -> j--.
 *   - Else (j doesn't know i): i can't be celebrity -> i++.
 *   - When i == j, that's the candidate.
 *
 *   WHY THIS WORKS: Each comparison eliminates exactly one person.
 *   After n-1 comparisons, only one candidate remains.
 *
 * STEP 2 — VERIFICATION (O(n)):
 *   - Check that everyone knows the candidate.
 *   - Check that the candidate knows nobody.
 *
 * TIME: O(n) — n-1 comparisons + n verification.
 * SPACE: O(1) — just two pointers.
 *
 * NOTE: The brute force (check all pairs) is O(n^2).
 */
public class CelebrityProblem {

    static int celebrity(int[][] matrix, int n) {
        // Step 1: Find candidate via elimination
        int i = 0, j = n - 1;
        while (i < j) {
            if (matrix[j][i] == 1)  // j knows i -> j is NOT celebrity
                j--;
            else                    // j doesn't know i -> i is NOT celebrity
                i++;
        }
        int candidate = i;

        // Step 2: Verify the candidate
        for (i = 0; i < n; i++) {
            if (i != candidate) {
                // Everyone must know the candidate, and candidate must know nobody
                if (matrix[i][candidate] == 0 || matrix[candidate][i] == 1)
                    return -1;
            }
        }
        return candidate;
    }

    public static void main(String[] args) {
        int[][] matrix = {
            {0, 0, 1, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 0},  // Person 2 knows nobody
            {0, 0, 1, 0}
        };
        // Person 2 is known by everyone (column 2 all 1s except self)
        // Person 2 knows nobody (row 2 all 0s)
        System.out.println("Celebrity: " + celebrity(matrix, 4)); // 2
    }
}
