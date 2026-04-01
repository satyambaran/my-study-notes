#include <bits/stdc++.h>
using namespace std;
// int a, b, c, d, e, h, i, j, k, l, m, n, o, p, q, r, s,
//     t = 1, u, v, w, x, y, z, tot = 0, si = -1, sj, ei = 0, ej = 0;
// vector<int> dp;
// vector<int> nextBig, nextBigReverse;

void solve2() {
    int n, d;
    cin >> n;
    vector<int> nums;
    cin >> d;
    nums.push_back(d);
    for (int i = 1; i < n; i++) {
        cin >> d;
        if (nums.back() < d)
            nums.push_back(d);
        else {
            auto pos  = lower_bound(nums.begin(), nums.end(), d) - nums.begin();
            nums[pos] = d;
        }
    }
    cout << nums.size();
}

int main() {
    solve2();
    return 0;
}