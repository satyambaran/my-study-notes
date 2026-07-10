package main

import "fmt"

var name string

func init() {
	fmt.Println("This will get called on main initialization")
	name = "Elliot"
}
func init() {
	fmt.Println("ion")
	name = "Elliot"
}
func init() {
	fmt.Println("Tation")
	name = "Elliot"
}

func main() {
	fmt.Println("My Wonderful Go Program")
	fmt.Printf("Name: %s\n", name)
}
// The init function is called automatically when the package is first initialized. Its execution order within a package is not guaranteed.