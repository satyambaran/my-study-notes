package main

import (
    "fmt"
)

func divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, fmt.Errorf("cannot divide %f by %f", a, b)
        // return 0, errors.New("cannot divide by zero")
    }
    return a / b, nil
}

func main() {
    result, err := divide(4, 0)
    if err != nil {
        fmt.Println("Error:", err.Error())
        return
    }
    fmt.Println("Result:", result)
}
