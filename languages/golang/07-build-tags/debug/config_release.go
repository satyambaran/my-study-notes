//go:build !debug
// +build !debug

package config

func IsDebug() bool {
    return false
}
