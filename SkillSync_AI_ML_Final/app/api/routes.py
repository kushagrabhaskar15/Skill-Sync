import requests
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.core import config
from app.core.database import ping
from app.integrations.spring_boot_client import SpringBootClient
from app.repositories.skill_sync_repository import SkillSyncRepository
from app.services.nlp_service import normalize_many
from app.services.recommendation_service import RecommendationService, ingestion_payload

router = APIRouter()
repo = SkillSyncRepository()
service = RecommendationService(repo)
spring = SpringBootClient()


class SkillRequest(BaseModel):
    skills: list[str] = Field(min_length=1, max_length=100)


class FeedbackRequest(BaseModel):
    recommendationId: int
    userId: int
    rating: int = Field(ge=1, le=5)
    feedbackText: str | None = Field(default=None, max_length=1000)


@router.get("/health")
def health():
    try:
        ping()
        return {"status": "UP", "database": "CONNECTED"}
    except Exception:
        raise HTTPException(status_code=503, detail="Database unavailable.")


@router.get("/api/v1/users/{user_id}/profile")
def profile(user_id: int):
    user = repo.get_profile(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Active user not found.")
    return {
        "userId": user["id"],
        "name": user["name"],
        "bio": user["bio"],
        "location": user["location"],
        "teachSkills": user["teach"],
        "learnSkills": user["learn"],
        "interests": user["interests"],
    }


@router.get("/api/v1/users/{user_id}/recommendations")
def saved_recommendations(user_id: int):
    if not repo.get_profile(user_id):
        raise HTTPException(status_code=404, detail="Active user not found.")
    return {"userId": user_id, "recommendations": repo.get_saved_recommendations(user_id)}


@router.post("/api/v1/recommendations/preview/{user_id}")
def preview(user_id: int):
    try:
        return service.generate(user_id)
    except LookupError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.post("/api/v1/recommendations/generate/{user_id}")
def generate(user_id: int):
    try:
        result = service.generate(user_id)
        recommendations = result["recommendations"]
        payload = ingestion_payload(result)

        # The Spring ingestion endpoint requires a non-empty results array.
        if payload is None:
            return {
                "generated": 0,
                "recommendations": [],
                "integration": "skipped: no new matches",
            }

        integration = spring.post(config.SPRING_BOOT_RECOMMENDATION_INGEST_PATH, payload)
        return {
            "generated": len(recommendations),
            "recommendations": recommendations,
            "integration": integration,
        }
    except LookupError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except requests.RequestException as exc:
        raise HTTPException(status_code=502, detail=f"Spring Boot ingestion failed: {exc}") from exc


@router.get("/api/v1/recommendations/{recommendation_id}")
def recommendation(recommendation_id: int):
    row = repo.get_recommendation(recommendation_id)
    if not row:
        raise HTTPException(status_code=404, detail="Recommendation not found.")
    return row


@router.get("/api/v1/matches/{user_id}/{other_user_id}")
def match(user_id: int, other_user_id: int):
    try:
        return {"userId": user_id, "otherUserId": other_user_id, **service.compare(user_id, other_user_id)}
    except LookupError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.post("/api/v1/skills/normalize")
def normalize(body: SkillRequest):
    return {"normalizedSkills": normalize_many(body.skills)}


@router.post("/api/v1/feedback")
def feedback(_body: FeedbackRequest):
    raise HTTPException(
        status_code=501,
        detail="Feedback forwarding is not supported by the current Spring Boot API.",
    )
