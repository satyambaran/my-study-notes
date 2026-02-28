package main

import (
	"log"

	"github.com/gofiber/fiber/v2"
	fibLogger "github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/joho/godotenv"
	"github.com/satyambaran/UrlShortener/cache"
	"github.com/satyambaran/UrlShortener/config"
	"github.com/satyambaran/UrlShortener/controller"
	"github.com/satyambaran/UrlShortener/database"
	appLogger "github.com/satyambaran/UrlShortener/logger"
	"github.com/satyambaran/UrlShortener/model"
	"github.com/satyambaran/UrlShortener/repository"
	"github.com/satyambaran/UrlShortener/service"
)

func main() {
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, relying on environment variables")
	}

	cfg := config.Load()
	appLog := appLogger.Get()

	// --- Singletons ---------------------------------------------------------
	if err := database.Connect(cfg.DBUrl); err != nil {
		appLog.Error("failed to connect to database", "error", err)
		log.Fatal(err)
	}
	db := database.Get()
	db.AutoMigrate(&model.URL{})

	cache.Connect(cfg.RedisUrl, cfg.RedisPassword, appLog)

	// --- Wire layers --------------------------------------------------------
	repo := repository.NewURLRepository(db)
	svc := service.NewURLService(repo, cache.Get(), appLog)
	ctrl := controller.NewURLController(svc)

	// --- HTTP server --------------------------------------------------------
	app := fiber.New()
	app.Use(fibLogger.New())

	app.Post("/shorten", ctrl.Shorten)
	app.Get("/:shortURL", ctrl.Resolve)

	appLog.Info("server starting", "addr", ":3000")
	log.Fatal(app.Listen(":3000"))
}
