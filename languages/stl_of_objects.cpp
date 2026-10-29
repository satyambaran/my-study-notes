#include <bits/stdc++.h>
using namespace std;

struct MyObject {
    int id;
    string name;

    // Define equality operator for unordered containers
    bool operator==(const MyObject& other) const {
        return id == other.id && name == other.name;
    }

    // Define less-than operator for ordered containers
    bool operator<(const MyObject& other) const {
        return id < other.id || (id == other.id && name < other.name);
    }
};
namespace std {
template <>
struct hash<MyObject> {
    size_t operator()(const MyObject& obj) const {
        return hash<int>()(obj.id) ^ hash<string>()(obj.name);
    }
};
}  // namespace std
// Custom hash function for array<int, 3>
struct ArrayHash {
    size_t operator()(const array<int, 3>& arr) const {
        size_t h1 = hash<int>()(arr[0]);
        size_t h2 = hash<int>()(arr[1]);
        size_t h3 = hash<int>()(arr[2]);
        return h1 ^ (h2 << 1) ^ (h3 << 2);  // Combine the hashes
    }
};
// Custom hash function for std::vector<int>
struct VectorHash {
    std::size_t operator()(const std::vector<int>& vec) const {
        std::size_t seed = vec.size();
        for (const auto& i : vec) {
            seed ^= std::hash<int>()(i) + 0x9e3779b9 + (seed << 6) + (seed >> 2);
            // seed ^= std::hash<int>()(i);  //+ 0x9e3779b9 + (seed << 6) + (seed >> 2);
        }
        cout << seed << endl;
        return seed;
    }
};
int main() {
    // // Unordered Set of MyObject
    // unordered_set<MyObject> unorderedSet;

    // // Ordered Set of MyObject
    // set<MyObject> orderedSet;

    // // Multiset of MyObject
    // multiset<MyObject> multiSet;

    // // Ordered Map with MyObject as key and int as value
    // map<MyObject, int> orderedMap;

    // // Unordered Map with MyObject as key and int as value
    // unordered_map<MyObject, int> unorderedMap;

    // // Queue of MyObject
    // queue<MyObject> myQueue;

    // // Priority Queue of MyObject (using a custom comparator for the priority queue)
    // priority_queue<MyObject, vector<MyObject>, greater<MyObject>> myPriorityQueue;

    unordered_set<array<int, 3>, ArrayHash> unorderedSetArray;
    unorderedSetArray.insert({
        {1, 2, 3}
    });
    unorderedSetArray.insert({
        {4, 5, 6}
    });
    unorderedSetArray.insert({
        {7, 8, 9}
    });

    for (const auto& arr : unorderedSetArray) {
        cout << "Array: {" << arr[0] << ", " << arr[1] << ", " << arr[2] << "}\n";
    }
    // Ordered Set of array<int, 3>
    set<array<int, 3>> orderedSetArray;

    orderedSetArray.insert({
        {1, 2, 3}
    });
    orderedSetArray.insert({
        {4, 5, 6}
    });
    orderedSetArray.insert({
        {7, 8, 9}
    });

    for (const auto& arr : orderedSetArray) {
        cout << "Array: {" << arr[0] << ", " << arr[1] << ", " << arr[2] << "}\n";
    }
    // Multiset of array<int, 3>
    multiset<array<int, 3>> multiSetArray;

    multiSetArray.insert({
        {1, 2, 3}
    });
    multiSetArray.insert({
        {4, 5, 6}
    });
    multiSetArray.insert({
        {7, 8, 9}
    });
    multiSetArray.insert({
        {1, 2, 3}
    });
    multiSetArray.insert({
        {4, 5, 6}
    });
    multiSetArray.insert({
        {7, 8, 9}
    });

    for (const auto& arr : multiSetArray) {
        cout << "Array: {" << arr[0] << ", " << arr[1] << ", " << arr[2] << "}\n";
    }
    // // Ordered Map with array<int, 3> as key and int as value
    map<array<int, 3>, int> orderedMapArray;

    // // Unordered Map with array<int, 3> as key and int as value
    unordered_map<array<int, 3>, int, ArrayHash> unorderedMapArray;

    // // Queue of array<int, 3>
    queue<array<int, 3>> myQueueArray;

    // // Priority Queue of array<int, 3>
    priority_queue<array<int, 3>> myPriorityQueueArray;

    // // Similar for vector<int>

    set<vector<int>> orderedSetVector;
    multiset<vector<int>> multiSetVector;
    map<vector<int>, int> orderedMapVector;
    unordered_set<vector<int>, VectorHash> unorderedSetVector;
    unordered_map<vector<int>, int, VectorHash> unorderedMapVector;
    unorderedSetVector.insert({1, 2, 3});
    unorderedSetVector.insert({4, 5, 6});
    unorderedSetVector.insert({7, 7, 8, 9});
    unorderedSetVector.insert({7, 7, 7, 7, 7, 8, 9});
    unorderedSetVector.insert({7, 8, 8, 8, 8, 8, 9});
    unorderedSetVector.insert({4, 5, 6});
    unorderedSetVector.insert({7, 8, 9});

    for (const auto& vec : unorderedSetVector) {
        std::cout << "Vector: ";
        for (int num : vec) {
            std::cout << num << " ";
        }
        std::cout << "\n";
    }
    cout << hash<int>()(5);
    queue<vector<int>> myQueueVector;
    priority_queue<vector<int>> myPriorityQueueVector;
    return 0;
}