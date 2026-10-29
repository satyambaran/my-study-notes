
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
    accumulator(T init, Op op) : pimpl_(make_unique<pimpl>(move(init), move(op))) {}

    template <typename U>
    void generic_push(U&& value) {
        pimpl_->generic_push(forward<U>(value));
    }

    void push(T value) {
        pimpl_->push(move(value));
    }

    unique_ptr<T> finalize() {
        return pimpl_->finalize();
    }

private:
    struct pimpl {
        T value;
        Op op;
        mutable mutex mutex_;

        pimpl(T init, Op op) : value(move(init)), op(move(op)) {}

        template <typename U>
        void generic_push(U&& value) {
            // Dereference the pointers and apply the operation
            lock_guard<mutex> lock(mutex_);
            this->value = op(move(this->value), move(value));
        }

        void push(T value) {
            // Dereference the pointers and apply the operation
            lock_guard<mutex> lock(mutex_);
            this->value = op(move(this->value), move(value));
        }

        unique_ptr<T> finalize() {
            lock_guard<mutex> lock(mutex_);
            return make_unique<T>(move(value));
        }
    };

    unique_ptr<pimpl> pimpl_;
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
// thread safety
bool check_006() {
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

    return *acc.finalize() == op_per_thread * nthread;
}
bool check_007() {
    auto acc =
        accumulator(10, [](auto lhs, auto rhs) { return lhs + *rhs; });

    auto v1 = make_shared<int>(1);
    auto v2 = make_shared<int>(2);
    auto v3 = make_shared<int>(3);

    acc.generic_push(v1);
    acc.generic_push(v2);
    acc.generic_push(v3);
    bool valid_pointers = (v1 && v2 && v3);

    int final_value    = *acc.finalize();
    bool correct_value = (final_value == 16);
    cout << valid_pointers << " " << final_value << " " << correct_value << " " << endl;

    return (*acc.finalize() == 16) && (v1 && v2 && v3);
}
int main() {
    // cout << check_006();
    cout << check_007();
    return 0;
}
