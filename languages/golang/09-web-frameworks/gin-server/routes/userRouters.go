package routes

import (
    controller "example.com/web_server/controllers"
    "example.com/web_server/middleware"
    "github.com/gin-gonic/gin"
)

func UserRoutes(incomingRoutes *gin.Engine) {
    incomingRoutes.Use(middleware.Authenticate())
    v1 := incomingRoutes.Group("/v1")
    {
        v1.POST("/user", controller.GetUsers())
        v1.GET("/user/:user_id", controller.GetUserById())
    }
    incomingRoutes.GET("users", controller.GetUsers())
    incomingRoutes.GET("users/:user_id", controller.GetUserById())
}
