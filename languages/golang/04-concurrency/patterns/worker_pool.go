package main

import (
    "fmt"
    "runtime"
    "sync"
)

func worker(id int, jobs <-chan int, results chan<- int) {
    for job := range jobs {
        fmt.Printf("Worker %d processing job %d\n", id, job)
        results <- job * 2
    }
}

// creating a group of worker goroutines to process tasks concurrently, limiting the number of simultaneous operations
func main() {
    runtime.GOMAXPROCS(3)
    numJobs := 30
    numWorkers := 3

    jobs := make(chan int, numJobs)
    results := make(chan int, numJobs)

    var wg sync.WaitGroup

    // Start worker goroutines
    for i := 1; i <= numWorkers; i++ {
        wg.Add(1)
        go func(workerID int) {
            defer wg.Done()
            worker(workerID, jobs, results)
        }(i)
    }

    // Enqueue jobs
    for i := 1; i <= numJobs; i++ {
        jobs <- i
    }
    close(jobs)

    // Wait for all workers to finish
    go func() {
        wg.Wait()
        close(results)
    }()

    // Collect results
    for result := range results {
        fmt.Printf("Result: %d\n", result)
    }
}
