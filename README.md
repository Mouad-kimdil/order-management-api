# Order Management API

A RESTful backend for a small storefront built with Spring Boot 3: products, categories, and
user-owned orders, secured with JWT authentication and role-based + owner-scoped authorization.

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.14**
- **Spring MVC**, **Spring Data JPA** (Hibernate)
- **Spring Security** (stateless JWT) with **jjwt 0.12.6**
- **Spring Validation**
- **PostgreSQL** (production) / **H2** (testing)
- **Maven**

## Features

- JWT authentication: `register`, `login`, `refresh`
- Refresh-token rotation with reuse detection, hashed storage, and automatic cleanup
- Role-based authorization: admin-only product/category writes (`@PreAuthorize`)
- Owner-scoped orders: users only see their own orders; non-owner and missing reads return a
  uniform `403` (no existence leak); admins see everything and get a truthful `404`
- Order line items using a snapshot pattern (`sku`, `name`, `unitPrice` copied from the product
  at order time, so later product edits don't rewrite order history)
- Stock-safe order creation: insufficient stock → `409`; a `@Version` optimistic-lock column on
  `product` catches concurrent orders on the same product (lost race → `409`)
- Uniform error responses via `@RestControllerAdvice` (401 for missing/invalid tokens, 404 for
  unknown routes, etc.)

## Getting Started

### Prerequisites

- Java 21+
- PostgreSQL running on `localhost:5432`
- Maven

### Configuration

Set the database password via environment variable:

```shell
export DB_PASSWORD=your_password
```

Default connection settings (overridable in `application.properties`):
- URL: `jdbc:postgresql://localhost:5432/product_db`
- Username: `product_user`

JWT settings (in `application.properties`): `app.jwt.secret`, `app.jwt.expiration-ms`
(access token TTL), `app.jwt.refresh-expiration-ms` (refresh token TTL).

### Run

```shell
./mvnw spring-boot:run
```

### Tests

```shell
./mvnw test
```

## API Endpoints

### Auth

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/auth/register` | Create a user (role `USER`) | Public |
| POST | `/api/v1/auth/login` | Authenticate, returns access + refresh tokens | Public |
| POST | `/api/v1/auth/refresh` | Rotate a refresh token (single use) | Public |

### Health

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/v1/health` | Health check | Public |

### Products

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/products` | Create a product | Admin |
| GET | `/api/v1/products` | List products (paginated) | Authenticated |
| GET | `/api/v1/products/{id}` | Get product by ID | Authenticated |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU | Authenticated |
| GET | `/api/v1/products/summary` | Get product summaries (paginated) | Authenticated |
| PUT | `/api/v1/products/{id}` | Update a product | Admin |
| DELETE | `/api/v1/products/{id}` | Delete a product | Admin |

### Categories

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/category` | Create a category | Admin |
| GET | `/api/v1/category` | List all categories | Authenticated |
| GET | `/api/v1/category/{id}` | Get category with products | Authenticated |
| DELETE | `/api/v1/category/{id}` | Delete a category | Admin |

### Orders

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/orders` | Create an order (owned by the caller) | Authenticated |
| GET | `/api/v1/orders` | List own orders (admins: all orders) | Authenticated |
| GET | `/api/v1/orders/{id}` | Get own order (admins: any) | Authenticated |

Creating an order snapshots each item's `sku`, `name`, and `unitPrice` from the product,
decrements stock in the same transaction, and rejects requests that exceed available stock with
`409`. Concurrent updates to the same product are caught by the `@Version` optimistic-lock column
(also surfaced as `409`).

## Security Model

- Requests are stateless: a valid `Authorization: Bearer <jwt>` header is required on everything
  except `register`, `login`, and `refresh`.
- Access tokens carry `sub` (username), `id` (user id), `role`, `type`, and a unique `jti`;
  the user's role is always re-read from the database (never trusted from the token).
- Refresh tokens are stored hashed (SHA-256); each use rotates the token and revokes the old one.
  Reusing a revoked token revokes the whole token family, forcing a re-login.
- Orders are user-owned resources: access is decided by comparing the caller (from the security
  context) with the order's owner, never from any request body.
