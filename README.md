# AutoFlow

[![CI](https://github.com/DITT-BRUKERNAVN/AutoFlow/actions/workflows/ci.yml/badge.svg)](https://github.com/DITT-BRUKERNAVN/AutoFlow/actions/workflows/ci.yml)

AutoFlow is a digital workshop system for car repair shops. It will eventually manage
customers, vehicles, work orders, service history, parts, bookings, employees and
invoicing data. This repository contains the backend REST API.

> Current status: **first slice** — the Customer domain, with database, validation,
> error handling, tests and Docker. Vehicles and work orders come next.

## Technology stack

- Java 21
- Spring Boot 3.4
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Web
- Spring Validation (Jakarta Validation)
- Flyway (database migrations)
- Lombok
- JUnit 5, Mockito, Testcontainers
- Docker & Docker Compose
- Actuator (health/info only)

Spring Security is intentionally **not** included yet.

## Project structure

The project is organised **by feature/domain** instead of by technical layer. Each
feature package holds its own controller, service, repository, entity, DTOs and mapper.
This keeps related code together and scales better as new domains are added.

```text
com.autoflow
├── AutoflowApplication.java
├── common
│   ├── config              # JPA auditing configuration
│   └── exception           # custom exceptions + global handler + ApiError
└── customer
    ├── Customer.java        # JPA entity (never exposed via the API)
    ├── CustomerRepository.java
    ├── CustomerService.java
    ├── CustomerController.java
    ├── CustomerMapper.java
    └── dto                  # CreateCustomerRequest / UpdateCustomerRequest / CustomerResponse
```

## Requirements

- JDK 21
- Docker & Docker Compose (for the database and for running Testcontainers tests)
- Maven (or use your IDE's bundled Maven)

## Environment variables

Copy the example file and adjust the values. The real `.env` file is git-ignored.

```bash
cp .env.example .env
```

| Variable            | Description       | Example    |
|---------------------|-------------------|------------|
| `POSTGRES_DB`       | Database name     | `autoflow` |
| `POSTGRES_USER`     | Database user     | `autoflow` |
| `POSTGRES_PASSWORD` | Database password | `change_me`|

## Starting the database

Start only PostgreSQL and run the app from your IDE/terminal:

```bash
docker compose up -d db
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

## Starting the whole stack (database + application)

```bash
docker compose up --build
```

The API is then available at http://localhost:8080.

Health check: http://localhost:8080/actuator/health

## Running the tests

Tests use a real PostgreSQL database through Testcontainers, so Docker must be running.

```bash
mvn test
```

## API endpoints (Customer)

| Method | Path                        | Description              |
|--------|-----------------------------|--------------------------|
| POST   | `/api/v1/customers`         | Create a customer        |
| GET    | `/api/v1/customers`         | List all customers       |
| GET    | `/api/v1/customers/{id}`    | Get one customer         |
| PUT    | `/api/v1/customers/{id}`    | Update a customer        |
| DELETE | `/api/v1/customers/{id}`    | Delete a customer        |

### Example request

```bash
curl -i -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
        "firstName": "Ola",
        "lastName": "Nordmann",
        "phoneNumber": "12345678",
        "email": "ola@example.com",
        "address": "Storgata 1",
        "postalCode": "0155",
        "city": "Oslo"
      }'
```

### Example error response

```json
{
  "timestamp": "2026-07-25T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer with id 10 was not found",
  "path": "/api/v1/customers/10",
  "validationErrors": []
}
```

## Planned features

- Vehicle domain (`/api/v1/customers/{customerId}/vehicles`, `/api/v1/vehicles`)
- Work order domain with status/priority workflow
- Service history, parts, bookings, employees, invoicing
- Authentication and authorization (Spring Security)
