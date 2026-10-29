package codebook

import "container/heap"

// Stack push/pop are amortized O(1). Clearing a removed slot lets the garbage
// collector reclaim pointer values even if the backing array stays alive.
type Stack[T any] struct{ items []T }

func (s *Stack[T]) Push(value T) { s.items = append(s.items, value) }
func (s *Stack[T]) Len() int     { return len(s.items) }
func (s *Stack[T]) Pop() (T, bool) {
	if len(s.items) == 0 {
		var zero T
		return zero, false
	}
	i := len(s.items) - 1
	value := s.items[i]
	var zero T
	s.items[i] = zero
	s.items = s.items[:i]
	return value, true
}

// Queue avoids O(n) shifting on every pop. Periodic compaction releases old
// backing storage; operations are amortized O(1). It is not concurrency-safe.
type Queue[T any] struct {
	items []T
	head  int
}

func (q *Queue[T]) Push(value T) { q.items = append(q.items, value) }
func (q *Queue[T]) Len() int     { return len(q.items) - q.head }
func (q *Queue[T]) Pop() (T, bool) {
	if q.Len() == 0 {
		var zero T
		return zero, false
	}
	value := q.items[q.head]
	var zero T
	q.items[q.head] = zero
	q.head++
	if q.head >= 32 && q.head*2 >= len(q.items) {
		q.items = append([]T(nil), q.items[q.head:]...)
		q.head = 0
	}
	return value, true
}

type TreeNode struct {
	Value       int
	Left, Right *TreeNode
}

// TreeSum is DFS: O(n) time, O(height) call stack.
func TreeSum(root *TreeNode) int {
	if root == nil {
		return 0
	}
	return root.Value + TreeSum(root.Left) + TreeSum(root.Right)
}

// IntHeap implements heap.Interface; its smallest value is at index zero.
// Push/Pop through container/heap are O(log n); use heap.Init for existing data.
type IntHeap []int

func (h IntHeap) Len() int           { return len(h) }
func (h IntHeap) Less(i, j int) bool { return h[i] < h[j] }
func (h IntHeap) Swap(i, j int)      { h[i], h[j] = h[j], h[i] }
func (h *IntHeap) Push(x any)        { *h = append(*h, x.(int)) }
func (h *IntHeap) Pop() any {
	old := *h
	i := len(old) - 1
	value := old[i]
	*h = old[:i]
	return value
}

func SortedByHeap(values []int) []int {
	h := IntHeap(append([]int(nil), values...))
	heap.Init(&h)
	result := make([]int, 0, len(values))
	for h.Len() > 0 {
		result = append(result, heap.Pop(&h).(int))
	}
	return result
}
