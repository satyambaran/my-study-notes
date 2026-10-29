package codebook

import (
	"context"
	"errors"
	"sync"
	"sync/atomic"
)

// SafeCounter is for shared mutable state; its zero value is ready to use.
// Never copy it after first use, because that would copy the mutex.
type SafeCounter struct {
	mu sync.Mutex
	n  int
}

func (c *SafeCounter) Add() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.n++
}

func (c *SafeCounter) Value() int {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.n
}

// AtomicCounter is useful when the only operation is independent counting.
type AtomicCounter struct{ n atomic.Int64 }

func (c *AtomicCounter) Add()         { c.n.Add(1) }
func (c *AtomicCounter) Value() int64 { return c.n.Load() }

var ErrWorkers = errors.New("workers must be positive")

// MapWorkers caps active work at workers and retains input order. The producer
// owns and closes jobs; every worker observes cancellation. The first error
// cancels remaining work, but all started workers are joined before return.
// fn must honor ctx if it performs blocking I/O; cancellation cannot interrupt
// a function that ignores its context.
func MapWorkers(ctx context.Context, inputs []int, workers int, fn func(context.Context, int) (int, error)) ([]int, error) {
	if workers < 1 {
		return nil, ErrWorkers
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	workCtx, cancel := context.WithCancel(ctx)
	defer cancel()
	jobs := make(chan int)
	results := make([]int, len(inputs))
	var wg sync.WaitGroup
	var once sync.Once
	var firstErr error
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				select {
				case <-workCtx.Done():
					return
				case index, ok := <-jobs:
					if !ok {
						return
					}
					value, err := fn(workCtx, inputs[index])
					if err != nil {
						once.Do(func() { firstErr = err; cancel() })
						return
					}
					results[index] = value // one worker per index; read after Wait
				}
			}
		}()
	}
sendLoop:
	for index := range inputs {
		select {
		case <-workCtx.Done():
			break sendLoop
		case jobs <- index:
		}
	}
	close(jobs)
	wg.Wait()
	if firstErr != nil {
		return nil, firstErr
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	return results, nil
}
