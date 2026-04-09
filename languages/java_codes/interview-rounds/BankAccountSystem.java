import java.util.HashMap;
import java.util.Map;

/**
 * BANK ACCOUNT SYSTEM — Strategy Pattern (Interview Design Problem)
 * ===================================================================
 *
 * PROBLEM: Design a banking system with multiple account types
 * (Savings, Current) that have different interest rates and withdrawal limits.
 *
 * DESIGN APPROACH: Strategy Pattern
 * - AccountStrategy encapsulates the rules (interest rate, withdrawal limit).
 * - Account uses a strategy without knowing the specifics.
 * - New account types can be added by defining a new strategy — Open/Closed Principle.
 *
 * CLASS HIERARCHY:
 *   AccountStrategy (interface) -> SavingsStrategy, CurrentStrategy
 *   Account (uses strategy for behavior)
 *   AccountManager (factory for creating accounts)
 *
 * PRINCIPLES APPLIED:
 * - Strategy Pattern: Encapsulate varying behavior (interest, limits).
 * - Factory Pattern: AccountManager creates accounts.
 * - Open/Closed: Add new account types without modifying existing code.
 */

interface AccountStrategy {
    float getInterestRate();
    int getWithdrawLimit();
}

class SavingsStrategy implements AccountStrategy {
    public float getInterestRate() { return 4.0f; }
    public int getWithdrawLimit() { return 5; }   // max 5 withdrawals/month
}

class CurrentStrategy implements AccountStrategy {
    public float getInterestRate() { return 0.5f; }
    public int getWithdrawLimit() { return 100; }  // virtually unlimited
}

class Account {
    private final String accountNumber;
    private final String name;
    private final AccountStrategy strategy;
    private int balance;
    private int withdrawalCount;

    Account(String accountNumber, String name, AccountStrategy strategy) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.strategy = strategy;
        this.balance = 0;
        this.withdrawalCount = 0;
    }

    public boolean deposit(int amount) {
        if (amount <= 0) return false;
        balance += amount;
        System.out.println("Deposited " + amount + ". Balance: " + balance);
        return true;
    }

    public boolean withdraw(int amount) {
        if (amount <= 0 || amount > balance) return false;
        if (withdrawalCount >= strategy.getWithdrawLimit()) {
            System.out.println("Withdrawal limit reached!");
            return false;
        }
        balance -= amount;
        withdrawalCount++;
        System.out.println("Withdrew " + amount + ". Balance: " + balance);
        return true;
    }

    public float calculateInterest() {
        return balance * strategy.getInterestRate() / 100;
    }

    public int getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Balance: %d | Interest: %.2f",
            accountNumber, name, balance, calculateInterest());
    }
}

enum AccountType { SAVINGS, CURRENT }

class AccountManager {
    private final Map<AccountType, AccountStrategy> strategies = new HashMap<>();
    private int accountCounter = 1000;

    AccountManager() {
        strategies.put(AccountType.SAVINGS, new SavingsStrategy());
        strategies.put(AccountType.CURRENT, new CurrentStrategy());
    }

    public Account createAccount(String name, AccountType type) {
        AccountStrategy strategy = strategies.get(type);
        if (strategy == null) throw new IllegalArgumentException("Unknown account type: " + type);
        return new Account("ACC" + (++accountCounter), name, strategy);
    }
}

public class BankAccountSystem {
    public static void main(String[] args) {
        AccountManager manager = new AccountManager();

        Account savings = manager.createAccount("Alice", AccountType.SAVINGS);
        Account current = manager.createAccount("Bob", AccountType.CURRENT);

        savings.deposit(10000);
        savings.withdraw(500);
        System.out.println("Interest: " + savings.calculateInterest());
        System.out.println(savings);

        System.out.println();

        current.deposit(50000);
        current.withdraw(10000);
        System.out.println("Interest: " + current.calculateInterest());
        System.out.println(current);
    }
}
