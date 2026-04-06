package main

import (
    "fmt"
    "sync"
)
// Waiting for a collection of goroutines to finish before proceeding
func main() {
    var wg sync.WaitGroup

    for i := 0; i < 4; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            fmt.Printf("Goroutine %d started\n", id)
        }(i)
    }

    wg.Wait()
    fmt.Println("All goroutines finished")
}
