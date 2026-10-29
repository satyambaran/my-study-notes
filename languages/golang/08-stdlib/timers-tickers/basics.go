package main

import (
	"fmt"
	"time"
)

func main() {
	fmt.Println("go tickers tutorials....")
	ticker := time.NewTicker(1 * time.Second)
	for _ = range ticker.C {
		fmt.Println("tock")
	}
}
