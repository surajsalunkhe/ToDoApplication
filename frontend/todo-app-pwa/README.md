# ToDo App PWA (React)

## Overview
React frontend for the ToDo Application.

> **Needs input**: confirm the backend base URL and auth mode (Basic vs JWT) used by the app in local/dev.

## Prerequisites
- Node.js (as per root README)
- npm (project includes `package-lock.json`)

## Run locally
```bash
cd frontend/todo-app-pwa
npm install
npm start
```
App runs on: http://localhost:4200 (as per root README)

If you hit OpenSSL/webpack legacy provider issues:
```bash
NODE_OPTIONS=--openssl-legacy-provider npm start
```

## Configuration
- Constants: `src/constants.js`
- API clients:
  - `src/api/todo/HelloWorldService.js`
  - `src/api/todo/TodoDataService.js`

> **Needs input**: if there are `.env` files / env vars (e.g., `REACT_APP_*`) they should be documented here.

## Tests
```bash
cd frontend/todo-app-pwa
npm test
```

## Tech
- React
- Material UI
- Axios

## CRA reference
This project was bootstrapped with Create React App.
See CRA docs: https://create-react-app.dev/
