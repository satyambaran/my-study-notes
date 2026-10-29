# Go study notes

This is the one-page review checklist. The [HTML guide](index.html) supplies explanations and expandable examples; the [runnable codebook](codebook/README.md) supplies tested Go 1.21 code. The older topic folders are exploratory scratch files, not a single buildable package.

## 1. Language foundations (`01-basics`)

- **Declarations and types:** use `var`, `const`, type inference with `:=`, and grouped declarations. Go has fixed-width numeric types, strings, booleans, arrays, slices, maps, structs, pointers, functions, and interfaces. A zero value is usable for many types, but a nil map cannot be assigned to until made.
- **Functions:** parameters are passed by value; pass a pointer when the callee must mutate the caller's value. Functions may return multiple values (commonly `value, error`), use named results, and accept a final variadic parameter (`...T`).
- **Scope and packages:** identifiers beginning with an uppercase letter are exported. `init` runs before `main`; use it sparingly for package setup. Packages are imported by module path; `go.mod` declares requirements and the Go version, while `go.sum` records cryptographic checksums (it is not a lockfile).
- **Pointers and escape analysis:** `&x` takes an address and `*p` dereferences it. Returning a pointer to a local is safe: the compiler moves it to the heap when needed. Stack versus heap is an implementation detail, not a lifetime rule to manage manually.
- **Structs and methods:** structs model data; instantiate with literals, access fields with `.`, and use composition by embedding. Value-receiver methods get a copy; pointer-receiver methods can mutate and avoid copying. Keep receiver choice consistent for a type.
- **Strings, bytes, maps, and slices:** strings are immutable bytes; use `[]byte` for mutable byte data and `strings` helpers for text. `make(map[K]V)` creates a writable map. Map lookup may use `v, ok := m[k]`; iteration order is deliberately unspecified. `append` can reallocate a slice, so use its returned slice.
- **Generics:** declare type parameters such as `func Add[T Number](a, b T) T`. A constraint is an interface describing permitted types; `~int` admits named types with underlying type `int`. Go 1.21+ has `cmp.Ordered` for ordering, but use a narrower numeric constraint when arithmetic is required.

## 2. Interfaces and embedding (`02-interfaces`)

- An interface specifies behavior, and a type satisfies it implicitly by implementing its methods. Prefer small interfaces defined near their consumer.
- Method sets matter: a value with a pointer-receiver method implements an interface only as `*T`, not `T`. The standard `io.Writer`/`io.Reader` examples demonstrate behavioral polymorphism.
- Interfaces can compose other interfaces: a type assigned to a composite interface must implement every embedded method. Use type assertions or type switches only when behavior cannot be expressed by the interface.
- `any` is an alias for `interface{}` and accepts every value. It trades compile-time type information for flexibility; validate with assertions/switches. An interface is nil only when both its dynamic type and value are nil—an interface containing a typed nil pointer is not nil.
- Embedding promotes fields and methods, approximating composition rather than classical inheritance. An outer type can define a method with the same name to shadow/promote its own behavior.

## 3. Errors and panics (`03-error-handling`)

- Return and check errors explicitly: `if err != nil { return ..., fmt.Errorf("operation: %w", err) }`. `%w` preserves the cause for `errors.Is`/`errors.As`; use `errors.New` for a fixed message.
- Use `panic` for unrecoverable programmer/invariant failures, not ordinary input or I/O failures. `defer` runs during normal return and panic unwinding, so it is ideal for cleanup.
- `recover` works only when called directly by a deferred function in the same goroutine. Recovering stops that goroutine's panic; re-panicking propagates it to an outer recovery point.

## 4. Concurrency (`04-concurrency`)

- A goroutine (`go f()`) is lightweight and scheduled by the Go runtime; execution order is not guaranteed. The program exits when `main` returns, so establish a join point.
- `sync.WaitGroup`: call `Add` before starting work, `defer Done()` inside each worker, then `Wait`. Pass it by pointer; do not copy it, call `Done` too often, or add a new independent batch while a `Wait` is active.
- Protect shared mutable state with `sync.Mutex` (`Lock`/`Unlock`) or confine ownership to one goroutine and communicate requests through channels. Run `go test -race` or `go run -race` to detect races.
- Channels synchronize senders/receivers. Unbuffered sends rendezvous; buffered channels allow up to their capacity. Use directional types (`<-chan T`, `chan<- T`) in APIs. The owner of the sending side normally closes it when no more values will arrive; receive with `v, ok := <-ch` or `range ch` until closed. Sending on a closed channel panics.
- `select` multiplexes channel operations and supports cancellation/default/timeout cases. `chan struct{}` sends zero-sized values, but the channel itself can allocate. A blocking send/receive with no possible counterpart, mutex cycles, or a WaitGroup that never reaches zero causes deadlock.
- Avoid goroutine leaks: every blocking operation needs a completion or cancellation path. For early pipeline exit, propagate a `done` channel or `context.Context` and select on `ctx.Done()`.
- `context`: derive request-scoped contexts with `WithCancel`, `WithTimeout`, `WithDeadline`, or `WithCancelCause`; always call the returned cancel function. Cancellation flows from parent to children. Context values are for small request metadata, not optional parameters or general storage; use typed private keys.
- Patterns covered: fork/join with a WaitGroup or result channel; worker pools; pipelines; fan-out/fan-in; semaphore (buffered channel) concurrency limits; barrier coordination; rate limiting with tickers; and `sync.Once` for a safe singleton.
- Scheduler examples show that task order varies with workload and `GOMAXPROCS`; use synchronization for ordering, never sleeps. The theory examples also illustrate mutex deadlock/livelock and `sync.Cond` signaling.

