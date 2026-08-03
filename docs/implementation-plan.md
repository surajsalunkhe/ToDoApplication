# Implementation Plan — EPMEDUAI-1312: Secure multi-user ToDo access

This plan covers the approved stories under Jira epic **EPMEDUAI-1312 — Secure multi-user ToDo access**:

- **EPMEDUAI-1316** — Align on JWT authentication flow for frontend + document supported auth
- **EPMEDUAI-1315** — Enforce user ownership checks on ToDo endpoints (prevent IDOR)

Repository context (from project brief):

- Frontend: React / Material UI / Axios PWA (`frontend/todo-app-pwa`)
- Backend: Spring Boot REST `restful-web-services` secured with Spring Security (Basic Auth + JWT)
- Persistence: Hibernate JPA + H2 (in-memory)

---

## 1) Story Sequencing & Dependencies

### Principles
- **Contract first**: lock down the authentication flow (endpoints, headers, token storage, error handling) before hardening authorization.
  - Prevents backend changes the frontend can’t consume.
  - Reduces rework around Axios interceptors and Spring Security configuration.

### Sequence

1. **EPMEDUAI-1316 (prep/contract)**: Align on JWT authentication flow for frontend + document supported auth
   - **Depends on**: None (should be first deliverable)
   - **Unlocks**: all subsequent security hardening (ownership / IDOR)
   - **Outputs**:
     - Define supported auth modes (Basic Auth vs JWT) and what the PWA uses
     - Define login → token receipt → token attachment on API calls
     - Standardize headers: `Authorization: Bearer <token>`
     - Specify error behavior: 401 (unauthenticated) vs 403 (forbidden)
   - **Backend notes**: confirm/standardize auth endpoint(s) and the JWT claim used to identify the user (e.g., `sub` / username).
   - **Frontend notes**: Axios interceptor for attaching token + global handling for 401/403.

2. **EPMEDUAI-1315 (implement)**: Enforce user ownership checks on ToDo endpoints (prevent IDOR)
   - **Depends on**: EPMEDUAI-1316 (clear “current user” definition from JWT)
   - **Backend first**: enforce ownership at service/repository level so a malicious client cannot bypass it.
   - **Frontend follow-up**: ensure all ToDo calls attach token and gracefully handle 403/404.

---

## 2) Rough Effort Estimates

Estimates are rough (calendar days) assuming a single developer familiar with the codebase.

| Story | Range effort |
|---|---:|
| **EPMEDUAI-1316** | 0.5–1.5 days |
| **EPMEDUAI-1315** | 1–2.5 days |

Notes:
- If the existing JWT flow is already consistent, the effort for 1316 is low.
- 1315 should include automated tests because this is a security regression risk.

---

## 3) Branch Naming Convention

- Planning PR: `plan/EPMEDUAI-1312-implementation-plan`
- Feature branches: `feature/<STORY-KEY>-<slug>`
  - `feature/EPMEDUAI-1316-jwt-auth-flow-alignment`
  - `feature/EPMEDUAI-1315-ownership-checks-idor`

---

## 4) Risk / Impact Notes

### EPMEDUAI-1316 — Auth flow alignment + docs
- **Auth/API breaking changes**: standardizing on “Bearer JWT” can break existing calls until Axios interceptors and login flow match the backend contract.
- **CORS / preflight**: `Authorization` often requires allow-headers and exposed-headers configuration in Spring Security.
- **Token storage**: localStorage is convenient but increases XSS risk; in-memory is safer but requires re-login on reload. Decide and document.

### EPMEDUAI-1315 — Ownership checks (IDOR)
- **Expected API behavior change**: unauthorized access to another user’s ToDo should consistently fail (403 or 404). UI must handle this gracefully.
- **Implementation placement**: prefer service/repository-level filtering (query by id + username) to avoid accidental bypass.
- **DB/seed impact**: if ToDo rows are missing owners, expect seed data update or micro-schema adjustment (including H2).

---

## 5) Suggested Milestones / Sprint Grouping

- **Milestone 1: Auth Contract Locked (EPMEDUAI-1316)**
  - Exit criteria: PWA can login, receive JWT, and call at least one protected endpoint via Bearer token.

- **Milestone 2: IDOR Closed (EPMEDUAI-1315)**
  - Exit criteria: attempts to read/update/delete another user’s todo consistently fail, with tests proving it.
