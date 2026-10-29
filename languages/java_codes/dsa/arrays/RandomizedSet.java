import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * INSERT, DELETE, GET-RANDOM IN O(1) (LeetCode 380)
 * ====================================================
 *
 * PROBLEM: Design a data structure that supports:
 *   - insert(val): Insert val. Return false if already present.
 *   - remove(val): Remove val. Return false if not present.
 *   - getRandom(): Return a random element with equal probability.
 *   All operations must be average O(1).
 *
 * APPROACH: HashMap + ArrayList
 *   - HashMap<val, index>: maps value to its index in the list.
 *   - ArrayList<val>: stores values for O(1) random access.
 *
 * THE TRICK FOR O(1) DELETE:
 *   1. Swap the element to delete with the LAST element in the list.
 *   2. Update the swapped element's index in the map.
 *   3. Remove the last element from the list (O(1) for ArrayList).
 *   4. Remove the deleted element from the map.
 *
 * WHY NOT JUST USE A SET?
 *   - HashSet doesn't support O(1) random access (no indexing).
 *   - ArrayList doesn't support O(1) delete (shifting required).
 *   - Combining both gives us the best of both worlds.
 *
 * TIME: O(1) average for all operations.
 * SPACE: O(n) for both the map and the list.
 */
public class RandomizedSet {
    private final Map<Integer, Integer> valToIndex = new HashMap<>();
    private final List<Integer> list = new ArrayList<>();
    private final Random rand = new Random();

    public boolean insert(int val) {
        if (valToIndex.containsKey(val)) return false;
        valToIndex.put(val, list.size());
        list.add(val);
        return true;
    }

    public boolean remove(int val) {
        if (!valToIndex.containsKey(val)) return false;

        int idx = valToIndex.get(val);
        int lastVal = list.get(list.size() - 1);

        // Swap target with last element
        list.set(idx, lastVal);
        valToIndex.put(lastVal, idx);

        // Remove last element
        list.remove(list.size() - 1);
        valToIndex.remove(val);
        return true;
    }

    public int getRandom() {
        return list.get(rand.nextInt(list.size()));
    }

    public static void main(String[] args) {
        RandomizedSet rs = new RandomizedSet();
        System.out.println(rs.insert(1));   // true
        System.out.println(rs.insert(2));   // true
        System.out.println(rs.insert(1));   // false (duplicate)
        System.out.println(rs.remove(1));   // true
        System.out.println(rs.getRandom()); // 2
    }
}
