import java.util.*;

public class GridTraversal {
    static final int mxn = 1001;
    static final int mxx = 5000;
    static final int[] di = { 1, 0, -1, 0 };
    static final int[] dj = { 0, 1, 0, -1 };
    static final char[] dc = { 'D', 'R', 'U', 'L' };
    static int[][] d = new int[mxn][mxn];
    static int[][] dm = new int[mxn][mxn];
    static int[][] p = new int[mxn][mxn];
    static int[][] pm = new int[mxn][mxn];
    static int[][] vis = new int[mxn][mxn];
    static String[] s = new String[mxn];
    static String[] path = new String[mxn];
    static int n, m, si, sj;

    static boolean e(int i, int j) {
        return i >= 0 && j >= 0 && i < n && j < m && s[i].charAt(j) == '.' && vis[i][j] == 0;
    }

    static void bfs(List<int[]> v, int[][] d, int[][] p) {

        for (int i = 0; i < n; i++) {
            Arrays.fill(vis[i], 0);
            Arrays.fill(d[i], Integer.MAX_VALUE);
        }
        Queue<int[]> q = new LinkedList<>();
        for (int[] k : v) {
            q.add(k);
            vis[k[0]][k[1]] = 1;
            d[k[0]][k[1]] = 0;
        }
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int i = 0; i < 4; i++) {
                int ni = c[0] + di[i];
                int nj = c[1] + dj[i];
                if (!e(ni, nj))
                    continue;
                vis[ni][nj] = 1;
                q.add(new int[] { ni, nj });
                p[ni][nj] = i;
                d[ni][nj] = d[c[0]][c[1]] + 1;
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        n = scanner.nextInt();
        m = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        List<int[]> vm = new ArrayList<>();
        // Initialize vis and d arrays with appropriate values
        for (int i = 0; i < n; i++) {
            Arrays.fill(vis[i], -1);
            // Arrays.fill(d[i], Integer.MAX_VALUE);
        }

        for (int i = 0; i < n; i++) {
            s[i] = scanner.nextLine();
            for (int j = 0; j < m; j++) {
                if (s[i].charAt(j) == 'A') {
                    si = i;
                    sj = j;
                    s[i] = s[i].substring(0, j) + '.' + s[i].substring(j + 1);
                }
                if (s[i].charAt(j) == 'M') {
                    vm.add(new int[] { i, j });
                    s[i] = s[i].substring(0, j) + '.' + s[i].substring(j + 1);
                }
            }
            path[i] = String.join("", Collections.nCopies(m, ""));
        }

        bfs(Arrays.asList(new int[] { si, sj }), d, p);
        bfs(vm, dm, pm);

        int ti = -1, tj = -1;
        for (int i = 0; i < n; i++) {
            if (d[i][0] < dm[i][0]) {
                ti = i;
                tj = 0;
            }
            if (d[i][m - 1] < dm[i][m - 1]) {
                ti = i;
                tj = m - 1;
            }
        }
        for (int j = 0; j < m; j++) {
            if (d[0][j] < dm[0][j]) {
                ti = 0;
                tj = j;
            }
            if (d[n - 1][j] < dm[n - 1][j]) {
                ti = n - 1;
                tj = j;
            }
        }
        if (ti != -1) {
            System.out.println("YES");
            StringBuilder t = new StringBuilder();
            while (si != ti || sj != tj) {
                t.append(dc[p[ti][tj]]);
                int dd = p[ti][tj] ^ 2;
                ti += di[dd];
                tj += dj[dd];
            }
            System.out.println(t.length());
            System.out.println(t.reverse().toString());
        } else {
            System.out.println("NO");
        }
    }
}