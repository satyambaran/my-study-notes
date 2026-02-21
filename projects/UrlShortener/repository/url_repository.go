package repository

import (
	"errors"

	"github.com/satyambaran/UrlShortener/model"
	"gorm.io/gorm"
)

type URLRepository interface {
	Create(url *model.URL) error
	FindByShortURL(shortURL string) (*model.URL, error)
	FindByOriginalURL(originalURL string) (*model.URL, error)
}

type urlRepository struct {
	db *gorm.DB
}

func NewURLRepository(db *gorm.DB) URLRepository {
	return &urlRepository{db: db}
}

func (r *urlRepository) Create(url *model.URL) error {
	return r.db.Create(url).Error
}

func (r *urlRepository) FindByShortURL(shortURL string) (*model.URL, error) {
	var url model.URL
	result := r.db.First(&url, "short_url = ?", shortURL)
	if errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil, gorm.ErrRecordNotFound
	}
	return &url, result.Error
}

func (r *urlRepository) FindByOriginalURL(originalURL string) (*model.URL, error) {
	var url model.URL
	result := r.db.First(&url, "original_url = ? AND is_custom_alias = false", originalURL)
	if errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil, gorm.ErrRecordNotFound
	}
	return &url, result.Error
}
