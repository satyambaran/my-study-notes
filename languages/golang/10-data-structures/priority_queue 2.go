package main

import (
    "fmt"
    "math/rand"
    "time"

    pq "github.com/nu7hatch/gopqueue"
)

type Task struct {
    val int
}
type Block struct {
    val  int
    next *Block
}

func (a *Task) Less(b interface{}) bool {
    return a.val < b.(*Task).val
}
func main() {
    q := pq.New(0)
    q.Enqueue(&Task{5})
    q.Enqueue(&Task{2})
    q.Enqueue(&Task{11})
    q.Enqueue(&Task{8})
    q.Enqueue(&Task{25})

    for q.Len() > 0 {
        fmt.Println(q.Dequeue())
    }

    rand.Seed(time.Now().UnixNano())
    root := &Block{2, nil}
    list := root
    for i := 0; i < 6; i++ {
        a := 1
        // fmt.Scan(&a)
        a = rand.Intn(10)
        (*list).next = &Block{a, nil}
        list = list.next
    }
    for root.next != nil {
        fmt.Println(root.next.val)
        root = root.next
    }

    mp := map[int]int{}
    for i := 0; i < 10; i++ {
        a, b := rand.Intn(10), rand.Intn(10)
        mp[a] = b
        fmt.Println(a, b)
    }
    fmt.Println(mp)

    set := map[int]struct{}{}
    for i := 0; i < 10; i++ {
        a := rand.Intn(10)
        set[a] = struct{}{}
    }
    fmt.Println(set)
}
