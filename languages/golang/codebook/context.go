package codebook

import (
	"context"
	"time"
)

// WaitOrCancel always terminates on either a timer or parent cancellation.
// Stop avoids retaining a timer longer than necessary.
func WaitOrCancel(ctx context.Context, delay time.Duration) error {
	timer := time.NewTimer(delay)
	defer timer.Stop()
	select {
	case <-timer.C:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	}
}

// Context keys should be private comparable types, never plain strings.
type requestIDKey struct{}

func WithRequestID(ctx context.Context, id string) context.Context {
	return context.WithValue(ctx, requestIDKey{}, id)
}

func RequestID(ctx context.Context) (string, bool) {
	id, ok := ctx.Value(requestIDKey{}).(string)
	return id, ok
}

// A derived timeout must be canceled by its owner, even if it fires first.
func WithShortTimeout(parent context.Context, delay time.Duration) (context.Context, context.CancelFunc) {
	return context.WithTimeout(parent, delay)
}
