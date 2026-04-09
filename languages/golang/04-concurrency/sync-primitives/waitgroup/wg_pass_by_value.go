package main

import (
	"fmt"
	"sync"
)

func main() {
	var wg sync.WaitGroup
	wg.Add(1)
	go work(wg)
	wg.Wait()
}

func work(wg sync.WaitGroup) {
	//it will give panic because we are not passing reference
	//and we are passing value whitch is new instance of wg
	// and it will decrement wg to negative
	defer wg.Done()
	fmt.Println("work is done")
}
