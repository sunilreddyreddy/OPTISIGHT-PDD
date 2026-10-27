from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import Dict, Any, Optional

from ..database import get_db
from ..models import User, AnalysisReport
from ..schemas import UserProfileUpdate, UserProfileResponse
from ..security import get_current_user, require_current_user

router = APIRouter(prefix="/users", tags=["User Profile"])

@router.get("/me")
def get_user_profile(
    current_user: Optional[User] = Depends(get_current_user),
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    # Fallback to default/demo user if guest / unauthenticated
    if not current_user:
        current_user = db.query(User).first()
        if not current_user:
            current_user = User(
                full_name="Sunil Kumar Yadav",
                email="sunil.kumar@example.com",
                mobile="+91 93925 71978",
                age="24",
                gender="Male",
                hashed_password="default_hashed_pass"
            )
            db.add(current_user)
            db.commit()
            db.refresh(current_user)

    total_scans = db.query(AnalysisReport).filter(AnalysisReport.user_id == current_user.id).count()
    
    # Calculate average health score
    reports = db.query(AnalysisReport).filter(AnalysisReport.user_id == current_user.id).all()
    avg_score = round(sum(r.health_score for r in reports) / len(reports)) if reports else 96

    return {
        "id": current_user.id,
        "full_name": current_user.full_name,
        "email": current_user.email,
        "mobile": current_user.mobile or "+91 93925 71978",
        "age": current_user.age or "24",
        "gender": current_user.gender or "Male",
        "profile_photo_url": current_user.profile_photo_url,
        "total_scans": total_scans,
        "health_score": avg_score,
        "status": "Active"
    }

@router.put("/me/profile")
def update_user_profile(
    profile_data: Dict[str, Any],
    current_user: Optional[User] = Depends(get_current_user),
    db: Session = Depends(get_db)
) -> Dict[str, Any]:
    if not current_user:
        current_user = db.query(User).first()
        if not current_user:
            raise HTTPException(status_code=404, detail="User not found")

    if "full_name" in profile_data:
        current_user.full_name = profile_data["full_name"]
    if "fullName" in profile_data:
        current_user.full_name = profile_data["fullName"]
    if "mobile" in profile_data:
        current_user.mobile = profile_data["mobile"]
    if "age" in profile_data:
        current_user.age = str(profile_data["age"])
    if "gender" in profile_data:
        current_user.gender = profile_data["gender"]
    if "profile_photo_url" in profile_data:
        current_user.profile_photo_url = profile_data["profile_photo_url"]

    db.commit()
    db.refresh(current_user)

    return {
        "status": "success",
        "message": "Profile updated successfully",
        "user": {
            "id": current_user.id,
            "full_name": current_user.full_name,
            "email": current_user.email,
            "mobile": current_user.mobile,
            "age": current_user.age,
            "gender": current_user.gender,
            "profile_photo_url": current_user.profile_photo_url
        }
    }
