package main

import (
	"fmt"
	"sync"
)

// TransactionState represents the current state of a transaction
type TransactionState int

const (
	Inactive TransactionState = iota
	Active
	Committed
	Rolledback
)

// Transaction represents a transaction with nested transaction support
type Transaction struct {
	id       string
	state    TransactionState
	data     map[string]interface{}
	parent   *Transaction
	children []*Transaction
	snapshot map[string]interface{}
	mu       sync.RWMutex
	depth    int
}

// TransactionManager manages multiple top-level transactions
type TransactionManager struct {
	transactions map[string]*Transaction
	mu           sync.RWMutex
}

// NewTransactionManager creates a new TransactionManager
func NewTransactionManager() *TransactionManager {
	return &TransactionManager{
		transactions: make(map[string]*Transaction),
	}
}

// Begin starts a new transaction
func (tm *TransactionManager) Begin(id string, parentTx ...*Transaction) (*Transaction, error) {
	tm.mu.Lock()
	defer tm.mu.Unlock()

	// Check if transaction already exists
	if _, exists := tm.transactions[id]; exists {
		return nil, fmt.Errorf("transaction with ID %s already exists", id)
	}

	// Determine parent and depth
	var parent *Transaction
	depth := 0
	if len(parentTx) > 0 {
		parent = parentTx[0]
		depth = parent.depth + 1
	}

	// Create new transaction
	transaction := &Transaction{
		id:       id,
		state:    Active,
		data:     make(map[string]interface{}),
		parent:   parent,
		snapshot: make(map[string]interface{}),
		depth:    depth,
	}

	// If there's a parent, add this as a child
	if parent != nil {
		parent.children = append(parent.children, transaction)
	} else {
		// Only add top-level transactions to the manager
		tm.transactions[id] = transaction
	}

	return transaction, nil
}

// Set adds or updates a key-value pair in the transaction
func (t *Transaction) Set(key string, value interface{}) error {
	t.mu.Lock()
	defer t.mu.Unlock()

	if t.state != Active {
		return fmt.Errorf("cannot modify transaction %s: not in active state", t.id)
	}

	// Store original value in snapshot if not already stored
	if _, exists := t.snapshot[key]; !exists {
		t.snapshot[key] = t.data[key]
	}

	t.data[key] = value
	return nil
}

// Get retrieves a value from the transaction
func (t *Transaction) Get(key string) (interface{}, bool) {
	t.mu.RLock()
	defer t.mu.RUnlock()

	value, exists := t.data[key]
	return value, exists
}

// Commit finalizes the transaction
func (t *Transaction) Commit() error {
	t.mu.Lock()
	defer t.mu.Unlock()

	if t.state != Active {
		return fmt.Errorf("cannot commit transaction %s: not in active state", t.id)
	}

	// Commit all child transactions first
	for _, child := range t.children {
		if err := child.Commit(); err != nil {
			return fmt.Errorf("error committing child transaction: %v", err)
		}
	}

	// If this is a nested transaction, merge data with parent
	if t.parent != nil {
		t.parent.mu.Lock()
		for key, value := range t.data {
			t.parent.data[key] = value
		}
		t.parent.mu.Unlock()
	}

	t.state = Committed
	t.snapshot = nil // Clear snapshot on commit
	return nil
}

// Rollback reverts changes made in the transaction
func (t *Transaction) Rollback() error {
	t.mu.Lock()
	defer t.mu.Unlock()

	if t.state != Active {
		return fmt.Errorf("cannot rollback transaction %s: not in active state", t.id)
	}

	// Rollback all child transactions first
	for _, child := range t.children {
		if err := child.Rollback(); err != nil {
			return fmt.Errorf("error rolling back child transaction: %v", err)
		}
	}

	// Restore original data from snapshot
	t.data = make(map[string]interface{})
	for key, value := range t.snapshot {
		if value != nil {
			t.data[key] = value
		}
	}

	t.state = Rolledback
	return nil
}

// State returns the current state of the transaction
func (t *Transaction) State() TransactionState {
	t.mu.RLock()
	defer t.mu.RUnlock()

	return t.state
}
func main() {
	// Create a new transaction manager
	tm := NewTransactionManager()

	// Begin a top-level transaction
	tx1, _ := tm.Begin("transaction1")

	// Set some values in the top-level transaction
	tx1.Set("user", "John")

	// Begin a nested transaction
	tx2, _ := tm.Begin("transaction2", tx1)
	tx2.Set("email", "john@example.com")

	// Begin another nested transaction
	tx3, _ := tm.Begin("transaction3", tx2)
	tx3.Set("age", 30)

	// Commit the innermost transaction
	tx3.Commit()

	// Rollback the middle transaction
	tx2.Rollback()

	// At this point:
	// - tx1 still has "user" set to "John"
	// - "email" and "age" will be rolled back
	// - Only the committed values from tx3 would have persisted if tx2 wasn't rolled back

	// Commit the top-level transaction
	tx1.Commit()
}
