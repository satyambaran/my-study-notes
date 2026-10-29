package main

import (
	"fmt"
	"reflect"
	"sort"
	"strconv"
	"sync/atomic"
)

// https://rickyanto.com/interesting-things-from-go-programming-language-golang/
// init() function is a special function that gets called automatically when a package is initialized
// A single package can have multiple init() functions, either in the same file or across multiple files within the same package. They will be executed in the order in which they are defined within the package.
var i int = 42
var (
	name = "satyam"
	home = "sono"
)
var (
	name2 = "kundan"
	home2 = "blr"
)

func printValue(v interface{}) {
	if val, ok := v.(int); ok {
		fmt.Println("Integer value:", val)
	} else {
		fmt.Println("Not an integer")
	}
}
func getValues() (x int, y int) {
	x = 10
	y = 20
	return // No need to explicitly return variables
}

func main() {
	people := []struct {
		Name string
		Age  int
	}{
		{"Alice", 30},
		{"Bob", 25},
		{"Charlie", 35},
	}

	sort.Slice(people, func(i, j int) bool {
		return people[i].Age < people[j].Age
	})

	arr := [5]int{1, 2, 3, 4, 5}
	slice := arr[1:3]
	extended := slice[:cap(slice)]
	// Can slice up to the capacity
	fmt.Println(extended)
	// Output: [2 3 4 5]

outerLoop:
	for i := 0; i < 5; i++ {
		for j := 0; j < 5; j++ {
			if j == 3 {
				break outerLoop // Break out of the outer loop
			}
			fmt.Println(i, j)
		}
	}

	fmt.Println(i)
	var i int // 0 initialization
	fmt.Println(i)
	i = 42
	// atomic.AddInt32(&i, -1)
	var j int = 42
	i, j = j, i
	k := 42
	_ = 53
	fmt.Println(i, j, k)
	var f float64 = 42.5
	h := int(f)
	fmt.Println(f, h)
	f = 42
	h = int(f)
	fmt.Println(f, h, string(h), string(k), strconv.Itoa(h)) // 42 42 * * 42
	var flag bool = true
	fmt.Println(flag)
	// int8 -127 128
	// uint8 0 255
	var i8 int8 = 10
	var i32 int = 16
	fmt.Println(i8, i32)
	// fmt.Println(i8+i32) // wont work
	a := 10 //1010
	b := 3  //0011
	fmt.Println(a&b, a|b, a^b, a&^b)
	fl := 3.14 //  2.1e14, 13.7e3
	fmt.Println(fl, 2.1e14, 13.7e3)
	fl = 10.2
	fl2 := 3.7
	fmt.Println(fl/fl2, fl*fl2, fl+fl2, fl-fl2)
	var comp complex64 = 1 + 2i
	fmt.Printf("%v, %T\n", comp, comp)
	fmt.Printf("%v, %T\n", real(comp), real(comp))
	comp = complex(1, 1)
	fmt.Printf("%v, %T\n", imag(comp), imag(comp))
	arrrr := [3]int{32, 3}
	fmt.Println(arrrr[0], arrrr[1], arrrr[2])
	// arrr := []int{32, 3}
	// fmt.Println(arrr[0], arrr[1]) // , arrr[2] error
	for i := 0; i < len(arr); i++ {
		fmt.Println(arr[i])
	}
	arr2 := [...]int{32, 3} // or  []int{32, 3}
	//! arrays are of fixed size/capacity
	fmt.Println("dddd", arr2[0], arr2[1], len(arr2), cap(arr2))
	// fmt.Println(arr2[2]) arrr[2] error

	mySlice := []int{1, 2, 3, 4, 5, 6, 7, 8, 9}
	newSlice := mySlice[2:6]    // [low:high]
	newSlice2 := mySlice[2:6:6] // [low:high:max] cap=max-low; max should be less than or equal to cap
	fmt.Println(mySlice, newSlice, newSlice2)
	fmt.Println(len(mySlice), len(newSlice), len(newSlice2))
	fmt.Println(cap(mySlice), cap(newSlice), cap(newSlice2))
	// !
	newSlice = append(newSlice, 50)
	newSlice2 = append(newSlice2, 100)
	fmt.Println(mySlice, newSlice, newSlice2)
	arr3 := [3]int{32, 3, 3}
	fmt.Println(arr3[0])
	arr4 := arr3
	arr4[1] = 4
	fmt.Println(arr3, arr4)

	arr5 := [6]interface{}{"tt", 5, false}
	fmt.Println(arr5, reflect.TypeOf(arr5), "    ", reflect.TypeOf(arr5[0]), reflect.TypeOf(arr5[4]))

	// a := [3]int{5, 78, 8}
	// var b [5]int
	// // b = a //not possible since [3]int and [5]int are distinct types
	// fmt.Println(b, a)
}
