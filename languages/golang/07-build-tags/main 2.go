// go build/run -tags debug
// go build/run
// go build -tags linux
package main

import (
    "fmt"

    config "example.com/build/debug"
)

func main() {
    fmt.Println(config.IsDebug())
}
