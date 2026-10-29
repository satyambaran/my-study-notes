package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

// Fixed ignores separators, then keeps exactly n bytes, including internal spaces.
func fixed(in *bufio.Reader, n int) string {
	value := make([]byte, n)
	for i := 0; i < n; {
		c, err := in.ReadByte()
		if err != nil {
			panic(err)
		}
		if i > 0 || c > ' ' {
			value[i] = c
			i++
		}
	}
	return string(value)
}

// Line returns the next non-empty line.
func line(in *bufio.Reader) string {
	for {
		value, err := in.ReadString('\n')
		value = strings.TrimSpace(value)
		if value != "" || err != nil {
			return value
		}
	}
}

func main() {
	in := bufio.NewReader(os.Stdin)

	var x int
	fmt.Fscan(in, &x)
	text := make([]string, x)
	for i := range text {
		var length int
		fmt.Fscan(in, &length)
		text[i] = fixed(in, length)
	}

	var n, m int
	fmt.Fscan(in, &n, &m)
	matrix := make([][]int, n)
	sum := 0
	for i := range matrix {
		matrix[i] = make([]int, m)
		for j := range matrix[i] {
			fmt.Fscan(in, &matrix[i][j])
			sum += matrix[i][j]
		}
	}

	var b int
	fmt.Fscan(in, &b)
	lines := make([]string, b)
	for i := range lines {
		lines[i] = line(in)
	}

	fmt.Println("text:", text)
	for _, row := range matrix {
		fmt.Println(row)
	}
	fmt.Println("sum:", sum)
	fmt.Println("lines:", lines)
}
