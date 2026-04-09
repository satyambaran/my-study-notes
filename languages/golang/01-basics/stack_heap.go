package main

import "fmt"

func fun() *int {
    p := 5
    fmt.Println(&p)
    return &p
}
func main() {
    k := fun()
    //! since function went out of stack, this varible will get lost so pointer wont be able to access it
    //! but compiler detect this scenario, so they will movie it heap from function stack
    //! increases work of garbage collector, as function stack variables will get destroyed automatically as they go out of scope, but heap ones will gets removed by garbage collector
    fmt.Println(*k, k)
}
