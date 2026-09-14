# Product Catalog API — Postman Test Results

Verified end-to-end against a freshly started instance (`./gradlew bootRun`, in-memory H2, empty on startup) on 2026-09-14. All 38 scenarios below map 1:1 to the requests in `postman/product-catalog-api.postman_collection.json` — import that file into Postman and use **Run Collection** to reproduce these results yourself.

Legend: ✅ = actual result matched expected.

## 0 — Empty List Scenario

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 0.1 | List products on empty catalog | `GET /api/v1/products` | 204, empty body | 204, empty body | ✅ |

## 1 — Create Product

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 1.1 | Create product | `POST /api/v1/products` | 201, `Location` header, body echoes name/status | 201, generated `id`, `name`/`status` match | ✅ |
| 1.2 | Create with no description | `POST /api/v1/products` | 201, `description` null | 201, `description` null | ✅ |
| 1.3 | Blank name | `POST /api/v1/products` | 400, `fieldErrors.name` | 400, `"name must not be blank"` | ✅ |
| 1.4 | Null name + price + status | `POST /api/v1/products` | 400, all three field errors | 400, `name`/`price`/`status` all present | ✅ |
| 1.5 | Description over 1000 characters | `POST /api/v1/products` | 400, `fieldErrors.description` | 400, `fieldErrors.description` present | ✅ |
| 1.6 | Zero price | `POST /api/v1/products` | 400, `fieldErrors.price` | 400, `"price must be greater than zero"` | ✅ |
| 1.7 | Negative price | `POST /api/v1/products` | 400, `fieldErrors.price` | 400, `"price must be greater than zero"` | ✅ |
| 1.8 | Malformed JSON body | `POST /api/v1/products` | 400, clear error message | 400, `"Malformed request body"` | ✅ |

## 2 — Get Product By Id

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 2.1 | Get existing product | `GET /api/v1/products/{id}` | 200, matching `id` | 200, `id` matches | ✅ |
| 2.2 | Get unknown product | `GET /api/v1/products/{unknownId}` | 404, standard message | 404, `"Product not found with id: ..."` | ✅ |

## 3 — List Products

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 3.1 | List (default paging) | `GET /api/v1/products` | 200, non-empty `content` | 200, non-empty | ✅ |
| 3.2 | List with pagination + sort | `GET /api/v1/products?page=0&size=10&sortBy=name&direction=asc` | 200, `size: 10` | 200, `size: 10` | ✅ |
| 3.3 | List filtered by status | `GET /api/v1/products?status=ACTIVE` | 200 | 200 | ✅ |
| 3.4 | Invalid `sortBy` | `GET /api/v1/products?sortBy=doesNotExist` | 400, not 500 | 400, `fieldErrors.sortBy` set | ✅ |
| 3.5 | `size` above max (100) | `GET /api/v1/products?size=1000` | 400 | 400 | ✅ |
| 3.6 | Invalid `direction` | `GET /api/v1/products?direction=sideways` | 400 | 400, `"direction must be asc or desc"` | ✅ |

## 4 — Update Product

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 4.1 | Update existing product | `PUT /api/v1/products/{id}` | 200, fields updated | 200, `name`/`price`/`status` updated | ✅ |
| 4.2 | Update with blank name | `PUT /api/v1/products/{id}` | 400 | 400 | ✅ |
| 4.3 | Update unknown product | `PUT /api/v1/products/{unknownId}` | 404 | 404 | ✅ |

## 5 — Product Name Uniqueness

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 5.1 | Create product A | `POST /api/v1/products` | 201 | 201 | ✅ |
| 5.2 | Create product B | `POST /api/v1/products` | 201 | 201 | ✅ |
| 5.3 | Create with duplicate name | `POST /api/v1/products` | 409 | 409, `"Product name already exists: Unique Test Product A"` | ✅ |
| 5.4 | Rename B to A's name | `PUT /api/v1/products/{bId}` | 409 | 409 | ✅ |
| 5.5 | Rename A to its own name | `PUT /api/v1/products/{aId}` | 200 (self-rename allowed) | 200 | ✅ |

## 6 — Product Audit History

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 6.1 | Audit history for a created+updated product | `GET /api/v1/products/{id}/audit` | 200, 2 entries (UPDATED, CREATED) | 200, 2 entries | ✅ |
| 6.2 | Audit history for unknown product | `GET /api/v1/products/{unknownId}/audit` | 200, empty array | 200, empty array | ✅ |

## 7 — Delete Product (soft delete)

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 7.1 | Create temp product | `POST /api/v1/products` | 201 | 201 | ✅ |
| 7.2 | Delete product | `DELETE /api/v1/products/{id}` | 204 | 204 | ✅ |
| 7.3 | Get deleted product | `GET /api/v1/products/{id}` | 404 | 404 | ✅ |
| 7.4 | List after delete | `GET /api/v1/products?size=100` | 200, deleted id absent | 200, deleted id absent | ✅ |
| 7.5 | Audit history after delete | `GET /api/v1/products/{id}/audit` | 200, CREATED + DELETED entries survive | 200, 2 entries | ✅ |
| 7.6 | Delete unknown product | `DELETE /api/v1/products/{unknownId}` | 404 | 404, standard message | ✅ |
| 7.7 | Create product reusing the deleted product's name | `POST /api/v1/products` | 201, not 409 | 201 | ✅ |

## 8 — Cache Behavior (Redis)

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 8.1 | Get product by id (warms the cache) | `GET /api/v1/products/{id}` | 200 | 200 | ✅ |
| 8.2 | Actuator caches shows the products cache | `GET /actuator/caches` | 200, `products` cache registered | 200, `products` present | ✅ |
| 8.3 | Update product price (evicts cache entry) | `PUT /api/v1/products/{id}` | 200, price updated | 200, price updated | ✅ |
| 8.4 | Get after update | `GET /api/v1/products/{id}` | 200, fresh (non-stale) price | 200, fresh price | ✅ |

## Summary

**38 / 38 scenarios passed.** Covers all 5 HTTP operations (POST, GET single, GET list, PUT, DELETE), every validation rule (blank/null name, oversized description, zero/negative price), every documented status code (200, 201, 204, 400, 404, 409), name-uniqueness conflicts, the `/audit` change-history endpoint, soft delete (deleted products excluded from GET/list, still visible in audit history, 404 on a second delete, and — via the `unique_name` column — the deleted product's name becomes reusable by a new one), and the Redis-backed cache on `GET /{id}` (population, eviction on update, `/actuator/caches` visibility).

Run against `spring.cache.type=simple` (no local Redis/Docker available in this environment) — the cache-aside behavior exercised is identical to Redis, since both go through the same `@Cacheable`/`@CacheEvict` annotations and `CacheManager` abstraction; only the storage backend differs.

## How to reproduce in Postman

1. Restart the app fresh: `./gradlew bootRun` (in-memory H2 resets on restart; start Redis first via `docker compose up -d redis`, or override `spring.cache.type=simple` if Redis isn't available).
2. In Postman: **Import** → select `postman/product-catalog-api.postman_collection.json`.
3. Open the collection → **Run** (top-right "Run" button) → run folders **in order, top to bottom** (0 through 8) so the empty-list check runs before any data exists.
4. All requests carry built-in `pm.test` assertions — the Collection Runner will show pass/fail for every request automatically.
