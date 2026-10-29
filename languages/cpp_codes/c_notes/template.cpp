// https://usaco.guide/general/generic-code#templates
#include <bits/stdc++.h>
using namespace std;
#if (__cplusplus < 201703L)
/**
 * "Clamps" v between the values of lo and hi if it's
 * out of the bounds defined by those two values.
 */
template <class T>
constexpr const T &clamp(const T &v, const T &lo, const T &hi) {
    assert(lo <= hi);
    if (v < lo) {
        return lo;
    } else if (hi < v) {
        return hi;
    }
    return v;
}
#endif

template <class T>
struct Point3D {
    T x;
    T y;
    T z;
};
template <class T>
bool ckmin(T &a, const T &b) {
    if (b < a) {
        a = b;
        return true;
    }
    return false;
};
template <class T>
int sz(const T &container) {
    return (int)container.size();
}
struct CPS {
    template <class T, class U>
    bool operator()(const pair<T, U> &a, const pair<T, U> &b) {
        return make_pair(a.second, a.first) < make_pair(b.second, b.first);
    }
};
template <class T, int SZ>
using arr = std::array<T, SZ>;
template <int SZ>
using ai = std::array<int, SZ>;
struct Point {
    using T = int;
    T x;
    T y;
    T z;
};
#define hh
#ifdef hh
#endif
#undef hh

namespace v1 {
const string buggy_feature = "bugs";
const string greeting = "hi";
};  // namespace v1

inline namespace v2 {
// Inline namespaces are not technically real namespaces but allow us to chunk
// up code and avoid having to use namespaces just to gain access
const string buggy_feature = "what buggy feature?";
const string greeting = "hi";
};  // namespace v2

int main() {
    // Point3D<int> p2{3, 4, 5};
    // Point3D<double> p3{1.1, 2.2, 3.3};
    // Point3D<string> p4{"d", "b", "c"};

    cout << buggy_feature << "\n";      // outputs "what buggy feature?"
    cout << v1::buggy_feature << "\n";  // outputs "bugs"
    return 0;
}