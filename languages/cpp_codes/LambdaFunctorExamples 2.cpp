#include <bits/stdc++.h>
using namespace std;
struct print {
    void operator()(int element) { cout << element << endl; }
};
int main() {
    int x = 100, y = 200;
    auto print = [&] { // Capturing everything by reference(not recommended though)
        cout << __PRETTY_FUNCTION__ << " : " << x << " , " << y << endl;
    };
    print();
    return EXIT_SUCCESS;
}
// int main(void) {
//     vector<int> v = {1, 2, 3, 4, 5};
//     for_each(v.begin(), v.end(), print());
//     for_each(v.begin(), v.end(), [](int ele) { cout << ele << endl; });
//     return EXIT_SUCCESS;
// }