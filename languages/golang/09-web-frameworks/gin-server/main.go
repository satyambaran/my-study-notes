package main

import (
    "net/http"
    "os"

    "example.com/web_server/routes"
    "github.com/gin-gonic/gin"
)

func main() {
    port := os.Getenv("PORT")
    if port == "" {
        port = "8000"
    }
    router := gin.New()
    // router2:=gin.Default()
    router.Use(gin.Logger())

    routes.AuthRoutes(router)
    routes.UserRoutes(router)

    router.GET("/api/1", func(c *gin.Context) {
        c.JSON(http.StatusOK, gin.H{"message": "success"})
    })
    router.Run(":" + port)
}
