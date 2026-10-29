//? context helps us add a timeout/ cancellation to a goroutine in between execution

package main

import (
    "context"
    "io/ioutil"
    "net/http"
    "time"

    "github.com/gin-gonic/gin"
)

func main() {
    r := gin.Default()
    r.GET("/hello", func(ctx *gin.Context) {
        timeOutContext, cancelFunc := context.WithTimeout(ctx.Request.Context(), time.Millisecond*50)
        defer cancelFunc()
        // req, err := http.NewRequestWithContext(ctx.Request.Context(), http.MethodGet, "http://yahoo.com", nil)
        req, err := http.NewRequestWithContext(timeOutContext, http.MethodGet, "http://yahoo.com", nil)
        if err != nil {
            panic(err)
        }

        res, err := http.DefaultClient.Do(req)
        if err != nil {
            panic(err)
        }
        defer res.Body.Close()
        data, err := ioutil.ReadAll(res.Body)
        if err != nil {
            panic(err)
        }
        ctx.Data(200, "text/html", data)
    })
    r.Run()
}

// func main() {
//     timeOutContext, cancelFunc := context.WithTimeout(context.Background(), time.Millisecond*50)
//     defer cancelFunc()
//     req, err := http.NewRequestWithContext(timeOutContext, http.MethodGet, "http://placehold.it/2000x2000", nil)
//     if err != nil {
//         panic(err)
//     }
//     res, err := http.DefaultClient.Do(req)
//     if err != nil {
//         panic(err)
//     }
//     defer res.Body.Close()
//     imageData, err := ioutil.ReadAll(res.Body)
//     // fmt.Println(imageData)
//     fmt.Println(len(imageData))
// }
