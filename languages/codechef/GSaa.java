/*
 * Click `Run` to execute the snippet below!
 */

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.*;

/*
 * To execute Java, please define "static void main" on a class
 * named Solution.
 *
 * If you need more classes, simply define them inline.
 */

class GSaa {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("Hello, World!");
        strings.add("Welcome to CoderPad.");
        strings.add("This pad is running Java " + Runtime.version().feature());

        for (String string : strings) {
            System.out.println(string);
        }

        // below test case should run.
        runTests();

    }

    private static void runTests() {
        String s = "abcdae";
        assertEquals("bcdae", longestStringWithoutRepeatingChar(s));
        s = "abcaabc";
        assertEquals("abc", longestStringWithoutRepeatingChar(s));
        s = "aaaaa";
        assertEquals("a", longestStringWithoutRepeatingChar(s));
    }

    private static String longestStringWithoutRepeatingChar(String s) {
        // code
        /*
         * 
         * start, end
         * while(end<len){
         * while(freq[end]>0){
         * freq[start]--
         * start++
         * }
         * freq[end]++
         * if(maxLen<end-start+1){
         * maxLen=end-start+1
         * startIdx=start
         * }
         * }
         * 
         * 
         */
        int rtr = ord('a');
        int start = 0, end = 0, len = s.length(), maxLen = 0, startIdx = -1;
        if (len == 0)
            return s;
        int[] freq = new int[26];
        while (end < len) {
            while (freq[s.charAt(end) - 'a'] > 0) {
                freq[s.charAt(start) - 'a']--;
                start++;
            }
            freq[s.charAt(end) - 'a']++;
            if (maxLen < end - start + 1) {
                startIdx = start;
                maxLen = end - start + 1;
            }
            // System.out.printf("%d %d\n", startIdx, maxLen);
            end++;
        }
        /*
         * abcdae
         * start=1, end=4
         */
        return s.substring(startIdx, startIdx + maxLen);
    }
}

// Your previous Plain Text content is preserved below: