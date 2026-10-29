public class PalindromeQueries {
    private static final int[] BIT_COUNT_LOOKUP = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            BIT_COUNT_LOOKUP[i] = Integer.bitCount(i);
        }
    }

    public static int countSetBits(long x) {
        return BIT_COUNT_LOOKUP[(int) (x & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 8) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 16) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 24) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 32) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 40) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 48) & 0xFF)] +
                BIT_COUNT_LOOKUP[(int) ((x >> 56) & 0xFF)];
    }

    // Method to determine if the substring can be converted to a palindrome
    public static String canConvertToPalindrome(String s, int[][] queries) {
        int n = s.length();
        StringBuilder result = new StringBuilder();
        long[] freq = new long[n + 1];
        for (int i = 0; i < n; i++) {
            freq[i + 1] = freq[i] ^ (1L << (s.charAt(i) - 'a'));
        }
        for (int[] query : queries) {
            int start = query[0];
            int end = query[1];
            int nos = query[2];

            // Calculate the XOR of frequencies in the range [start, end]
            long rangeFreq = freq[end + 1] ^ freq[start];

            // Count the number of 1-bits in the result
            int oddCount = countSetBits(rangeFreq);

            // If oddCount is less than or equal to 2 * nos, we can form a palindrome
            if (oddCount / 2 <= nos) {
                result.append('1');
            } else {
                result.append('0');
            }
        }

        return result.toString();
    }

    // Example usage
    public static void main(String[] args) {
        String s = "aabdcdwq";
        int[][] queries = {
                { 1, 3, 1 },
                { 0, 3, 1 },
                { 0, 7, 1 }
        };

        String result = canConvertToPalindrome(s, queries);
        System.out.println(result); // Output: 110
    }
}