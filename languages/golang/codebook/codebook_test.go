package codebook

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"os"
	"reflect"
	"strings"
	"sync"
	"testing"
	"time"
)

func TestBasics(t *testing.T) {
	n, ready, label := ZeroValues()
	if n != 0 || ready || label != "" {
		t.Fatal("incorrect zero values")
	}
	x := 10
	if IncrementCopy(x) != 11 || x != 10 {
		t.Fatal("value argument unexpectedly changed")
	}
	IncrementPointer(&x)
	if x != 11 {
		t.Fatal("pointer did not update caller")
	}
	first, second := NewCounter(), NewCounter()
	if first() != 1 || first() != 2 || second() != 1 {
		t.Fatal("closures shared state")
	}
	if FormatCount(42) != "42" || Sum(1, 2, 3) != 6 || Sum() != 0 {
		t.Fatal("conversion or variadic sum")
	}
}

func TestSlices(t *testing.T) {
	array, grown := AppendShared()
	if array != [3]int{1, 2, 9} || !reflect.DeepEqual(grown, []int{1, 2, 9}) {
		t.Fatal("append aliasing")
	}
	original := []int{1, 2}
	copy := AppendIsolated(original, 3)
	copy[0] = 99
	if original[0] != 1 {
		t.Fatal("clone aliases input")
	}
	if got := DeleteAt([]int{1, 2, 3}, 1); !reflect.DeepEqual(got, []int{1, 3}) {
		t.Fatalf("delete = %v", got)
	}
	if b, r := StringLengths("Go界"); b != 5 || r != 3 {
		t.Fatalf("lengths = %d, %d", b, r)
	}
	if RuneFrequency("a界a")['a'] != 2 {
		t.Fatal("rune frequency")
	}
	if !reflect.DeepEqual(SortedKeys(map[string]int{"z": 1, "a": 2}), []string{"a", "z"}) {
		t.Fatal("keys not sorted")
	}
	set := make(Set[string])
	set.Add("go")
	if !set.Has("go") || set.Has("rust") {
		t.Fatal("set membership")
	}
}

func TestInterfacesAndGenerics(t *testing.T) {
	c := &Counter{}
	IncrementTwice(c)
	if c.Snapshot() != 2 || TypedNilIsNil() {
		t.Fatal("receiver or typed nil")
	}
	var out bytes.Buffer
	if err := WriteGreeting(&out, "Go"); err != nil || out.String() != "hello, Go" {
		t.Fatalf("writer: %s, %v", out.String(), err)
	}
	got := MapSlice([]int{1, 2}, func(n int) string { return fmt.Sprint(n) })
	if !reflect.DeepEqual(got, []string{"1", "2"}) {
		t.Fatalf("map = %v", got)
	}
	type score int
	if Add(score(2), score(3)) != 5 {
		t.Fatal("underlying-type constraint")
	}
}

type failingCloser struct{ io.Reader }

func (f failingCloser) Close() error { return errors.New("close failed") }

func TestErrorsAndDefer(t *testing.T) {
	_, err := Lookup(nil, "missing")
	if !errors.Is(err, ErrNotFound) {
		t.Fatalf("missing sentinel: %v", err)
	}
	var field *FieldError
	if !errors.As(ValidateName(""), &field) || field.Field != "name" {
		t.Fatal("missing typed error")
	}
	var out bytes.Buffer
	_, err = CopyAndClose(&out, failingCloser{strings.NewReader("ok")})
	if out.String() != "ok" || err == nil || err.Error() != "close failed" {
		t.Fatalf("copy/close: %q, %v", out.String(), err)
	}
	if err := RecoverBoundary(func() { panic("boom") }); err == nil {
		t.Fatal("panic not recovered")
	}
}

func TestWorkerPool(t *testing.T) {
	values, err := MapWorkers(context.Background(), []int{1, 2, 3}, 2, func(_ context.Context, n int) (int, error) { return n * n, nil })
	if err != nil || !reflect.DeepEqual(values, []int{1, 4, 9}) {
		t.Fatalf("pool: %v, %v", values, err)
	}
	if _, err := MapWorkers(context.Background(), nil, 0, func(context.Context, int) (int, error) { return 0, nil }); !errors.Is(err, ErrWorkers) {
		t.Fatal("worker validation")
	}
	boom := errors.New("boom")
	_, err = MapWorkers(context.Background(), []int{1, 2, 3}, 2, func(_ context.Context, n int) (int, error) {
		if n == 2 {
			return 0, boom
		}
		return n, nil
	})
	if !errors.Is(err, boom) {
		t.Fatalf("lost worker error: %v", err)
	}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	_, err = MapWorkers(ctx, []int{1}, 1, func(context.Context, int) (int, error) { t.Fatal("ran canceled work"); return 0, nil })
	if !errors.Is(err, context.Canceled) {
		t.Fatalf("cancellation: %v", err)
	}
}

