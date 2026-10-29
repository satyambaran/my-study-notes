#include <bits/stdc++.h>

#include <cstdlib>
#include <ctime>
#include <iostream>
#include <queue>
#include <stdexcept>
using namespace std;

// Define the structure for a Tree Node
struct TreeNode {
    int val;
    std::vector<TreeNode*> children;

    TreeNode(int value) : val(value) {}
};

// Define the Tree class with the required methods
class Tree {
public:
    TreeNode* root;
    std::vector<TreeNode*> nodes;      // To keep track of all nodes
    std::vector<TreeNode*> leafNodes;  // To keep track of leaf nodes

    Tree(int rootVal) {
        root = new TreeNode(rootVal);
        nodes.push_back(root);
        leafNodes.push_back(root);
        std::srand(static_cast<unsigned int>(std::time(0)));  // Seed the random number generator
    }

    void addNode(TreeNode* parent, int val) {
        if (!parent) {
            throw std::invalid_argument("Parent node cannot be null");
        }

        TreeNode* newNode = new TreeNode(val);
        parent->children.push_back(newNode);
        nodes.push_back(newNode);

        // If parent was a leaf node, it's not a leaf anymore
        auto it = std::find(leafNodes.begin(), leafNodes.end(), parent);
        if (it != leafNodes.end()) {
            leafNodes.erase(it);
        }

        // New node is a leaf node
        leafNodes.push_back(newNode);
    }

    TreeNode* getRandomNode() {
        if (nodes.empty()) {
            throw std::runtime_error("The tree is empty");
        }
        int randomIndex = std::rand() % nodes.size();
        return nodes[randomIndex];
    }

    TreeNode* getRandomLeafNode() {
        if (leafNodes.empty()) {
            throw std::runtime_error("No leaf nodes in the tree");
        }
        int randomIndex = std::rand() % leafNodes.size();
        return leafNodes[randomIndex];
    }
};
void solve() {
    vector<vector<int>> v = {
        {1, 2},
        {3, 2},
        {5, 3},
        {6, 2},
        {7, 2}
    };
    int cur = 0, n = v.size();
    map<int, int> a, b;
    a[0]    = 0;
    int day = 0;
    for (int i = 0; i < n; i++) {
        for (auto it : a) {
            // no buying
            b[it.first + 1] = max(b[it.first + 1], it.second);
            // buying
            if (it.first + 1 >= v[i][1]) {
                b[it.first + 1 - v[i][1]] = max(b[it.first + 1 - v[i][1]], it.second + 1);
            }
        }
    }
}
int main() {
    Tree tree(1);  // Create a tree with root node value 1

    // Add nodes to the tree
    tree.addNode(tree.root, 2);
    tree.addNode(tree.root, 3);
    tree.addNode(tree.root->children[0], 4);
    tree.addNode(tree.root->children[0], 5);
    tree.addNode(tree.root->children[1], 6);
    tree.addNode(tree.root->children[1], 7);

    // Get a random node
    TreeNode* randomNode = tree.getRandomNode();
    std::cout << "Random node value: " << randomNode->val << std::endl;

    // Get a random leaf node
    TreeNode* randomLeafNode = tree.getRandomLeafNode();
    std::cout << "Random leaf node value: " << randomLeafNode->val << std::endl;

    return 0;
}