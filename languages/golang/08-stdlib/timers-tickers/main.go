package main

import (
	"fmt"
	"sync"
	"time"
)

func backgroundTask(wg *sync.WaitGroup) {
	fmt.Println("go tickers tutorials....")
	ticker := time.NewTicker(1 * time.Second)
	for _ = range ticker.C {
		fmt.Println("tock")
	}
	defer wg.Done()
}
func main() {
	fmt.Println("Go Tickers Tutorial")
	var wg sync.WaitGroup
	wg.Add(1)
	go backgroundTask(&wg)
	fmt.Println("The rest of my application can continue")
	wg.Wait()

}
