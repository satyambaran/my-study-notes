package main

import (
    "context"
    "fmt"
    "sync"
    "time"
)

func worker(ctx context.Context) {
    for {
        select {
        case <-ctx.Done():
            fmt.Println("Stopping worker...")
            return
        default:
            fmt.Println("Working...")
            time.Sleep(500 * time.Millisecond) // Simulate work
        }
    }
}

func worker2(done chan bool) {
    for {
        select {
        case <-done:
            fmt.Println("Stopping worker 2...")
            return
        default:
            fmt.Println("2 is Working...")
            time.Sleep(500 * time.Millisecond) // Simulate work
        }
    }
}

func worker3(wg *sync.WaitGroup, done chan bool) {
    defer wg.Done()
    for {
        select {
        case <-done:
            fmt.Println("Stopping worker...")
            return
        default:
            fmt.Println("Working...")
            time.Sleep(500 * time.Millisecond)
        }
    }
}

func main() {
    //? context
    ctx, cancel := context.WithCancel(context.Background())
    go worker(ctx)

    time.Sleep(2 * time.Second) // Let the worker run for a while
    cancel()                    // Signal the worker to stop
    time.Sleep(1 * time.Second) // Wait for the worker to finish

    //? done channel
    done := make(chan bool)
    go worker2(done)

    time.Sleep(2 * time.Second) // Let the worker run for a while
    done <- true                // Send stop signal
    time.Sleep(1 * time.Second) // Wait for the worker to finish

    //? wait WaitGroup
    var wg sync.WaitGroup
    done2 := make(chan bool)

    wg.Add(1)
    go worker3(&wg, done2)

    time.Sleep(2 * time.Second)
    close(done2) // Close the channel to signal the worker to stop
    wg.Wait()   // Wait for all goroutines to complete
}
