
#include <launch.h>

#include <functional>
#include <future>
#include <iostream>
#include <memory>
#include <vector>
using namespace std;

template <typename T, typename Op>
class accumulator {
public:
    accumulator(T init, Op op) : pimpl_(std::make_unique<pimpl>(init, std::move(op))) {}

    template <typename U>
    void generic_push(U&& value) {
        pimpl_->generic_push(std::forward<U>(value));
    }

    void push(T value) {
        pimpl_->push(value);
    }

    T finalize() {
        return pimpl_->finalize();
    }

private:
    struct pimpl {
        T value;
        Op op;

        pimpl(T init, Op op) : value(init), op(std::move(op)) {}

        template <typename U>
        void generic_push(U&& value) {
            // Apply the operation to the current value and the new value
            this->value = op(this->value, std::forward<U>(value));
        }

        void push(T value) {
            // Apply the operation to the current value and the new value
            this->value = op(this->value, value);
        }

        T finalize() {
            return value;
        }
    };

    std::unique_ptr<pimpl> pimpl_;
};
// basic usage
bool check_001() {
    auto acc = accumulator(10, std::plus<int>());
    acc.push(1);
    acc.push(2);
    acc.push(3);
    return acc.finalize() == 16;
}

// eager evaluation
bool check_002() {
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
}

// // T is move-only
bool check_003() {
    auto acc =
        accumulator(std::make_unique<int>(10), [](auto lhs, auto rhs) {
            return std::make_unique<int>(*lhs + *rhs);
        });
    acc.push(std::make_unique<int>(1));
    acc.push(std::make_unique<int>(2));
    acc.push(std::make_unique<int>(3));
    return *acc.finalize() == 16;
    // return **acc.finalize() == 16;
}
template <typename T>
struct move_only_plus : std::plus<T> {
    using std::plus<T>::plus;
    move_only_plus(move_only_plus const&)            = delete;
    move_only_plus& operator=(move_only_plus const&) = delete;
    move_only_plus(move_only_plus&&)                 = default;
    move_only_plus& operator=(move_only_plus&&)      = default;
    using std::plus<T>::operator();
};
// Op is move-only
bool check_004() {
    auto acc = accumulator(10, move_only_plus<int>());
    acc.push(1);
    acc.push(2);
    acc.push(3);
    return acc.finalize() == 16;
}

// stateful Op
bool check_005() {
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
}
// class latch {
// public:
//     explicit latch(int count) : count_(count) {}

//     void arrive_and_wait() {
//         // std::unique_lock<mutex> lock(mutex_);
//         std::unique_lock lock(mutex_);  // original
//         count_--;
//         cv_.notify_all();
//         cv_.wait(lock, [this] { return count_ == 0; });
//     }

// private:
//     int count_{};
//     std::mutex mutex_;
//     std::condition_variable cv_;
// };
// // thread safety
// bool check_006() {
//     constexpr auto nthread       = 10;
//     constexpr auto op_per_thread = 100000;
//     latch latch(nthread);
//     std::vector<std::future<void>> futures;

//     auto acc = accumulator(0, std::plus<int>());
//     for (auto i = 0; i < nthread; ++i) {
//         futures.push_back(std::async(std::launch::async, [&] {
//             latch.arrive_and_wait();
//             for (auto j = 0; j < op_per_thread; ++j) {
//                 acc.push(1);
//             }
//         }));
//     }
//     futures.clear();

//     return acc.finalize() == op_per_thread * nthread;
// }

// // generic_push
// template <typename T>
// struct strong {
//     T value;
// };
// bool check_007() {
//     auto acc =
//         accumulator(10, [](auto lhs, auto rhs) { return lhs + rhs.value; });
//     acc.generic_push(strong<int>{1});
//     acc.generic_push(strong<int>{2});
//     acc.generic_push(strong<int>{3});
//     return acc.finalize() == 16;
// }

// // generic_push with move-only U
// bool check_008() {
//     auto acc =
//         accumulator(10, [](auto lhs, auto rhs) { return lhs + *rhs; });
//     acc.generic_push(std::make_unique<int>(1));
//     acc.generic_push(std::make_unique<int>(2));
//     acc.generic_push(std::make_unique<int>(3));
//     return acc.finalize() == 16;
// }

// // generic_push does not steal ownership
// bool check_009() {
//     auto acc =
//         accumulator(10, [](auto lhs, auto rhs) { return lhs + *rhs; });

//     auto v1 = std::make_shared<int>(1);
//     auto v2 = std::make_shared<int>(2);
//     auto v3 = std::make_shared<int>(3);

//     acc.generic_push(v1);
//     acc.generic_push(v2);
//     acc.generic_push(v3);

//     return acc.finalize() == 16 && v1 && v2 && v3;
// }

int main() {
    cout<<check_003();
    auto acc = accumulator(10, move_only_plus<int>());
    acc.push(1);
    acc.push(2);
    acc.push(3);
    cout << acc.finalize();
    return 0;
}

// template <typename T, typename Op>
// class accumulator {
// public:
//     accumulator(T init, Op op) : pimpl_(std::make_unique<pimpl>(init, std::move(op))) {}

//     template <typename U>
//     void generic_push(U&& value) {
//         pimpl_->generic_push(std::forward<U>(value));
//     }

//     void push(T value) {
//         pimpl_->push(value);
//     }

//     T finalize() {
//         return pimpl_->finalize();
//     }

// private:
//     struct pimpl {
//         T value;
//         Op op;

//         pimpl(T init, Op op) : value(init), op(std::move(op)) {}

//         template <typename U>
//         void generic_push(U&& value) {
//             // Apply the operation to the current value and the new value
//             this->value = op(this->value, std::forward<U>(value));
//         }

//         void push(T value) {
//             // Apply the operation to the current value and the new value
//             this->value = op(this->value, value);
//         }

//         T finalize() {
//             return value;
//         }
//     };

//     std::unique_ptr<pimpl> pimpl_;
// };

// // move_only_plus functor
// template <typename T>
// struct move_only_plus : std::plus<T> {
//     using std::plus<T>::plus;
//     move_only_plus(move_only_plus const&)            = delete;
//     move_only_plus& operator=(move_only_plus const&) = delete;
//     move_only_plus(move_only_plus&&)                 = default;
//     move_only_plus& operator=(move_only_plus&&)      = default;
//     using std::plus<T>::operator();
// };

// int main() {
//     auto acc = accumulator(10, move_only_plus<int>());
//     acc.push(1);
//     acc.push(2);
//     acc.push(3);
//     cout << acc.finalize();
// }
