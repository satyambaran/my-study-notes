import java.util.Random;

/**
 * Skip List: a probabilistic data structure that layers sorted linked lists.
 *
 * Layer 0 (bottom): every element
 * Layer 1:          ~half the elements
 * Layer 2:          ~quarter of elements
 * ...
 *
 * Search, insert, delete: O(log n) expected
 */
public class SkipList {

    private static final int MAX_LEVEL = 16;
    private static final double P = 0.5; // probability of promoting a node to the next level

    // ── CRITICAL: sentinel head node ─────────────────────────────────────────
    // head.forward[i] is the first real node at level i.
    // Having a sentinel avoids null-checks on every comparison.
    private final Node head = new Node(Integer.MIN_VALUE, MAX_LEVEL);
    private int currentLevel = 0;   // highest level actually in use
    private final Random rng = new Random();

    // ── Node ─────────────────────────────────────────────────────────────────
    static class Node {
        final int val;
        final Node[] forward; // forward[i] = next node at level i

        Node(int val, int levels) {
            this.val = val;
            this.forward = new Node[levels];
        }
    }

    // ── CRITICAL: randomLevel ─────────────────────────────────────────────────
    // This is what makes skip lists probabilistic.
    // Each new node is promoted to successive levels with probability P.
    // Result: on average only 1/(1-P) nodes per element across all levels combined.
    private int randomLevel() {
        int level = 0;
        while (rng.nextDouble() < P && level < MAX_LEVEL - 1)
            level++;
        return level;
    }

    // ── CRITICAL: search finger array ────────────────────────────────────────
    // update[i] = the rightmost node at level i whose forward[i].val < target.
    // This is the "bookmark" needed by both insert and delete to rewire pointers.
    private Node[] findUpdate(int target) {
        Node[] update = new Node[MAX_LEVEL];
        Node cur = head;
        for (int i = currentLevel; i >= 0; i--) {
            while (cur.forward[i] != null && cur.forward[i].val < target)
                cur = cur.forward[i];
            update[i] = cur; // last node at level i that is < target
        }
        return update;
    }

    // ── search ────────────────────────────────────────────────────────────────
    // Descend from the top level. At each level, skip as far right as possible
    // before the target, then drop down. Only compare at level 0 for the hit.
    public boolean search(int target) {
        Node cur = head;
        for (int i = currentLevel; i >= 0; i--)
            while (cur.forward[i] != null && cur.forward[i].val < target)
                cur = cur.forward[i];
        cur = cur.forward[0];
        return cur != null && cur.val == target;
    }

    // ── insert ────────────────────────────────────────────────────────────────
    public void insert(int val) {
        Node[] update = findUpdate(val);

        // Don't insert duplicates
        Node candidate = update[0].forward[0];
        if (candidate != null && candidate.val == val) return;

        int lvl = randomLevel();

        // CRITICAL: if new node rises above currentLevel, the sentinel head
        // becomes the predecessor for those new levels.
        if (lvl > currentLevel) {
            for (int i = currentLevel + 1; i <= lvl; i++)
                update[i] = head;
            currentLevel = lvl;
        }

        Node node = new Node(val, lvl + 1);

        // Splice the node into every level it participates in — O(log n) pointer swaps.
        for (int i = 0; i <= lvl; i++) {
            node.forward[i] = update[i].forward[i];
            update[i].forward[i] = node;
        }
    }

    // ── delete ────────────────────────────────────────────────────────────────
    public void delete(int val) {
        Node[] update = findUpdate(val);
        Node target = update[0].forward[0];
        if (target == null || target.val != val) return;

        // Unlink from each level bottom-up.
        for (int i = 0; i <= currentLevel; i++) {
            if (update[i].forward[i] != target) break; // target doesn't reach this level
            update[i].forward[i] = target.forward[i];
        }

        // Shrink currentLevel if top levels are now empty.
        while (currentLevel > 0 && head.forward[currentLevel] == null)
            currentLevel--;
    }

    // ── demo ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SkipList sl = new SkipList();
        int[] vals = {3, 6, 7, 9, 12, 19, 17, 26, 21, 25};
        for (int v : vals) sl.insert(v);

        System.out.println(sl.search(19)); // true
        System.out.println(sl.search(5));  // false

        sl.delete(19);
        System.out.println(sl.search(19)); // false
    }
}
