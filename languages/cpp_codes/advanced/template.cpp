
#include <bits/stdc++.h>
using namespace std;
/*
    template <parameter-list> function-declaration	(1)
    template <parameter-list> requires constraint function-declaration	(2)	(since C++20)
    function-declaration-with-placeholders	(3)	(since C++20)
    export template < parameter-list > function-declaration (4)	(removed in C++11)
*/
auto fun1(auto a, auto b) {
    return max(a, b);
}
template <class T, class T>
T funt1(T, T) {
    return max()
}
auto fun(a auto...) {
    return max(a, b);
}

void f1(auto);
void f2(C1 auto);
void f3(C2 auto...);
void f3(auto... args) {
    std::cout << "f3 called with " << sizeof...(args) << " arguments." << std::endl;
}
void f4(const C3 auto*, C4 auto&);
void f4(const auto* ptr, auto& ref) {
    std::cout << "f4 called with pointer to const: " << *ptr << ", and reference: " << ref << std::endl;
}
void f1(auto) {
}

void f2(C1 auto) {
    cout << C1;
}
int main() {
    cout << max(4, 5) << " " << max("satyam", "kundan") << endl;
    f2("string");
}
