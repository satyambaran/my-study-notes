import java.util.*;
import java.util.Map.Entry;

/**
 * CRICBUZZ ROUND 1 — Anagram Grouping & Frequency Tracking
 * ==========================================================
 *
 * PROBLEM 1: GROUP ANAGRAMS
 * Given an array of strings, group anagrams together.
 * Anagrams are words with the same characters in different order.
 *
 * Approach: Sort each word's characters -> use as key in HashMap.
 * - "act" sorted -> "act", "tac" sorted -> "act" -> same group.
 *
 * TIME: O(n * k*log(k)) where n = number of words, k = max word length.
 * SPACE: O(n * k)
 *
 * ---
 *
 * PROBLEM 2: FREQUENCY-SORTED OUTPUT
 * Given an array of integers, output elements sorted by frequency (descending).
 *
 * Approach: HashMap for frequency + TreeMap (reverse order) for sorting.
 * - TreeMap key = frequency, value = set of elements with that frequency.
 *
 * TIME: O(n log n) — TreeMap operations are O(log n).
 * SPACE: O(n)
 */
public class Cricbuzz_R1_AnagramsAndFrequency {

    public static void main(String[] args) {
        System.out.println("=== Anagram Grouping ===");
        groupAnagrams();

        System.out.println("\n=== Frequency Sorting ===");
        frequencySortedOutput();
    }

    /**
     * Group anagrams by sorting each word and using the sorted form as a key.
     */
    static void groupAnagrams() {
        String[] words = {"act", "tac", "dog", "cat", "god"};

        // Key = sorted characters, Value = set of original word indices
        HashMap<String, List<String>> anagramGroups = new HashMap<>();

        for (String word : words) {
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);

            anagramGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
        }

        // Print groups
        for (Map.Entry<String, List<String>> entry : anagramGroups.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " -> " + entry.getValue());
        }
        // Output: Key: act -> [act, tac, cat]
        //         Key: dgo -> [dog, god]
    }

    /**
     * Count frequencies and display sorted by frequency (highest first).
     * Uses TreeMap with reverse ordering for automatic sorting.
     */
    static void frequencySortedOutput() {
        int[] nums = {2, 1, 1, 4, 4, 4, 3, 3, 3, 3};

        // Step 1: Count frequencies
        HashMap<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) {
            freqMap.merge(num, 1, Integer::sum);
        }

        // Step 2: Invert -> group by frequency (descending via reverseOrder)
        TreeMap<Integer, Set<Integer>> freqToValues = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            freqToValues.computeIfAbsent(entry.getValue(), k -> new HashSet<>())
                        .add(entry.getKey());
        }

        // Step 3: Print
        System.out.println("Frequency map: " + freqMap);
        for (Map.Entry<Integer, Set<Integer>> entry : freqToValues.entrySet()) {
            System.out.println("Frequency " + entry.getKey() + ": " + entry.getValue());
        }
        // Output: Frequency 4: [3]
        //         Frequency 3: [4]
        //         Frequency 2: [1]
        //         Frequency 1: [2]
    }
}
