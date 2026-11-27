# StockMgmtAPI

Spring Boot inventory API with PostgreSQL and optimistic locking, so two concurrent purchases of the same product cannot silently overwrite each other.

## What it does

The API stores products (name, quantity, and price) in PostgreSQL and exposes two endpoints under `/api/inventory`. The `Product` entity has a Hibernate `@Version` field. When two clients try to buy the last remaining unit at the same time, the second update fails with `OptimisticLockingFailureException`, and the API returns HTTP 409 Conflict.

## Tech stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA and Hibernate
- PostgreSQL 16
- Docker Compose
- Maven
- Lombok

## API

Base URL: `http://localhost:8080`

### Create a product

`POST /api/inventory/products`

Request body (JSON):

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | yes | Product name |
| `quantity` | integer | yes | Units in stock |
| `price` | number | yes | Stored as `BigDecimal` |
| `id` | long | no | Generated on insert |
| `version` | long | no | Optimistic-lock field, managed by Hibernate |

Example:

```bash
curl -X POST http://localhost:8080/api/inventory/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","quantity":10,"price":4999.90}'
```

The response is the persisted `Product`, including `id` and `version`.

### Buy one unit of a product

`POST /api/inventory/buy/{id}`

This always purchases **one** unit of the product with the given `id`. The controller calls `InventoryService.processPurchase(id, 1)`; the quantity is not taken from the path or body.

Example:

```bash
curl -X POST http://localhost:8080/api/inventory/buy/1
```

Responses:

- `200 OK` — `Purchase Successful!`
- `409 Conflict` — `Race condition detected! Please try again.` (optimistic lock failure)
- `400 Bad Request` — other errors, including `Product not found` and `Not enough stock!`

There is no list, get-by-id, or update endpoint yet.

## Data model

`Product` maps to the `products` table:

- `id` — `Long`, identity primary key
- `name` — `String`, not null
- `quantity` — `Integer`, not null
- `price` — `BigDecimal`, not null
- `version` — `Long`, `@Version` for optimistic locking

`InventoryService.processPurchase` loads the product, checks that stock is sufficient, decrements `quantity`, and saves. The method is `@Transactional`.

## Running locally

### 1. Start PostgreSQL

```bash
cp .env.example .env
docker compose up -d
```

Postgres 16 listens on host port **5433** so it does not clash with a local Postgres on 5432. The defaults, which also work without a `.env` file, are:

- user: `admin`
- password: `password123`
- database: `inventory_service`

Override them in `.env` if you want. Keep the Compose and Spring values in sync.

### 2. Run the API

```bash
./mvnw spring-boot:run
```

The app connects to `jdbc:postgresql://localhost:5433/inventory_service` by default. You can override `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.

## Configuration

| Variable | Default | Used by |
| --- | --- | --- |
| `POSTGRES_USER` | `admin` | Docker Compose |
| `POSTGRES_PASSWORD` | `password123` | Docker Compose |
| `POSTGRES_DB` | `inventory_service` | Docker Compose |
| `POSTGRES_PORT` | `5433` | Docker Compose host port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/inventory_service` | Spring Boot |
| `SPRING_DATASOURCE_USERNAME` | `admin` | Spring Boot |
| `SPRING_DATASOURCE_PASSWORD` | `password123` | Spring Boot |

Change the password in `.env` for anything other than local development.

## Tests and CI

`src/test` currently has a Spring Boot `contextLoads` test, which needs a running PostgreSQL instance with the same credentials as `application.properties`.

GitHub Actions runs `mvn -B package` on Java 17 (Temurin) with a PostgreSQL 16 service mapped to port 5433.

```bash
./mvnw test
```

## License

MIT License. See [LICENSE](LICENSE).
