package main

import (
	"encoding/json"
	"fmt"
)

func main() {
	readingString := `{"name": "battery sensor", "capacity": 40, "time": "2019-01-21T19:07:28Z"}`
	var readingAnswer map[string]interface{}
	err := json.Unmarshal([]byte(readingString), &readingAnswer)
	if err != nil {
		fmt.Println("not able to unmarshal json string")
	}
	fmt.Println(readingAnswer)
}
