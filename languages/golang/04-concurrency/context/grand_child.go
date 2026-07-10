package main

import (
    "context"
    "fmt"
    "time"
)

func main() {
    // Create the parent context
    mainCtx, cancelMain := context.WithCancel(context.Background())
    defer cancelMain()

    // Launch two primary goroutines that will each launch a secondary goroutine
    go primaryTask("Task 1", mainCtx)
    go primaryTask("Task 2", mainCtx)

    // Allow the tasks to run for a while
    time.Sleep(3 * time.Second)

    // Cancel the main context
    fmt.Println("\nCancelling main context...\n")
    cancelMain()

    // Give time for goroutines to detect cancellation and exit
    time.Sleep(2 * time.Second)

    fmt.Println("Program finished.")
}

// primaryTask simulates a primary task that spawns another child goroutine
func primaryTask(taskName string, ctx context.Context) {
    // Create a child context for this task
    childCtx, cancelChild := context.WithCancel(ctx)
    defer cancelChild()

    // Start a secondary goroutine that works with the child context
    go secondaryTask(taskName+" - Child", childCtx)

    for {
        select {
        case <-ctx.Done():
            fmt.Printf("%s: Parent context cancelled. Exiting primary task.\n", taskName)
            retur n
        default:
            // Simulate work in the primary task
            fmt.Printf("%s is working...\n", taskName)
            time.Sleep(500 * time.Millisecond)
        }
    }
}

// secondaryTask simulates a secondary task that relies on its parent's context
func secondaryTask(taskName string, ctx context.Context) {
    for {
        select {
        case <-ctx.Done():
            fmt.Printf("%s: Child context cancelled. Exiting secondary task.\n", taskName)
            return
        default:
            // Simulate work in the secondary task
            fmt.Printf("%s is working...\n", taskName)
            time.Sleep(500 * time.Millisecond)
        }
    }
}
