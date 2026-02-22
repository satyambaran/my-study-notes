#!/bin/bash

echo "Stopping all services..."

if [ "$1" == "--fresh-redis" ]; then
    # Removes containers AND the redis-data volume.
    # On next start, Redis has no AOF file → redis-init re-seeds the counter.
    echo "Wiping Redis volume for a clean restart..."
    docker compose down -v
else
    # Normal stop: keeps the redis-data volume so the counter survives.
    docker compose down
fi

# docker image prune -a # Uncomment this line if you want to remove all unused images after stopping the services

echo "All services stopped."
