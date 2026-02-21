
echo "Stopping existing containers..."
docker compose down

echo "Stopping Go server..."
kill -9 $(lsof -t -i :3000)

echo "All services stopped."