package main

import (
    "fmt"
    "sync"
)
// Controlling access to resources by limiting the number of goroutines allowed at a time.
func main() {
    var count int
    var mutex sync.Mutex
    var wg sync.WaitGroup
    sem := make(chan struct{}, 10) // semaphore, only allowing 10 threads max to access the variable at a time

    for i := 0; i < 100; i++ {
        wg.Add(1)
        go func() {
            defer wg.Done()
            sem <- struct{}{}
            mutex.Lock()
            defer mutex.Unlock()
            count++
            <-sem
        }()
    }

    wg.Wait()
    fmt.Println("Final count:", count)
}
