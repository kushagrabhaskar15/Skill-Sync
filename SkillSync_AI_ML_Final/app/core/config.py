import os
from dotenv import load_dotenv
load_dotenv()

def number(name, default):
    try:
        return float(os.getenv(name, str(default)))
    except (TypeError, ValueError):
        return default

DB_HOST=os.getenv("DB_HOST","127.0.0.1")
DB_PORT=int(os.getenv("DB_PORT","3306"))
DB_NAME=os.getenv("DB_NAME","skillsync")
DB_USER=os.getenv("DB_USER","skillsync_ai")
DB_PASSWORD=os.getenv("DB_PASSWORD","")
SPRING_BOOT_BASE_URL=os.getenv("SPRING_BOOT_BASE_URL","http://localhost:8080").rstrip("/")
SPRING_BOOT_RECOMMENDATION_INGEST_PATH=os.getenv("SPRING_BOOT_RECOMMENDATION_INGEST_PATH","/api/recommendations/ai-ingest")
SPRING_BOOT_FEEDBACK_PATH=os.getenv("SPRING_BOOT_FEEDBACK_PATH","/api/recommendations/ai-feedback")
SPRING_BOOT_API_TOKEN=os.getenv("SPRING_BOOT_API_TOKEN","")
HTTP_TIMEOUT_SECONDS=int(os.getenv("HTTP_TIMEOUT_SECONDS","15"))
MAX_RECOMMENDATIONS=int(os.getenv("MAX_RECOMMENDATIONS","10"))
MIN_MATCH_SCORE=number("MIN_MATCH_SCORE",0.10)
WEIGHT_SKILLS=number("WEIGHT_SKILLS",0.50)
WEIGHT_INTERESTS=number("WEIGHT_INTERESTS",0.20)
WEIGHT_GOALS=number("WEIGHT_GOALS",0.20)
WEIGHT_AVAILABILITY=number("WEIGHT_AVAILABILITY",0.10)
SOURCE_VERSION=os.getenv("SOURCE_VERSION","skillsync-ai-1.0.0")

def validate():
    weights=[WEIGHT_SKILLS,WEIGHT_INTERESTS,WEIGHT_GOALS,WEIGHT_AVAILABILITY]
    if any(w < 0 for w in weights) or sum(weights) <= 0:
        raise ValueError("Weights must be non-negative and have a positive sum.")
    if not DB_PASSWORD:
        raise ValueError("DB_PASSWORD is empty. Configure it in .env.")
