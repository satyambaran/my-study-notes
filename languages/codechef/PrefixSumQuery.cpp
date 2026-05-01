#include <bits/stdc++.h>
using namespace std;

#define int    long long
#define ar     array
#define pii    pair<int, int>
#define mp     make_pair
#define all(a) a.begin(), a.end()

const int maxn = 104730, maxt = 502, mod = 1e9 + 7;
int di[4]    = {0, 1, 0, -1};
int dj[4]    = {1, 0, -1, 0};
char dirn[4] = {'R', 'D', 'L', 'U'};
int did[8]   = {0, 1, 0, -1, 1, 1, -1, -1};
int djd[8]   = {1, 0, -1, 0, 1, -1, 1, -1};
// int dp[maxt][maxt][2];
int a, b, bc, c, d, e, h, i, j, k, l, m, n, o, p, q, r, s,
    t = 1, u, v, w, x, y, z, tot = 0, mid, si = -1, sj, ei = -1, ej = 0, mx = INT_MIN, mn = INT_MAX, nc, nr;
bool flag, flag2;
char ch;
string str, stri;

// const int INF  = 1e17;
// const int NINF = INF * (-1);

// #define LEETCODE
// #define ONLINE_JUDGE
int c2(int k) {
    return (k * (k - 1)) / 2;
}

void solve() {
    // cin >> t;
    while (t--) {
        cin >> n >> q;
        tot = 0;
        vector<int> v(n);
        cin >> v[0];
        for (int i = 1; i < n; i++) {
            cin >> v[i];
            v[i] += v[i - 1];
        }
        for (int i = 0; i < q; i++) {
            cin >> a >> b;
            a--;
            b--;
            cout << v[b] - (a > 0 ? v[a - 1] : 0) << endl;
        }
    }
}
int32_t main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
#ifdef LEETCODE
#else
#ifndef ONLINE_JUDGE
    freopen("./bin/input.txt", "r", stdin);
    freopen("./bin/error.txt", "w", stderr);
    freopen("./bin/output.txt", "w", stdout);
#endif
    solve();
#endif
    return 0;
}