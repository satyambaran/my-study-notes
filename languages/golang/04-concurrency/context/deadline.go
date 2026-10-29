package main

import (
    "context"
    "fmt"
    "time"
)

func main() {
    // Create a deadline 2 seconds from now
    deadline := time.Now().Add(2 * time.Second)
    ctx, cancel := context.WithDeadline(context.Background(), deadline)
    defer cancel() // Ensure resources are cleaned up

    go func(ctx context.Context) {
        for {
            select {
            case <-ctx.Done():
                fmt.Println("Goroutine: Deadline reached, exiting...")
                return
            default:
                fmt.Println("Goroutine: Doing work...")
                time.Sleep(500 * time.Millisecond)
            }
        }
    }(ctx)

    // Give time for the goroutine to exit after deadline
    time.Sleep(3 * time.Second)
}
