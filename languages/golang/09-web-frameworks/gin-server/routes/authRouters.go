package routes

import (
    controller "example.com/web_server/controllers"
    "github.com/gin-gonic/gin"
)

func AuthRoutes(incomingRoutes *gin.Engine) {
    incomingRoutes.POST("user/signup", controller.SignUp())
    incomingRoutes.POST("user/login", controller.LogIn())
}
