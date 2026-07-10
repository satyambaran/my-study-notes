package main

import "fmt"

type Animal struct {
	Name   string `json:"name" required max:"10"`
	Origin string `json:"origin"`
	// Bird     //invalid recursive type Animal
}

type Bird struct {
	Animal `json:"animal"`
	Fly    bool
	Speed  float64
}


// Define the Human struct
type Human struct {
    Name string
}

// Method for Human
func (h Human) Speak() {
    fmt.Printf("%s says: Hello!\n", h.Name)
}

// Method for Human
func (h Human) Role() {
    fmt.Printf("%s is a human.\n", h.Name)
}

// Define the Student struct embedding Human
type Student struct {
    Human  // Embed the Human struct
    Course string // additional value
}

// Method specific to Student
func (s Student) Study() {
    fmt.Printf("%s is studying %s.\n", s.Name, s.Course)
}

// Override the Role method for Student
func (s Student) Role() {
    fmt.Printf("%s is a student.\n", s.Name)
}

func main() {
    // Create an instance of Human
    h := Human{Name: "Alice"}
    h.Speak() // Alice says: Hello!
    h.Role()  // Alice is a human.

    // Create an instance of Student
    s := Student{
        Human: Human{Name: "Bob"},
        Course: "Computer Science",
    }
    s.Speak() // Bob says: Hello! (inherited from Human)
    s.Role()  // Bob is a student. (overridden method)
    s.Study() // Bob is studying Computer Science.
}