import java.io.*;
import java.util.*;

public class template {

    // ---------- Constants ----------

    static final int MAX_N = 100_002;
    static final int MAX_T = 100_002;
    static final long MOD = 1_000_000_007L;
    static final long INF = Long.MAX_VALUE / 4;

    static final int[] DI4 = { 0, 1, 0, -1 };
    static final int[] DJ4 = { 1, 0, -1, 0 };

    static final int[] DI8 = { 0, 1, 0, -1, 1, 1, -1, -1 };
    static final int[] DJ8 = { 1, 0, -1, 0, 1, -1, 1, -1 };

    // ---------- Fast input and output ----------

    static final FastScanner in = new FastScanner(System.in);
    static final StringBuilder out = new StringBuilder();

    static int ni() throws IOException {
        return in.nextInt();
    }

    static long nl() throws IOException {
        return in.nextLong();
    }

    static double nd() throws IOException {
        return in.nextDouble();
    }

    static String ns() throws IOException {
        return in.next();
    }

    static char nc() throws IOException {
        return in.next().charAt(0);
    }

    static void p(Object value) {
        out.append(value);
    }

    static void pl(Object value) {
        out.append(value).append('\n');
    }

    static void pl() {
        out.append('\n');
    }

    static void yes() {
        pl("YES");
    }

    static void no() {
        pl("NO");
    }

    // ---------- Array helpers ----------

    static int[] readIntArray(int n) throws IOException {
        int[] a = new int[n];

        for (int i = 0; i < n; i++) {
            a[i] = ni();
        }

        return a;
    }

    static long[] readLongArray(int n) throws IOException {
        long[] a = new long[n];

        for (int i = 0; i < n; i++) {
            a[i] = nl();
        }

        return a;
    }

    static void print(int[] a) {
        for (int i = 0; i < a.length; i++) {
            if (i > 0)
                p(" ");
            p(a[i]);
        }
        pl();
    }

    static void print(long[] a) {
        for (int i = 0; i < a.length; i++) {
            if (i > 0)
                p(" ");
            p(a[i]);
        }
        pl();
    }

    // ---------- Utility methods ----------

    static boolean isValid(int i, int j, int n, int m) {
        return i >= 0 && i < n && j >= 0 && j < m;
    }

    static boolean isValid(int i, int j, char[][] grid) {
        return i >= 0 && i < grid.length
                && j >= 0 && j < grid[0].length
                && grid[i][j] == '.';
    }

    static long gcd(long a, long b) {
        while (b != 0) {
            long temp = a % b;
            a = b;
            b = temp;
        }

        return Math.abs(a);
    }

    static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }

    static long modPow(long base, long power) {
        long result = 1;
        base %= MOD;

        while (power > 0) {
            if ((power & 1) == 1) {
                result = result * base % MOD;
            }

            base = base * base % MOD;
            power >>= 1;
        }

        return result;
    }

    // ---------- Pair ----------

    static class Pair implements Comparable<Pair> {
        long first;
        long second;

        Pair(long first, long second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int compareTo(Pair other) {
            int comparison = Long.compare(first, other.first);

            if (comparison != 0) {
                return comparison;
            }

            return Long.compare(second, other.second);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            if (!(object instanceof Pair))
                return false;

            Pair other = (Pair) object;
            return first == other.first && second == other.second;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return first + " " + second;
        }
    }

    // ---------- Solution ----------

    static void solve() throws IOException {
        int t = ni();

        while (t-- > 0) {
            int n = ni();
            int k = ni();
            String str = ns();

            long[] a = readLongArray(n);

            Map<Long, Integer> frequency = new TreeMap<>();

            for (long value : a) {
                frequency.merge(value, 1, Integer::sum);
            }

            // Write solution here.

            // Examples:
            // pl(answer);
            // p(x);
            // p(" ");
            // pl(y);
            // yes();
            // print(a);
        }
    }

    public static void main(String[] args) throws Exception {
        solve();

        // Only one System.out call for the entire program.
        System.out.print(out);
    }

    // ---------- Fast scanner ----------

    static class FastScanner {
        private static final int BUFFER_SIZE = 1 << 16;

        private final InputStream input;
        private final byte[] buffer = new byte[BUFFER_SIZE];

        private int pointer = 0;
        private int length = 0;

        FastScanner(InputStream input) {
            this.input = input;
        }

        private int read() throws IOException {
            if (pointer >= length) {
                length = input.read(buffer);
                pointer = 0;

                if (length == -1) {
                    return -1;
                }
            }

            return buffer[pointer++];
        }

        String next() throws IOException {
            StringBuilder token = new StringBuilder();
            int c;

            do {
                c = read();
            } while (c <= ' ' && c != -1);

            while (c > ' ') {
                token.append((char) c);
                c = read();
            }

            return token.toString();
        }

        int nextInt() throws IOException {
            return (int) nextLong();
        }

        long nextLong() throws IOException {
            int c;

            do {
                c = read();
            } while (c <= ' ' && c != -1);

            boolean negative = false;

            if (c == '-') {
                negative = true;
                c = read();
            }

            long value = 0;

            while (c > ' ') {
                value = value * 10 + (c - '0');
                c = read();
            }

            return negative ? -value : value;
        }

        double nextDouble() throws IOException {
            return Double.parseDouble(next());
        }
    }
}