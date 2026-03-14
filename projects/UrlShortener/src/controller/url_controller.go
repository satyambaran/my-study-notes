package controller

import (
	"github.com/gofiber/fiber/v2"
	"github.com/satyambaran/UrlShortener/src/service"
)

type URLController struct {
	service service.URLService
}

func NewURLController(svc service.URLService) *URLController {
	return &URLController{service: svc}
}

func (c *URLController) Shorten(ctx *fiber.Ctx) error {
	type request struct {
		URL          string `json:"url"`
		RequestedURL string `json:"requested_url"`
	}
	var req request
	if err := ctx.BodyParser(&req); err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": "cannot parse JSON"})
	}
	shortURL, err := c.service.Shorten(req.URL, req.RequestedURL)
	if err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": err.Error()})
	}
	return ctx.JSON(fiber.Map{"short_url": shortURL})
}

func (c *URLController) Resolve(ctx *fiber.Ctx) error {
	shortURL := ctx.Params("shortURL")
	originalURL, err := c.service.Resolve(shortURL)
	if err != nil {
		return ctx.Status(fiber.StatusNotFound).JSON(fiber.Map{"error": err.Error()})
	}
	return ctx.Redirect(originalURL)
}
