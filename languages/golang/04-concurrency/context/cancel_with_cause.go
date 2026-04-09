package main

import (
    "context"
    "errors"
    "fmt"
    "time"
)

func main() {
    // Create a context with a cancel function that can provide a cancellation cause
    ctx, cancel := context.WithCancelCause(context.Background())

    // Start a goroutine to simulate some work
    go func(ctx context.Context) {
        for {
            select {
            case <-ctx.Done():
                // Once the context is canceled, we can access the cause
                fmt.Printf("Context canceled with cause: %v\n", context.Cause(ctx))
                return
            default:
                // Simulate some ongoing work
                fmt.Println("Goroutine: Doing work...")
                time.Sleep(500 * time.Millisecond)
            }
        }
    }(ctx)

    // Simulate some other operation, and then cancel the context after a delay
    time.Sleep(2 * time.Second)

    // Cancel the context with a specific error as the cause
    cancel(errors.New("network failure"))

    // Give some time for the goroutine to finish and print the cause
    time.Sleep(1 * time.Second)

    // Accessing the cause later from the main function
    fmt.Printf("Main: Context cause after cancellation: %v\n", context.Cause(ctx))
}
