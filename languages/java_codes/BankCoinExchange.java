import java.util.HashMap;
import java.util.Map;

public class BankCoinExchange {

    private Map<Integer, Integer> memo = new HashMap<>();

    // Function to calculate the maximum money
    public int getMaxMoney(int N) {
        // Base case: If N is 0, no money can be obtained
        if (N == 0) {
            return 0;
        }

        // Check if the result for N is already computed (memoization)
        if (memo.containsKey(N)) {
            return memo.get(N);
        }

        // Recursive step: max of taking the coin or exchanging it
        int exchangeValue = getMaxMoney(N / 2) + getMaxMoney(N / 3) + getMaxMoney(N / 4);
        int result = Math.max(N, exchangeValue);

        // Store the result in memoization map
        memo.put(N, result);

        return result;
    }

    public static void main(String[] args) {
        BankCoinExchange exchange = new BankCoinExchange();

        int N = 100; // Example coin value
        System.out.println("Maximum money for coin of value " + N + ": " + exchange.getMaxMoney(N));
    }
}