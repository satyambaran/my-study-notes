package codebook

import (
	"sort"
	"unicode/utf8"
)

// AppendShared starts with spare capacity. The returned slice is longer, and
// append writes to the original backing array. This is an aliasing example.
func AppendShared() (original [3]int, grown []int) {
	original = [3]int{1, 2, 3}
	view := original[:2]
	grown = append(view, 9)
	return original, grown // original is [1 2 9]
}

// AppendIsolated clones before appending, so later writes cannot change input.
func AppendIsolated(input []int, value int) []int {
	copyOfInput := append([]int(nil), input...)
	return append(copyOfInput, value)
}

// DeleteAt preserves order. Clearing the last slot releases any pointer held
// by the old backing array, which matters when that array remains reachable.
func DeleteAt[T any](values []T, index int) []T {
	if index < 0 || index >= len(values) {
		panic("index out of range") // programmer precondition, not user input
	}
	copy(values[index:], values[index+1:])
	var zero T
	values[len(values)-1] = zero
	return values[:len(values)-1]
}

// StringLengths separates bytes (len) from decoded Unicode code points.
// A displayed grapheme can contain multiple runes; this is not a glyph count.
func StringLengths(s string) (bytes, runes int) {
	return len(s), utf8.RuneCountInString(s)
}

// RuneFrequency counts Unicode code points, not bytes.
func RuneFrequency(s string) map[rune]int {
	counts := make(map[rune]int)
	for _, r := range s {
		counts[r]++
	}
	return counts
}

// SortedKeys makes output deterministic because Go map iteration order is not.
func SortedKeys[V any](m map[string]V) []string {
	keys := make([]string, 0, len(m))
	for key := range m {
		keys = append(keys, key)
	}
	sort.Strings(keys)
	return keys
}

// Set uses comparable keys and zero-size values for membership. Like any map,
// its nil zero value supports lookup but must be made before writing.
type Set[T comparable] map[T]struct{}

func (s Set[T]) Add(value T) { s[value] = struct{}{} }
func (s Set[T]) Has(value T) bool {
	_, ok := s[value]
	return ok
}
