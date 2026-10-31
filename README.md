# SkillSync Backend

Spring Boot backend for SkillSync — peer-to-peer learning, skill recommendations storage, connections, and chat.

## Stack

- Java 21
- Spring Boot 3.3
- Spring Security + JWT + BCrypt
- Spring Data JPA + Hibernate
- MySQL 8 + Flyway
- Springdoc OpenAPI

## Setup

1. Create/start MySQL 8 and ensure the app can create `skillsync` (or create it yourself).
2. Configure credentials via env vars or `application.yml`:

```bash
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=skillsync-dev-secret-key-change-me-in-production-must-be-long-enough-256bits
INTERNAL_API_KEY=skillsync-internal-dev-key
```

3. Run:

```bash
mvn spring-boot:run
```

API docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Main endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/auth/register` | Register |
| POST | `/api/v1/auth/login` | Login |
| GET/PUT | `/api/v1/users/me` | Own profile |
| GET | `/api/v1/users/{id}` | Public profile |
| GET | `/api/v1/skills?query=` | Search skills |
| GET/POST | `/api/v1/users/me/skills` | List/add skills |
| PUT/DELETE | `/api/v1/users/me/skills/{id}` | Update/remove skill |
| POST | `/api/v1/connections` | Send connection request |
| GET | `/api/v1/connections/requests` | Incoming requests |
| PATCH | `/api/v1/connections/{id}/accept` | Accept |
| PATCH | `/api/v1/connections/{id}/reject` | Reject |
| GET | `/api/v1/recommendations` | Get recommendations |
| POST | `/api/v1/internal/recommendations` | AI/ML ingest (`X-API-Key`) |
| GET/POST | `/api/v1/chat/conversations` | List/create conversations |
| GET/POST | `/api/v1/chat/conversations/{id}/messages` | History / send |
| PATCH | `/api/v1/chat/conversations/{id}/read` | Mark read |
| POST | `/api/v1/recommendations/{id}/feedback` | Submit feedback |

## Design notes

- Chat is allowed only after a connection is accepted; one private conversation is created per accepted pair.
- Recommendation scoring/inference lives outside this service; results are ingested via the secured internal API.
- First version uses REST polling for chat (no WebSockets).

## Tests

```bash
mvn test
```
