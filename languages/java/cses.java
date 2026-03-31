import java.io.*;
import java.util.*;

import DSA_CheatSheet.Point;

public class cses {
    static BufferedReader br;
    static PrintWriter out;

    static {
        try {
            FileReader fr = new FileReader("input.txt");
            FileWriter fw = new FileWriter("output.txt");
            br = new BufferedReader(fr, 65536);
            out = new PrintWriter(new BufferedWriter(fw));
        } catch (IOException e) {
            br = new BufferedReader(new InputStreamReader(System.in), 65536);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
    }

    static int nextInt() throws IOException {
        int num = 0, ch;
        while ((ch = br.read()) <= 32)
            ;
        boolean neg = ch == '-';
        if (neg)
            ch = br.read();
        do {
            num = num * 10 + ch - '0';
        } while ((ch = br.read()) > 32);
        return neg ? -num : num;
    }

    static String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = br.read()) <= 32)
            ;
        do {
            sb.append((char) ch);
        } while ((ch = br.read()) > 32);
        return sb.toString();
    }

    static boolean isValid(int i, int j, int n, int m) {
        return i >= 0 && j >= 0 && i < n && j < m;
    }

    static int[] dx = { 0, 1, 0, -1 };
    static int[] dy = { 1, 0, -1, 0 };

    static Point {
        int x, y;
    }

    public static void main(String[] args) throws IOException {
        int n = nextInt(), m = nextInt();

        char[][] maze = new char[n][m];

        for (int i = 0; i < n; i++) {
            maze[i] = readLine().toCharArray();
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (maze[i][j] == '.') {
                    ArrayDeque<int[]> q = new ArrayDeque<>();
                }
            }
            out.println();
        }
        ArrayDequeMine<Integer> g = new ArrayDequeMine<>();

        // out.println(dp[n][m]);
        out.flush();
    }
}