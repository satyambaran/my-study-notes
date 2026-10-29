# Runnable Go codebook

This package is the executable companion to `../index.html`. It uses only the standard library and targets Go 1.21. Every example is a small function with a focused contract and an automated test. The files are grouped by concept so you can read them in any order.

| File | Learn by inspecting |
| --- | --- |
| `basics.go` | zero values, value passing, closures, explicit conversions |
| `collections.go` | slice aliasing and cloning, UTF-8, deterministic map keys, sets |
| `methods_interfaces.go` | receivers, method sets, typed nil, generics |
| `errors.go` | sentinel and typed errors, wrapping, `defer`, recovery boundaries |
| `concurrency.go` | bounded workers, mutex and atomics |
| `channels.go` | channel closure, receive status, cancellation-safe sends |
| `context.go` | timeout, cancellation, context values, goroutine termination |
| `json_io.go` | JSON decoding and validation, bounded reading, file cleanup |
| `http_sql.go` | HTTP handler/client lifecycle, SQL lookup and transactions |
| `structures.go` | stack, queue, heap, tree traversal |
| `patterns.go` | functional options and safe construction |
| `codebook_test.go` | table tests, HTTP tests, race-sensitive tests, fuzzing, benchmarks |

Run from the parent directory:

```sh
go test ./codebook
go test -race ./codebook
go test -run TestSlices -v ./codebook
go test -bench BenchmarkCountRunes -benchmem ./codebook
```

Read the source alongside the HTML chapter. A good drill is: predict output or edge behavior, run the named test, then explain the reason in one sentence. The examples are intended to compile as a package; they are not standalone `main` programs.

If this Mac's Go 1.21.5 linker reports `missing LC_UUID load command`, add `-ldflags '-linkmode=external'` to the `go test` command or update Go.
