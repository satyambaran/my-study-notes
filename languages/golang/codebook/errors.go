package codebook

import (
	"errors"
	"fmt"
	"io"
)

// A sentinel represents a category, not a user-facing message to parse.
var ErrNotFound = errors.New("not found")

// Lookup wraps the category with context. Callers use errors.Is, not a string
// comparison, to discover the original cause.
func Lookup(items map[string]int, key string) (int, error) {
	value, ok := items[key]
	if !ok {
		return 0, fmt.Errorf("lookup %q: %w", key, ErrNotFound)
	}
	return value, nil
}

// FieldError carries structured detail for errors.As.
type FieldError struct {
	Field  string
	Reason string
}

func (e *FieldError) Error() string { return e.Field + ": " + e.Reason }

func ValidateName(name string) error {
	if name == "" {
		return fmt.Errorf("validate user: %w", &FieldError{Field: "name", Reason: "required"})
	}
	return nil
}

// CopyAndClose owns src and closes it on every return path, including errors.
// The named error lets a close failure be reported without hiding a copy error.
func CopyAndClose(dst io.Writer, src io.ReadCloser) (n int64, err error) {
	defer func() { err = errors.Join(err, src.Close()) }()
	return io.Copy(dst, src)
}

// RecoverBoundary is appropriate only at a deliberate boundary, such as a
// worker supervisor. Do not turn ordinary validation errors into panics.
// recover must run directly in a deferred function in this same goroutine.
func RecoverBoundary(fn func()) (err error) {
	defer func() {
		if value := recover(); value != nil {
			err = fmt.Errorf("worker panic: %v", value)
		}
	}()
	fn()
	return nil
}
