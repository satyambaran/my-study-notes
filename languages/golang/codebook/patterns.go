package codebook

import (
	"errors"
	"time"
)

// Functional options fit genuinely optional configuration; a plain struct
// is clearer when there are only a couple of required fields.
type ServiceConfig struct {
	Address string
	Timeout time.Duration
}

type ServiceOption func(*ServiceConfig) error

func WithAddress(address string) ServiceOption {
	return func(c *ServiceConfig) error {
		if address == "" {
			return errors.New("address must not be empty")
		}
		c.Address = address
		return nil
	}
}

func WithTimeout(timeout time.Duration) ServiceOption {
	return func(c *ServiceConfig) error {
		if timeout <= 0 {
			return errors.New("timeout must be positive")
		}
		c.Timeout = timeout
		return nil
	}
}

func NewServiceConfig(options ...ServiceOption) (ServiceConfig, error) {
	config := ServiceConfig{Address: ":8080", Timeout: 5 * time.Second}
	for _, option := range options {
		if option == nil {
			return ServiceConfig{}, errors.New("nil service option")
		}
		if err := option(&config); err != nil {
			return ServiceConfig{}, err
		}
	}
	return config, nil
}
