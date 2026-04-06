#include <bits/stdc++.h>
using namespace std;
const int mod  = 1e9 + 7;
const int maxN = 2e5 + 1;
pair<int, int> dp[maxN];
pair<int, int> fact(int n) {
    if (n == 1) return {0, 0};
    if (n == 2) return {1, 0};
    pair<int, int> first = fact(n - 1);
    pair<int, int> second = fact(n - 2);
    
}
int main() {
    int n;
    cin >> n;

    pair<int, int> p = fact(n);
}