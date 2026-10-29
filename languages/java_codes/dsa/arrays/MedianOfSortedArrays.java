/**
 * MEDIAN OF TWO SORTED ARRAYS (LeetCode 4) — Hard
 * ==================================================
 *
 * PROBLEM: Given two sorted arrays nums1 (size n1) and nums2 (size n2),
 * find the median of the merged array in O(log(min(n1, n2))) time.
 *
 * APPROACH: Binary search on the shorter array.
 *
 * KEY INSIGHT:
 * - The median splits the merged array into two equal halves.
 * - If we take `i` elements from nums1 and `j` elements from nums2
 *   for the left half, then i + j = (n1 + n2) / 2.
 * - We binary search for the correct `i` (partition point in nums1).
 * - j is determined: j = leftSize - i.
 *
 * VALID PARTITION:
 *   nums1[i-1] <= nums2[j]   (left side of nums1 <= right side of nums2)
 *   nums2[j-1] <= nums1[i]   (left side of nums2 <= right side of nums1)
 *
 * WHEN FOUND:
 *   - Odd total: median = min(nums1[i], nums2[j])
 *   - Even total: median = (max(left sides) + min(right sides)) / 2
 *
 * TIME: O(log(min(n1, n2))) — binary search on smaller array.
 * SPACE: O(1).
 *
 * VISUAL:
 *   nums1: [... aLeft | aRight ...]    partition at index i
 *   nums2: [... bLeft | bRight ...]    partition at index j
 *   Valid when: aLeft <= bRight AND bLeft <= aRight
 */
public class MedianOfSortedArrays {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int n1 = nums1.length, n2 = nums2.length;

        // Ensure we binary search on the smaller array
        if (n1 > n2) return findMedianSortedArrays(nums2, nums1);

        int total = n1 + n2;
        int leftSize = total / 2;

        // Binary search: l and r represent partition range in nums1
        // l=-1 means "take 0 elements from nums1"
        int l = -1, r = n1 - 1;

        while (true) {
            int i = l + (r - l) / 2;          // Partition index in nums1
            int j = leftSize - (i + 1) - 1;   // Partition index in nums2

            // Edge values (use -INF/+INF for out-of-bounds)
            int aLeft  = (i < 0)      ? Integer.MIN_VALUE : nums1[i];
            int bLeft  = (j < 0)      ? Integer.MIN_VALUE : nums2[j];
            int aRight = (i + 1 >= n1) ? Integer.MAX_VALUE : nums1[i + 1];
            int bRight = (j + 1 >= n2) ? Integer.MAX_VALUE : nums2[j + 1];

            // Check if partition is valid
            if (aLeft <= bRight && bLeft <= aRight) {
                if (total % 2 == 1) {
                    return Math.min(aRight, bRight);
                } else {
                    return ((double) Math.min(aRight, bRight)
                          + (double) Math.max(aLeft, bLeft)) / 2;
                }
            } else if (aLeft > bRight) {
                r = i - 1;  // Too many from nums1, move left
            } else {
                l = i + 1;  // Too few from nums1, move right
            }
        }
    }

    public static void main(String[] args) {
        MedianOfSortedArrays sol = new MedianOfSortedArrays();

        System.out.println(sol.findMedianSortedArrays(
            new int[]{1, 3}, new int[]{2}));        // 2.0
        System.out.println(sol.findMedianSortedArrays(
            new int[]{1, 2}, new int[]{3, 4}));     // 2.5
        System.out.println(sol.findMedianSortedArrays(
            new int[]{}, new int[]{1}));             // 1.0
    }
}
