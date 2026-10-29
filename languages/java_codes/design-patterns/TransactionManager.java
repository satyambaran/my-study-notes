import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * SIMPLE TRANSACTION MANAGER (begin/commit/rollback)
 * ===================================================
 *
 * CONCEPT: Simulates database transaction semantics using an in-memory HashMap.
 *
 * HOW IT WORKS:
 * - begin(): Snapshots the current database state and pushes it onto a stack.
 * - commit(): Discards the snapshot (changes are now permanent).
 * - rollback(): Restores the database to the last snapshot.
 *
 * NESTED TRANSACTIONS:
 * - Multiple begin() calls create nested transactions (savepoints).
 * - Each begin() pushes a new snapshot.
 * - rollback() only undoes changes to the last begin() point.
 *
 * REAL-WORLD CONNECTION:
 * - This mirrors SQL savepoints: BEGIN -> SAVEPOINT sp1 -> ROLLBACK TO sp1.
 * - Databases use write-ahead logs (WAL) instead of full copies for efficiency.
 *
 * TIME COMPLEXITY: begin/rollback are O(n) where n = database size (due to copy).
 * SPACE COMPLEXITY: O(n * d) where d = nesting depth.
 *
 * SEE ALSO: NestedTransactionManager.java for a more sophisticated version
 * with parent-child relationships and thread safety.
 */
public class TransactionManager {
    private final Map<String, String> database = new HashMap<>();
    private final Stack<Map<String, String>> transactionStack = new Stack<>();

    public void begin() {
        // Snapshot current state — changes after this point can be rolled back
        transactionStack.push(new HashMap<>(database));
        System.out.println("Transaction started.");
    }

    public void commit() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transaction to commit.");
            return;
        }
        transactionStack.pop(); // Discard snapshot; current state becomes permanent
        System.out.println("Transaction committed.");
    }

    public void rollback() {
        if (transactionStack.isEmpty()) {
            System.out.println("No transaction to rollback.");
            return;
        }
        Map<String, String> snapshot = transactionStack.pop();
        database.clear();
        database.putAll(snapshot); // Restore to snapshot
        System.out.println("Transaction rolled back.");
    }

    public void set(String key, String value) {
        database.put(key, value);
        System.out.println("Set: " + key + " = " + value);
    }

    public String get(String key) {
        return database.getOrDefault(key, null);
    }

    public void displayDatabase() {
        System.out.println("Database state: " + database);
    }

    public static void main(String[] args) {
        TransactionManager tm = new TransactionManager();

        tm.set("key1", "value1");
        tm.displayDatabase();           // {key1=value1}

        tm.begin();
        tm.set("key2", "value2");
        tm.displayDatabase();           // {key1=value1, key2=value2}

        tm.rollback();                  // Undo key2
        tm.displayDatabase();           // {key1=value1}

        tm.begin();
        tm.set("key3", "value3");
        tm.commit();                    // key3 is now permanent
        tm.displayDatabase();           // {key1=value1, key3=value3}

        // Nested transactions demo
        tm.begin();                     // Outer transaction
        tm.set("key4", "value4");
        tm.begin();                     // Inner transaction (savepoint)
        tm.set("key5", "value5");
        tm.rollback();                  // Undo key5 only
        tm.displayDatabase();           // {key1=value1, key3=value3, key4=value4}
        tm.commit();                    // Commit outer (key4 permanent)
        tm.displayDatabase();
    }
}
