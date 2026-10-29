package main

import "fmt"

// Server is the object we want to configure.
type Server struct {
    Host string
    Port int
    TLS  bool
}

// Option defines a function type that takes a pointer to Server.
type Option func(*Server)

// NewServer creates a Server and applies functional options.
func NewServer(opts ...Option) *Server {
    // Set default values
    server := &Server{
        Host: "localhost",
        Port: 8080,
        TLS:  false,
    }

    // Apply each option to modify the server
    for _, opt := range opts {
        opt(server)
    }

    return server
}

// Option functions to modify the server.

func WithHost(host string) Option {
    return func(s *Server) {
        s.Host = host
    }
}

func WithPort(port int) Option {
    return func(s *Server) {
        s.Port = port
    }
}

func WithTLS(tls bool) Option {
    return func(s *Server) {
        s.TLS = tls
    }
}

func main() {
    // Creating a server with default values
    defaultServer := NewServer()
    fmt.Printf("Default server: %+v\n", defaultServer)

    // Creating a server with custom options
    customServer := NewServer(WithHost("example.com"), WithPort(443), WithTLS(true))
    fmt.Printf("Custom server: %+v\n", customServer)

    
}
func doSomething() error {
    err := someFunction()
    if err != nil {
        return fmt.Errorf("do something failed, %w", err)
    }
    return nil
}
func someFunction() error {
    err := fmt.Errorf("someFunction Error")
    return err
}
