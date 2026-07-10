/*
Do not communicate by sharing memory; instead, share memory by communicating
    While Go standard library has the "sync" package for synchronization primitives,
    It emphasizes using channels to pass data between goroutines rather than directly sharing data protected by mutexes.
    https://go.dev/blog/codelab-share

Sync:
    WaitGroup
    Lock
    RLock
    Once:
        Ensures that a function is executed only once, even in the presence of multiple goroutines.{
                var once sync.Once
                initialize := func() {
                    fmt.Println("Initializing...")
                }
                once.Do(initialize) // Ensures this runs only once
            }
    Cond:
        Coordinating goroutines where one waits for a condition to become true, and another signals it.
        Key Methods:
            •	Wait(): Blocks until Signal() or Broadcast() is called.
            •	Signal(): Wakes up one waiting goroutine.
            •	Broadcast(): Wakes up all waiting goroutines.
        {
            var cond = sync.NewCond(&sync.Mutex{})
            queue := []int{}

            addToQueue := func(val int) {
                cond.L.Lock()
                queue = append(queue, val)
                cond.Signal() // Notify one waiting goroutine
                cond.L.Unlock()
            }

            removeFromQueue := func() {
                cond.L.Lock()
                for len(queue) == 0 {
                    cond.Wait() // Wait for a signal
                }
                fmt.Println("Removed:", queue[0])
                queue = queue[1:]
                cond.L.Unlock()
            }
        }
    Pool:
        Provides a thread-safe way to manage and reuse a pool of objects to reduce memory allocations.{
            var pool = sync.Pool{
                New: func() interface{} {
                    return "new instance"
                },
            }

            instance := pool.Get().(string)
            fmt.Println(instance) // Outputs: "new instance"

            pool.Put("reused instance")
            fmt.Println(pool.Get().(string)) // Outputs: "reused instance"
        }


Concurrency patterns in Go:

    Goroutines: 
        Independently executing function, launched by a go statement.
        It gets multiplexed dynamically onto threads as needed to keep all the goroutines running.
        Lightweight concurrent execution units. Has their own call stack
            Goroutines are unique to Go (though some other languages have a concurrency primitive that is similar). They’re not OS threads, and they’re not exactly green threads—threads that are managed by a language’s runtime—they’re a higher level of abstraction known as coroutines. Coroutines are simply concurrent subroutines (functions, closures, or methods in Go) that are nonpreemptive—that is, they cannot be interrupted. Instead, coroutines have multiple points throughout which allow for suspension or reentry. What makes goroutines unique to Go are their deep integration with Go’s runtime. Goroutines don’t define their own suspension or reentry points; Go’s runtime observes the runtime behavior of goroutines and automatically suspends them when they block and then resumes them when they become unblocked. In a way this makes them preemptable, but only at points where the goroutine has become blocked. It is an elegant partnership between the runtime and a goroutine’s logic. Thus, goroutines can be considered a special class of coroutine.

            Go’s mechanism for hosting goroutines is an implementation of what’s called an M:N scheduler, which means it maps M green threads to N OS threads. Goroutines are then scheduled onto the green threads. When we have more goroutines than green threads available, the scheduler handles the distribution of the goroutines across the available threads and ensures that when these goroutines become blocked, other goroutines can be run
    Channels: 
        A communication mechanism to pass data between goroutines.
        When main function executes <–c(unless buffered channel), it will wait for a value to be sent
        A sender and receiver must both be ready to play their part in the communication. Otherwise we wait until they are. Thus channels both communicate and synchronize.
        Buffering removes synchronization.
        Close:
            indicate that no more values will be sent over a channel
            we can read from a closed channel
            Closing a channel signals all goroutines waiting on it to exit gracefully.
        Select:
            select statement is the glue that binds channels together; it’s how we’re able to compose channels together in a program to form larger abstractions.


    Mutex: Provides mutual exclusion to ensure safe access to shared resources.
        Lock(only have Lock()), RWLock(allows both RLock() and Lock())
    WaitGroup: Allows you to wait for a collection of goroutines to finish.
        Similar to fork join(wg.Add is fork, wg.wait is join)
    Select: Lets you wait on multiple channel operations.
    Context: Provides a way to propagate request-scoped values, cancellation signals, and deadlines across API boundaries.
    Pipeline: Organizes goroutines into stages connected by channels, enabling data processing.
    Fan-out/Fan-in: Distributes work across multiple goroutines and collects the results.
These patterns allow you to write concurrent and parallel Go programs that leverage the language's built-in concurrency primitives.


Memory management in golang
    Go uses automatic memory management via a generational garbage collector. 
Key aspects:
    Stack vs Heap: Go manages variables on the stack or heap based on their lifetime.
    Escape Analysis: Compiler determines if variables need to be allocated on the heap.
    Garbage Collection: Periodic marking and sweeping of unused objects on the heap.
    Memory Allocator: Efficient allocation/deallocation of memory on the heap.
    Type-Specific Allocators: Optimized for common data types like slices and maps.
    Go provides language primitives like make() and new() for manual memory management when needed. Developers can also use runtime.GC() to trigger garbage collection.

    Overall, Go's memory model aims to provide automatic memory management with low overhead, while giving developers control when required.


GoRoutine Leak:
    occurs when a goroutine is left running indefinitely without being terminated
    Blocked on Channels:
	•	A goroutine waits indefinitely to send or receive on a channel that no longer has a corresponding reader or writer.
    Infinite Loops without Exit Conditions:
	•	A goroutine runs an infinite loop that doesn’t exit even when it’s no longer needed.
    Improper Use of select:
	•	A select block without a proper exit condition or default case can block forever
    Forgotten Cleanup:
	•	Goroutines are started but never terminated explicitly when no longer needed
    Waiting on Timers or External Events:
	•	A goroutine blocks while waiting for a timer or an event that never occurs.
Preventing Goroutine Leaks
	1.	Proper Channel Closure:
	•	Always close channels when they’re no longer in use to unblock goroutines.
    2.	Timeouts with ctx.Context:
	•	Use contexts to control the lifecycle of goroutines.
    3.	Avoid Infinite Loops Without Exit Conditions:
	•	Always include an exit condition for loops.
    	4.	Default Case in select:
	•	Use a default case to avoid blocking indefinitely when no channels are ready.

Context
Context helps parent to control life of its children
context is a standard library package (context) used to manage deadlines, timeouts, cancellation signals, and other request-scoped data across API boundaries and goroutines

    Feature	                Description
    Cancellation	        Propagate cancellation signals to goroutines or processes.
    Timeouts and Deadlines	Specify time limits for operations.
    Data Passing	        Attach request-scoped values using context for logging, tracing, or debugging.

    Function	            Description
ctx.Background()	    Returns an empty Context, often used as the root context in servers or main functions.
ctx.TODO()	            Returns an empty Context when it’s unclear which context to use (e.g., during development).
ctx.WithCancel(parent)	Creates a context derived from parent that can be canceled using the returned cancel() function.
ctx.WithTimeout(parent, t)	Creates a context with a timeout, automatically canceling after t.
ctx.WithDeadline(parent, d)	Creates a context that is canceled at a specific deadline (time.Time).
ctx.WithValue(parent, key, val)	Creates a context that carries a specific key-value pair.
ctx.Done()	            Returns a channel that is closed when the context is canceled or times out.
ctx.Err()	            Returns the reason the context was canceled (ctx.Canceled or ctx.DeadlineExceeded).
ctx.Value(key)	        Retrieves the value associated with key in the ctx.
*/