from pydantic import BaseModel, EmailStr
from typing import Optional, List, Dict, Any

# Authentication Schemas
class RegisterRequest(BaseModel):
    full_name: str
    email: EmailStr
    mobile: Optional[str] = None
    password: str

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: Dict[str, Any]

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class VerifyOTPRequest(BaseModel):
    email: EmailStr
    otp: str

class ResetPasswordRequest(BaseModel):
    email: EmailStr
    otp: Optional[str] = None
    new_password: str

# User Profile Schemas
class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    mobile: Optional[str] = None
    age: Optional[str] = None
    gender: Optional[str] = None
    profile_photo_url: Optional[str] = None

class UserProfileResponse(BaseModel):
    id: int
    full_name: str
    email: str
    mobile: Optional[str] = None
    age: Optional[str] = "24"
    gender: Optional[str] = "Male"
    profile_photo_url: Optional[str] = None
    total_scans: int = 0
    health_score: int = 95
    status: str = "Active"

# Analysis Schemas
class AnalysisExecuteRequest(BaseModel):
    image_url: Optional[str] = None
    file_id: Optional[str] = None
    condition: Optional[str] = None

class AnalysisDetailResponse(BaseModel):
    id: int
    image_url: str
    condition: str
    confidence: float
    health_score: int
    lens_recommendation: str
    analysis_date: str
    analysis_time: str
    bounding_boxes: Optional[List[Dict[str, Any]]] = []
    clinical_tips: Optional[List[str]] = []

# Lens & Health Tip Schemas
class LensRecommendation(BaseModel):
    condition: str
    lens_type: str
    description: str
    benefits: List[str]
    ideal_for: str

class HealthTipSchema(BaseModel):
    id: int
    title: str
    category: str
    description: str
    icon: str
