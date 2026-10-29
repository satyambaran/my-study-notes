import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * In-memory nested transactions with snapshot reads and optimistic,
 * serializable validation for point-key operations.
 *
 * <p>A root transaction snapshots the committed store when it begins. A child
 * snapshots its parent's visible state, including the parent's uncommitted
 * writes. Reads remain stable for the lifetime of that transaction.
 *
 * <p>Child commit merges only into the parent. Root commit validates that keys
 * read or written by a read-write transaction have not changed since its
 * snapshot, then atomically publishes its writes to the committed store.
 *
 * <p>All state transitions are serialized by this manager's monitor. Using one
 * synchronization boundary avoids parent-child lock-order deadlocks.
 *
 * <p>Snapshots copy key-to-value bindings, not the internal state of mutable
 * value objects. Values should therefore be immutable or independently copied.
 */
public final class NestedTransactionManager {
    public enum TransactionState {
        ACTIVE,
        COMMITTED,
        ROLLED_BACK
    }

    /**
     * Raised when optimistic validation detects that a transaction's snapshot
     * can no longer be committed serializably.
     */
    public static final class TransactionConflictException
            extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public TransactionConflictException(String message) {
            super(message);
        }
    }

    /**
     * A transaction handle. Construction and all state changes are controlled
     * by the owning manager, so callers cannot bypass manager invariants.
     */
    public static final class Transaction {
        private final NestedTransactionManager owner;
        private final String id;
        private final Transaction parent;

        // Root transactions use this version for committed-store validation.
        private final long startVersion;

        // Stable visible state captured when this transaction begins.
        private final Map<String, Object> baseSnapshot;

        // An overlay avoids mutating the snapshot before commit.
        private final Map<String, Object> writes = new LinkedHashMap<>();
        private final Set<String> deletions = new HashSet<>();
        private final Set<String> readKeys = new HashSet<>();

        // Only active children are retained, preventing completed-child leaks.
        private final Set<Transaction> activeChildren = new LinkedHashSet<>();

        private TransactionState state = TransactionState.ACTIVE;

        private Transaction(
                NestedTransactionManager owner,
                String id,
                Transaction parent,
                long startVersion,
                Map<String, Object> baseSnapshot) {
            this.owner = owner;
            this.id = id;
            this.parent = parent;
            this.startVersion = startVersion;
            this.baseSnapshot = baseSnapshot;
        }

        public String getId() {
            return id;
        }

        public Transaction getParent() {
            return parent;
        }

        public TransactionState getState() {
            return owner.stateOf(this);
        }

        /**
         * Returns the value visible in this transaction's stable snapshot plus
         * its own writes. Null means absent because null values are rejected.
         */
        public Object get(String key) {
            return owner.read(this, key);
        }

        /**
         * Reports whether a key exists in this transaction's visible state.
         */
        public boolean containsKey(String key) {
            return owner.containsKey(this, key);
        }

        /**
         * Adds or replaces a value in this transaction's private write set.
         */
        public void set(String key, Object value) {
            owner.write(this, key, value);
        }

        /**
         * Records a deletion without using null as a sentinel value.
         */
        public void delete(String key) {
            owner.delete(this, key);
        }

        /**
         * Commits into the parent, or into the global store for a root.
         */
        public void commit() {
            owner.commit(this);
        }

        /**
         * Discards this transaction. Active descendants are rolled back first.
         */
        public void rollback() {
            owner.rollback(this);
        }
    }

    private final Map<String, Object> committedData = new HashMap<>();

    // Each key records the version of its most recent committed change,
    // including deletion. This enables optimistic conflict validation.
    private final Map<String, Long> keyVersions = new HashMap<>();

    // The manager owns every active transaction ID, including child IDs.
    private final Map<String, Transaction> activeTransactions = new HashMap<>();

    private long committedVersion;

    /**
     * Begins a root transaction from the current committed snapshot.
     */
    public synchronized Transaction begin(String id) {
        return begin(id, null);
    }

    /**
     * Begins a root transaction when parent is null, otherwise a child
     * transaction from the parent's current visible state.
     */
    public synchronized Transaction begin(String id, Transaction parent) {
        requireTransactionId(id);
        if (activeTransactions.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Active transaction '" + id + "' already exists");
        }

        Map<String, Object> snapshot;
        long startVersion;

        if (parent == null) {
            // A root sees a stable copy of committed key bindings.
            snapshot = new HashMap<>(committedData);
            startVersion = committedVersion;
        } else {
            requireOwnedActive(parent);

            // A child inherits exactly what its parent can see at this moment.
            // Later parent changes do not alter the child's snapshot.
            snapshot = materializeVisibleState(parent);
            startVersion = parent.startVersion;
        }

        Transaction transaction =
                new Transaction(this, id, parent, startVersion, snapshot);
        activeTransactions.put(id, transaction);

        if (parent != null) {
            parent.activeChildren.add(transaction);
        }

        return transaction;
    }

    /**
     * Returns an active transaction by ID, or null after it completes.
     */
    public synchronized Transaction getTransaction(String id) {
        return activeTransactions.get(id);
    }

    /**
     * Reads committed data outside a transaction.
     */
    public synchronized Object getCommitted(String key) {
        requireKey(key);
        return committedData.get(key);
    }

    /**
     * Returns an immutable snapshot of all currently committed bindings.
     */
    public synchronized Map<String, Object> committedSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(committedData));
    }

    public synchronized long getCommittedVersion() {
        return committedVersion;
    }

    private synchronized TransactionState stateOf(Transaction transaction) {
        requireOwned(transaction);
        return transaction.state;
    }

    private synchronized Object read(Transaction transaction, String key) {
        requireKey(key);
        requireOwnedActive(transaction);

        // Read-set tracking is used to reject write-skew schedules when this
        // transaction later commits writes.
        transaction.readKeys.add(key);
        return visibleValue(transaction, key);
    }

    private synchronized boolean containsKey(
            Transaction transaction,
            String key) {
        requireKey(key);
        requireOwnedActive(transaction);

        transaction.readKeys.add(key);
        return visibleContainsKey(transaction, key);
    }

    private synchronized void write(
            Transaction transaction,
            String key,
            Object value) {
        requireKey(key);
        Objects.requireNonNull(value, "value");
        requireOwnedActive(transaction);

        // A write overrides a previous delete in the same transaction.
        transaction.deletions.remove(key);
        transaction.writes.put(key, value);
    }

    private synchronized void delete(Transaction transaction, String key) {
        requireKey(key);
        requireOwnedActive(transaction);

        // Keep deletion separate from null so absent and null never collide.
        transaction.writes.remove(key);
        transaction.deletions.add(key);
    }

    private synchronized void commit(Transaction transaction) {
        requireOwnedActive(transaction);

        // Silently committing children can publish work the caller intended to
        // roll back. Require an explicit decision for every active child.
        if (!transaction.activeChildren.isEmpty()) {
            throw new IllegalStateException(
                    "Transaction '" + transaction.id
                            + "' has active child transactions");
        }

        if (transaction.parent == null) {
            commitRoot(transaction);
        } else {
            commitChild(transaction);
        }
    }

    private void commitRoot(Transaction transaction) {
        boolean hasWrites =
                !transaction.writes.isEmpty()
                        || !transaction.deletions.isEmpty();

        if (hasWrites) {
            // Validate both reads and writes. This prevents two transactions
            // from committing a write-skew cycle over point-key operations.
            Set<String> conflictKeys = new HashSet<>(transaction.readKeys);
            conflictKeys.addAll(transaction.writes.keySet());
            conflictKeys.addAll(transaction.deletions);

            for (String key : conflictKeys) {
                long latestVersion = keyVersions.getOrDefault(key, 0L);
                if (latestVersion > transaction.startVersion) {
                    throw new TransactionConflictException(
                            "Transaction '" + transaction.id
                                    + "' conflicts on key '" + key + "'");
                }
            }

            long commitVersion = ++committedVersion;

            for (String key : transaction.deletions) {
                committedData.remove(key);
                keyVersions.put(key, commitVersion);
            }

            for (Map.Entry<String, Object> entry
                    : transaction.writes.entrySet()) {
                committedData.put(entry.getKey(), entry.getValue());
                keyVersions.put(entry.getKey(), commitVersion);
            }
        }

        complete(transaction, TransactionState.COMMITTED);
    }

    private void commitChild(Transaction transaction) {
        Transaction parent = transaction.parent;
        requireOwnedActive(parent);

        // A parent may continue doing unrelated work while a child is active.
        // Reject the child only when a key it depended on changed in the parent
        // after the child's snapshot was created.
        Set<String> conflictKeys = new HashSet<>(transaction.readKeys);
        conflictKeys.addAll(transaction.writes.keySet());
        conflictKeys.addAll(transaction.deletions);

        for (String key : conflictKeys) {
            boolean snapshotContains = transaction.baseSnapshot.containsKey(key);
            boolean parentContains = visibleContainsKey(parent, key);

            if (snapshotContains != parentContains
                    || (snapshotContains
                            && !Objects.equals(
                                    transaction.baseSnapshot.get(key),
                                    visibleValue(parent, key)))) {
                throw new TransactionConflictException(
                        "Child transaction '" + transaction.id
                                + "' conflicts with parent on key '" + key + "'");
            }
        }

        // Propagate reads so a later root commit validates data observed by
        // successfully committed descendants.
        parent.readKeys.addAll(transaction.readKeys);

        for (String key : transaction.deletions) {
            parent.writes.remove(key);
            parent.deletions.add(key);
        }

        for (Map.Entry<String, Object> entry
                : transaction.writes.entrySet()) {
            parent.deletions.remove(entry.getKey());
            parent.writes.put(entry.getKey(), entry.getValue());
        }

        complete(transaction, TransactionState.COMMITTED);
    }

    private synchronized void rollback(Transaction transaction) {
        requireOwnedActive(transaction);
        rollbackInternal(transaction);
    }

    private void rollbackInternal(Transaction transaction) {
        // Work on a copy because completing a child removes it from this set.
        Set<Transaction> children =
                new LinkedHashSet<>(transaction.activeChildren);
        for (Transaction child : children) {
            rollbackInternal(child);
        }

        complete(transaction, TransactionState.ROLLED_BACK);
    }

    private void complete(
            Transaction transaction,
            TransactionState finalState) {
        transaction.state = finalState;
        activeTransactions.remove(transaction.id);

        if (transaction.parent != null) {
            transaction.parent.activeChildren.remove(transaction);
        }

        // Completed handles retain identity and state, but no longer retain
        // snapshots or write sets that could grow manager memory indefinitely.
        transaction.activeChildren.clear();
        transaction.baseSnapshot.clear();
        transaction.writes.clear();
        transaction.deletions.clear();
        transaction.readKeys.clear();
    }

    private Map<String, Object> materializeVisibleState(
            Transaction transaction) {
        Map<String, Object> result =
                new HashMap<>(transaction.baseSnapshot);

        for (String key : transaction.deletions) {
            result.remove(key);
        }
        result.putAll(transaction.writes);
        return result;
    }

    private Object visibleValue(Transaction transaction, String key) {
        if (transaction.writes.containsKey(key)) {
            return transaction.writes.get(key);
        }
        if (transaction.deletions.contains(key)) {
            return null;
        }
        return transaction.baseSnapshot.get(key);
    }

    private boolean visibleContainsKey(
            Transaction transaction,
            String key) {
        if (transaction.writes.containsKey(key)) {
            return true;
        }
        if (transaction.deletions.contains(key)) {
            return false;
        }
        return transaction.baseSnapshot.containsKey(key);
    }

    private void requireOwned(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        if (transaction.owner != this) {
            throw new IllegalArgumentException(
                    "Transaction belongs to another manager");
        }
    }

    private void requireOwnedActive(Transaction transaction) {
        requireOwned(transaction);
        if (transaction.state != TransactionState.ACTIVE) {
            throw new IllegalStateException(
                    "Transaction '" + transaction.id
                            + "' is " + transaction.state);
        }
    }

    private static void requireTransactionId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "transaction id must not be blank");
        }
    }

    private static void requireKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(
                    "key must not be empty");
        }
    }

    public static void main(String[] args) {
        NestedTransactionManager manager =
                new NestedTransactionManager();

        Transaction parent = manager.begin("tx1");
        parent.set("user", "John");

        Transaction child = manager.begin("tx2", parent);
        System.out.println(child.get("user")); // John: inherited snapshot.
        child.set("email", "john@example.com");

        Transaction grandchild = manager.begin("tx3", child);
        grandchild.set("age", 30);
        grandchild.commit(); // Merges age into child only.

        child.rollback(); // Discards both email and merged age.
        parent.commit();  // Publishes user to the committed store.

        System.out.println(manager.getCommitted("user"));  // John
        System.out.println(manager.getCommitted("email")); // null
        System.out.println(manager.getCommitted("age"));   // null
    }
}
