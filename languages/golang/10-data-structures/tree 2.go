package main

import (
	"fmt"
	"log"
	"time"
)

type Node struct {
	val         int
	left, right *Node
}

func StartTimer(name string) func() {
	t := time.Now()
	log.Println(name, "started")
	return func() {
		d := time.Now().Sub(t)
		log.Println(name, "took", d)
	}
}
func RunTimer() {
	stop := StartTimer("My timer")
	defer stop()
	time.Sleep(1 * time.Second)
}

func main() {
	// var root *Node   //! need to use make
	// root.val=5
	root := &Node{1, &Node{val: 2}, &Node{val: 3}}
	root.left.left = &Node{val: 7}
	root.left.right = &Node{val: 6}
	root.right.left = &Node{val: 5}
	root.right.right = &Node{val: 4}

	cur := []*Node{}
	next := []*Node{}
	cur = append(cur, root)

	LtoR := true
	for len(cur) > 0 {
		root := cur[len(cur)-1]
		cur = cur[:len(cur)-1]
		fmt.Println(root.val)
		if LtoR {
			if root.left != nil {
				next = append(next, root.left)
			}
			if root.right != nil {
				next = append(next, root.right)
			}
		} else {
			if root.right != nil {
				next = append(next, root.right)
			}
			if root.left != nil {
				next = append(next, root.left)
			}
		}
		if len(cur) == 0 {
			LtoR = !LtoR
			cur, next = next, cur
		}
	}

}
