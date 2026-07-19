# AirCargo Docker

## Infrastructure (DB + RabbitMQ)
docker compose -f docker-compose.infrastructure.yml up -d

## All services
docker compose -f docker-compose.infrastructure.yml -f docker-compose.services.yml up -d
