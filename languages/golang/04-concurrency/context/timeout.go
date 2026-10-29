package main

import (
    "context"
    "fmt"
    "time"
)

func main() {
    // Create a context that will cancel after 2 seconds
    ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
    defer cancel() // Ensure resources are cleaned up

    // Start a goroutine that listens to the context
    go func(ctx context.Context) {
        for {
            select {
            case <-ctx.Done(): // Context is cancelled after timeout
                fmt.Println("Goroutine: Timeout, exiting...")
                return
            default:
                fmt.Println("Goroutine: Doing work...")
                time.Sleep(500 * time.Millisecond)
            }
        }
    }(ctx)

    // Give the goroutine enough time to work before the timeout
    time.Sleep(3 * time.Second)
}
