/**
 * MICROSOFT ROUND 3 — Find In-Order Predecessor in BST
 * ======================================================
 *
 * PROBLEM: Given a BST and a value, find the in-order predecessor
 * (the largest value smaller than the given value).
 *
 * APPROACH:
 *   Traverse the BST searching for the target value.
 *   - If we go RIGHT (node.val < value), this node could be the predecessor.
 *     Track it as `candidate` and continue right.
 *   - If we go LEFT (node.val > value), continue left (no update to candidate).
 *   - When found: if the node has a left subtree, the predecessor is the
 *     RIGHTMOST node in the left subtree. Otherwise, it's the tracked candidate.
 *
 * EXAMPLE:
 *         20
 *        /  \
 *      10    30
 *     / \   / \
 *    5  15 25  35
 *
 *   Predecessor of 20 = 15 (rightmost of left subtree)
 *   Predecessor of 25 = 20 (tracked while going right at root, then left at 30)
 *   Predecessor of 5  = null (smallest element)
 *
 * TIME: O(h) where h = height of tree (O(log n) for balanced BST).
 * SPACE: O(1) iterative, O(h) recursive.
 */
public class Microsoft_R3_BSTPredecessor {

    static class TreeNode {
        int val;
        TreeNode left, right;

        TreeNode(int val) { this.val = val; }
    }

    /**
     * Iterative approach — cleaner and O(1) space.
     */
    static Integer findPredecessor(TreeNode root, int value) {
        Integer predecessor = null;
        TreeNode current = root;

        while (current != null) {
            if (current.val == value) {
                // Found the node: predecessor is rightmost of left subtree
                if (current.left != null) {
                    TreeNode temp = current.left;
                    while (temp.right != null) temp = temp.right;
                    return temp.val;
                }
                // No left subtree: predecessor is the last ancestor we turned right at
                return predecessor;

            } else if (value < current.val) {
                current = current.left;    // Go left, don't update predecessor
            } else {
                predecessor = current.val; // This node is smaller -> potential predecessor
                current = current.right;   // Go right looking for closer match
            }
        }
        return predecessor; // Value not found in tree, return closest smaller value
    }

    public static void main(String[] args) {
        // Build BST:     20
        //               /  \
        //             10    30
        //            / \   / \
        //           5  15 25  35
        TreeNode root = new TreeNode(20);
        root.left = new TreeNode(10);
        root.right = new TreeNode(30);
        root.left.left = new TreeNode(5);
        root.left.right = new TreeNode(15);
        root.right.left = new TreeNode(25);
        root.right.right = new TreeNode(35);

        System.out.println("Predecessor of 20: " + findPredecessor(root, 20)); // 15
        System.out.println("Predecessor of 25: " + findPredecessor(root, 25)); // 20
        System.out.println("Predecessor of 5: "  + findPredecessor(root, 5));  // null
        System.out.println("Predecessor of 30: " + findPredecessor(root, 30)); // 25
    }
}
