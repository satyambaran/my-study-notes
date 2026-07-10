package main

import "fmt"

func Test1() {
    fmt.Println("test1")
}
func test2() {   //? this is not exported since it's name is not capitalised
    fmt.Println("test1")
}
