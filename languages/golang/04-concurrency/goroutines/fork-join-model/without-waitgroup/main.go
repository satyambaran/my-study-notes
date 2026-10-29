package main

import (
	"fmt"
	"runtime"
	"time"
)

func main() {
	fmt.Println("number of cores:", runtime.NumCPU())
	//var wg sync.WaitGroup
	for i := 0; i < 10; i++ {
		go work(i + 1)
	}
	time.Sleep(100 * time.Millisecond)
	fmt.Println("main is done")
}

func work(id int) {
	time.Sleep(100 * time.Millisecond)
	fmt.Println("task", id, "is done")
}
