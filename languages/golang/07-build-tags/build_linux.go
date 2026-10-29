//go:build linux
// +build linux

package main

import "fmt"

func platformSpecific() {
    fmt.Println("This is Linux-specific code")
}