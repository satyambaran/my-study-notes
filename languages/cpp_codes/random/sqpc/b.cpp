#include <functional>
#include <iostream>
#include <memory>
#include <mutex>
using namespace std;

template <typename T, typename Op>
class accumulator {
public:
    accumulator(T init, Op op) : value_(init), op_(op) {}

    template <typename U>
    void generic_push(U&& value) {
        lock_guard<mutex> lock(mutex_);
        value_ = op_(value_, *value);
    }

    T finalize() {
        lock_guard<mutex> lock(mutex_);
        return value_;
    }

private:
    T value_;
    Op op_;
    mutable mutex mutex_;
};

bool check_007() {
    auto acc = accumulator(
        10,
        [](auto lhs, auto rhs) {
            return lhs + *rhs;
        });

    auto v1 = make_shared<int>(1);
    auto v2 = make_shared<int>(2);
    auto v3 = make_shared<int>(3);

    cout << "Before generic_push - v1 valid: " << (v1 ? "true" : "false") << endl;
    cout << "Before generic_push - v2 valid: " << (v2 ? "true" : "false") << endl;
    cout << "Before generic_push - v3 valid: " << (v3 ? "true" : "false") << endl;

    acc.generic_push(v1);
    acc.generic_push(v2);
    acc.generic_push(v3);

    cout << "After generic_push - v1 valid: " << (v1 ? "true" : "false") << endl;
    cout << "After generic_push - v2 valid: " << (v2 ? "true" : "false") << endl;
    cout << "After generic_push - v3 valid: " << (v3 ? "true" : "false") << endl;

    bool valid_pointers = (v1 && v2 && v3);

    int final_value    = acc.finalize();
    bool correct_value = (final_value == 16);

    cout << "Valid Pointers: " << valid_pointers << endl;
    cout << "Final Value: " << final_value << endl;
    cout << "Correct Value: " << correct_value << endl;

    return correct_value && valid_pointers;
}

int main() {
    cout << "Result: " << boolalpha << check_007() << endl;
    return 0;
}
