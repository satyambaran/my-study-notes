package main

import (
    "fmt"
    "sync"
)

// Singleton represents the singleton struct
type Singleton struct {
    value string
}

// Private variable to hold the Singleton instance
var instance *Singleton

// sync.Once to ensure the singleton instance is created only once
var once sync.Once

// GetInstance returns the singleton instance
func GetInstance(id int) *Singleton {
    // Do calls the function  if and only if Do is being called for the first time for this instance of Once
    once.Do(func() {
        instance = &Singleton{
            value: "Singleton Instance" + fmt.Sprintf("%v", id),
        }
    })
    return instance
}

// Method to get the value of the Singleton
func (s *Singleton) GetValue() string {
    return s.value
}

func main() {
    // Create a WaitGroup to wait for goroutines
    var wg sync.WaitGroup
    numGoroutines := 5

    // Spawn multiple goroutines to demonstrate thread-safe access
    for i := 0; i < numGoroutines; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            singletonInstance := GetInstance(id)
            fmt.Printf("Goroutine %d: %s\n", id, singletonInstance.GetValue())
        }(i)
    }

    // Wait for all goroutines to finish
    wg.Wait()

    // Final check to see if all goroutines got the same instance
    s1 := GetInstance(100)
    fmt.Printf("Final Instance Value: %s\n", s1.GetValue())
}
