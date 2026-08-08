# DevConnect Contest Service

A standalone microservice that powers coding contests for [DevConnect](https://github.com/OMKAR131005/DevConnect) — a full-stack developer social networking platform. This service handles problem management, contest scheduling, code submissions, and automated judging, decoupled from the main DevConnect backend so it can scale independently under judging load.

## Why a Separate Service

- **Resource isolation** — code execution/judging is CPU-intensive and benefits from scaling independently of the core social features (feed, profiles, chat).
- **Security boundary** — untrusted user code execution is isolated from the main application and its user data.
- **Independent deployment** — this service can be redeployed, scaled, or restarted without affecting the rest of DevConnect.

## Architecture

```
DevConnect Main Backend (auth, users, social features)
        │
        │  shared JWT (cookie-based)
        ▼
Contest Service (this repo)
        │
        ├── MySQL (own database — problems, contests, submissions)
        ├── Judge0 (code execution & verdict)
        └── RabbitMQ (async submission result delivery)
```

- Authentication is **not duplicated** — this service validates the same JWT issued by the main DevConnect backend (shared secret), so users stay logged in across both services without a separate login.
- Cross-service references (e.g., which user made a submission) are stored as plain IDs, not foreign keys, since user data lives in a separate database owned by the main backend.

## Tech Stack

- **Java / Spring Boot** — REST API
- **Spring Security** — JWT-based auth, role-based access control (`USER` / `ADMIN`)
- **Spring Data JPA / MySQL** — persistence (dedicated database, separate from the main DevConnect DB)
- **Judge0** — sandboxed code execution and verdict generation
- **RabbitMQ** — async communication for submission results
- **JJWT** — JWT parsing and validation

## Core Features

- **Problem management** — CRUD for coding problems with statements, constraints, difficulty, and test cases (admin-only write access)
- **Sample vs. hidden test cases** — only sample cases are exposed to users; hidden cases are used for judging
- **Contests** — scheduled contests bundling multiple problems with start/end windows
- **Submissions** — code submission, async judging via Judge0, verdict tracking (`AC`, `WA`, `TLE`, `MLE`, `RE`, `CE`)
- **Scoring** — points-based scoring feeding into DevConnect's skill-based ranking system

## Getting Started

### Prerequisites

- Java 17+
- MySQL
- Judge0 (self-hosted via Docker or hosted API)
- RabbitMQ
- The main [DevConnect backend](https://github.com/OMKAR131005/DevConnect) running, for shared JWT secret/cookie config

### Configuration

Copy the example properties file and fill in your local values:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Key values that **must match the main DevConnect backend** exactly:

- `app.jwt.secret`
- `app.cookie.name`

`application.properties` is gitignored and should never be committed — use `application-example.properties` as the template for required keys.

### Run

```bash
mvn spring-boot:run
```

The service starts on the port configured in `application.properties` (default separate from the main backend's port).

## API Overview

| Endpoint | Method | Access | Description |
|---|---|---|---|
| `/api/problems` | GET | Public | List problems |
| `/api/problems/{id}` | GET | Public | View problem (sample test cases only) |
| `/api/problems` | POST | Admin | Create problem |
| `/api/problems/{id}` | PUT | Admin | Update problem |
| `/api/problems/{id}` | DELETE | Admin | Delete problem |

*(Contest and submission endpoints follow the same pattern — full list in progress.)*

## Status

🚧 Actively under development as part of DevConnect's contest/ranking feature.

## Related Repos

- [DevConnect](https://github.com/OMKAR131005/DevConnect) — main platform (auth, profiles, social features)
