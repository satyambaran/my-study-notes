package main

import "sync"

func main() {
	var wg sync.WaitGroup
	wg.Add(1) // this is dead lock situation because it is not going to call wg.done() so  it never going to execuet anything
	wg.Wait()
}
