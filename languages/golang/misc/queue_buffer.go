package main

import (
    "fmt"
    "sync"
    "time"
)

type QueueBuffer struct {
    buffer []int      // Queue to hold the data
    size   int        // Maximum size of the queue
    mu     sync.Mutex // Mutex for thread-safety
    cond   *sync.Cond // Condition variable for coordination
}

// NewQueueBuffer initializes a new QueueBuffer
func NewQueueBuffer(size int) *QueueBuffer {
    q := &QueueBuffer{
        buffer: make([]int, 0, size),
        size:   size,
    }
    q.cond = sync.NewCond(&q.mu) // Create a condition variable with the mutex
    return q
}

// Enqueue adds an item to the buffer. Blocks if the buffer is full.
func (q *QueueBuffer) Enqueue(item int) {
    q.mu.Lock()
    defer q.mu.Unlock()

    // Wait if the buffer is full
    for len(q.buffer) == q.size {
        q.cond.Wait()
    }

    // Add the item to the queue
    q.buffer = append(q.buffer, item)
    fmt.Printf("Produced: %d | Buffer: %v\n", item, q.buffer)

    // Notify a waiting consumer
    q.cond.Signal()
}

// Dequeue removes an item from the buffer. Blocks if the buffer is empty.
func (q *QueueBuffer) Dequeue() int {
    q.mu.Lock()
    defer q.mu.Unlock()

    // Wait if the buffer is empty
    for len(q.buffer) == 0 {
        fmt.Println("before") //? before is printed before waiting
        q.cond.Wait()         //~ This puts the current goroutine to sleep (waits) until another goroutine signals that there is new data available in the buffer
    //     q.mu and q.cond Usage
	// •	q.mu.Lock() ensures that only one thread can execute the critical section of code at any given time.
	// •	q.cond.Wait() temporarily releases the lock held by the current thread (via q.mu.Unlock() internally) and puts the thread to sleep until another thread signals the condition variable using q.cond.Signal() or q.cond.Broadcast().
	// •	When the waiting thread is awakened, it re-acquires the lock (q.mu.Lock()) before continuing.
        fmt.Println("after")  //? after is printed after being woken up
    }

    // Remove the item from the front of the queue
    item := q.buffer[0]
    q.buffer = q.buffer[1:]
    fmt.Printf("Consumed: %d | Buffer: %v\n", item, q.buffer)

    // Notify a waiting producer
    q.cond.Signal()

    return item
}

func main() {
    // Create a queue buffer with size 5
    buffer := NewQueueBuffer(5)

    // WaitGroup to ensure goroutines finish
    var wg sync.WaitGroup

    // Start producer goroutine
    wg.Add(1)
    go func() {
        defer wg.Done()
        for i := 1; i <= 10; i++ {
            buffer.Enqueue(i)
            time.Sleep(10 * time.Millisecond) // Simulate work
        }
    }()

    // Start consumer goroutine
    wg.Add(1)
    go func() {
        defer wg.Done()
        for i := 1; i <= 10; i++ {
            buffer.Dequeue()
            time.Sleep(2000 * time.Millisecond) // Simulate work
        }
    }()

    wg.Wait()
    fmt.Println("All operations completed!")
}
