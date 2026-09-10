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
ProductService (interface)
 │
 ▼
ProductServiceImpl
 │
 ▼
ProductRepository
 │
 ▼
Database
```

The code is split into layered packages by technical concern: `controller`, `service`, `repository`, `entity`, `dto`, `exception`, and `constants`.

### Responsibilities

**Controller** (`controller`)
- HTTP contract
- request validation
- HTTP status codes
- pagination parameters

**Service** (`service`)
- `ProductService` — the contract the controller depends on (dependency inversion; no implementation details leak into the controller)
- `ProductServiceImpl` — business/use-case orchestration, transaction boundary, resource existence rules

**Entity** (`entity`)
- `Product` entity and behavior
- `ProductStatus`
- no HTTP concerns

**Repository** (`repository`)
- persistence abstraction (Spring Data JPA)
- no business logic

**DTO** (`dto`)
- `ProductRequest` / `ProductResponse` — the public API contract, separate from the JPA entity

**Exception Handler** (`exception`)
- consistent API error contract
- maps domain/application/framework errors to HTTP responses

**Constants** (`constants`)
- `ApiConstants` — pagination defaults/limits
- `ProductConstants` — field-level constraints (name length, price precision/digits)
- `ValidationConstants` — reserved for future validation-only constants

## SOLID

- **Single Responsibility** — each class has one reason to change.
- **Open/Closed** — new persistence implementations or application policies can be introduced without changing the controller contract.
- **Liskov Substitution** — the controller depends on the `ProductService` abstraction, so any conforming implementation (e.g. `ProductServiceImpl`) can be substituted without changing calling code.
- **Interface Segregation** — `ProductRepository` only exposes catalog persistence operations; `ProductService` only exposes catalog use cases the controller needs.
- **Dependency Inversion** — `ProductController` depends on the `ProductService` interface, not `ProductServiceImpl`; `ProductServiceImpl` depends on the `ProductRepository` abstraction, while Spring Data supplies the implementation.

## Design Patterns

- **Service Layer** — `ProductService`/`ProductServiceImpl` encapsulate business use cases behind an interface.
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
| List | `GET /api/v1/products` | 200 OK (non-empty) / 204 No Content (empty) |
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

If the catalog (or the filtered result set) is empty, the endpoint returns `204 No Content` with no body instead of `200 OK` with an empty page.

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
- `status`: required (`ACTIVE`, `INACTIVE` — see `ProductStatus`)
- `page`: `>= 0`
- `size`: `1–100`
- `direction`: `asc`/`desc`

### Persistence constraints (`Product` entity)
- `id`, `name`, `price`, `status` are non-null
- database indexes on `name` and `status`

## Error Contract

All errors are handled centrally by `GlobalExceptionHandler` and returned as `ApiError` — including bean-validation failures, an unknown `sortBy` field, a malformed JSON body, framework-level parameter validation (e.g. `size` above the max), and any unexpected exception (mapped to `500` with a safe generic message). No request failure falls through to Spring Boot's default error page.

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

- `ProductServiceTest` — unit tests for `ProductServiceImpl` (Mockito)
- `ProductControllerTest` — `@SpringBootTest` + MockMvc integration tests for the HTTP layer

### `ProductServiceTest`

| Test | Verifies |
|---|---|
| `shouldCreateProductAndNormalizeName` | create() normalizes whitespace in `name` and persists the product |
| `shouldUpdateExistingProduct` | update() applies new name/price/status to an existing product |
| `shouldThrow404WhenProductDoesNotExist` | getById() throws `ProductNotFoundException` for an unknown id |
| `shouldNotDeleteUnknownProduct` | delete() throws `ProductNotFoundException` and never calls `repository.delete()` for an unknown id |

### `ProductControllerTest`

| Test | Verifies |
|---|---|
| `shouldCreateGetUpdateAndDeleteProduct` | Full CRUD lifecycle: 201 on create, 200 on get/update, 204 on delete, 404 on get after delete |
| `shouldRejectBlankName` | Blank `name` → 400 with `fieldErrors.name` |
| `shouldRejectNullNamePriceAndStatus` | `name`, `price`, `status` all `null` → 400 with all three field errors |
| `shouldRejectZeroPrice` | `price = 0` → 400 with `fieldErrors.price` |
| `shouldRejectNegativePrice` | `price < 0` → 400 with `fieldErrors.price` |
| `shouldReturn404ForUnknownProductOnGet` | GET unknown id → 404 |
| `shouldReturn404WhenUpdatingUnknownProduct` | PUT unknown id → 404 |
| `shouldReturn404WhenDeletingUnknownProduct` | DELETE unknown id → 404 |
| `shouldReturnNoContentWhenListIsEmpty` | GET list with no products → 204 No Content |
| `shouldSupportPagination` | GET list with `page`/`size`/`sortBy`/`direction` → 200 with the requested page size |
| `shouldRejectInvalidSortField` | Unknown `sortBy` value → 400 (not an unhandled 500) with `fieldErrors.sortBy` |
| `shouldRejectPageSizeAboveMaximum` | `size` above the max (100) → 400 |
| `shouldRejectMalformedJsonBody` | Unparseable JSON body → 400 |

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
product-catalog-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/infobean/productcatalog/
│   │   │       │
│   │   │       ├── ProductCatalogApplication.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── ProductController.java
│   │   │       │   └── ApiPaths.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ProductService.java        # interface — controller depends on this
│   │   │       │   └── ProductServiceImpl.java     # implementation
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── ProductRepository.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── Product.java
│   │   │       │   └── ProductStatus.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── ProductRequest.java
│   │   │       │   └── ProductResponse.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── ApiError.java
│   │   │       │   ├── ErrorMessages.java
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ProductNotFoundException.java
│   │   │       │
│   │   │       └── constants/
│   │   │           ├── ApiConstants.java           # pagination defaults/limits
│   │   │           ├── ProductConstants.java        # field-level constraints
│   │   │           └── ValidationConstants.java      # reserved for future use
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│       └── java/
│           └── com/infobean/productcatalog/
│               ├── controller/
│               │   └── ProductControllerTest.java
│               └── service/
│                   └── ProductServiceTest.java
│
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```
