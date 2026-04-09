import java.util.*;

/**
 * AVERAGE OF LEVELS IN BINARY TREE (LeetCode 637)
 * ==================================================
 *
 * PROBLEM: Given a binary tree, return the average value of the nodes
 * on each level as a list.
 *
 * APPROACH: Standard BFS (level-order traversal).
 * - Process all nodes at current level, compute average.
 * - Enqueue children for next level.
 *
 * TIME: O(n) — visit each node once.
 * SPACE: O(w) — queue holds at most one level, w = max width of tree.
 *
 * PATTERN: This is the "BFS level-by-level" template — useful for:
 * - Level order traversal, zigzag traversal, right side view,
 *   minimum depth, level averages, etc.
 */
public class LevelAverages {

    public List<Double> averageOfLevels(TreeNode root) {
        List<Double> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            double sum = 0;

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                sum += node.val;

                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            result.add(sum / levelSize);
        }

        return result;
    }
}
