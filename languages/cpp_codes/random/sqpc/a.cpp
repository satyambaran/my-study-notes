#include <bits/stdc++.h>

#include <functional>
#include <iostream>
#include <memory>
#include <mutex>

using namespace std;
constexpr bool check_001_enabled = true, check_002_enabled = true, check_003_enabled = true, check_004_enabled = true, check_005_enabled = true, check_006_enabled = true, check_007_enabled = true, check_008_enabled = true, check_009_enabled = true;
// template <typename T, typename Op>
// class accumulator {
// public:
//     accumulator(T init, Op op) : pimpl_(make_unique<pimpl>(init), op))) {}

//     template <typename U>
//     void generic_push(U&& value) {
//         pimpl_->generic_push(forward<U>(value));
//     }

//     void push(T value) {
//         pimpl_->push(value));
//     }

//     unique_ptr<T> finalize() {
//         return pimpl_->finalize();
//     }

// private:
//     struct pimpl {
//         T value;
//         Op op;
//         mutable mutex mutex_;

//         pimpl(T init, Op op) : value(init)), op(op)) {}

//         template <typename U>
//         void generic_push(U&& value) {
//             lock_guard<mutex> lock(mutex_);
//             this->value = op(this->value), value));
//         }

//         void push(T value) {
//             lock_guard<mutex> lock(mutex_);
//             this->value = op(this->value), value));
//         }

//         unique_ptr<T> finalize() {
//             lock_guard<mutex> lock(mutex_);
//             return make_unique<T>(value));
//         }
//     };
//     unique_ptr<pimpl> pimpl_;
// };
template <typename T, typename Op>
class accumulator {
public:
    accumulator(T init, Op op) : pimpl_(make_unique<pimpl>(init, op)) {}

    template <typename U>
    void generic_push(U&& value) {
        pimpl_->generic_push(forward<U>(value));
    }

    void push(T value) {
        pimpl_->push(value);
    }

    T finalize() {
        return pimpl_->finalize();
    }
    // unique_ptr<T> finalize() {
    //     return pimpl_->finalize();
    // }

private:
    struct pimpl {
        T value;
        Op op;
        mutable mutex mutex_;

        pimpl(T init, Op op) : value(init), op(op) {}

        // template <typename U>
        void generic_push(T value) {
            lock_guard<mutex> lock(mutex_);
            this->value = op(this->value, value);
        }

        void push(T value) {
            lock_guard<mutex> lock(mutex_);
            this->value = op(this->value, value);
        }

        T finalize() {
            lock_guard<mutex> lock(mutex_);
            return value;
        }
    };
    unique_ptr<pimpl> pimpl_;
};

template <typename T>
struct strong {
    T value;
};

class latch {
public:
    explicit latch(int count) : count_(count) {}

    void arrive_and_wait() {
        // std::unique_lock<mutex> lock(mutex_);
        std::unique_lock lock(mutex_);  // original
        count_--;
        cv_.notify_all();
        cv_.wait(lock, [this] { return count_ == 0; });
    }

private:
    int count_{};
    std::mutex mutex_;
    std::condition_variable cv_;
};

template <typename T>
struct move_only_plus : std::plus<T> {
    using std::plus<T>::plus;
    move_only_plus(move_only_plus const&)            = delete;
    move_only_plus& operator=(move_only_plus const&) = delete;
    move_only_plus(move_only_plus&&)                 = default;
    move_only_plus& operator=(move_only_plus&&)      = default;
    using std::plus<T>::operator();
};

// basic usage
bool check_001() {
    if constexpr (check_001_enabled) {
        auto acc = accumulator(10, std::plus<int>());
        acc.push(1);
        acc.push(2);
        acc.push(3);
        return acc.finalize() == 16;
    } else {
        return false;
    }
}

// eager evaluation
bool check_002() {
    if constexpr (check_002_enabled) {
        auto invoked = 0;
        auto acc     = accumulator(10, [&](auto lhs, auto rhs) {
            ++invoked;
            return lhs + rhs;
        });

        acc.push(1);
        if (invoked != 1) return false;

        acc.push(2);
        if (invoked != 2) return false;

        acc.push(3);
        if (invoked != 3) return false;

        return acc.finalize() == 16;
    } else {
        return false;
    }
}

