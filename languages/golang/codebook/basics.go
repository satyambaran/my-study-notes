// Package codebook contains small, testable examples for Go interview revision.
package codebook

import "strconv"

// ZeroValues shows that declared variables start at their type's zero value.
func ZeroValues() (int, bool, string) {
	var count int
	var ready bool
	var label string
	return count, ready, label
}

// IncrementCopy demonstrates that every function argument is passed by value.
// The caller's n does not change; return the new value to use it.
func IncrementCopy(n int) int { return n + 1 }

// IncrementPointer changes the caller's variable through an explicitly passed
// pointer. It does not make Go a pass-by-reference language: the pointer is copied.
func IncrementPointer(n *int) { (*n)++ }

// NewCounter returns a closure. Each returned function owns an independent n.
// The compiler may move n to the heap because it outlives this call.
func NewCounter() func() int {
	n := 0
	return func() int {
		n++
		return n
	}
}

// FormatCount shows an explicit numeric-to-text conversion. string(42) means
// the rune '*' rather than the decimal digits "42".
func FormatCount(n int) string { return strconv.Itoa(n) }

// Sum is variadic; pass a slice with Sum(values...). A zero-argument call is 0.
func Sum(values ...int) int {
	total := 0
	for _, value := range values {
		total += value
	}
	return total
}
