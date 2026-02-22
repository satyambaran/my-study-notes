#!/bin/bash
set -e

echo "Building and starting all services..."
docker compose up --build
