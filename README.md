# Product Catalog API

A production-style REST API for managing a product catalog, built to demonstrate clean architecture, SOLID principles, REST semantics, validation, persistence abstraction, transaction boundaries, centralized error handling, pagination, and automated testing.

## Stack

- Java 25
- Spring Boot 4.1.1
- Gradle (wrapper included)
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 (in-memory, local/test persistence)
- Spring Boot Actuator
- JUnit 5, Mockito, MockMvc

## Prerequisites

- JDK 25 (or let the Gradle toolchain resolve/download it)
- No local Gradle install required — use the included wrapper (`gradlew` / `gradlew.bat`)

## Quick Start

```bash
# run tests
./gradlew test

# run the application
./gradlew bootRun
```

On Windows PowerShell, use `gradlew.bat` instead of `./gradlew`.

The API is available at:

```text
http://localhost:8080
```

H2 console (JDBC URL `jdbc:h2:mem:productdb`, user `sa`, empty password):

```text
http://localhost:8080/h2-console
```

## Architecture

```text
HTTP
 │
 ▼
ProductController
 │
 ▼
ProductService
 │
 ▼
ProductRepository
 │
 ▼
Database
```

The code is split into API, application, domain, and shared exception concerns.

### Responsibilities

**Controller** (`product/api`)
- HTTP contract
- request validation
- HTTP status codes
- pagination parameters

**Service** (`product/application`)
- business/use-case orchestration
- transaction boundary
- resource existence rules

**Domain** (`product/domain`)
- `Product` entity and behavior
- `ProductStatus`
- no HTTP concerns

**Repository** (`product/application`)
- persistence abstraction (Spring Data JPA)
- no business logic

**Exception Handler** (`shared/exception`)
- consistent API error contract
- maps domain/application errors to HTTP responses

## SOLID

- **Single Responsibility** — each class has one reason to change.
- **Open/Closed** — new persistence implementations or application policies can be introduced without changing the controller contract.
- **Liskov Substitution** — the service depends on the repository abstraction rather than concrete persistence behavior.
- **Interface Segregation** — `ProductRepository` only exposes catalog persistence operations.
- **Dependency Inversion** — the application layer depends on `ProductRepository` (an abstraction), while Spring Data supplies the implementation.

## Design Patterns

- **Service Layer** — encapsulates business use cases.
- **Repository Pattern** — separates persistence from business logic.
- **DTO Pattern** — `ProductRequest`/`ProductResponse` prevent exposing the JPA entity as the public API contract.
- **Factory Method** — `Product.create(...)` centralizes valid domain object creation.
- **Strategy / Policy Extension Point** — the service is structured so pricing, validation, or catalog policies can later be extracted behind interfaces instead of growing the controller.

## API Reference

Base path: `/api/v1/products`

| Operation | Method & Path | Success Status |
|---|---|---|
| Create | `POST /api/v1/products` | 201 Created |
| Read | `GET /api/v1/products/{id}` | 200 OK |
| List | `GET /api/v1/products` | 200 OK |
| Update | `PUT /api/v1/products/{id}` | 200 OK |
| Delete | `DELETE /api/v1/products/{id}` | 204 No Content |

