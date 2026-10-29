#include <bits/stdc++.h>
using namespace std;
int a, b, c, d, e, h, i, j, k, l, m, n, o, p, q, r, s,
    t = 1, u, v, w, x, y, z, tot = 0, si = -1, sj, ei = 0, ej = 0;
void solve() {
    // cin >> t;
    while (t--) {
        cin >> n >> m;
        vector<vector<int>> v(n, vector<int>(30));
        deque<int> k;
        for (int i = 0; i < n; i++) {
            cin >> v[i][0];
            v[i][0]--;
        }
        for (int i = 1; i < 30; i++) {
            for (int j = 0; j < n; j++) {
                v[j][i] = v[v[j][i - 1]][i - 1];
                cout << v[j][i] << " ";
            }
            cout << endl;
        }

        for (int i = 0; i < m; i++) {
            cin >> a >> b;
            a--;
            for (int j = 0; j < 30; j++) {
                if (b & (1 << j)) {
                    a = v[a][j];
                }
            }
            cout << a + 1 << endl;
        }
    }
}

void print(int i) {
    if (i > 1) {
        print(i / 2);
        print(i / 2);
    }
    tot++;
    cout << "#";
}
void print(int a[], int n) {
    int s[n];
    s[0] = 1;
    for (int i = 1; i < n; i++) {
        s[i] = 1;
        for (int j = i - 1; (j >= 0) && (a[i] >= a[j]); j--) {
            s[i]++;
        }
    }
    for (int j = 0; j < n; j++) cout << s[j] << " ";
}
struct node {
    int val;
    struct node *next;
    node(int val) { this->val = val; }
};
void reorder(struct node *list) {
    if (!list || !list->next) return;
    int temp;
    struct node *a, *b = 0;  // b is nullptr
    a = list, b = list->next;
    while (b) {
        temp   = b->val;
        b->val = a->val;
        a->val = temp;
        a      = b->next;
        b      = a ? a->next : 0;
        // w/o value swap
        //  1 2
        //  3 4
        //  5 6
        //  7 n
    }
}
void solve(char *str) {
    char *s = str;
    int l = 0, r = strlen(s) - 1, ans = -1;
    while (l <= r) {
        int mid = (l + r) / 2;
        if (s[mid] == '1') {
            ans = mid;
            r   = mid - 1;
        } else {
            l = mid + 1;
        }
    }
    printf("%d", ans);
}
#define val                                                              \
    {                                                                    \
        476, "become", 256, "logic", 346, "program", 412, "office", 678, \
            "festival"                                                   \
    }
struct record {
    int i;
    char *c;
} st[]      = val;
record ff[] = val;  // same as st
void pr() {
    record *p = st;
    cout << p->c << endl;
    p += 1;
    cout << p->c << endl;
    ++p->c;
    cout << p->c << endl;
    cout << p++->c << endl;
    cout << p->c << endl;     // program
    cout << *++p->c << endl;  // r
    cout << p[0].i << endl;
}
int main() {
    // int n = 7,i;
    // // // cin >> i;
    // // // int a[n]={1}; // 1,0,0,0,0,0,0
    // int a[n] = {1, 2, 3, 4, 5, 6, 7};
    // // print(a, n);
    // node *list = new node(a[0]);
    // node *temp = list;
    // for (int i = 1; i < n; i++) {
    //     list->next = new node(a[i]);
    //     list = list->next;
    // }
    // reorder(temp);
    // while (temp) {
    //     cout << temp->val << " ";
    //     temp = temp->next;
    // }
    pr();
    // char *c =
    //     "1566666666666666666666666666666666666666666666666666666665463250111111"
    //     "11111111111";
    // solve(c);
    return 0;
}
// 2 * n *n - 2 * n + 1
// 0000-9999

// 4c4+4c3*9+4c2*9*9+4c1*9*9*9+4c0*9*9*9*9
// (1+9)4
