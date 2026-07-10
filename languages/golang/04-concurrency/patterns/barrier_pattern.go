package main

import (
    "fmt"
    "sync"
)

func main() {
    // Create a WaitGroup to track the completion of goroutines
    var wg sync.WaitGroup

    // Create a barrier channel with a buffer size of 4
    barrier := make(chan struct{}, 4)

    // Start 4 goroutines
    for i := 0; i < 4; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()

            // Simulate some work
            fmt.Printf("Goroutine %d started\n", id)
            // ... do work ...

            // Signal that the goroutine has reached the barrier
            barrier <- struct{}{}
            fmt.Printf("Goroutine %d passed the barrier\n", id)
        }(i)
    }

    // Wait for all 4 goroutines to reach the barrier
    for i := 0; i < 4; i++ {
        <-barrier
    }

    // Wait for all goroutines to finish
    wg.Wait()
    fmt.Println("All goroutines finished")
}
