#!/bin/bash
echo "Stopping existing containers..."
docker compose down

echo "Starting db and redis ..."
docker compose up -d db redis

echo "Waiting for postgres to be ready..."
until docker exec db pg_isready -U postgres > /dev/null 2>&1; do
    sleep 1
done

echo "Starting Go server..."
go run main.go
