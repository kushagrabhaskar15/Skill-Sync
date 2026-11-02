# SkillSync AI/ML recommendation service

This service ranks active SkillSync learners and submits new results to the existing Spring Boot API. The browser communicates only with Spring Boot; this service is intended to run on loopback and is not a frontend API.

The scorer is a transparent content-based baseline, not a trained model or a measured accuracy claim. It combines complementary teach/learn skills (70%) with TF-IDF similarity between shared interests (30%). Learning goals and availability are not included because the current Spring Boot Flyway schema does not contain those tables.

## Data flow

1. Python uses a MySQL account with `SELECT` access only to `users`, `skills`, `user_skills`, `user_interests`, `connections`, and `recommendations`.
2. The service ranks active profiles, excluding self, pending/accepted/blocked connections, and recommendation pairs already saved for that learner.
3. The service posts new results to Spring Boot. Spring Boot remains the only writer to the recommendations table.
4. The frontend reads those saved recommendations from the existing Spring Boot `GET /api/v1/recommendations` endpoint.

The service does not create or alter tables. **Do not run `database_schema_reference.sql` against your database**; it is a historical schema dump with destructive statements and does not match the current Flyway schema.

## Local Windows setup

Use an activated Python environment. The `python` command below must refer to that environment.

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
Copy-Item .env.example .env
```

Create a dedicated MySQL read-only user as a MySQL administrator, choosing a strong password locally:

```sql
CREATE USER 'skillsync_ai'@'127.0.0.1' IDENTIFIED BY 'choose-a-strong-password';
GRANT SELECT ON skillsync.users TO 'skillsync_ai'@'127.0.0.1';
GRANT SELECT ON skillsync.skills TO 'skillsync_ai'@'127.0.0.1';
GRANT SELECT ON skillsync.user_skills TO 'skillsync_ai'@'127.0.0.1';
GRANT SELECT ON skillsync.user_interests TO 'skillsync_ai'@'127.0.0.1';
GRANT SELECT ON skillsync.connections TO 'skillsync_ai'@'127.0.0.1';
GRANT SELECT ON skillsync.recommendations TO 'skillsync_ai'@'127.0.0.1';
```

Set `DB_PASSWORD` in `.env` to the password you chose. Set `SPRING_BOOT_API_KEY` to the same locally configured value as Spring Boot's `INTERNAL_API_KEY`. Never put either value in frontend configuration or commit `.env`.

Start the service bound to loopback:

```powershell
uvicorn app.main:app --host 127.0.0.1 --port 8001
```

The service docs are available at `http://127.0.0.1:8001/docs`. The frontend does not call this service. For development, generate recommendations explicitly with `POST /api/v1/recommendations/generate/{user_id}`. The service skips ingestion when there are no new matches, because Spring Boot requires a non-empty result list.

## Spring Boot integration contract

The configured target is `POST /api/v1/internal/recommendations` with `X-API-Key` matching `INTERNAL_API_KEY`:

```json
{
  "userId": 101,
  "sourceVersion": "skillsync-ai-1.0.0",
  "results": [
    {
      "recommendedUserId": 208,
      "score": 0.92,
      "explanation": "You can teach Java; this user can teach you Spring Boot."
    }
  ]
}
```

This matches the existing Spring Boot `RecommendationIngestionRequest`. No frontend or public backend route changes are required. Recommendation feedback forwarding is not implemented because the current backend feedback endpoint requires a user JWT and has no internal service-to-service contract.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/health` | AI service and MySQL health |
| GET | `/api/v1/users/{id}/profile` | AI-ready profile |
| GET | `/api/v1/users/{id}/recommendations` | Saved recommendations |
| POST | `/api/v1/recommendations/preview/{id}` | Generate without writing |
| POST | `/api/v1/recommendations/generate/{id}` | Generate and ingest new results |
| GET | `/api/v1/recommendations/{id}` | Read one saved recommendation |
| GET | `/api/v1/matches/{id}/{other_id}` | Compare profiles |
| POST | `/api/v1/skills/normalize` | Normalize skill names |

## Tests

```powershell
python -m pytest -q
```

The MySQL account is read-only; all application data writes go through Spring Boot.

## Optional ESCO Web API enrichment

The recommendation service can add occupation search context from the European Commission's ESCO Web Services API. This enrichment only adds context to recommendations; it does not change peer-match scores. API failures fail open, so peer recommendations can still be generated.

The client uses the currently published web service at `https://ec.europa.eu/esco/api`, matching each skill to an ESCO skill concept, then using its declared essential and optional occupation relations. Results are cached for 24 hours and limited to three unique skills per generation by default. Configure `ESCO_API_BASE_URL`, `ESCO_LANGUAGE`, and optionally `ESCO_SELECTED_VERSION` in `.env` if needed.

ESCO is a European skills and occupations classification, so the suggested occupation context may be less relevant outside that labor-market context. Recommendation explanations attribute ESCO and link to https://esco.ec.europa.eu/.

ESCO announced that a replacement API will require API keys as part of a future migration. The currently published web-service documentation does not yet provide the new key request steps or authentication contract; this integration will need updating when that version is released. See https://esco.ec.europa.eu/en/use-esco/use-esco-services-api/esco-web-service-api.
