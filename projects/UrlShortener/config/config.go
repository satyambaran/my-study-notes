package config

import "os"

type Config struct {
	DBUrl         string
	RedisUrl      string
	RedisPassword string
	BASEURL       string
}

func Load() *Config {
	return &Config{
		DBUrl:         os.Getenv("DB_URL"),
		RedisUrl:      os.Getenv("REDIS_URL"),
		RedisPassword: os.Getenv("REDIS_PASSWORD"),
		BASEURL:       os.Getenv("BASE_URL"),
	}
}
