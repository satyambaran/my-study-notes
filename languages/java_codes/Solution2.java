
// you can also use imports, for example:
// import java.util.*;

public class Solution2 {

    public static void main(String[] args) {
        // you can write to stdout for debugging purposes, e.g.
        System.out.println("This is a debug message");
    }
}

/*

*/
abstract class Account {
    private String currency;

    private String accountNumber;
    private String name;
    private int balance;
    private int withDrawalCounter; // number of txn happened
    private Strategy accountStrategy;

    public boolean deposit(int amount) {
        balance += amount;
    }

    public boolean withdraw(int amount) {
        if (accountStrategy.withdrawLimit <= withDrawalCounter) {
            return false;
        } else {
            withDrawalCounter++;
            balance -= amount;
        }
    }

    public int calcInterest() {
        float interest = (float) (balance * accountStrategy.interestRate) / 100;
        return interest;
    }

    public int checkBalance() {
        return balance;
    }
}

class SavingsAccount implements Account {
    private float interestRate;
    private int withdrawLimit;

    SavingsAccount(float _interestRate, int _withdrawLimit) {
        interestRate = _interestRate;
        withdrawLimit = _withdrawLimit;
    }

}

class CurrentAccount implements Account {
    private float interestRate;
    private int withdrawLimit;

    CurrentAccount(float _interestRate, int _withdrawLimit) {
        interestRate = _interestRate;
        withdrawLimit = _withdrawLimit;
    }
}

class AccountManager {
    HashMap<AccountType,Strategy>;
    createAccount(AccountType accountType){
        String accountNumber = genAccountNumber()
        switch(accountType){
            case SAVINGS:
                SavingsAccount(accountNumber, );
            case CURRENT:
                CurrentAccount()
            default:
                
        }
    }
}

enum AccountType {
    SAVINGS,
    CURRENT
}
/*
 * Bank, User
 * 
 * Account:
 * deposit, withdraw, checkBalance, calcInterest, accountStrategy, balance
 * 
 * Savings Account
 * Current Account
 * 
 * accountStrategy
 */
