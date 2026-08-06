# RESTful Web Services (Spring Boot) — ToDo Application

## Overview
Spring Boot backend providing authentication (Basic + JWT) and ToDo CRUD APIs.

> **Needs input**: confirm which auth mode is the default for the frontend (Basic vs JWT) and whether both are actively used.

## Prerequisites
- JDK **14.0.1** (as per project root README)
- Maven Wrapper included (`./mvnw`)

## Run locally
```bash
cd restful-web-services
./mvnw clean install
./mvnw spring-boot:run
```
Backend runs on:
- http://localhost:8080

## Database (H2 In-Memory)
- H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`

> Note: In-memory DB resets when the app stops.

## Configuration
- Application config: `src/main/resources/application.properties`
- Seed data: `src/main/resources/data.sql`

> **Needs input**: document any environment variables or profiles used in CI/deploy.

## Tests
```bash
cd restful-web-services
./mvnw test
```

## Key packages / entry points
- Main app: `src/main/java/.../RestfulWebServicesApplication.java`
- JWT config: `src/main/java/.../jwt/`
- ToDo resources: `src/main/java/.../todo/`

## API surface (documentation placeholder)
> **Needs input**: confirm the canonical endpoint paths used by the frontend for:
> - login/auth
> - list todos
> - create/update/delete todo
> - hello-world endpoints (if used)
>
> Once confirmed, we will add a concise endpoint table with request/response examples aligned to controllers.
