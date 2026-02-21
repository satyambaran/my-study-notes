package model

type URL struct {
	ID          uint   `gorm:"primaryKey;autoIncrement"`
	ShortURL    string `gorm:"unique;not null"`
	OriginalURL string `gorm:"not null"`
}
