//go:build windows
// +build windows

package main

import "fmt"

func platformSpecific() {
    fmt.Println("This is Windows-specific code")
}
