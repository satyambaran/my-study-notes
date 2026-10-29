package main

import "fmt"

type person struct{}

func main() {
    p := adult(20)
    if p == nil { // both type and value should be 'nil'
        fmt.Println("kid")
    } else {
        fmt.Println("adult")
    }
}
func adult(age int) interface{} {
    var ret *person = nil // value is nil, but not the type
    if age < 18 {
        return nil
    }
    return ret
}
