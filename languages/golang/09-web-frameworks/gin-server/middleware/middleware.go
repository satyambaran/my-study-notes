package middleware

import (
    "math/rand"

    "github.com/gin-gonic/gin"
)

func Authenticate() gin.HandlerFunc {
    return func(ctx *gin.Context) {
        if rand.Intn(2) == 0 {
            ctx.Next()
        } else {
            ctx.JSON(401, gin.H{"message": "Unauthorized"})
            ctx.Abort()
        }
    }
}
