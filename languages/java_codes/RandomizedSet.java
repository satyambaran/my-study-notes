import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomizedSet {
    Map<Integer, Integer> mp = new HashMap<>();
    List<Integer> list = new ArrayList<>();
    Random rand = new Random();

    public RandomizedSet() {
    }

    public boolean insert(int val) {
        if (mp.containsKey(val))
            return false;
        map.put(val, list.size());
        list.add(val);
        return true;
    }

    public boolean remove(int val) {
        if (!mp.containsKey(val))
            return false;
        int idx = mp.get(val);
        int last = list.get(list.size() - 1);
        mp.put(last, idx);
        list.set(idx, last);
        list.remove(list.size() - 1);
        mp.remove(val);
        return true;
    }

    public int getRandom() {
        return list.get(rand.nextInt(list.size()));
    }
}