## 5. Functional options (`05-design-patterns`)

- The functional-options builder creates a default object, then applies `Option func(*T)` values: `NewServer(WithHost(...), WithPort(...))`. It keeps constructors readable as optional settings grow. Validate options and document conflicts; use error-returning options/constructors when configuration can be invalid.

## 6. JSON (`06-json`)

- `json.Marshal(v)` serializes exported struct fields; `json.Unmarshal(data, &v)` decodes into an addressable destination. Always handle either error.
- Tags rename fields (`json:"name"`), omit them (`json:"-"`), omit zero values (`omitempty`), or encode a number as JSON text (`string`). Tags such as `required`/`max` are ignored by `encoding/json` unless another validator reads them.
- Nested structs/slices model predictable payloads. Decode unstructured JSON into `map[string]any` only when schema is genuinely dynamic—numbers arrive as `float64` by default. Use `os.ReadFile` for small whole files, or `json.Decoder` for streams; close any file you open. Limit untrusted input size and validate fields after decoding.

## 7. Build constraints (`07-build-tags`)

- `//go:build expression` at the top of a file includes it only when its condition matches; the legacy `// +build` line may accompany it for compatibility. The examples select debug/release configuration and OS-specific implementations.
- Build with tags using `go build -tags debug` (or `go run -tags debug .`). Prefer built-in target constraints such as `linux`/`windows` and ensure exactly one implementation of a platform-specific symbol is selected.

## 8. Standard library and tooling (`08-stdlib`)

- `init` examples demonstrate package initialization before `main`; initialize variables first, then call `init` functions in dependency order. Do not rely on cross-file source order where portability matters.
- `time.NewTicker` delivers periodic values on `Ticker.C`; call `Stop` when finished. A ticker loop without a cancellation condition never ends, so it also prevents the associated `WaitGroup.Done` in the sample from running.
- SQL examples use `database/sql` with PostgreSQL: open a driver-backed DB, `Ping`, execute writes, query reads, close rows and DB, and handle every error. In production, use parameter placeholders rather than building SQL strings.
- Modules: `go mod init`, `go get`, `go mod tidy`, `go build`, and `go run` manage and use dependencies. Do not hand-edit `go.sum`.

## 9. Data structures and supporting exercises (`10-data-structures`, `misc`)

- Binary-tree nodes hold a value plus `Left`/`Right` pointers; recursive traversal is natural. A deferred timer helper (`defer StartTimer("name")()`) measures a function scope.
- A priority queue is represented by a heap-backed collection ordered through a `Less` method; heap operations provide efficient insertion/removal of the next priority item.
- The queue-buffer exercise uses a slice plus mutex/condition variables to implement bounded blocking `Enqueue`/`Dequeue`: wait while full/empty, mutate under the lock, then signal waiting peers.
- The transaction-manager exercise models active/committed/rolled-back states, nested parent transactions, local writes, commit merging, and rollback. State transitions and map access need synchronization when shared by goroutines.
- Smaller snippets reinforce pointer swapping, channels, depth-first search for graph components/safe nodes, and contest-style min/max/frequency techniques.

## 10. Web applications (`09-web-frameworks`)

- **Gin server:** create an engine, attach middleware, register auth/user route groups, and return handlers from controllers. Middleware can abort unauthorized requests. The project also shows MySQL access through `database/sql` and AWS Secrets Manager CRUD; keep credentials out of source and return/propagate cloud errors.
- **Fiber + Auth0:** load environment configuration with Viper; build a Fiber app; create a user route group; validate bearer access tokens in Auth0 middleware; and use graceful shutdown to stop on OS signals. The companion Vite/React client obtains credentials from its own environment. Follow `app.env.example` and the project README to run it.

## Practical checklist

1. For the curated code, run `gofmt -w codebook/*.go`, `go test ./codebook`, `go test -race ./codebook`, and `go vet ./codebook`. The older folders have multiple independent `main` functions, so `go test ./...` is not a useful check here.
2. Prefer explicit errors, small interfaces, contexts for request cancellation, and bounded concurrency.
3. Close files/rows/tickers and cancel contexts; ensure every goroutine can finish.
4. Keep secrets in environment variables or a secret manager, parameterize SQL, and validate external input.
