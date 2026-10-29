package main

import (
    "fmt"
    "runtime"
    "sync"
)

func main() {
    cpuCount := runtime.NumCPU()
    for i := 0; i < cpuCount; i++ {
        fmt.Println(i)
    }
    list := []int{2, 5, 3, 1, 9, 6, 4}
    in := getChan(&list)
    squaredChan := getSquaredChan(in)

    // Create a separate WaitGroup for the print function
    printWg := &sync.WaitGroup{}
    printWg.Add(1)
    go print(squaredChan, printWg)

    printWg.Wait()
}

func getChan(in *[]int) chan int {
    out := make(chan int)
    go func(in *[]int) {
        for _, val := range *in {
            out <- val
        }
        close(out)
    }(in)
    return out
}

func getSquaredChan(in chan int) chan int {
    out := make(chan int)
    go func() {
        for cur := range in {
            out <- (cur * cur)
        }
        close(out)
    }()
    return out
}

func print(squaredChan chan int, wg *sync.WaitGroup) {
    go func() {
        for cur := range squaredChan {
            fmt.Println(cur)
        }
        wg.Done()
    }()
}
