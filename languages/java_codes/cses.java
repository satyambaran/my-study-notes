import java.io.*;
import java.util.*;

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

    static boolean isValid(int i, int j, int n, int m) { return i >= 0 && j >= 0 && i < n && j < m; }

    static int[] dx = { 0, 1, 0, -1 };
    static int[] dy = { 1, 0, -1, 0 };
    static char[] dc = { 'R', 'D', 'L', 'U' };

    public static void main(String[] args) throws IOException {
        int n = nextInt(), m = nextInt();
        int ei = -1, ej = -1, si = -1, sj = -1;
        char[][] lab = new char[n][m];
        int[][] dir = new int[n][m];
        int[][] dis = new int[n][m];
        int[][] mDis = new int[n][m];
        Queue<int[]> me = new ArrayDeque<>();
        Queue<int[]> mq = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            Arrays.fill(dis[i], -1);
            Arrays.fill(mDis[i], -1);
            Arrays.fill(dir[i], -1);
        }
        for (int i = 0; i < n; i++) {
            lab[i] = readLine().toCharArray();
            for (int j = 0; j < m; j++) {
                if (lab[i][j] == 'A') {
                    me.offer(new int[] { i, j });
                    si = i;
                    sj = j;
                    lab[i][j] = '.';
                    dis[i][j] = 0;
                } else if (lab[i][j] == 'M') {
                    mq.offer(new int[] { i, j });
                    mDis[i][j] = 0;
                }
            }
        }
        while (!mq.isEmpty()) {
            int[] cur = mq.poll();
            for (int i = 0; i < dx.length; i++) {
                int nx = cur[0] + dx[i];
                int ny = cur[1] + dy[i];
                if (isValid(nx, ny, n, m) && lab[nx][ny] != '#' && mDis[nx][ny] == -1) {
                    mDis[nx][ny] = 1 + mDis[cur[0]][cur[1]];
                    mq.add(new int[] { nx, ny });
                }
            }
        }
        while (!me.isEmpty()) {
            int[] cur = me.poll();
            int cd = dis[cur[0]][cur[1]];
            for (int i = 0; i < dx.length; i++) {
                int nx = cur[0] + dx[i];
                int ny = cur[1] + dy[i];
                if (isValid(nx, ny, n, m) && lab[nx][ny] == '.' && dis[nx][ny] == -1) {
                    int nd = cd + 1;
                    if (mDis[nx][ny] == -1 || nd < mDis[nx][ny]) {
                        dis[nx][ny] = nd;
                        dir[nx][ny] = i;
                        me.add(new int[] { nx, ny });
                    }
                }
            }
        }
        for (int i = 0; i < n && ei == -1; i++) {
            if (dis[i][0] != -1) {
                ei = i;
                ej = 0;
            } else if (dis[i][m - 1] != -1) {
                ei = i;
                ej = m - 1;
            }
        }
        for (int j = 0; j < m && ei == -1; j++) {
            if (dis[0][j] != -1) {
                ei = 0;
                ej = j;
            } else if (dis[n - 1][j] != -1) {
                ei = n - 1;
                ej = j;
            }
        }
        if (ei == -1) {
            out.println("NO");
        } else {
            StringBuilder sb = new StringBuilder();
            while (ei != si || ej != sj) {
                int temp = dir[ei][ej];
                sb.append(dc[temp]);
                ei -= dx[temp];
                ej -= dy[temp];
            }
            sb.reverse();
            out.println("YES");
            out.println(sb.length());
            out.println(sb.toString());
        }
        out.flush();
    }
}
