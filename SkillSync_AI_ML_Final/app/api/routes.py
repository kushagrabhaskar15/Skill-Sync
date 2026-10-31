import requests
from fastapi import APIRouter,HTTPException
from pydantic import BaseModel,Field
from app.core.database import ping
from app.repositories.skill_sync_repository import SkillSyncRepository
from app.services.recommendation_service import RecommendationService
from app.services.nlp_service import normalize_many
from app.integrations.spring_boot_client import SpringBootClient

router=APIRouter()
repo=SkillSyncRepository()
service=RecommendationService(repo)
spring=SpringBootClient()

class SkillRequest(BaseModel):
    skills:list[str]=Field(min_length=1,max_length=100)

class FeedbackRequest(BaseModel):
    recommendationId:int
    userId:int
    rating:int=Field(ge=1,le=5)
    feedbackText:str|None=Field(default=None,max_length=1000)

@router.get("/health")
def health():
    try:
        ping()
        return {"status":"UP","database":"CONNECTED"}
    except Exception as exc:
        raise HTTPException(503,detail=f"Database unavailable: {exc}")

@router.get("/api/v1/users/{user_id}/profile")
def profile(user_id:int):
    p=repo.get_profile(user_id)
    if not p: raise HTTPException(404,detail="Active user not found.")
    return {"userId":p["id"],"name":p["name"],"bio":p["bio"],"location":p["location"],
            "teachSkills":p["teach"],"learnSkills":p["learn"],"interests":p["interests"],
            "learningGoals":p["goals"],"availability":p["availability"]}

@router.get("/api/v1/users/{user_id}/recommendations")
def saved_recommendations(user_id:int):
    if not repo.get_profile(user_id): raise HTTPException(404,detail="Active user not found.")
    return {"userId":user_id,"recommendations":repo.get_saved_recommendations(user_id)}

@router.post("/api/v1/recommendations/preview/{user_id}")
def preview(user_id:int):
    try: return service.generate(user_id)
    except LookupError as exc: raise HTTPException(404,detail=str(exc))

@router.post("/api/v1/recommendations/generate/{user_id}")
def generate(user_id:int):
    try:
        result=service.generate(user_id)
        # Keep the Spring contract aligned with recommendations table fields.
        payload={"userId":result["userId"],"sourceVersion":result["sourceVersion"],
                 "recommendations":[{k:item[k] for k in ["recommendedUserId","matchScore",
                   "matchedSkills","learningOpportunities","sharedInterests","explanation"]}
                   for item in result["recommendations"]]}
        return {"generated":len(payload["recommendations"]),
                "integration":spring.post(__import__("app.core.config",fromlist=["SPRING_BOOT_RECOMMENDATION_INGEST_PATH"]).SPRING_BOOT_RECOMMENDATION_INGEST_PATH,payload)}
    except LookupError as exc: raise HTTPException(404,detail=str(exc))
    except requests.RequestException as exc: raise HTTPException(502,detail=f"Spring Boot ingestion failed: {exc}")

@router.get("/api/v1/recommendations/{recommendation_id}")
def recommendation(recommendation_id:int):
    row=repo.get_recommendation(recommendation_id)
    if not row: raise HTTPException(404,detail="Recommendation not found.")
    return row

@router.get("/api/v1/matches/{user_id}/{other_user_id}")
def match(user_id:int,other_user_id:int):
    try: return service.compare(user_id,other_user_id)
    except LookupError as exc: raise HTTPException(404,detail=str(exc))

@router.post("/api/v1/skills/normalize")
def normalize(body:SkillRequest):
    return {"normalizedSkills":normalize_many(body.skills)}

@router.post("/api/v1/feedback")
def feedback(body:FeedbackRequest):
    from app.core import config
    try:
        return spring.post(config.SPRING_BOOT_FEEDBACK_PATH,body.model_dump())
    except requests.RequestException as exc:
        raise HTTPException(502,detail=f"Spring Boot feedback endpoint failed: {exc}")