func TestCountersAndContext(t *testing.T) {
	var mutexCounter SafeCounter
	var atomicCounter AtomicCounter
	var wg sync.WaitGroup
	for i := 0; i < 100; i++ {
		wg.Add(1)
		go func() { defer wg.Done(); mutexCounter.Add(); atomicCounter.Add() }()
	}
	wg.Wait()
	if mutexCounter.Value() != 100 || atomicCounter.Value() != 100 {
		t.Fatal("lost count")
	}
	ctx := WithRequestID(context.Background(), "req-1")
	if id, ok := RequestID(ctx); !ok || id != "req-1" {
		t.Fatal("context value")
	}
	canceled, cancel := context.WithCancel(ctx)
	cancel()
	if err := WaitOrCancel(canceled, time.Hour); !errors.Is(err, context.Canceled) {
		t.Fatalf("wait: %v", err)
	}
	deadline, stop := WithShortTimeout(ctx, time.Hour)
	defer stop()
	if _, ok := deadline.Deadline(); !ok {
		t.Fatal("missing deadline")
	}
}

func TestChannels(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	stream := CountUntil(ctx, 3)
	for want := 0; want < 3; want++ {
		got, ok, err := FirstOrCancel(ctx, stream)
		if err != nil || !ok || got != want {
			t.Fatalf("receive %d: %d, %v, %v", want, got, ok, err)
		}
	}
	if _, ok := <-stream; ok {
		t.Fatal("producer did not close channel")
	}
	blocked := CountUntil(ctx, 100)
	cancel() // unblocks a producer even if we do not read the remaining values
	for range blocked {
	}
	canceled, stop := context.WithCancel(context.Background())
	stop()
	if _, _, err := FirstOrCancel[int](canceled, make(chan int)); !errors.Is(err, context.Canceled) {
		t.Fatalf("select cancellation: %v", err)
	}
}

func TestJSON(t *testing.T) {
	tests := []struct {
		input   string
		limit   int64
		wantErr error
	}{
		{`{"name":"Ada","age":36}`, 100, nil},
		{`{"name":"Ada","unknown":1}`, 100, errors.New("invalid")},
		{`{"name":"Ada"} {}`, 100, errors.New("invalid")},
		{`{"name":"Ada","age":36}`, 5, ErrTooLarge},
		{`{"name":""}`, 100, errors.New("invalid")},
	}
	for _, tc := range tests {
		user, err := DecodeUser(strings.NewReader(tc.input), tc.limit)
		if tc.wantErr == nil && (err != nil || user.Name != "Ada") {
			t.Fatalf("decode %q: %+v, %v", tc.input, user, err)
		}
		if tc.wantErr != nil && err == nil {
			t.Fatalf("expected error for %q", tc.input)
		}
		if errors.Is(tc.wantErr, ErrTooLarge) && !errors.Is(err, ErrTooLarge) {
			t.Fatalf("size error: %v", err)
		}
	}
	path := t.TempDir() + "/user.json"
	if err := os.WriteFile(path, []byte(`{"name":"Lin","age":28}`), 0600); err != nil {
		t.Fatal(err)
	}
	if user, err := LoadUser(path, 100); err != nil || user.Name != "Lin" {
		t.Fatalf("load: %+v, %v", user, err)
	}
}

type fakeStore struct {
	user User
	err  error
}

func (f fakeStore) FindUser(_ context.Context, _ int64) (User, error) { return f.user, f.err }

type roundTripFunc func(*http.Request) (*http.Response, error)

func (fn roundTripFunc) RoundTrip(request *http.Request) (*http.Response, error) {
	return fn(request)
}

