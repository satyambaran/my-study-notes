#include <bits/stdc++.h>
using namespace std;
#define int long long
const int maxn = 1000001, maxt = 100002, mod = 998244353;
int dp[maxn];
int a, b, c, d, e, i, j, k, l, m = -1, n, o, p, q, r, s, t = 1, u, v, w, x, y, z, tot = 0;
bool flag;
string str;
int fact(int n) {
    if (n == 1) return 1;
    return (n * dp[n - 1]) % mod;
}
int32_t main() {
    vector<string> v = {"abc", "ab", "a"};
    sort(v.begin(), v.end());
    for (auto k : v) cout << k << endl;
    return 0;
}