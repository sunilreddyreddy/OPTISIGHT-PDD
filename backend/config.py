import os
from pathlib import Path

# Base Directories
BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent
UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(exist_ok=True)
STATIC_DIR = BASE_DIR / "static"
STATIC_DIR.mkdir(exist_ok=True)
FRONTEND_DIR = PROJECT_ROOT / "frontend"

# Database
DATABASE_URL = f"sqlite:///{BASE_DIR / 'optisight.db'}"

# Security
SECRET_KEY = os.getenv("SECRET_KEY", "optisight-super-secret-jwt-key-2026-secure-vision-ai")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7  # 7 days

# YOLO Model Path
TRAINED_MODEL_PATH = PROJECT_ROOT / "runs" / "detect" / "OptiSightModel" / "vision_analysis-4" / "weights" / "best.pt"
FALLBACK_MODEL_PATH = PROJECT_ROOT / "yolo11n.pt"

# Server Settings
HOST = "0.0.0.0"
PORT = 8000
