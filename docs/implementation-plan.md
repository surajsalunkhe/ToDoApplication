# Implementation Plan — Improve ToDo API reliability, usability, and safety

Repo: https://github.com/surajsalunkhe/ToDoApplication

This plan implements the approved requirements (1 Epic + 3 Stories):

- Story 1: **Paginate and search ToDo list by username**
- Story 2: **Prevent cross-user access to ToDos (ownership validation)**
m Story 3: **Standardize API error responses and input validation for ToDo endpoints**

Note: Keys are not yet available in this repo flow; use placeholders in branch names (e.g., `feature/TODO-123-slug` when Jira Keys exist).


---

## High-level implementation strategy

The focus is production-readiness without expanding scope: we hinder other users' ToDos, make errors consistent and usable, and add scalable listing (pagination + search).

* **Backend first** for API contract changes. Frontend changes follow the new contract.
  - Close the IDOR/cross-user access gap before we make listing more powerful (page/search).
  - Introduce a standard error body layer before adding new input validation and query params.

* **Backwardcompatibility is a priority**. For pagination, prefer a mode where the existing endpoint still returns a full list if no `page`/`size` are provided, but supports a paged response when they are provided.

* **Test-the-high-risk along the way**. Security and contract regressions are more damageful than bugs in frontend wiring.

---

## Development phases (suggested milestones/sprints)

Assumption: 1 sprint = 1 week. Adjust as needed.

### Phase 0 — Contract alignment (0.5 day)
- Confirm behavior:
  - Ownership failure policy: **404 recommended** (to reduce username/id enumeration) vs 403.
  - Pagination response shape: Spring `Page<Todo>` vs custom DOT (items + meta)
  - Standard error schema (field errors structure)

### Phase 1 — Security/data safety (Story 2) (1 – 2 days)
- Backend: ownership-aware lookups for GET/PUT/DELETE id-based operations
- Tests: assert cross-user access is denied consistently (404/403)

### Phase 2 — Standard errors + validation (Story 3) (2 – 3 days)
- Backend: @ControllerAdvice + Error DTO contract
- Backend: Bean Validation on ToDo body and query params
- Frontend: normalize Axios errors based on new structured error body


### Phase 3 — Pagination + search (Story 1) (2 – 3 days)
- Backend: add page/size / q query params for list endpoint
- Backend: paged repository methods and search
- Frontend: update TodoDataService to pass query params and handle paged or unpaged response shape

  
### Phase 4 – Hardening & docs (1 day)
- Api examples (curl)
- Smoke test flow: auth -> list -> create -> update -> delete
- Review logging for secret leakage (tokens)
