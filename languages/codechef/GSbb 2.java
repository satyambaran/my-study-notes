
import java.io.*;
import java.util.*;

public class GSbb {
    public static int subArrayExceedsSum(int oldArr[], int target) {
        int start = 0, end = 0, len = oldArr.length, minLen = Integer.MAX_VALUE;
        int[] arr = new int[len + 1];
        for (int i = 1; i <= len; i++) {
            arr[i] = oldArr[i - 1] + arr[i - 1];
        }
        while (start <= len) {
            if (end == len + 1) {
                if (arr[end - 1] - arr[start] <= target) {
                    break;
                } else {
                    minLen = Math.min(minLen, end - start - 1);
                    start++;
                }
            } else if (arr[end] - arr[start] > target) {
                minLen = Math.min(minLen, end - start);
                start++;
            } else {
                end++;
            }
        }
        return minLen == Integer.MAX_VALUE ? -1 : minLen;
    }
    public static void doTestsPass() {
        boolean result = true;
        // int[] arr = { 1, 2, 3, 4 };
        int[] arr = { 1, 4, 45, 6, 0, 19 };
        result = result && subArrayExceedsSum(arr, 51) == 3;
        // result = result && subArrayExceedsSum( arr, 6 ) == 2;
        // result = result && subArrayExceedsSum( arr, 12 ) == -1;

        if (result) {
            System.out.println("All tests pass\n");
        } else {
            System.out.println("There are test failures\n");
        }
    };
    public static void main(String[] args) {
        doTestsPass();
    }
};
