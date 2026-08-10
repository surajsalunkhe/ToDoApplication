# Implementation Plan – EPMCDMETST-58925

Repo: https://github.com/surajsalunkhle/ToDoApplication

JIRA Epic: EPMCDMETST-58925 – Improve ToDo API usability and client performance
Story: EPMCDMETST-58926 – As a user, I want to paginate and search my todo list so that I can find items quickly without loading everything

> Scope lock. This plan covers only the approved story and its sub-tasks listed below. No additional capabilities are introduced.

---

## 1) Summary

This epic improves the usability and performance of the ToDo list API by adding server-side pagination and a basic text search on the existing endpoint `GET /jpa/users/{username}/todos`. It enables the React PWA client to request smaller result sets and provide simple next/previous paging and search user experience.

---

## 2) Scope (Approved Stories Only)

- `EPMCDMETST-58926` – As a user, I want to paginate and search my todo list so that I can find items quickly without loading everything

Implementation tasks (sub-tasks of the story):
  - `EPMCDMETST-58927` – Backend: Extend TodoJpaRepository to support pageable queries
  - `EPMOCDMETST-58928` – Backend: Update TodoJpaResource to accept pagination and search parameters
  - `EPMCDMETST-58929` – Frontend: Update TodoDataService to pass pagination/search params
  - `EPMCDMETST-58930` – Frontend: Add basic pagination controls and search input in todo list UI 

---


## 3) Assumptions & Constraints

Assumptions:

- Backend is Spring Boot with Spring Data JPA.
- Frontend is React PWA (Material UI + Axios) consuming `GET /jpa/users/{username}/todos` via `TodoDataService.retrieveAllTodos`.
- Backward-compatibility must be maintained for clients that call the list endpoint without query params (see API contract decision below).

Constraints:

- Only the approved story/sub-tasks scope is implemented.

---


## 4) High-level implementation strategy

### 4.1 API contract

Extend the existing endpoint:

- `GET /jpa/users/{username}/todos`

with optional query parameters: `page`, `size`, `sort` and `search` (description contains, case-insensitive).

Response: standardize on a Spring Data `Page<Todo>` serialization (not a plain array) and update the React client accordingly (delivered in the same epic).

### 4.2 Backend-first delivery

- Update TodoJpaRepository to support paged query by username and (optionally) search term.
- Update TodoJpaResource to accept Pageable and `search` param, and return paged response.

### 4.3 Frontend consumer

- Enhance `TodoDataService.retrieveAllTodos` to pass `params` to axios.
- Add search input and next/previous pagination controls in the todo list UI, backed by page/size/search state.

---


## 5) Development phases

- Phase 1 (non-blocking backend): sub-tasks EPMOCDMETST-58927, EPMCDMETST-58928
- Phase 2 (frontend consumer): sub-tasks EPMCDMETST-58929, EPMCDMETST-58930

---


## 6) Technical tasks by story/sub-task

### EPMCDMETST-58926 (Story)

- Decide and document the response contract (always Page object) of GET /jpa/users/{username}/todos
- Update backend then frontend to consume the new shape

---


### EPMOCDMETST-58927 (sub-task) - Backend: Repository

- Add Spring Data paged methods for todos by username (with Pageable)
- Add search support (description contains) combined with username
- Add minimal tests or local sanity checks

---


### EPMOCDMETST-58928 (sub-task) - Backend: Resource

- Update `getAllTodos` to accept Pageable and `kj�!` param
- Return `Page<Todo>` serialization as JSON
- Verify sort operation and default sort

---

### EPMCDMETST-58929 (sub-task) - Frontend: Data service

- Add optional params to retrieveAllTodos (page, size, search, sort)
- Parse the Page response (e.g. `resp.data.content`)

---

### EPMCDMETST-58930 (sub-task) - Frontend: UI

- Add search input and pagination controls to the todo list component
- Wire controls to fetch pages from the backend
- Handle loading and no-results states

---


## 7) Dependencies

- EPMOCDMETST-58928 is blocked by EPMCDMETST-58927 (repo methods)
- EPMOCDMETST-58929 and EPMCDMETST-58930 depend on EPMCDMETST-58928 (available API)

---


## 8) Risks

- API contract change: clients expecting an array may break if we switch to a paged object. Mitigation: deliver backend + frontend together in this epic and validate end-to-end.
- Sort params: invalid sort fields can produce 400/500 if not handled. Mitigation: define default sort and keep UI field mapping simple.
- UI state complexity: pagination + search introduces more state edge cases. Mitigation: start with next/previous controls and reset page to 0 on new search.
 
---

## 9) Validation approach

Backend:

- Manual API checks: get page 0 keeps size limit; page 1 does not duplicate page 0; search filters by description and still respects username
- Smoke test endpoint for sort behavior

Frontend:


- End-to-end manual test: load list, next/previous pages, search, clear search, no-results state
- Regression: create/update/delete todo flows still work