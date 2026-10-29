import java.util.HashMap;
import java.util.Map;

/**
 * BANK COIN EXCHANGE — Maximize Money via Recursive Exchange
 * ============================================================
 *
 * PROBLEM: You have a coin of value N. You can either keep it as N,
 * or exchange it for coins of value N/2 + N/3 + N/4 (integer division).
 * Find the maximum money you can get.
 *
 * APPROACH: Top-down DP with memoization.
 *   maxMoney(N) = max(N, maxMoney(N/2) + maxMoney(N/3) + maxMoney(N/4))
 *
 * WHY IT WORKS:
 * - At each step, we choose the better option: keep or exchange.
 * - Subproblems overlap heavily (e.g., N/2/3 == N/3/2), so memoization
 *   prevents recomputation.
 *
 * TIME: O(N) unique subproblems (each N/2, N/3, N/4 reduces quickly).
 *       Actually O(N^(2/3)) due to the nature of the recursion tree.
 * SPACE: O(N^(2/3)) for memoization map + recursion stack.
 *
 * EXAMPLES:
 *   N=12 -> max(12, f(6)+f(4)+f(3)) = max(12, 6+4+3) = 13
 *   N=2  -> max(2, f(1)+f(0)+f(0)) = max(2, 1+0+0) = 2
 */
public class CoinExchange {

    private final Map<Integer, Integer> memo = new HashMap<>();

    public int getMaxMoney(int n) {
        if (n == 0) return 0;
        if (memo.containsKey(n)) return memo.get(n);

        int exchangeValue = getMaxMoney(n / 2) + getMaxMoney(n / 3) + getMaxMoney(n / 4);
        int result = Math.max(n, exchangeValue);

        memo.put(n, result);
        return result;
    }

    public static void main(String[] args) {
        CoinExchange exchange = new CoinExchange();

        System.out.println("N=12: " + exchange.getMaxMoney(12));    // 13
        System.out.println("N=100: " + exchange.getMaxMoney(100));  // Expected > 100
        System.out.println("N=2: " + exchange.getMaxMoney(2));      // 2
    }
}
