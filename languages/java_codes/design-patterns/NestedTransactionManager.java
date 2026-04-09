import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NESTED TRANSACTION MANAGER (Parent-Child Hierarchy)
 * =====================================================
 *
 * CONCEPT: A more sophisticated transaction system where transactions form
 * a tree structure. Child transactions inherit parent data and can be
 * independently committed or rolled back.
 *
 * DESIGN DECISIONS:
 * - Each Transaction has its own data map, snapshot, state, and parent reference.
 * - commit() on a child MERGES its data into the parent (not the global DB).
 * - rollback() on a parent cascades to ALL children.
 * - Thread-safe: synchronized methods on Transaction for concurrent access.
 *
 * TRANSACTION LIFECYCLE:
 *   ACTIVE -> COMMITTED (via commit())
 *   ACTIVE -> ROLLEDBACK (via rollback())
 *   Once COMMITTED or ROLLEDBACK, no further modifications are allowed.
 *
 * REAL-WORLD PARALLEL:
 * - Similar to database savepoints or distributed transaction coordinators.
 * - In Spring: @Transactional(propagation = NESTED) creates savepoints.
 *
 * EXAMPLE:
 *   tx1 (parent) -> set("user", "John")
 *     tx2 (child) -> set("email", "john@example.com")
 *       tx3 (grandchild) -> set("age", 30)
 *
 *   If tx2 is rolled back, tx3 is also rolled back.
 *   tx1 retains its own data.
 */

enum TransactionState {
    INACTIVE,
    ACTIVE,
    COMMITTED,
    ROLLEDBACK
}

class Transaction {
    private final String id;
    private TransactionState state;
    private final Map<String, Object> data;
    private final Map<String, Object> snapshot;    // Stores original values for rollback
    private final Transaction parent;
    private final List<Transaction> children;

    public Transaction(String id, Transaction parent) {
        this.id = id;
        this.state = TransactionState.ACTIVE;
        this.data = new HashMap<>();
        this.snapshot = new HashMap<>();
        this.parent = parent;
        this.children = new ArrayList<>();

        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(Transaction child) {
        synchronized (children) {
            children.add(child);
        }
    }

    public synchronized void set(String key, Object value) throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot modify transaction " + id + ": state is " + state);
        }
        // Save original value for rollback (only first time per key)
        if (!snapshot.containsKey(key)) {
            snapshot.put(key, data.get(key));
        }
        data.put(key, value);
    }

    public synchronized Object get(String key) {
        return data.get(key);
    }

    /**
     * Commit: merge this transaction's data into the parent.
     * Commits children first (bottom-up).
     */
    public synchronized void commit() throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot commit transaction " + id + ": state is " + state);
        }

        // Commit children first
        for (Transaction child : children) {
            if (child.getState() == TransactionState.ACTIVE) {
                child.commit();
            }
        }

        // Merge data into parent
        if (parent != null) {
            synchronized (parent) {
                parent.data.putAll(this.data);
            }
        }

        state = TransactionState.COMMITTED;
        snapshot.clear();
    }

    /**
     * Rollback: restore original data from snapshot.
     * Cascades to all children.
     */
    public synchronized void rollback() throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot rollback transaction " + id + ": state is " + state);
        }

        // Rollback children first
        for (Transaction child : children) {
            if (child.getState() == TransactionState.ACTIVE) {
                child.rollback();
            }
        }

        // Restore from snapshot
        data.clear();
        data.putAll(snapshot);
        state = TransactionState.ROLLEDBACK;
    }

    public synchronized TransactionState getState() { return state; }
    public String getId() { return id; }
}

public class NestedTransactionManager {
    private final Map<String, Transaction> transactions = new HashMap<>();

    public synchronized Transaction begin(String id, Transaction parent) throws Exception {
        if (transactions.containsKey(id)) {
            throw new Exception("Transaction '" + id + "' already exists");
        }
        Transaction tx = new Transaction(id, parent);
        if (parent == null) {
            transactions.put(id, tx); // Only top-level transactions tracked by manager
        }
        return tx;
    }

    public synchronized Transaction getTransaction(String id) {
        return transactions.get(id);
    }

    // Demo
    public static void main(String[] args) {
        try {
            NestedTransactionManager tm = new NestedTransactionManager();

            // Parent transaction
            Transaction tx1 = tm.begin("tx1", null);
            tx1.set("user", "John");

            // Child transaction
            Transaction tx2 = tm.begin("tx2", tx1);
            tx2.set("email", "john@example.com");

            // Grandchild transaction
            Transaction tx3 = tm.begin("tx3", tx2);
            tx3.set("age", 30);
            tx3.commit(); // age=30 merged into tx2

            // Rollback tx2 -> also undoes tx3's merged data
            tx2.rollback();

            // Commit parent
            tx1.commit();

            System.out.println("User: " + tx1.get("user"));    // "John"
            System.out.println("Email: " + tx1.get("email"));  // null (rolled back)
            System.out.println("Age: " + tx1.get("age"));      // null (rolled back)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
