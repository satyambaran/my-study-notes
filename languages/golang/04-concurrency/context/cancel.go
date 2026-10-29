package main

import (
    "context"
    "fmt"
    "time"
)

func main() {
    // Create a new context with cancel function
    ctx, cancel := context.WithCancel(context.Background())

    // Start a goroutine that listens to the context
    go func(ctx context.Context) {
        for {
            select {
            case <-ctx.Done(): // Context is cancelled
                fmt.Println("Goroutine: Context cancelled, exiting...")
                return
            default:
                fmt.Println("Goroutine: Doing work...")
                time.Sleep(500 * time.Millisecond) // Simulating work
            }
        }
    }(ctx)

    // Simulate doing work in the main routine
    time.Sleep(2 * time.Second)

    // Cancel the context
    fmt.Println("Main: Cancelling context")
    cancel()

    // Give time for the goroutine to exit
    time.Sleep(1 * time.Second)
}
