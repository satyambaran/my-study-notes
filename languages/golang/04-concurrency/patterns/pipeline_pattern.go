// pipeline pattern structures a series of processing stages, with each stage executed concurrently. Data flows through these stages sequentially, allowing efficient data transformation and processin

package main

import (
    "fmt"
)

func main() {
    // Create the initial channel with some data
    data := []int{1, 2, 3, 4, 5}
    input := make(chan int, len(data))
    for _, d := range data {
        input <- d
    }
    close(input) // we can't send data on closed channels but can read though

    // First stage of the pipeline: Doubles the input values
    doubleOutput := make(chan int)
    go func() {
        defer close(doubleOutput)
        for num := range input {
            doubleOutput <- num * 2
        }
    }()

    // Second stage of the pipeline: Squares the doubled values
    squareOutput := make(chan int)
    go func() {
        defer close(squareOutput)
        for num := range doubleOutput {
            squareOutput <- num * num
        }
    }()

    // Third stage of the pipeline: Prints the squared values
    for result := range squareOutput {
        fmt.Println(result)
    }
}
