# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Overview

This is a full-stack Todo application with two independently runnable modules:

- **`frontend/todo-app-pwa/`** — React 16 PWA (Create React App, Material UI v4, Axios, React Router v5, Formik)
- **`restful-web-services/`** — Spring Boot 2.2 REST API (Spring Security + JWT, Spring Data JPA, H2 in-memory DB)

The frontend runs on port **4200** and the backend on port **8080**. CORS is explicitly allowed from `http://localhost:4200` in the backend controllers (`@CrossOrigin`).

### Authentication Flow

The app uses **JWT** (active path) alongside a legacy Basic Auth path that still exists in code but is superseded.

1. Frontend sends `POST /authenticate` with `{username, password}`
2. Backend (`JwtAuthenticationRestController`) returns a JWT token
3. Frontend stores the username in `sessionStorage` and attaches the `Authorization: Bearer <token>` header to all subsequent Axios requests via a global interceptor (`authenticationService.js`)
4. JWT config and secret live in `application.properties` (`jwt.signing.key.secret=mySecret`)

Users are defined in-memory in `JwtInMemoryUserDetailsService` with BCrypt-encoded passwords.

### API Layer

Two REST resource classes exist for todos — only `TodoJpaResource` is actively used by the frontend:

- `TodoJpaResource` — JPA-backed endpoints at `/jpa/users/{username}/todos` (CRUD)
- `TodoResource` — hardcoded service, legacy/unused by frontend

Frontend API calls go through:
- `src/api/todo/TodoDataService.js` — todo CRUD via `JPA_API_URL` (`http://localhost:8080/jpa`)
- `src/components/authenticationService.js` — login, logout, session management, Axios interceptor setup

### Database

H2 in-memory database auto-created at startup. Pre-seeded via `data.sql` with 3 todos for user `harshnagra`. All data is lost on backend restart. H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`).

## Commands

### Frontend (`frontend/todo-app-pwa/`)

```bash
npm install          # Install dependencies
npm start            # Start dev server on http://localhost:4200
npm test             # Run tests (Jest / React Testing Library)
npm run build        # Production build
```

If you get an OpenSSL error on newer Node versions:
```bash
NODE_OPTIONS=--openssl-legacy-provider npm start
```

### Backend (`restful-web-services/`)

```bash
./mvnw clean install       # Build and run tests
./mvnw spring-boot:run     # Start server on http://localhost:8080
```

Run a single test class:
```bash
./mvnw test -Dtest=RestfulWebServicesApplicationTests
```
