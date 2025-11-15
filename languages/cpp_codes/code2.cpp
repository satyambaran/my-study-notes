#include <bits/stdc++.h>
using namespace std;

// Helper function to find the MEX of a vector
int get_mex(const vector<int> &arr)
{
    int n = arr.size();
    vector<bool> seen(n + 2, false);
    for (int x : arr)
    {
        if (x >= 0 && x <= n)
            seen[x] = true;
    }
    for (int i = 0; i <= n + 1; i++)
    {
        if (!seen[i])
            return i;
    }
    return n + 1;
}

// Brute force MEX split function
vector<int> maxArray(vector<int> a)
{
    vector<int> result;
    while (!a.empty())
    {
        int max_mex = get_mex(a);
        vector<int> prefix;
        for (int i = 0; i < (int)a.size(); i++)
        {
            prefix.push_back(a[i]);
            int cur_mex = get_mex(prefix);
            if (cur_mex == max_mex)
            {
                result.push_back(max_mex);
                // remove prefix from a
                a.erase(a.begin(), a.begin() + i + 1);
                break;
            }
            // if no cut found (shouldn't happen), we finish
            if (i == (int)a.size() - 1)
            {
                result.push_back(max_mex);
                a.clear();
            }
        }
    }
    return result;
}

int main()
{
    vector<vector<int>> tests = {
        {},
        {0, 1, 1, 0},
        {0, 1, 0, 1},
        {0, 0, 0},
        {1, 2, 3},
        {0, 1, 2, 3, 4},
        {2, 0, 1, 3, 0, 2, 1, 3},
        {0, 1, 1, 0},
        {8, 2, 2, 3, 4, 0, 1, 2, 0},
        {6, 0, 1, 2, 3, 4, 6, 6, 6, 6}};

    for (auto &arr : tests)
    {
        cout << "Input: ";
        for (int x : arr)
            cout << x << " ";
        cout << "\nOutput: ";
        auto res = maxArray(arr);
        for (int x : res)
            cout << x << " ";
        cout << "\n\n";
    }
    return 0;
}