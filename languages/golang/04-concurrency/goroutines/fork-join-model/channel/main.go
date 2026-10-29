package main

import (
	"fmt"
	"time"
)

func main() {
	done := make(chan struct{})
	go func() { //fork point
		work()
		done <- struct{}{}
	}()
	<-done //joint point
	fmt.Println("waiting done ....")
}

func work() {
	time.Sleep(500 * time.Millisecond)
	fmt.Println("print something done...")
}
