import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final Map<String, Object> snapshot;
    private final Transaction parent;
    private final List<Transaction> children;
    private final int depth;

    public Transaction(String id, Transaction parent) {
        this.id = id;
        this.state = TransactionState.ACTIVE;
        this.data = new HashMap<>();
        this.snapshot = new HashMap<>();
        this.parent = parent;
        this.children = new ArrayList<>();
        this.depth = (parent != null) ? parent.depth + 1 : 0;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(Transaction child) {
        synchronized (this.children) {
            this.children.add(child);
        }
    }

    public synchronized void set(String key, Object value) throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot modify transaction " + id + ": not in active state");
        }

        // Store original value in snapshot if not already stored
        if (!snapshot.containsKey(key)) {
            snapshot.put(key, data.get(key));
        }

        data.put(key, value);
    }

    public synchronized Object get(String key) {
        return data.get(key);
    }

    public synchronized void commit() throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot commit transaction " + id + ": not in active state");
        }

        // Commit all child transactions first
        for (Transaction child : children) {
            child.commit();
        }

        // Merge data with parent if nested
        if (parent != null) {
            synchronized (parent) {
                parent.data.putAll(this.data);
            }
        }

        state = TransactionState.COMMITTED;
        snapshot.clear(); // Clear snapshot on commit
    }

    public synchronized void rollback() throws Exception {
        if (state != TransactionState.ACTIVE) {
            throw new Exception("Cannot rollback transaction " + id + ": not in active state");
        }

        // Rollback all child transactions first
        for (Transaction child : children) {
            child.rollback();
        }

        // Restore original data from snapshot
        data.clear();
        data.putAll(snapshot);

        state = TransactionState.ROLLEDBACK;
    }

    public synchronized TransactionState getState() {
        return state;
    }

    public String getId() {
        return id;
    }
}

class TransactionManager {
    private final Map<String, Transaction> transactions;

    public TransactionManager() {
        this.transactions = new HashMap<>();
    }

    public synchronized Transaction begin(String id, Transaction... parentTx) throws Exception {
        if (transactions.containsKey(id)) {
            throw new Exception("Transaction with ID " + id + " already exists");
        }

        Transaction parent = (parentTx.length > 0) ? parentTx[0] : null;
        Transaction transaction = new Transaction(id, parent);

        // Only add top-level transactions to the manager
        if (parent == null) {
            transactions.put(id, transaction);
        }

        return transaction;
    }

    public synchronized Transaction getTransaction(String id) {
        return transactions.get(id);
    }
}

public class TransactionManagerImpl {
    public static void main(String[] args) {
        try {
            // Create a new transaction manager
            TransactionManager tm = new TransactionManager();

            // Begin a top-level transaction
            Transaction tx1 = tm.begin("transaction1");

            // Set some values in the top-level transaction
            tx1.set("user", "John");

            // Begin a nested transaction
            Transaction tx2 = tm.begin("transaction2", tx1);
            tx2.set("email", "john@example.com");

            // Begin another nested transaction
            Transaction tx3 = tm.begin("transaction3", tx2);
            tx3.set("age", 30);

            // Commit the innermost transaction
            tx3.commit();

            // Rollback the middle transaction
            tx2.rollback();

            // At this point:
            // - tx1 still has "user" set to "John"
            // - "email" and "age" will be rolled back
            // - Only the committed values from tx3 would have persisted if tx2 wasn't
            // rolled back

            // Commit the top-level transaction
            tx1.commit();

            // Access committed data
            System.out.println("Transaction1 User: " + tx1.get("user")); // "John"
            System.out.println("Transaction1 Email: " + tx1.get("email")); // null (rolled back)
            System.out.println("Transaction1 Age: " + tx1.get("age")); // null (rolled back)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}