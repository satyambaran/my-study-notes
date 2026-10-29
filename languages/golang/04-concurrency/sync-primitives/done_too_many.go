package main

import "sync"

func main() {
	var wg sync.WaitGroup
	//calling wg.Done() without adding so it will get negative by one so panic error
	wg.Done()
}
