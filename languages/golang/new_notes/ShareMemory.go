package main

import (
    "fmt"
    "sync"
)

type Account struct {
    balance int
    ops     chan func()
    wg      sync.WaitGroup
}

// NewAccount initializes an Account with a goroutine handling all operations.
func NewAccount(initialBalance int) *Account {
    acc := &Account{
        balance: initialBalance,
        ops:     make(chan func()),
    }

    go func() {
        for op := range acc.ops {
            op() // Execute the operation
        }
    }()
    return acc
}

// Deposit adds money to the account.
func (acc *Account) Deposit(amount int) {
    acc.wg.Add(1)
    acc.ops <- func() {
        acc.balance += amount
        acc.wg.Done()
    }
}

// Withdraw deducts money from the account.
func (acc *Account) Withdraw(amount int) {
    acc.wg.Add(1)
    acc.ops <- func() {
        if acc.balance >= amount {
            acc.balance -= amount
        } else {
            fmt.Println("Insufficient funds")
        }
        acc.wg.Done()
    }
}

// GetBalance retrieves the current balance.
func (acc *Account) GetBalance() int {
    acc.wg.Add(1)
    balanceChan := make(chan int)
    acc.ops <- func() {
        balanceChan <- acc.balance
        acc.wg.Done()
    }
    return <-balanceChan
}

// CloseAccount stops all operations on the account.
func (acc *Account) CloseAccount() {
    acc.wg.Wait()
    close(acc.ops)
}

func main() {
    account := NewAccount(100)

    // Perform operations concurrently
    for i := 0; i < 100; i++ {
        go account.Deposit(50)
        go account.Withdraw(30)
        go account.Deposit(70)
    }

    // Wait for all operations to complete
    account.wg.Wait()

    // Get and print the final balance
    fmt.Println("Final Balance:", account.GetBalance())

    // Close the account
    account.CloseAccount()
}
