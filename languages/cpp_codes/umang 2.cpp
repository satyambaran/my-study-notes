#include <bits/stdc++.h>
using namespace std;
// class Message {
// public:
//     Message* next;
//     Message* prev;
//     int time;
//     string name;
//     Message(string _name, int _time) {
//         name = _name;
//         time = _time;
//     }
// };
// class Solution {
//     Message *start, *end;
//     unordered_map<string, Message*> um;
//     int window;

// public:
//     Solution(int _window) {
//         start  = nullptr;
//         end    = nullptr;
//         window = _window;
//     }
//     void dataStream(string name, int time) {
//         while (start && ((start->time + window) < time)) {
//             // cout << start->name << endl;
//             um.erase(start->name);
//             if (start == end) {
//                 start = start->next;
//                 if (start) start->prev = nullptr;
//                 end = end->next;
//                 if (end) end->prev = nullptr;
//             } else {
//                 start = start->next;
//                 if (start) start->prev = nullptr;
//             }
//         }
//         if (um.find(name) == um.end()) {
//             cout << name << "\n";
//         } else {
//             Message* temp = um[name];
//             if (temp->time + window < time) {
//                 cout << name << endl;
//             }
//             if (temp == start) {
//                 start = start->next;
//                 if (start) start->prev = nullptr;
//             } else if (temp == end) {
//                 end = end->prev;
//             } else {
//                 temp->prev->next = temp->next;
//                 temp->next->prev = temp->prev;
//             }
//         }
//         Message* node = new Message(name, time);
//         um[name]      = node;
//         if (end == nullptr) {
//             start = end = node;
//         } else {
//             end->next  = node;
//             node->prev = end;
//             end        = node;
//         }
//     }
// };

// void solve() {
//     int t = 1;
//     // cin >> t;
//     while (t--) {
//         int window, n, time;
//         string s;
//         cin >> n >> window;
//         auto stream = new Solution(window);
//         while (n--) {
//             cin >> time >> s;
//             // cout << time << " " << s << endl;
//             stream->dataStream(s, time);
//         }
//     }
// }

// int main() {
//     solve();
//     /*
//         7 10
//         2 satyam
//         5 divya
//         8 umang
//         11 satyam
//         13 satyam
//         14 divya
//         19 umang
//     */
// }

// CPP Program to implement Deque in STL
#include <deque>
#include <iostream>

using namespace std;

void showdq(deque<int> g) {
    deque<int>::iterator it;
    for (it = g.begin(); it != g.end(); ++it)
        cout << '\t' << *it;
    cout << '\n';
}

int main() {
    deque<int> gquiz;
    gquiz.push_back(10);
    gquiz.push_front(20);
    gquiz.push_back(30);
    gquiz.push_front(15);
    while (gquiz.size()) {
        cout << gquiz.front() << " ";
        gquiz.pop_front();
    }
    cout << endl;
    gquiz.push_back(10);
    gquiz.push_front(20);
    gquiz.push_back(30);
    gquiz.push_front(15);
    while (gquiz.size()) {
        cout << gquiz.back() << " ";
        gquiz.pop_back();
    }

    return 0;
}
class Message {
public:
    Message* next;
    int time;
    string name;
    Message(string _name, int _time) {
        name = _name;
        time = _time;
    }
};
class Solution {
    Message *start, *end;
    unordered_set<string> us;
    int window;

public:
    Solution(int _window) {
        start  = nullptr;
        end    = nullptr;
        window = _window;
    }
    void dataStream(string name, int time) {
        while (start && ((start->time + window) < time)) {
            // cout << start->name << endl;
            us.erase(start->name);
            if (start == end) {
                start = start->next;
                end   = end->next;
            } else {
                start = start->next;
            }
        }
        if (us.find(name) == us.end()) {
            cout << name << "\n";
            us.insert(name);
            if (end == nullptr) {
                start = end = new Message(name, time);
            } else {
                end->next = new Message(name, time);
                end       = end->next;
            }
        }
    }
};

void solve() {
    int t = 1;
    // cin >> t;
    while (t--) {
        int window, n, time;
        string s;
        cin >> n >> window;
        auto stream = new Solution(window);
        while (n--) {
            cin >> time >> s;
            // cout << time << " " << s << endl;
            stream->dataStream(s, time);
        }
    }
}
