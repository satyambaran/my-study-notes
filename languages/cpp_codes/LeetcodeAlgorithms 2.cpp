#include <bits/stdc++.h>
using namespace std;
int solve()
{
    vector<vector<int>> v = {{5, 2}, {5, 4}, {10, 3}, {20, 1}};
    int start = 0, end = 0, wait = 0;
    for (int i = 0; i < v.size(); i++)
    {
        if (end < v[i][0])
        {
            start = v[i][0];
        }
        else
        {
            start = end;
        }
        end = start + v[i][1];
        wait += end - v[i][0];
    }
    return round((double)wait / v.size());
}
int solve2()
{
    string str = "abacbc";
    unordered_map<char, int> end;
    int n = str.size();
    for (int i = 0; i < n; i++)
    {
        end[str[i]] = i;
    }
    int i = 0, endIdx = 0, cnt = 0;
    while (i < n)
    {
        endIdx = end[str[i]];
        while (i < endIdx)
        {
            endIdx = max(endIdx, end[str[i]]);
            i++;
        }
        i++;
        cnt++;
    }
    return cnt;
}
class SmallestMissingPositive
{
private:
    set<int> seen;
    int smallest = 0;

public:
    void add(int num)
    {
        if (num >= 0)
        {
            seen.insert(num);
        }
        while (seen.count(smallest))
        {
            smallest++;
        }
    }

    int getSmallest()
    {
        return smallest;
    }

    int getLargest()
    {
        if (seen.empty())
            return -1;
        return *seen.rbegin();
    }

    void clear()
    {
        seen.clear();
        smallest = 0;
    }
};

vector<int> getMaxArray(vector<int> &v, int n)
{
    vector<int> ans;
    int mxVal = 0, mxIdx = -1;
    SmallestMissingPositive *smp = new SmallestMissingPositive();

    int i;
    while (mxIdx < n - 1)
    {
        for (i = mxIdx + 1; i < n; i++)
        {
            smp->add(v[i]);
            if (smp->getSmallest() > mxVal)
            {
                mxVal = smp->getSmallest();
                mxIdx = i;
            }
        }
        ans.push_back(mxVal);
        if (mxVal == 0)
        {
            mxIdx++;
        }

        smp->clear();
        mxVal = 0;
    }
    return ans;
}

int main()
{
    int n = 4;
    // vector<int> v = {0, 1, 1, 0};
    // vector<int> v = {8, 2, 2, 3, 4, 0, 1, 2, 0};
    vector<int> v = {6, 0, 1, 2, 3, 4, 6, 6, 6, 6};

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
        auto res = getMaxArray(arr, arr.size());
        for (int x : res)
            cout << x << " ";
        cout << "\n\n";
    }

    cout << solve2();
    return 0;
}
// n = v.size();
// vector<int> result = getMaxArray(v, n);
// for (auto x : result)
// {
//     cout << x << " ";
// }
// return 0;
// }

// set<int> s;

// vector<int> ans;
// int mxVal = 0, mxIdx = -1;
// SmallestMissingPositive *smp = new SmallestMissingPositive();

// int i;
// while (mxIdx < n - 1)
// {
//     for (i = mxIdx + 1; i < n; i++)
//     {
//         smp->add(v[i]);
//         if (smp->getSmallest() > mxVal)
//         {
//             mxVal = smp->getSmallest();
//             mxIdx = i;
//         }
//     }
//     if (mxVal == 0)
//         break;
//     ans.push_back(mxVal);
//     smp->clear();
//     mxVal = 0;
// }
// for (auto x : ans)
// {
//     cout << x << " ";
// }
// return 0;
// }