import java.util.ArrayList;
import java.util.List;

/**
 * PERMUTATIONS (LeetCode 46) — Backtracking
 * ============================================
 *
 * PROBLEM: Given an array of distinct integers, return all possible permutations.
 *
 * APPROACH: Backtracking with a visited array.
 *   - Build permutation one element at a time.
 *   - For each position, try all unvisited elements.
 *   - When permutation is complete (size == n), add a copy to results.
 *   - Backtrack: unmark visited and remove the last element.
 *
 * BACKTRACKING TEMPLATE:
 *   1. Choose: pick an element (mark visited, add to current)
 *   2. Explore: recurse to fill next position
 *   3. Unchoose: undo (unmark visited, remove from current)
 *
 * TIME: O(n! * n) — n! permutations, each takes O(n) to copy.
 * SPACE: O(n) for the recursion stack + visited array.
 *
 * VARIATIONS:
 * - With duplicates (LC 47): Sort array, skip same element at same level.
 * - Next permutation (LC 31): Find next lexicographic permutation in-place.
 */
public class Permutations {

    private List<List<Integer>> result = new ArrayList<>();

    public List<List<Integer>> permute(int[] nums) {
        backtrack(nums, new ArrayList<>(), new boolean[nums.length]);
        return result;
    }

    private void backtrack(int[] nums, List<Integer> current, boolean[] visited) {
        // Base case: permutation is complete
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current)); // Must copy! current keeps changing
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (!visited[i]) {
                // Choose
                visited[i] = true;
                current.add(nums[i]);

                // Explore
                backtrack(nums, current, visited);

                // Unchoose (backtrack)
                visited[i] = false;
                current.remove(current.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        Permutations sol = new Permutations();
        List<List<Integer>> perms = sol.permute(new int[]{1, 2, 3});
        for (List<Integer> p : perms) {
            System.out.println(p);
        }
        // Output: [1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], [3,2,1]
    }
}
