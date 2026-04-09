package main

import (
	"fmt"
	"sync"
)

func main() {
	var wg sync.WaitGroup
	wg.Add(3)
	go func() {
		defer wg.Done()
		fmt.Println("a..1")
	}()
	go func() {
		defer wg.Done()
		fmt.Println("a...2")
	}()
	go func() {
		defer wg.Done()
		fmt.Println("a...3")
	}()
	wg.Wait()
}
