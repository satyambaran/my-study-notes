
import java.io.*;
import java.util.*;

public class GiantPizza_SCC {

    static List<List<Integer>> adj = new ArrayList<>();
    static List<List<Integer>> adj2 = new ArrayList<>();
    static List<Integer> v = new ArrayList<>();
    static boolean[] vis;
    static int[] comp;
    static int[] tval;
    static int k = 0;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        // Input reading
        String[] nm = br.readLine().split(" ");
        int n = Integer.parseInt(nm[0]);
        int m = Integer.parseInt(nm[1]);

        // Initialize adjacency lists and other structures
        adj = new ArrayList<>(2 * m + 1);
        adj2 = new ArrayList<>(2 * m + 1);
        vis = new boolean[2 * m + 1];
        comp = new int[2 * m + 1];
        tval = new int[m + 1];

        for (int i = 0; i <= 2 * m; i++) {
            adj.add(new ArrayList<>());
            adj2.add(new ArrayList<>());
        }

        // Input for clauses and construction of graphs
        for (int i = 0; i < n; i++) {
            String[] clause = br.readLine().split(" ");
            char x = clause[0].charAt(0);
            int a = Integer.parseInt(clause[1]);
            char y = clause[2].charAt(0);
            int b = Integer.parseInt(clause[3]);

            if (x == '-') a = 2 * m - a + 1;
            if (y == '-') b = 2 * m - b + 1;

            adj.get(2 * m - a + 1).add(b);
            adj.get(2 * m - b + 1).add(a);
            adj2.get(a).add(2 * m - b + 1);
            adj2.get(b).add(2 * m - a + 1);
        }

        // Perform DFS for graph traversal
        for (int i = 1; i <= 2 * m; i++) {
            if (!vis[i]) dfs(i);
        }

        // Reset visit array for the second DFS pass
        Arrays.fill(vis, false);

        // Process vertices in reverse finishing order
        for (int i = v.size() - 1; i >= 0; i--) {
            int x = v.get(i);
            if (!vis[x]) {
                k++;
                dfs2(x);
            }
        }

        // Check for contradictions in the 2-SAT solution
        for (int i = 1; i <= m; i++) {
            if (comp[i] == comp[2 * m - i + 1]) {
                out.println("IMPOSSIBLE");
                out.flush();
                return;
            }
            tval[i] = comp[i] > comp[2 * m - i + 1] ? 1 : 0;
        }

        // Output the result
        for (int i = 1; i <= m; i++) {
            out.print(tval[i] == 1 ? "+" : "-");
        }

        out.flush();
    }

    // DFS for the original graph
    static void dfs(int s) {
        if (vis[s]) return;
        vis[s] = true;
        for (int i : adj.get(s)) {
            dfs(i);
        }
        v.add(s);
    }

    // DFS for the transposed graph
    static void dfs2(int s) {
        if (vis[s]) return;
        vis[s] = true;
        comp[s] = k;
        for (int i : adj2.get(s)) {
            dfs2(i);
        }
    }
}