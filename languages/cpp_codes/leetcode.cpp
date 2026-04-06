#include <bits/stdc++.h>
using namespace std;
class Node {
   public:
    int val, key, freq;
    Node *prev, *next;
    Node(int val = 0, int key = 0, int freq = 0) {
        this->val = val;
        this->key = key;
        this->freq = freq;
    }
};
class LFUCache {
    int sz, minFreq;
    unordered_map<int, Node*> mp;
    unordered_map<int, array<Node*, 2>> freqList;

   public:
    LFUCache(int capacity) {
        sz = capacity;
        minFreq = 0;
        freqList[minFreq] = {new Node(), new Node()};
        freqList[minFreq][1]->next = freqList[minFreq][0];
        freqList[minFreq][0]->prev = freqList[minFreq][1];
    }

    int get(int key) {
        auto it = mp.find(key);
        if (it == mp.end()) return -1;
        Node* node = mp[key];
        viewList(node->freq);
        remove(node);
        node->freq++;
        insert(node);
        return node->val;
    }

    void put(int key, int value) {
        auto it = mp.find(key);
        Node* node;
        if (it != mp.end()) {
            node = mp[key];
            remove(node);
        }
        if (sz == mp.size()) {
            remove(freqList[minFreq][1]->next);
        }
        insert(new Node(key, value, 0));
    }
    void insert(Node* node) {
        minFreq = min(minFreq, node->freq);
        if (freqList.find(node->freq) == freqList.end()) {
            freqList[node->freq] = {new Node(), new Node()};
            freqList[node->freq][1]->next = freqList[node->freq][0];
            freqList[node->freq][0]->prev = freqList[node->freq][1];
        }
        Node *he = freqList[node->freq][0], *ta = freqList[node->freq][1];
        Node* t = he->prev;
        node->prev = t;
        node->next = he;
        he->prev = node;
        t->next = node;
        mp[node->key] = node;
    }
    void remove(Node* node) {
        Node* nex = node->next;
        Node* pre = node->prev;
        pre->next = nex;
        nex->prev = pre;
        mp.erase(node->key);
        while (freqList.find(minFreq) == freqList.end() ||
               freqList[minFreq][0]->prev == freqList[minFreq][1]) {
            freqList.erase(minFreq);
            minFreq++;
        }
    }
    void viewList(int freq) {
        cout << "from head" << endl;
        Node* cur = freqList[freq][0];
        while (cur != nullptr) {
            int v = cur->val;
            cout << v << ",";
            cur = cur->next;
        }
        cout << endl;
    }
};

/**
 * Your LFUCache object will be instantiated and called as such:
 * LFUCache* obj = new LFUCache(capacity);
 * int param_1 = obj->get(key);
 * obj->put(key,value);
 */
/*


*/