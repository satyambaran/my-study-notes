package main

import (
    "fmt"
    "sync"
)
// Protecting shared resources using mutexes (sync.Mutex) to ensure exclusive access.
// Only one can access it a time
func main() {
    var count int
    var mutex sync.Mutex

    wg := sync.WaitGroup{}
    wg.Add(100)

    for i := 0; i < 100; i++ {
        go func() {
            defer wg.Done()
            mutex.Lock()
            defer mutex.Unlock()
            count++
        }()
    }

    wg.Wait()
    fmt.Println("Final count:", count)
}
