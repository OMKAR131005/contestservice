# DevConnect Contest Service

A standalone microservice for **DevConnect** that powers coding problems, contests, and code submissions with a custom-built Java judge — no external judging API required.

## Overview

This service owns all Problem / Contest / Submission data in its own MySQL database, completely separate from the main DevConnect backend's database. It validates the same JWT issued by the main backend using a **shared secret** — this service has no login endpoint of its own; it is a resource server only.

Cross-service references to users (`userId`) are stored as plain `Long` values, never as JPA foreign keys, since `User` lives in a different database entirely.

## Tech Stack

- Java, Spring Boot
- Spring Security (JWT resource server, shared secret with main backend)
- Spring Data JPA + MySQL
- springdoc-openapi (Swagger UI)
- Maven

## Key Features

### Custom Java Code Judge
Rather than relying on an external judging API, this service compiles and runs Java submissions in-process:
- Compiles submitted code to a temp directory and runs it with a memory cap (`-Xmx256m`)
- Verdicts: `AC`, `WA`, `CE`, `RE`, `TLE`, `MLE`
- Concurrent threads for stdin writing, stdout reading, and stderr reading avoid pipe-buffer deadlocks
- Out-of-memory detection via non-zero exit code + `"OutOfMemoryError"` in stderr

### Problems
- Full CRUD (`POST/GET/PUT/DELETE /api/problems`)
- Each problem has sample and hidden test cases — **hidden test cases are never exposed** through any API response (create, read, or update)
- `createdBy` field: `null` = official/admin problem, non-null = user-created (feature not yet built)

### Contests
- **System contests**: auto-generated weekly via a scheduled job, built from the official problem pool
- **User-created contests**: any authenticated user can create a contest; always forced to `USER_CREATED` type server-side to prevent privilege escalation
- **Visibility**: `PUBLIC` (anyone can see) or `PRIVATE` (creator + explicitly invited participants only)
- Contest responses use a dedicated response DTO (not raw entities) to avoid lazy-loading issues and to prevent hidden test cases from leaking through nested problem data

### Submissions
- `POST /api/submissions` — submits code against a problem, optionally scoped to a contest
- User identity is always taken from the JWT (`@AuthenticationPrincipal`), never trusted from the request body
- Contest submissions are validated against the contest's active time window and the user's access rights (public vs. private/invited)
- Runs all test cases (sample + hidden), fast-fails on the first mismatch
- Failure details (input/expected/actual) are only returned for **sample** test cases; hidden test case content is never leaked

## API Documentation

Interactive API docs are available via Swagger UI once the service is running:

```
http://localhost:<port>/swagger-ui.html
```

> Note: Swagger UI does not automatically send the auth cookie, so JWT-protected endpoints should still be tested via Postman (or another client that carries the cookie) rather than directly through Swagger UI.

## Security

- Stateless JWT authentication, validated against a secret shared with the main DevConnect backend
- Role-based access control (`ADMIN` / authenticated user) on sensitive endpoints
- Custom `401 Unauthorized` response for unauthenticated requests, custom `403 Forbidden` for authenticated-but-unauthorized requests

## Getting Started

```bash
mvn clean install
mvn spring-boot:run
```

Configure the following in `application.properties` / `application.yml`:
- MySQL connection details (separate database from the main DevConnect backend)
- `app.jwt.secret` — must match the main backend's JWT secret
- `app.cookie.name` — must match the main backend's auth cookie name

## Status

Actively in development as part of the DevConnect platform. Core judging, problem management, contest system, and submission flow are built and tested. Frontend integration and automated (JUnit/Mockito) test coverage are in progress.
