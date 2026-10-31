from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import router

app=FastAPI(title="SkillSync AI/ML Service",version="1.0.0",
            description="AI recommendation service. Spring Boot owns application database writes.")
app.add_middleware(CORSMiddleware,allow_origins=["http://localhost:3000","http://localhost:5173"],
                   allow_credentials=True,allow_methods=["*"],allow_headers=["*"])
app.include_router(router)
