# Ktor ChatX backend

## ENV

- `JWT_SECRET` - JWT secret key (default: `SECRET`)
- `JWT_DOMAIN` - JWT domain (default: `https://jwt-provider-domain/`)
- `USE_IN_MEMORY_DATABASE` - flag indicates that server uses h2 database (default: `true`)
- `DB_URL` - external db jdbc url (null if `USE_IN_MEMORY_DATABASE`)
- `DB_USER` - external db user (null if `USE_IN_MEMORY_DATABASE`)
- `DB_PASSWORD` - external db pass (null if `USE_IN_MEMORY_DATABASE`)
- `PORT` - app internal port (default: `8080`)

## Local startup

1. `./gradlew run` - starts server with default port (`8080`) and in memory database

## Docker startup

1. `docker build --tag 'chatx-ktor-backend' .`

2. `docker run --name 'ChatX-Backend' -d -p '8090:8080' 'chatx-ktor-backend''`

Container starts with port `8090`