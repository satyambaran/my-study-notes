package main

import (
    "fmt"

    "github.com/things-go/container/queue"
)

func dfs(con [][]int, i int, vis []bool) {
    vis[i] = true
    for j := 0; j < len(con); j++ {
        if con[i][j] == 1 && vis[j] == false {
            dfs(con, j, vis)
        }
    }
}
func findCircleNum(con [][]int) int {
    n, cnt := len(con), 0
    vis := make([]bool, n)
    for i := 0; i < n; i++ {
        if vis[i] == false {
            cnt++
            dfs(con, i, vis)
        }
    }
    return cnt
}
func eventualSafeNodes(graph [][]int) []int {
    n := len(graph)
    cnt := make([]int, n)
    adj := make([][]int, n)
    // adj[0] = append(adj[0], 5)
    for i, v := range graph {
        for _, val := range v {
            adj[i] = append(adj[i], val)
        }
        cnt[i] = len(v)
    }
    q := queue.New()
    q.Add(15)
    q.Add(19)
    q.Add("22")

}
func main() {
    fmt.Println(eventualSafeNodes([][]int{[]int{1, 2}, []int{2, 3}, []int{5}, []int{0}, []int{5}, []int{}, []int{}}))
}
