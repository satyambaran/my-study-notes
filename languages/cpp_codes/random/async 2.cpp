#include <bits/stdc++.h>
using namespace std;

using ull = uint64_t;
bool isPrime(ull n) {
    for (int i = sqrt(n); i > 1; i--) {
        if (n % i == 0) return false;
    }
    return true;
}
ull findLargestPrimeInRange(ull start, ull end) {
    // Ensure 'start' is odd
    if (start % 2 == 0)
        start--;

    // Iterate backwards through the range, checking for primality
    for (ull i = start; i >= end; i -= 2) {
        if (isPrime(i))
            return i;
    }

    return 2;  // 2 is the smallest prime number
}
// a non-optimized way of checking for prime numbers:
bool is_prime(uint64_t x) {
    cout << "Calculating. Please, wait...\n";
    for (int i = 2; i < x; ++i) {
        // sleep
        if (x % i == 0) return false;
    }
    return true;
}

int main() {
    // ull start = 18446744073709551615ull;
    // ull end   = 0;

    // // Find the largest prime number in the range
    // ull largestPrime = findLargestPrimeInRange(start, end);

    // // Output the result
    // cout << "Largest prime number in the range: " << largestPrime <<endl;

    uint64_t k       = 18446744073709551557ull;
    future<bool> fut = async(is_prime, k);

    cout << "Checking whether " << k << " is prime.\n";
    // ...

    bool ret = fut.get();  // waits for is_prime to return

    if (ret)
        cout << "It is prime!\n";
    else
        cout << "It is not prime.\n";

    return 0;
}