// T is move-only
bool check_003() {
    if constexpr (check_003_enabled) {
        auto acc =
            accumulator(std::make_unique<int>(10), [](auto lhs, auto rhs) {
                return std::make_unique<int>(*lhs + *rhs);
            });
        acc.push(std::make_unique<int>(1));
        acc.push(std::make_unique<int>(2));
        acc.push(std::make_unique<int>(3));
        return *acc.finalize() == 16;
    } else {
        return false;
    }
}

// Op is move-only
bool check_004() {
    if constexpr (check_004_enabled) {
        auto acc = accumulator(10, move_only_plus<int>());
        acc.push(1);
        acc.push(2);
        acc.push(3);
        return acc.finalize() == 16;
    } else {
        return false;
    }
}

// stateful Op
bool check_005() {
    if constexpr (check_005_enabled) {
        constexpr double eps = 1e-6;
        auto acc =
            accumulator(0.0, [count = 0, sum = 0.0](auto _, auto rhs) mutable {
                sum += rhs;
                count++;
                return sum / count;
            });

        acc.push(0);
        acc.push(2);
        acc.push(4);
        acc.push(4);
        return std::abs(acc.finalize() - 2.5) < eps;
    } else {
        return false;
    }
}

// thread safety
bool check_006() {
    if constexpr (check_006_enabled) {
        constexpr auto nthread       = 10;
        constexpr auto op_per_thread = 100000;
        latch latch(nthread);
        std::vector<std::future<void>> futures;

        auto acc = accumulator(0, std::plus<int>());
        for (auto i = 0; i < nthread; ++i) {
            futures.push_back(std::async(std::launch::async, [&] {
                latch.arrive_and_wait();
                for (auto j = 0; j < op_per_thread; ++j) {
                    acc.push(1);
                }
            }));
        }
        futures.clear();

        return acc.finalize() == op_per_thread * nthread;
    } else {
        return false;
    }
}

// // generic_push
// bool check_007() {
//     if constexpr (check_007_enabled) {
//         auto acc =
//             accumulator(10, [](auto lhs, auto rhs) { return lhs + rhs.value; });
//         acc.generic_push(strong<int>{1});
//         acc.generic_push(strong<int>{2});
//         acc.generic_push(strong<int>{3});
//         return acc.finalize() == 16;
//     } else {
//         return false;
//     }
// }

// // generic_push with move-only U
// bool check_008() {
//     if constexpr (check_008_enabled) {
//         auto acc =
//             accumulator(10, [](auto lhs, auto rhs) { return lhs + *rhs; });
//         acc.generic_push(std::make_unique<int>(1));
//         acc.generic_push(std::make_unique<int>(2));
//         acc.generic_push(std::make_unique<int>(3));
//         return acc.finalize() == 16;
//     } else {
//         return false;
//     }
// }

// // generic_push does not steal ownership
// bool check_009() {
//     if constexpr (check_009_enabled) {
//         auto acc =
//             accumulator(10, [](auto lhs, auto rhs) { return lhs + *rhs; });

//         auto v1 = std::make_shared<int>(1);
//         auto v2 = std::make_shared<int>(2);
//         auto v3 = std::make_shared<int>(3);

//         acc.generic_push(v1);
//         acc.generic_push(v2);
//         acc.generic_push(v3);

//         return acc.finalize() == 16 && v1 && v2 && v3;
//     } else {
//         return false;
//     }
// }

int main() {
    // cout << "Result: " << check_001() << endl;
    // cout << "Result: " << check_002() << endl;
    // cout << "Result: " << check_003() << endl;
    // cout << "Result: " << check_004() << endl;
    // cout << "Result: " << check_005() << endl;
    // cout << "Result: " << check_006() << endl;
    // cout << "Result: " << check_007() << endl;
    // cout << "Result: " << check_008() << endl;
    // cout << "Result: " << check_009() << endl;
    return 0;
}
