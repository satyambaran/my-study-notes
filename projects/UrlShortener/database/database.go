package database

// Singleton GORM connection using sync.Once.
// Connect() must be called once at startup (main.go).
// Get() returns the shared *gorm.DB everywhere else.

import (
	"errors"
	"sync"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

var (
	instance *gorm.DB
	once     sync.Once
)

func Connect(dsn string) error {
	var initErr error
	once.Do(func() {
		db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
		if err != nil {
			initErr = err
			return
		}
		instance = db
	})
	if initErr != nil {
		return initErr
	}
	if instance == nil {
		return errors.New("database.Connect was already called but failed; cannot retry")
	}
	return nil
}

func Get() *gorm.DB {
	return instance
}
