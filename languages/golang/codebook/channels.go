package codebook

import "context"

// CountUntil illustrates channel ownership. This function starts the only
// sender, so that sender closes the channel. A receiver can stop early by
// canceling ctx; without that path, an unbuffered send could leak a goroutine.
func CountUntil(ctx context.Context, limit int) <-chan int {
	out := make(chan int)
	go func() {
		defer close(out)
		for i := 0; i < limit; i++ {
			select {
			case out <- i:
			case <-ctx.Done():
				return
			}
		}
	}()
	return out
}

// FirstOrCancel selects one result or a cancellation. Never use default here:
// it would make the receive nonblocking and could accidentally discard work.
func FirstOrCancel[T any](ctx context.Context, input <-chan T) (T, bool, error) {
	select {
	case value, ok := <-input:
		return value, ok, nil
	case <-ctx.Done():
		var zero T
		return zero, false, ctx.Err()
	}
}
