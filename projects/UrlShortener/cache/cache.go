package cache

// Singleton Redis client using sync.Once.
// Connect() must be called once at startup (main.go).
// Get() returns the shared *redis.Client everywhere else.

import (
	"context"
	"log/slog"
	"sync"

	"github.com/go-redis/redis/v8"
)

var (
	instance *redis.Client
	once     sync.Once
	ctx      = context.Background()
)

func Connect(addr, password string, log *slog.Logger) {
	once.Do(func() {
		instance = redis.NewClient(&redis.Options{
			Addr:     addr,
			Password: password,
		})
		setEvictionPolicy(log)
	})
}

func Get() *redis.Client {
	return instance
}

func setEvictionPolicy(log *slog.Logger) {
	// volatile-lfu evicts only keys that have a TTL set (the URL cache entries).
	// The ID counter key has no TTL and is therefore never eligible for eviction.
	const policy = "volatile-lfu"
	if _, err := instance.ConfigSet(ctx, "maxmemory-policy", policy).Result(); err != nil {
		log.Warn("failed to set Redis eviction policy", "error", err)
		return
	}
	result, err := instance.ConfigGet(ctx, "maxmemory-policy").Result()
	if err != nil {
		log.Warn("failed to get Redis eviction policy", "error", err)
		return
	}
	log.Info("Redis eviction policy", "policy", result)
}
