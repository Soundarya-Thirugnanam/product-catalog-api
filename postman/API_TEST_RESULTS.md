# Product Catalog API — Postman Test Results

Verified end-to-end against a freshly started instance (`./gradlew bootRun`, in-memory H2, empty on startup) on 2026-09-10. All 21 scenarios below map 1:1 to the requests in `postman/product-catalog-api.postman_collection.json` — import that file into Postman and use **Run Collection** to reproduce these results yourself.

Legend: ✅ = actual result matched expected.

## 0 — Empty List Scenario

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 0.1 | List products on empty catalog | `GET /api/v1/products` | 204, empty body | 204, empty body | ✅ |

## 1 — Create Product

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 1.1 | Create product | `POST /api/v1/products` | 201, `Location` header, body echoes name/status | 201, generated `id`, `name`/`status` match | ✅ |
| 1.2 | Blank name | `POST /api/v1/products` | 400, `fieldErrors.name` | 400, `"name must not be blank"` | ✅ |
| 1.3 | Null name + price + status | `POST /api/v1/products` | 400, all three field errors | 400, `name`/`price`/`status` all present | ✅ |
| 1.4 | Zero price | `POST /api/v1/products` | 400, `fieldErrors.price` | 400, `"price must be greater than zero"` | ✅ |
| 1.5 | Negative price | `POST /api/v1/products` | 400, `fieldErrors.price` | 400, `"price must be greater than zero"` | ✅ |
| 1.6 | Malformed JSON body | `POST /api/v1/products` | 400, clear error message | 400, `"Malformed request body"` | ✅ |

## 2 — Get Product By Id

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 2.1 | Get existing product | `GET /api/v1/products/{id}` | 200, matching `id` | 200, `id` matches | ✅ |
| 2.2 | Get unknown product | `GET /api/v1/products/{unknownId}` | 404, standard message | 404, `"Product not found with id: ..."` | ✅ |

## 3 — List Products

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 3.1 | List (default paging) | `GET /api/v1/products` | 200, non-empty `content` | 200, 1 item | ✅ |
| 3.2 | List with pagination + sort | `GET /api/v1/products?page=0&size=10&sortBy=name&direction=asc` | 200, `size: 10` | 200, `size: 10` | ✅ |
| 3.3 | List filtered by status | `GET /api/v1/products?status=ACTIVE` | 200 | 200 | ✅ |
| 3.4 | Invalid `sortBy` | `GET /api/v1/products?sortBy=doesNotExist` | 400, not 500 | 400, `fieldErrors.sortBy` set | ✅ |
| 3.5 | `size` above max (100) | `GET /api/v1/products?size=1000` | 400 | 400, `"must be less than or equal to 100"` | ✅ |
| 3.6 | Invalid `direction` | `GET /api/v1/products?direction=sideways` | 400 | 400, `"direction must be asc or desc"` | ✅ |

## 4 — Update Product

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 4.1 | Update existing product | `PUT /api/v1/products/{id}` | 200, fields updated | 200, `name`/`status` updated | ✅ |
| 4.2 | Update with blank name | `PUT /api/v1/products/{id}` | 400 | 400, `"name must not be blank"` | ✅ |
| 4.3 | Update unknown product | `PUT /api/v1/products/{unknownId}` | 404 | 404, standard message | ✅ |

## 5 — Delete Product

| # | Scenario | Method & Path | Expected | Actual | Result |
|---|---|---|---|---|---|
| 5.1 | Create temp product | `POST /api/v1/products` | 201 | 201 | ✅ |
| 5.2 | Delete product | `DELETE /api/v1/products/{id}` | 204 | 204 | ✅ |
| 5.3 | Get deleted product | `GET /api/v1/products/{id}` | 404 | 404 | ✅ |
| 5.4 | Delete unknown product | `DELETE /api/v1/products/{unknownId}` | 404 | 404, standard message | ✅ |

## Summary

**21 / 21 scenarios passed.** Covers all 5 HTTP operations (POST, GET single, GET list, PUT, DELETE), every validation rule in the user story (blank/null name, zero/negative price), every documented status code (200, 201, 204, 400, 404), and the error-handling edge cases (invalid `sortBy`, oversized `size`, invalid `direction`, malformed JSON) that previously fell through to unhandled errors.

## How to reproduce in Postman

1. Restart the app fresh: `./gradlew bootRun` (in-memory H2 resets on restart).
2. In Postman: **Import** → select `postman/product-catalog-api.postman_collection.json`.
3. Open the collection → **Run** (top-right "Run" button) → run folders **in order, top to bottom** (0 through 5) so the empty-list check runs before any data exists.
4. All requests carry built-in `pm.test` assertions — the Collection Runner will show pass/fail for every request automatically.
