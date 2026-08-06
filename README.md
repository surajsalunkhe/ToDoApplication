# Todo App — Full Stack (React PWA + Spring Boot)

##### Demo
[Click here](https://drive.google.com/file/d/1aUtitzqu06ibs0KCbH_zC9fJhmOFHEN4/view?usp=sharing) to view the application.

## Repository structure
- Frontend: `frontend/todo-app-pwa`
- Backend: `restful-web-services`

## Local setup

### Prerequisites
- Node.js + npm
- JDK **14.0.1**

### 1) Backend
```bash
cd restful-web-services
./mvnw clean install
./mvnw spring-boot:run
```
Backend: http://localhost:8080

### 2) Frontend
```bash
cd frontend/todo-app-pwa
npm install
npm start
```
Frontend: http://localhost:4200

If you hit OpenSSL legacy provider issues:
```bash
NODE_OPTIONS=--openssl-legacy-provider npm start
```

## Database (H2 In-Memory)
- H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`

> Note: in-memory DB resets when the backend stops.

## Component docs
- Frontend README: `frontend/todo-app-pwa/README.md`
- Backend README: `restful-web-services/README.md`

> **Needs input**: add deployment/CI details and confirm which auth mode (Basic vs JWT) is the default integration.
