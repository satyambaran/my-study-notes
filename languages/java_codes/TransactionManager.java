import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TransactionManager {
    private final Map<String, String> database = new HashMap<>(); // Simulated database
    private final Stack<Map<String, String>> transactionStack = new Stack<>(); // Stack to manage transactions

    // Start a new transaction
    public void begin() {
        // Save the current state of the database
        transactionStack.push(new HashMap<>(database));
        System.out.println("Transaction started.");
    }

    // Commit the current transaction
    public void commit() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transaction to commit.");
            return;
        }
        transactionStack.pop(); // Remove the saved state from the stack
        System.out.println("Transaction committed.");
    }

    // Rollback the current transaction
    public void rollback() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transaction to rollback.");
            return;
        }
        Map<String, String> lastState = transactionStack.pop(); // Restore the last saved state
        database.clear();
        database.putAll(lastState);
        System.out.println("Transaction rolled back.");
    }

    // Set a key-value pair in the database
    public void set(String key, String value) {
        database.put(key, value);
        System.out.println("Set: " + key + " = " + value);
    }

    // Get a value by key
    public String get(String key) {
        return database.getOrDefault(key, null);
    }

    // Display the current state of the database
    public void displayDatabase() {
        System.out.println("Database state: " + database);
    }

    public static void main(String[] args) {
        TransactionManager tm = new TransactionManager();

        // Basic usage example
        tm.set("key1", "value1");
        tm.displayDatabase();

        tm.begin(); // Start transaction
        tm.set("key2", "value2");
        tm.displayDatabase();

        tm.rollback(); // Rollback transaction
        tm.displayDatabase();

        tm.begin(); // Start new transaction
        tm.set("key3", "value3");
        tm.displayDatabase();

        tm.commit(); // Commit transaction
        tm.displayDatabase();
    }
}