### Create a product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","price":19.99,"status":"ACTIVE"}'
```

Response `201 Created` (with `Location: /api/v1/products/{id}`):

```json
{
  "id": "e2f1a2b0-1234-4a5b-8cde-1234567890ab",
  "name": "Wireless Mouse",
  "price": 19.99,
  "status": "ACTIVE"
}
```

### List products (paginated)

```text
GET /api/v1/products?page=0&size=20&sortBy=name&direction=asc
```

Optional status filter:

```text
GET /api/v1/products?status=ACTIVE&page=0&size=20
```

Pagination parameters:

| Param | Default | Constraints |
|---|---|---|
| `page` | `0` | `>= 0` |
| `size` | `20` | `1–100` |
| `sortBy` | `name` | any `Product` field |
| `direction` | `asc` | `asc` or `desc` |

Maximum page size is capped at 100 to avoid accidental large queries.

### Update a product

```bash
curl -X PUT http://localhost:8080/api/v1/products/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","price":17.99,"status":"ACTIVE"}'
```

### Delete a product

```bash
curl -X DELETE http://localhost:8080/api/v1/products/{id}
```

## Validation

### Request validation (`ProductRequest`)
- `name`: required, non-blank, max 200 characters
- `price`: required, greater than zero
- `price`: maximum 15 integer digits and 4 decimal places
- `status`: required (`ACTIVE`, `INACTIVE`, `DISCONTINUED` — see `ProductStatus`)
- `page`: `>= 0`
- `size`: `1–100`
- `direction`: `asc`/`desc`

### Persistence constraints (`Product` entity)
- `id`, `name`, `price`, `status` are non-null
- database indexes on `name` and `status`

## Error Contract

All errors are handled centrally by `GlobalExceptionHandler` and returned as `ApiError`.

Validation error (`400 Bad Request`):

```json
{
  "timestamp": "2026-09-08T07:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "fieldErrors": {
    "name": "name must not be blank",
    "price": "price must be greater than zero"
  }
}
```

Not found (`404 Not Found`):

```json
{
  "timestamp": "2026-09-08T07:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: ...",
  "path": "/api/v1/products/...",
  "fieldErrors": {}
}
```

## Idempotency

`PUT` is designed as an idempotent operation: sending the same complete representation multiple times produces the same final resource state.

`DELETE` is also modeled around the final state: after a successful deletion, the resource is absent.

`POST` is intentionally not treated as inherently idempotent. A production system would typically add an `Idempotency-Key` header with a persistent idempotency store and a unique constraint, rather than an in-memory map (which fails with multiple application instances):

```text
POST + Idempotency-Key
        ↓
Idempotency store
        ↓
If key exists → return stored response
If key is new → execute request and persist response
```

## Configuration

See `src/main/resources/application.yml`:

- H2 in-memory database (`jdbc:h2:mem:productdb`), schema created via `ddl-auto: create-drop`
- H2 console enabled at `/h2-console`
- Server port `8080`, graceful shutdown
- Actuator endpoints exposed: `health`, `info`, `metrics`

## Testing

```bash
./gradlew test
```

- `ProductServiceTest` — unit tests for the service layer (Mockito)
- `ProductControllerIntegrationTest` — MockMvc-based integration tests for the HTTP layer

## Production Hardening Roadmap

For an actual production deployment, consider:

1. PostgreSQL instead of H2.
2. Flyway/Liquibase database migrations.
3. Optimistic locking with `@Version`.
4. `Idempotency-Key` support for `POST`.
5. API authentication/authorization with OAuth2/JWT.
6. OpenAPI/Swagger documentation.
7. Correlation/request ID propagation.
8. Structured JSON logging.
9. Micrometer metrics and distributed tracing.
10. Rate limiting.
11. Resilience patterns where external dependencies exist.
12. Redis caching for high-read catalog endpoints.
13. Outbox pattern if catalog changes publish Kafka events.
14. Contract/integration tests using Testcontainers.
15. CI pipeline with checkstyle/spotbugs/dependency scanning/tests.
16. Secrets sourced from environment/secret manager.
17. PostgreSQL indexes based on actual query patterns.

### Concurrency

For concurrent updates, the production version should add optimistic locking:

```java
@Version
private Long version;
```

Then stale updates fail instead of silently overwriting newer changes.

### System Design at Scale

```text
                 API Gateway
                      |
              Load Balancer
                      |
        +-------------+-------------+
        |             |             |
   Catalog API   Catalog API   Catalog API
        |             |             |
        +-------------+-------------+
                      |
                PostgreSQL
                      |
              Redis Cache
                      |
                   Kafka
                      |
       +--------------+--------------+
       |                             |
 Search Index                 Other Services
```

Read-heavy catalog traffic can use Redis caching, with the database as the source of truth. Kafka can publish product-created/product-updated/product-deleted events when eventual consistency is acceptable.

## Project Structure

```text
src/main/java/com/infobean/productcatalog/
├── ProductCatalogApplication.java
├── product/
│   ├── api/            # controller, pagination defaults
│   ├── application/    # service, repository interface, request/response DTOs
│   └── domain/         # entity, status enum, constraints
└── shared/
    ├── constants/      # API path constants
    └── exception/      # global exception handler, ApiError, error messages
```
