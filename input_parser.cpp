#include <bits/stdc++.h>
using namespace std;

struct Input {
    int nextInt() {
        int value;
        cin >> value;
        return value;
    }

    string token() {
        string value;
        cin >> value;
        return value;
    }

    // Ignore separators, then keep exactly n characters, including internal spaces.
    string fixed(int n) {
        string value(n, ' ');
        cin >> ws;
        cin.read(value.data(), n);
        return value;
    }

    // Return the next non-empty line.
    string line() {
        string value;
        getline(cin >> ws, value);
        return value;
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    Input in;

    int x = in.nextInt();
    vector<string> text(x);
    for (string& value : text) {
        int length = in.nextInt();
        value = in.fixed(length);
    }

    int n = in.nextInt(), m = in.nextInt(), sum = 0;
    vector<vector<int>> matrix(n, vector<int>(m));
    for (auto& row : matrix)
        for (int& value : row)
            value = in.nextInt(), sum += value;

    int b = in.nextInt();
    vector<string> lines(b);
    for (string& value : lines) value = in.line();

    cout << "text:\n";
    for (const string& value : text) cout << value << '\n';
    cout << "matrix:\n";
    for (const auto& row : matrix) {
        for (int value : row) cout << value << ' ';
        cout << '\n';
    }
    cout << "sum: " << sum << "\nlines:\n";
    for (const string& value : lines) cout << value << '\n';
}
