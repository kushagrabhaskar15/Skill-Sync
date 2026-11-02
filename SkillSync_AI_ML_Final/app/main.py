from fastapi import FastAPI
from app.api.routes import router

app = FastAPI(
    title="SkillSync AI/ML Service",
    version="1.0.0",
    description="Private recommendation service. Spring Boot owns application database writes.",
)
app.include_router(router)
