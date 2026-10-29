#include <bits/stdc++.h>
using namespace std;
#define int long long
class Node {
   public:
    int val;
    Node* next;
    Node() {}
    Node(int val) {
        this->val = val;
        this->next = nullptr;
    }
};
Node* merge(Node* a, Node* b) {
    if (a == nullptr) return b;
    if (b == nullptr) return a;
    // Node* root = a;
    // if (a->val>b->val) {
    //     root = b;
    // }
    // while (a&&b) {
    //     if (a->val>b->val) {
    //         root->next = b;
    //         b = b->next;
    //     }
    //     else {
    //         root->next = a;
    //         a = a->next;
    //     }
    //     root = root->next;
    // }
    // while (b) {
    //     root->next = b;
    //     b = b->next;
    //     root = root->next;
    // }
    // while (a) {
    //     root->next = a;
    //     a = a->next;
    //     root = root->next;
    // }
    // return root;

    Node* root;
    if (a->val < b->val) {
        root = a;
        root->next = merge(a->next, b);
    } else {
        root = b;
        root->next = merge(a, b->next);
    }
    return root;
}
Node* getMid(Node* root) {
    Node *slow = root, *fast = root->next;  //? tricky
    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
    }
    return slow;
}
Node* mergeSort(Node* root) {
    if (root == nullptr || root->next == nullptr) {
        return root;
    }
    Node* mid = getMid(root);
    Node* a = mid->next;
    mid->next = nullptr;
    Node* left = mergeSort(root);
    Node* right = mergeSort(a);
    return merge(left, right);
}
Node* removeDuplicates(Node* root) {
    Node* ret = root;
    if (!root) return root;
    int val = root->val;
    while (root && root->next) {
        if (root->val == root->next->val) {
            Node* temp = root->next;
            root->next = root->next->next;
            free(temp);
        }
        root = root->next;
    }
    return ret;
}
int32_t main() {
    int a, n;
    cin >> n;
    Node *root, *cur, *prev;
    if (n >= 1) {
        cin >> a;
        root = new Node(a);
        prev = root;
        n--;
    }
    while (n--) {
        cin >> a;
        cur = new Node(a);
        prev->next = cur;
        prev = cur;
    }
    Node* head = root;
    root = mergeSort(head);  // sort
    head = root;
    while (root) {
        cout << root->val << " ";
        root = root->next;
    }
    cout << endl;
    root = removeDuplicates(head);  // removee duplicates
    while (root) {
        cout << root->val << " ";
        root = root->next;
    }

    return 0;
}

// #include <bits/stdc++.h>
// using namespace std;
// int partition(int arr[], int l, int h) {
//     int temp = arr[h], i = l;
//     for (int j = l; j <= h; j++) {
//         if (arr[j] < temp) {
//             swap(arr[j], arr[i++]);
//         }
//     }
//     swap(arr[i], arr[h]);
//     return i;
// }
// void quicksort(int arr[], int l, int h) {
//     if (l >= h) return;
//     int m = partition(arr, l, h);
//     quicksort(arr, l, m - 1);
//     quicksort(arr, m + 1, h);
// }
// int main() {
//     int arr[] = {10, 7, 8, 9, 1, 5};
//     int n = sizeof(arr) / sizeof(int);
//     quicksort(arr, 0, n - 1);
//     cout << n;
//     for (int i = 0; i < n; i++) cout << arr[i];
//     return 0;
// }
