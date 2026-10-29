# Go study path

The original files in `01-basics` through `10-data-structures` are exploratory, mostly standalone `package main` programs. Many share a directory and declare their own `main`; therefore `go test ./...` is **not** a valid check for the whole folder. Some examples deliberately demonstrate a race, deadlock, or panic. The root `interview.go` is also a scratch placeholder. Treat these as historical experiments, not templates for a service.

Use these three resources in order:

1. Open [index.html](index.html) for the complete interview guide. Read the summaries, then expand the examples where you need practice. Features added after Go 1.21 are marked by version.
2. Read [GO_NOTES.md](GO_NOTES.md) for a text-only checklist.
3. Work through [`codebook/`](codebook/README.md) for a small set of corrected, runnable, standard-library examples. Read each file's comments, predict the results, then run its tests.

```sh
cd languages/golang
go test ./codebook
go test -race ./codebook
go test -v ./codebook
```

The codebook targets the local `go 1.21` module, so it also works on newer Go releases. The HTML notes explain newer features through Go 1.27 separately.

On this Mac, the installed Go 1.21.5 linker may print `missing LC_UUID load command` when running a test binary. If that happens, use `go test -ldflags '-linkmode=external' ./codebook` (and add `-race` for the race run), or update the Go toolchain. This is a local linker issue, not a failing codebook test.

## What the old notes were missing

- A testable path through the basics; multiple `main` functions prevent package-wide compilation.
- Precise ownership and cancellation in concurrency examples; some use sleeps to keep `main` alive.
- Reliable error handling and resource cleanup for I/O, HTTP, and SQL.
- Clear distinction between an illustration of a failure and a recommended pattern.
- Tests for slice aliasing, typed nils, worker pools, HTTP handlers, and data structures.

The old files remain useful for seeing how a concept was explored. Run a single old file with `go run path/to/file.go` only after checking its imports, side effects, and expected behavior. The `misc/` directory also contains unrelated scratch material; it is not part of the revision sequence.
