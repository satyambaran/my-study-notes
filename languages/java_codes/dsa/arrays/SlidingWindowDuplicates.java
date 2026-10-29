import java.util.HashSet;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * CONTAINS DUPLICATE PROBLEMS (LeetCode 219 & 220)
 * ===================================================
 *
 * PROBLEM 1 (LC 219 — Contains Duplicate II):
 * Given array nums and integer k, return true if nums[i] == nums[j]
 * and |i - j| <= k for some i != j.
 *
 * APPROACH: Sliding window with HashSet of size k.
 * - Maintain a window of the last k elements.
 * - If current element is already in window -> duplicate found.
 *
 * TIME: O(n), SPACE: O(k)
 *
 * ---
 *
 * PROBLEM 2 (LC 220 — Contains Duplicate III):
 * Return true if |nums[i] - nums[j]| <= valueDiff and |i - j| <= indexDiff.
 *
 * APPROACH: Sliding window with TreeMap (balanced BST).
 * - TreeMap allows finding the closest value in O(log k) via ceilingEntry/floorEntry.
 * - For each nums[i], check if there's a value in the window within valueDiff.
 *
 * TIME: O(n log k), SPACE: O(k)
 *
 * KEY INSIGHT: HashSet gives exact match; TreeMap gives range queries.
 */
public class SlidingWindowDuplicates {

    /**
     * Contains Duplicate II: exact duplicate within window of size k.
     */
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        HashSet<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            // Shrink window: remove element that's now out of range
            if (i > k) window.remove(nums[i - k - 1]);

            // Check if current element already in window
            if (window.contains(nums[i])) return true;

            window.add(nums[i]);
        }
        return false;
    }

    /**
     * Contains Duplicate III: value within valueDiff, index within indexDiff.
     * Uses TreeMap for efficient nearest-value lookup.
     */
    public boolean containsNearbyAlmostDuplicate(int[] nums, int indexDiff, int valueDiff) {
        TreeMap<Integer, Integer> window = new TreeMap<>();

        for (int i = 0; i < nums.length; i++) {
            // Remove element outside the window
            if (i > indexDiff) {
                int old = nums[i - indexDiff - 1];
                window.remove(old);
            }

            // Find closest value >= (nums[i] - valueDiff) in the window
            Entry<Integer, Integer> entry = window.ceilingEntry(nums[i] - valueDiff);
            if (entry != null && entry.getKey() <= nums[i] + valueDiff) {
                return true; // Found a value within range
            }

            window.put(nums[i], i);
        }
        return false;
    }

    public static void main(String[] args) {
        SlidingWindowDuplicates sol = new SlidingWindowDuplicates();

        // Contains Duplicate II
        System.out.println(sol.containsNearbyDuplicate(new int[]{1, 2, 3, 1}, 3));     // true
        System.out.println(sol.containsNearbyDuplicate(new int[]{1, 2, 3, 1, 2, 3}, 2)); // false

        // Contains Duplicate III
        System.out.println(sol.containsNearbyAlmostDuplicate(new int[]{1, 2, 3, 1}, 3, 0));  // true
        System.out.println(sol.containsNearbyAlmostDuplicate(new int[]{1, 5, 9, 1, 5, 9}, 2, 3)); // false
    }
}
