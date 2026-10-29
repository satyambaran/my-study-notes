package codebook

import "fmt"

// A value receiver reads a copy. A pointer receiver can change the original.
type Counter struct{ Value int }

func (c Counter) Snapshot() int { return c.Value }
func (c *Counter) Increment()   { c.Value++ }

// Incrementer is deliberately defined by the consumer and has one method.
// *Counter satisfies it; Counter does not, because Increment uses *Counter.
type Incrementer interface{ Increment() }

var _ Incrementer = (*Counter)(nil) // compile-time method-set check

func IncrementTwice(item Incrementer) {
	item.Increment()
	item.Increment()
}

// TypedNilIsNil illustrates an interface's (dynamic type, dynamic value) pair.
// The pointer is nil, but the interface holding it is not nil.
func TypedNilIsNil() bool {
	var pointer *Counter
	var value any = pointer
	return value == nil
}

// InterfaceWriter chooses an interface because callers can supply any writer.
// This is useful at an I/O boundary, unlike accepting any for all business data.
type StringWriter interface{ Write([]byte) (int, error) }

func WriteGreeting(writer StringWriter, name string) error {
	_, err := fmt.Fprintf(writer, "hello, %s", name)
	return err
}

// MapSlice uses a type parameter because one algorithm works on many concrete
// input/output types without assertions or reflection.
func MapSlice[A, B any](items []A, transform func(A) B) []B {
	result := make([]B, len(items))
	for i, item := range items {
		result[i] = transform(item)
	}
	return result
}

// ~int includes both int and named types whose underlying type is int.
type Integer interface{ ~int | ~int32 | ~int64 }

func Add[T Integer](a, b T) T { return a + b }
