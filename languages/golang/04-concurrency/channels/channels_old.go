package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

// use -race flag to test all this scenario
func main() {
	example()
}

func example() {
	wg := &sync.WaitGroup{}
	ch := make(chan int) //? unbuffered channel nothing can be stored here
	//? it needs to leave as it arrives
	//? leads to synchronous communication between the go routines
	//? sender will wait till we have a receiver
	go func() {
		/*
			sending 3 2024-10-16 04:23:41.507
			recieved: 3 2024-10-16 04:23:42.508
			just sent 3 2024-10-16 04:23:42.508
			sending 4 2024-10-16 04:23:42.508
			recieved: 4 2024-10-16 04:23:43.509
			just sent 4 2024-10-16 04:23:43.509
		*/
		wg.Add(2)
		go func() {
			defer wg.Done()
			for i := 0; i < 10; i++ {
				fmt.Println("sending", i, time.Now().Format("2006-01-02 15:04:05.000"))
				ch <- i
				fmt.Println("just sent", i, time.Now().Format("2006-01-02 15:04:05.000"))
			}
			// ch <- -1
			close(ch)
		}()
		go func() {
			defer wg.Done()
			for {
				i, ok := <-ch
				if !ok {
					// ok is false when the channel is closed
					break
				}
				// for i := range ch {
				fmt.Println("recieved:", i, time.Now().Format("2006-01-02 15:04:05.000"))
				time.Sleep(50 * time.Millisecond)
				if i == -1 {
					break
				}
			}
		}()
	}()
	for j := 0; j < 5; j++ {
		wg.Add(2)
		go func() {
			defer wg.Done()
			i := <-ch
			fmt.Println(i)
		}()
		go func() {
			i := 42
			ch <- i //we pass copy of data, so it'll be 42
			i = 27
			wg.Done()
		}()
	}
	wg.Wait()
}
func example00() {
	//! very very very important
	ch := make(chan int)
	go func() {
		fmt.Println("2")
		ch <- 42 //? An unbuffered channel is a channel with a capacity of 0. When a value is sent on an unbuffered channel, the sender blocks this goroutine until some other gor routine is ready to receive the value.
	}()
	i := <-ch
	fmt.Println("2", i)
	//! here it wont work
	// go func() {
	// 	fmt.Println("2")
	// 	ch <- 42 //? An unbuffered channel is a channel with a capacity of 0. When a value is sent on an unbuffered channel, the sender blocks this goroutine until some other gor routine is ready to receive the value.
	// }()
}
func example0() {
	//! very very very important
	wg := &sync.WaitGroup{}
	ch := make(chan int)
	wg.Add(1)
	go func() {
		fmt.Println("2")
		ch <- 42 //? An unbuffered channel is a channel with a capacity of 0. When a value is sent on an unbuffered channel, the sender blocks until the value is received by the receiver.
		// code will go in deadlock

		// buffered channels allow for asynchronous communication
		i := <-ch
		fmt.Println("2", i)
		wg.Done()
	}()
	wg.Wait()
}
func example1() {
	// very important
	wg := &sync.WaitGroup{}
	ch := make(chan int)
	wg.Add(2)
	go func() {
		fmt.Println("1")
		i := <-ch //? since it was waiting already
		fmt.Println("1", i)
		ch <- 27
		wg.Done()
	}()
	go func() {
		fmt.Println("2")
		ch <- 42
		i := <-ch
		fmt.Println("2", i) //? this wont get executed until previous one is printed
		wg.Done()
	}()
	wg.Wait()
}
func example2() {
	// very important
	wg := &sync.WaitGroup{}
	ch := make(chan int)
	wg.Add(2)
	go func(ch <-chan int) {
		i := <-ch
		fmt.Println("1", i)
		wg.Done()
	}(ch)
	go func(ch chan<- int) {
		ch <- 42
		wg.Done()
	}(ch) //(ch<-) wont work
	wg.Wait()
}
func example3() {
	//` this program wont work
	wg := &sync.WaitGroup{}
	ch := make(chan int)
	go func() {
		defer wg.Done()
		i := <-ch
		fmt.Println(i)
	}()
	for j := 0; j < 5; j++ {
		wg.Add(2)
		go func(j int) {
			i := 42
			ch <- i //we pass copy of data, so it'll be 42
			// code is stopped here at  j=1 iteration, because no one's there to read it
			fmt.Println(j)
			i = 27
			wg.Done()
		}(j)
	}
	wg.Wait() //without this wait this function would have run fine, because it wont be waiting for anything
}
func example4() {
	//?`      this program will work,
	//todo      but some data will get lost
	wg := &sync.WaitGroup{}
	ch := make(chan int, 50) // if wouldve kept the size 3, this program would have blocked.
	// but go program can terminate itself with value still in channel
	go func() {
		defer wg.Done()
		i := <-ch
		fmt.Println(i)
	}()
	wg.Add(6)
	for j := 0; j < 5; j++ {
		go func(j int) {
			i := 42
			ch <- i // we pass copy of data, so it'll be 42
			// code wont be stopping here as we have places to store int in channels
			fmt.Println(j)
			i = 27
			wg.Done()
			fmt.Println(len(ch), "len")
		}(j)
	}
	fmt.Println(len(ch), "len")
	wg.Wait()
}
func example5() {
	wg := &sync.WaitGroup{}
	ch := make(chan int, 50)
	wg.Add(2)
	go func() {
		defer wg.Done()
		// for i := range ch {
		// 	fmt.Println(i)
		// }
		for {
			if i, ok := <-ch; ok {
				fmt.Println(i, ok)
			} else {
				break
			}
		}
	}()
	go func() {
		i := 42
		ch <- i
		i = 27
		ch <- i
		close(ch) // without this close,    range ch/ for{}   will keep expecting values and hence deadlock
		// after closing, we cant send message to chan but we can receive though

		wg.Done()
	}()

	wg.Wait()
}
func example6() {
	var count int
	wg := &sync.WaitGroup{}

	for i := 0; i < 1000; i++ {
		wg.Add(1)
		go func() {
			count++
			wg.Done()
		}()
	}

	fmt.Println(count) //? here count wont be less than below count because this part can be executed just after loop, without finishing all go routines
	wg.Wait()

	fmt.Println(count) //? this wont be a thousand because increment is not atomic

	// ?need to add mutex
	var mu sync.Mutex
	count = 0
	for i := 0; i < 1000; i++ {
		wg.Add(1)
		go func() {
			mu.Lock()
			count++
			mu.Unlock()
			wg.Done()
		}()
	}
	fmt.Println(count)
	wg.Wait()
	fmt.Println(count)
}
func example7() {
	var count int
	var mutex sync.Mutex
	wg := &sync.WaitGroup{}

	for i := 0; i < 1000; i++ {
		wg.Add(1)
		go func() {
			mutex.Lock()
			count++
			mutex.Unlock()
			wg.Done()
		}()
	}

	wg.Wait()

	fmt.Println(count)
}
func example8() {
	ch := make(chan int)
	go func() {
		for i := 0; i < 5; i++ {
			ch <- i
		}
		// close(ch) // very imp to close, otherwise it'll keep reading
	}()
	for val := range ch {
		fmt.Println(val)
	}
}
func Delay() int {
	time.Sleep(time.Second)
	return rand.Intn(10)
}
func example9() {
	wg := &sync.WaitGroup{}
	ch := make(chan int)
	go func() {
		for i := 0; i < 5; i++ {
			wg.Add(1)
			go func() {
				defer wg.Done()
				ch <- Delay()
			}()
		}
		wg.Wait()
		close(ch) // very ` to close, otherwise it'll keep reading
	}()
	for val := range ch {
		fmt.Println(val)
	}
}
