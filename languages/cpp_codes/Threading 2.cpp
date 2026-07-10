#include <bits/stdc++.h>
using namespace std;
class BoundedBlockingQueue {
    int n;
    condition_variable cv;
    mutex mtx;
    queue<int> q;
    BoundedBlockingQueue(int n) {
        this->n = n;
    }
    void Enqueue(int a) {
        unique_lock<mutex> l(mtx);
        while (q.size() == n) {
            cv.wait(l);
        }
        q.push(a);
        cv.notify_all();
    }
    int Dequeue() {
        unique_lock<mutex> l(mtx);
        while (q.size() == 0) {
            cv.wait(l);
        }
        int cur = q.front();
        q.pop();
        cv.notify_all();
        return cur;
    }
};

class Foo {
public:
    condition_variable cv;
    mutex mtx;
    int a, b, c;
    Foo() {
        a = b = c = 0;
    }

    //         function<return_type(...args)> name
    void first(function<void(int)> print) {
        print(1);
        a = 1;
        cv.notify_all();
    }
    void second(function<void(int)> print) {
        unique_lock<mutex> l(mtx);
        while (a == 0) {
            cv.wait(l);
        }
        print(2);
        b = 1;
        cv.notify_all();
    }
    void third(function<void(int)> print) {
        unique_lock<mutex> l(mtx);
        while (b == 0) {
            cv.wait(l);
        }
        print(3);
        c = 1;
        cv.notify_all();
    }
};
void print(int a) { cout << a << " "; }
int main() {
    auto k = new Foo();
    Foo foo;
    // thread t1(&Foo::third, &foo, print);
    // thread t2(&Foo::first, &foo, print);
    // thread t3(&Foo::second, &foo, print);
    thread t1(&Foo::third, k, print);
    thread t2(&Foo::first, k, print);
    thread t3(&Foo::second, k, print);
    t1.join();
    t2.join();
    t3.join();

    // vector<int> vec = {45, 40, 35, 12, 6, 3};
    // auto itr1       = lower_bound(vec.rbegin(), vec.rend(), 40) - vec.rbegin();
    // auto itr2       = upper_bound(vec.rbegin(), vec.rend(), 40) - vec.rbegin();
    // auto itr3 =
    //     lower_bound(vec.begin(), vec.end(), 40, greater<int>()) - vec.begin();
    // auto itr4 =
    //     upper_bound(vec.begin(), vec.end(), 40, greater<int>()) - vec.begin();
    // cout << itr1 << itr2 << itr3 << itr4;
}