import os
import uvicorn
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import JSONResponse, FileResponse

from .config import UPLOAD_DIR, STATIC_DIR, HOST, PORT, FRONTEND_DIR
from .database import engine, Base, SessionLocal
from .models import User, AnalysisReport
from .security import hash_password
from .routers import auth, users, analysis, recommendations

# 1. Initialize database tables
Base.metadata.create_all(bind=engine)

# 2. Initialize FastAPI Application
app = FastAPI(
    title="OptiSight AI Vision Backend API",
    description="Full-stack AI Healthcare Backend providing Authentication, YOLO Vision Diagnostics, Profile Management, and Clinical Lens Recommendations.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 3. Enable CORS for Android Emulator & Web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 4. Mount Uploads & Frontend static files
app.mount("/uploads", StaticFiles(directory=str(UPLOAD_DIR)), name="uploads")
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")
if FRONTEND_DIR.exists():
    app.mount("/frontend", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="frontend")

# 5. Include API Routers with /api/v1 prefix
app.include_router(auth.router, prefix="/api/v1")
app.include_router(users.router, prefix="/api/v1")
app.include_router(analysis.router, prefix="/api/v1")
app.include_router(recommendations.router, prefix="/api/v1")

# 6. Seed Default Demo User on Startup
@app.on_event("startup")
def startup_event():
    db = SessionLocal()
    try:
        demo_user = db.query(User).filter(User.email == "sunil.kumar@example.com").first()
        if not demo_user:
            demo_user = User(
                full_name="Sunil Kumar Yadav",
                email="sunil.kumar@example.com",
                mobile="+91 93925 71978",
                age="24",
                gender="Male",
                hashed_password=hash_password("password123")
            )
            db.add(demo_user)
            db.commit()
            print("[Backend] Seeded demo user: sunil.kumar@example.com (Password: password123)")
    finally:
        db.close()

# 7. Frontend & Health Check Endpoints
@app.get("/")
def root():
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return FileResponse(str(index_file))
    return {
        "app": "OptiSight AI Vision API",
        "status": "Online",
        "version": "1.0.0",
        "docs": "/docs"
    }

@app.get("/app")
def serve_app():
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return FileResponse(str(index_file))
    return JSONResponse(status_code=404, content={"message": "Frontend not found"})

@app.get("/api/info")
def api_info():
    return {
        "app": "OptiSight AI Vision API",
        "status": "Online",
        "version": "1.0.0",
        "docs": "/docs",
        "endpoints": {
            "auth": "/api/v1/auth",
            "users": "/api/v1/users",
            "analysis": "/api/v1/analysis",
            "recommendations": "/api/v1/recommendations"
        }
    }

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "OptiSight AI Vision Backend",
        "version": "1.0.0"
    }

if __name__ == "__main__":
    uvicorn.run("backend.main:app", host=HOST, port=PORT, reload=False)
