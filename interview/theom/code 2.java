import java.io.*;
import java.util.*;

public class code {
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
        
        // out.println(dp[n][m]);
        out.flush();
    }
}