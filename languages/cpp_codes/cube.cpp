
#include <iostream>
#include <utility>
using namespace std;
int main() {
    auto a =
#define F(x, y) std::make_pair(x##y, #y);
#include "data.inc"
#undef F
        cout << a.first << " " << a.second;
}
131 92
9239 92

9370


152 86
8666 86

8818 


_ _ 4

5 6 4

2*57*7

57*7
399+1

h =2*s-1
2h/3=s+10
4h/3=2s+20

h/3=21


x*a =  n
(x-10)*(a+1)=n
ax-10a+x-10=n
ax-200-25a+8x=n
x-25*(a+8)=n

10a+10=x
200+25a=8x
25+25/8a=x

55/8a=15
a=120/55=24/11





// #include <bits/stdc++.h>
// using namespace std;
// void m() {
//     static int a = 6;
//     printf("%d", ++a);
// }
// int main() {
//     m();
//     m();
//     m();
//     // vector<int> fighters = {4, 5, 2, 7, 9};
//     // priority_queue<array<int, 2>> pq;
//     // for (int i = 0; i < fighters.size(); i++) {
//     //     pq.push({fighters[i], i});
//     // }
//     // while (pq.size() >= 2) {
//     //     auto first = pq.top();
//     //     pq.pop();
//     //     auto second = pq.top();
//     //     pq.pop();
//     //     if (first[0] == second[0]) {
//     //         continue;
//     //     } else {
//     //         if (first[0] > second[0]) {
//     //             pq.push({first[0] - second[0], first[1]});
//     //         } else {
//     //             pq.push({second[0] - first[0], second[1]});
//     //         }
//     //     }
//     // }
//     // auto first = pq.top();
//     // cout << first[1] << " ";
//     // pq.pop();
//     // if (pq.size()) {
//     //     auto second = pq.top();
//     //     cout << second[1];
//     // }
// }