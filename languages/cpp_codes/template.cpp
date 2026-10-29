#include <bits/stdc++.h>
using namespace std;

#define int    long long
#define pd     pair<int, int>
#define ar     array
#define pii    pair<int, int>
#define mp     make_pair
#define all(a) a.begin(), a.end()
const int maxn = 100002, maxt = 100002, mod = 1000000007;
int di[4]  = {0, 1, 0, -1};
int dj[4]  = {1, 0, -1, 0};
int did[8] = {0, 1, 0, -1, 1, 1, -1, -1};
int djd[8] = {1, 0, -1, 0, 1, -1, 1, -1};
// int dp[maxt], ans[6 * maxt];
int a, b, c, d, e, h, i, j, k, l, m, n, o, p, q, r, s,
    t = 1, u, v, w, x, y, z, tot = 0, si = -1, sj, ei = 0, ej = 0;
bool flag, flag2;
string str, stri;
bool isValid(int i, int j, vector<string>& v) {
    return i > -1 && i < n && j > -1 && j < m && v[i][j] == '.';
}
void solve() {
    cin >> t;
    while (t--) {
        cin >> n >> k;
        cin >> str;
        vector<int> v;
        map<int, int> mp;
        map<int, int> tot;
        for (int i = 0; i < n; i++) {
            cin >> a;
            v.push_back(a);
            mp[a]++;
        }
        sort(v.begin(), v.end());
        int reached = mp.begin()->first;
        for (auto& cur : mp) {
            int sum = 0;
            if (reached >= cur.first) sum = reached - cur.first;
            for (int j = 0; j < cur.second; j++) {
                tot[sum + j]++;
            }
            reached = cur.first + sum + cur.second;
        }
        if (tot.count(0)) tot.erase(0);
        int cnt = 0;
        while (!tot.empty()) {
            auto cur = tot.rbegin();
            for (int i = 0; i < k; i++) {
                cnt = max(cnt, cur->second);
                cur++;
            }
        }
    }
    // 1 1 1 2 2 2 2 2 2  10
    // 1 2 3 4 5 6 7 8 9
}

// #define ONLINE_JUDGE
int32_t main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
#ifndef ONLINE_JUDGE
    freopen("./bin/input.txt", "r", stdin);
    freopen("./bin/error.txt", "w", stderr);
    freopen("./bin/output.txt", "w", stdout);
#endif
    solve();
    return 0;
}

// "files.autoSave": "afterDelay",
// "workbench.colorTheme": "Default Light Modern",
// "cmake.configureOnOpen": true,
// "terminal.integrated.enableMultiLinePasteWarning": false,
// "editor.minimap.enabled": false,
// "terminal.integrated.tabs.location": "left",
// "git.autofetch": true,
// "git.enableSmartCommit": true,
// "prettier.tabWidth": 4,
// "editor.fontFamily": "Menlo, Monaco, 'Courier New', monospace",
// "editor.letterSpacing": 0.2,
// "editor.fontSize": 12,
// "[javascript]": {
//     "editor.defaultFormatter": "esbenp.prettier-vscode"
// },
// "editor.formatOnSave": true,
// "javascript.updateImportsOnFileMove.enabled": "always",
// "[json]": {
//     "editor.defaultFormatter": "esbenp.prettier-vscode"
// },
// "docker.containers.sortBy": "CreatedTime",
// "C_Cpp.autocompleteAddParentheses": true,
// "C_Cpp.default.cppStandard": "c++17",
// "C_Cpp.default.cStandard": "c17",
// "[jsonc]": {
//     "editor.defaultFormatter": "esbenp.prettier-vscode"
// },
// "C_Cpp.clang_format_fallbackStyle": "{ BasedOnStyle: Google, IndentWidth: 4
// }", "window.nativeTabs": true