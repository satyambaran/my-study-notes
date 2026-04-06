/*
 * list of integers
 * 
 * return frequency of each integer
 * 
 * 2,1,1,4,4,4,3,3,3,3
 * 
 * 1-2
 * 2-1
 * 3-4
 * 4-3
 */

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.*;

public class CricbuzzR1 {
    public static void main(String[] args) {
        String[] words = new String[] { "act", "tac", "dog", "cat", "god" };
        HashMap<String, HashSet<Integer>> anagramIndexMap = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            String curWord = words[i];
            
            char[] charArray = curWord.toCharArray();

            Arrays.sort(charArray);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < charArray.length; j++) {
                sb.append(charArray[j]);
            }
            curWord = sb.toString();

            HashSet<Integer> indexesOfAnagram = anagramIndexMap.getOrDefault(curWord, new HashSet<>());
            indexesOfAnagram.add(i);
            anagramIndexMap.put(curWord, indexesOfAnagram);
        }
        // O(n * max (log(n), (wordSize log(wordSize))))
        Iterator<Entry<String, HashSet<Integer>>> it2 = anagramIndexMap.entrySet().iterator();
        while (it2.hasNext()) {
            Entry<String, HashSet<Integer>> curEntry = it2.next();
            System.out.printf("Anagram: %s \nMatching Strings: ", curEntry.getKey());
            Iterator<Integer> keySetIterator = curEntry.getValue().iterator();
            while (keySetIterator.hasNext()) {
                System.out.printf("%s ", words[keySetIterator.next()]);
            }
            System.out.println();
        }
    }
}

class Node {
    public int freq;
    public HashSet<Integer> keys;

    Node(int _freq) {
        freq = _freq;
        keys = new HashSet<>();
    }

    void main(String[] args) {
        int[] nums = new int[] { 2, 1, 1, 4, 4, 4, 3, 3, 3 };

        /*
         * Node(val, HashSet<key>)
         * 
         * BST (node) := AVL
         * 
         * 
         */
        HashMap<Integer, Integer> freqMap = new HashMap<>();
        TreeMap<Integer, HashSet<Integer>> valueKeyMap = new TreeMap<>(Comparator.reverseOrder());
        // sum(freq) = n
        // O(n log(n)) : time
        // O(n): space
        // freq 1, freq 1000
        for (int i = 0; i < nums.length; i++) {
            int curFreq = freqMap.getOrDefault(nums[i], 0);
            freqMap.put(nums[i], curFreq + 1);
            if (curFreq > 0) {
                HashSet<Integer> prevSet = valueKeyMap.get(curFreq);
                prevSet.remove(nums[i]);
                if (prevSet.isEmpty()) {
                    valueKeyMap.remove(curFreq);
                }
            }
            HashSet<Integer> curSet = valueKeyMap.getOrDefault(curFreq + 1, new HashSet());
            curSet.add(nums[i]);
            valueKeyMap.put(curFreq + 1, curSet);
        }
        // []List<Integer>
        // Entry
        Iterator<Entry<Integer, Integer>> it = freqMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, Integer> curEntry = it.next();
            System.out.printf("Key: %d, Value: %d\n", curEntry.getKey(), curEntry.getValue());
        }
        Iterator<Entry<Integer, HashSet<Integer>>> it2 = valueKeyMap.entrySet().iterator();
        while (it2.hasNext()) {
            Entry<Integer, HashSet<Integer>> curEntry = it2.next();
            System.out.printf("Frequency: %d \nKeys: ", curEntry.getKey());
            Iterator<Integer> keySetIterator = curEntry.getValue().iterator();
            while (keySetIterator.hasNext()) {
                System.out.printf("%d ", keySetIterator.next());
            }
            System.out.println();
        }

    }
}
/*
 * 
 * 
 * 
 */