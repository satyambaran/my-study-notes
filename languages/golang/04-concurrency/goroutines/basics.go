package main

import (
	"fmt"
	"runtime"
	"sync"
	"time"
)

// when all go routines are done processing and something(either waitGroup or unbuffered channel) is still waiting then we get //?fatal error: all goroutines are asleep - deadlock!

// https://betterprogramming.pub/common-goroutine-leaks-that-you-should-avoid-fe12d12d6ee
//https://betterprogramming.pub/writing-better-code-with-go-concurrency-patterns-9bc5f9f73519

func main() {
	example4()
}

func example() {
	//? race condition can be detected by       go run -race ./golang/tutorial/goroutines.go
	var msg = "Hello"
	go func() {
		fmt.Println("1", msg)
	}()
	msg = "Goodbye"
	// will print Goodbye
	go func(msg string) { // better practice
		fmt.Println("2", msg) //? this line will always print 2 Goodbye
	}(msg)
	msg = "Goodbye22"
	time.Sleep(100 * time.Millisecond)
}
func example1() {
	var wg = &sync.WaitGroup{}
	wg.Add(1) // this should be done before every go routine call
	msg := "hi"
	go func(msg string) {
		fmt.Println(msg)
		wg.Done()
	}(msg)
	wg.Wait()
}

func example2() {
	var wg = &sync.WaitGroup{}
	var counter = 0

	sayHello := func(wg *sync.WaitGroup, counter int) {
		defer wg.Done()
		fmt.Println("hello ", counter) // this will print zero because we passed counter itself which was not updateed yet
	}
	sayHello2 := func(wg *sync.WaitGroup) {
		defer wg.Done()
		fmt.Println("hello2 ", counter) // this will print random things in random order, go routines are not syncronized
	}
	increment := func(wg *sync.WaitGroup) {
		defer wg.Done()
		counter++
	}
	for i := 0; i < 5; i++ {
		wg.Add(3)
		go sayHello(wg, counter)
		go sayHello2(wg)
		go increment(wg)
	}
	wg.Wait()
}

func example3() {
	wg := &sync.WaitGroup{}
	m := &sync.RWMutex{}

	// in this example we have removed concurrency completely by bounding it

	counter := 0
	sayHello := func(wg *sync.WaitGroup, counter int) {
		defer wg.Done()
		fmt.Println("hello ", counter) // this will print zero because we passed counter itself which was not updated yet
		m.RUnlock()
	}
	increment := func(wg *sync.WaitGroup) {
		defer wg.Done()
		counter++
		m.Unlock()
	}
	for i := 0; i < 5; i++ {
		wg.Add(2)
		m.RLock() // RLock locks rw for reading. It should not be used for recursive read locking; a blocked Lock call excludes new readers from acquiring the lock. See the documentation on the RWMutex type.
		go sayHello(wg, counter)
		m.Lock() // Lock locks rw for writing. If the lock is already locked for reading or writing, Lock blocks until the lock is available.
		go increment(wg)
	}
	wg.Wait()
}

func example4() {
	//more threads can increase performance but unnecessarily more will slow it down
	runtime.GOMAXPROCS(2) //GOMAXPROCS sets the maximum number of CPUs that can be executing simultaneously and returns the previous setting.
	fmt.Printf("%v", runtime.GOMAXPROCS(-1))
}

/*
M:N Scheduling

Go uses an M:N scheduling model:

	•	M represents the number of goroutines.
	•	N represents the number of operating system threads.

In this model:

	•	The Go scheduler multiplexes M goroutines onto N OS threads. This means that many goroutines can be run on a smaller number of threads.
	•	The Go runtime automatically manages the scheduling, deciding which goroutine to run on which thread, allowing for efficient use of CPU resources.

Scheduler
The Go scheduler manages the lifecycle of goroutines:
	•	It keeps track of which goroutines are runnable, blocked, or in a waiting state (e.g., waiting for I/O operations).
	•	When a goroutine blocks (e.g., waiting on a channel or I/O), the scheduler can switch to another runnable goroutine, optimizing CPU usage.
	•	When the blocked goroutine is ready to continue, it will be scheduled again based on the available OS threads.


Blocking and Context Switching
	•	Blocking: When a goroutine blocks (e.g., waiting for a channel operation), the scheduler switches to another goroutine that is ready to run. This prevents the OS thread from being idle.
	•	Context Switching: The Go scheduler handles context switching between goroutines, allowing it to efficiently manage the execution of multiple goroutines.


*/
