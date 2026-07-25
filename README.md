# Order Management API

A RESTful API for managing products and categories built with Spring Boot 3.

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.14**
- **Spring Data JPA**
- **Spring Validation**
- **PostgreSQL** (production) / **H2** (testing)
- **Maven**

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

### Run

```shell
./mvnw spring-boot:run
```

### Tests

```shell
./mvnw test
```

## API Endpoints

### Health

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/health` | Health check |

### Products

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/products` | Create a product |
| GET | `/api/v1/products` | List products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU |
| GET | `/api/v1/products/summary` | Get product summaries (paginated) |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Delete a product |

### Categories

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/category` | Create a category |
| GET | `/api/v1/category` | List all categories |
| GET | `/api/v1/category/{id}` | Get category with products |
| DELETE | `/api/v1/category/{id}` | Delete a category |
