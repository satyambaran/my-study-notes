/**
 * MAXIMUM SUM BST IN BINARY TREE (LeetCode 1373)
 * ================================================
 *
 * PROBLEM: Given a binary tree, find the maximum sum of all keys of any
 * subtree which is also a Binary Search Tree (BST).
 *
 * APPROACH: Post-order DFS returning 4 values per node:
 *   [isBST, sum, min, max]
 *
 * For each node, check:
 *   - Left subtree is BST AND right subtree is BST
 *   - left.max < node.val < right.min (BST property)
 *
 * If valid BST: sum = left.sum + node.val + right.sum
 * Track global maximum sum.
 *
 * TIME: O(n) — visit each node once.
 * SPACE: O(h) — recursion stack, h = height of tree.
 *
 * KEY INSIGHT: We need post-order (bottom-up) because we need children's
 * info before we can determine if the current subtree is a BST.
 */
public class MaxSumBST {

    private int maxSum = 0;

    public int maxSumBST(TreeNode root) {
        postOrder(root);
        return maxSum;
    }

    /**
     * Returns: [isBST (1/0), sum, min value, max value]
     *
     * Base case (null node): isBST=1, sum=0, min=MAX, max=MIN
     * - min=MAX_VALUE so any real node will be smaller (satisfies left.max < node.val)
     * - max=MIN_VALUE so any real node will be larger (satisfies right.min > node.val)
     */
    private int[] postOrder(TreeNode root) {
        if (root == null) {
            return new int[]{1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE};
        }

        int[] left = postOrder(root.left);
        int[] right = postOrder(root.right);

        // Check if current subtree forms a valid BST
        boolean isBST = left[0] == 1
                && right[0] == 1
                && left[3] < root.val    // left max < current value
                && right[2] > root.val;  // right min > current value

        if (isBST) {
            int sum = left[1] + root.val + right[1];
            maxSum = Math.max(maxSum, sum);

            // Compute min and max for this subtree
            int min = (left[2] == Integer.MAX_VALUE) ? root.val : left[2];
            int max = (right[3] == Integer.MIN_VALUE) ? root.val : right[3];

            return new int[]{1, sum, min, max};
        }

        return new int[]{0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE};
    }
}

class TreeNode {
    int val;
    TreeNode left, right;

    TreeNode(int val) { this.val = val; }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
