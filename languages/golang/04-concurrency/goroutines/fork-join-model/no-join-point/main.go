package main

import (
	"fmt"
	"time"
)

func main() {
	go work()
	time.Sleep(100 * time.Millisecond)
	fmt.Println("waiting done ....")
	//no fork join point and it will not wait to execute work() function to run complete it thinks it executes all things
}

func work() {
	time.Sleep(500 * time.Millisecond)
	fmt.Println("print something done...")
}
