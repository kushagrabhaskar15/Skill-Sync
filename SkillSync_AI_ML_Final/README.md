# SkillSync AI/ML Backend - schema aligned

This project was generated against the uploaded `skillsync_schema.sql` (MySQL 8.0.43). It uses the existing database and does not create, alter, or drop tables.

## Architecture
- Python/FastAPI reads profile and matching data using a SELECT-only MySQL account.
- Python calculates recommendation scores and explanations.
- Python sends recommendation batches and feedback to Spring Boot over HTTP.
- Spring Boot owns all writes to `recommendations` and `recommendation_feedback`.
- Chat remains in Spring Boot and is not part of the AI service.

## Existing schema fields used
- `users`: `id`, `name`, `bio`, `location`, `status`
- `skills`: `id`, `name`, `category`
- `user_skills`: `user_id`, `skill_id`, `skill_type`, `proficiency_level`, `years_experience`
- `user_interests`: `user_id`, `interest_name`
- `learning_goals`: `user_id`, `title`, `description`, `status`
- `availability_slots`: `user_id`, `day_of_week`, `start_time`, `end_time`, `timezone`
- `connections`: `requester_id`, `receiver_id`, `status`
- `recommendations`: existing recommendation output fields
- `recommendation_feedback`: existing feedback fields
Chat tables are intentionally not read by AI.

## Windows setup
Run these commands in CMD from this extracted project folder:

```cmd
py -3.13 -m venv .venv
.venv\Scripts\activate
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
copy .env.example .env
```

Edit `.env` with your MySQL read-only account credentials. Do not use root. Ensure `DB_PASSWORD` is not blank.

If you still need to create/reset the read-only account, run in MySQL as an administrator (choose your own password):
```sql
CREATE USER IF NOT EXISTS 'skillsync_ai'@'localhost' IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';
GRANT SELECT ON skillsync.* TO 'skillsync_ai'@'localhost';
FLUSH PRIVILEGES;
```
If it already exists and you need to reset its password:
```sql
ALTER USER 'skillsync_ai'@'localhost' IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';
GRANT SELECT ON skillsync.* TO 'skillsync_ai'@'localhost';
FLUSH PRIVILEGES;
```

Start the service:
```cmd
uvicorn app.main:app --reload --host 127.0.0.1 --port 8001
```
Swagger: http://127.0.0.1:8001/docs

## API
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/health` | FastAPI and MySQL health |
| GET | `/api/v1/users/{id}/profile` | AI-ready profile |
| GET | `/api/v1/users/{id}/recommendations` | Read saved recommendations |
| POST | `/api/v1/recommendations/preview/{id}` | Generate without sending/saving |
| POST | `/api/v1/recommendations/generate/{id}` | Generate and POST batch to Spring Boot |
| GET | `/api/v1/recommendations/{recommendation_id}` | Read one saved recommendation |
| GET | `/api/v1/matches/{user_id}/{other_user_id}` | Compare two profiles |
| POST | `/api/v1/skills/normalize` | Normalize skill names |
| POST | `/api/v1/feedback` | Forward feedback to Spring Boot |

## Scoring baseline
Default weights: 50% two-way complementary skill coverage, 20% TF-IDF interest similarity, 20% TF-IDF learning-goal similarity, 10% overlapping availability. Weights are normalized by their sum and configurable in `.env`. This is a transparent heuristic baseline, not a trained supervised model or a measured accuracy claim. Alias mappings are conservative; Java and JavaScript remain distinct.

Existing pending, accepted and blocked connection pairs are excluded from new suggestions.

## Spring Boot integration is a required companion change
Python uses a read-only DB user, so Spring Boot must implement these endpoints (or update the paths in `.env`):
1. `POST /api/recommendations/ai-ingest`: accept `spring_boot_contract.json`, validate IDs and score 0..1, then upsert `recommendations` using unique key `(user_id,recommended_user_id)`. Store the three arrays in the JSON columns and set `source_version`.
2. `POST /api/recommendations/ai-feedback`: validate that the recommendation belongs to the submitting user, validate rating 1..5, and insert into `recommendation_feedback`.

Until these Spring Boot routes exist and are reachable, `/generate` and `/feedback` will return HTTP 502. `/preview` and all read/compare routes can work independently.

## Notes
- `recommendations` has a unique constraint on `(user_id,recommended_user_id)`, so ingestion must upsert rather than blindly insert duplicates.
- The SQL dump contains schema only, not sample user rows. Recommendation output depends on active users having skill/interests/goals data.
- Keep `.env` private. Add service-to-service authentication, retries/idempotency, rate limiting and structured logging before production.