func TestHTTPHandlerAndClient(t *testing.T) {
	handler := UserHandler(fakeStore{user: User{ID: 7, Name: "Ada"}})
	recorder := httptest.NewRecorder()
	handler.ServeHTTP(recorder, httptest.NewRequest(http.MethodGet, "/user?id=7", nil))
	if recorder.Code != http.StatusOK {
		t.Fatalf("HTTP %d", recorder.Code)
	}
	var decoded User
	if err := json.Unmarshal(recorder.Body.Bytes(), &decoded); err != nil || decoded.ID != 7 {
		t.Fatalf("response: %+v, %v", decoded, err)
	}
	// In-memory transport tests the client without requiring a listening socket.
	client := &http.Client{Timeout: time.Second, Transport: roundTripFunc(func(request *http.Request) (*http.Response, error) {
		r := httptest.NewRecorder()
		handler.ServeHTTP(r, request)
		return r.Result(), nil
	})}
	user, err := FetchUser(context.Background(), client, "https://example.test/user?id=7")
	if err != nil || user.Name != "Ada" {
		t.Fatalf("fetch: %+v, %v", user, err)
	}
	for _, tc := range []struct {
		body string
		want error
	}{
		{`{"id":7} {}`, errors.New("trailing value")},
		{strings.Repeat(" ", (1<<20)+1), ErrTooLarge},
	} {
		badClient := &http.Client{Transport: roundTripFunc(func(*http.Request) (*http.Response, error) {
			return &http.Response{StatusCode: http.StatusOK, Body: io.NopCloser(strings.NewReader(tc.body))}, nil
		})}
		_, err := FetchUser(context.Background(), badClient, "https://example.test/user")
		if err == nil || (errors.Is(tc.want, ErrTooLarge) && !errors.Is(err, ErrTooLarge)) {
			t.Fatalf("bad response accepted: %v", err)
		}
	}
	for _, tc := range []struct {
		method, path string
		want         int
	}{{"POST", "/user?id=7", 405}, {"GET", "/user?id=x", 400}} {
		r := httptest.NewRecorder()
		handler.ServeHTTP(r, httptest.NewRequest(tc.method, tc.path, nil))
		if r.Code != tc.want {
			t.Fatalf("%s %s: %d", tc.method, tc.path, r.Code)
		}
	}
	r := httptest.NewRecorder()
	UserHandler(fakeStore{err: ErrNotFound}).ServeHTTP(r, httptest.NewRequest("GET", "/user?id=7", nil))
	if r.Code != 404 {
		t.Fatalf("not found: %d", r.Code)
	}
	if err := TransferMoney(context.Background(), nil, 1, 1, 5); err == nil {
		t.Fatal("same-account transfer accepted")
	}
}

func TestStructuresAndOptions(t *testing.T) {
	var stack Stack[int]
	stack.Push(1)
	stack.Push(2)
	if v, ok := stack.Pop(); !ok || v != 2 {
		t.Fatal("stack order")
	}
	var queue Queue[int]
	for i := 0; i < 100; i++ {
		queue.Push(i)
	}
	for i := 0; i < 100; i++ {
		if v, ok := queue.Pop(); !ok || v != i {
			t.Fatal("queue order")
		}
	}
	if _, ok := queue.Pop(); ok {
		t.Fatal("empty queue")
	}
	if TreeSum(&TreeNode{Value: 1, Left: &TreeNode{Value: 2}, Right: &TreeNode{Value: 3}}) != 6 {
		t.Fatal("tree sum")
	}
	if !reflect.DeepEqual(SortedByHeap([]int{3, 1, 2}), []int{1, 2, 3}) {
		t.Fatal("heap order")
	}
	config, err := NewServiceConfig(WithAddress(":9090"), WithTimeout(time.Second))
	if err != nil || config.Address != ":9090" || config.Timeout != time.Second {
		t.Fatalf("options: %+v, %v", config, err)
	}
	if _, err := NewServiceConfig(WithTimeout(0)); err == nil {
		t.Fatal("invalid option accepted")
	}
}

func FuzzDecodeUser(f *testing.F) {
	f.Add([]byte(`{"name":"Ada","age":36}`))
	f.Fuzz(func(t *testing.T, input []byte) {
		_, _ = DecodeUser(bytes.NewReader(input), 1024)
	})
}

func BenchmarkCountRunes(b *testing.B) {
	for i := 0; i < b.N; i++ {
		StringLengths("Go is fast 🐹")
	}
}
