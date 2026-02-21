package logger

// Singleton structured logger using the standard library log/slog (Go 1.21+).
// Call Get() from anywhere — the same *slog.Logger instance is always returned.

import (
	"log/slog"
	"os"
	"sync"
)

var (
	instance *slog.Logger
	once     sync.Once
)

func Get() *slog.Logger {
	once.Do(func() {
		instance = slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{
			Level: slog.LevelInfo,
		}))
	})
	return instance
}
