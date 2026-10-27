from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import Dict, Any

from ..database import get_db
from ..models import User
from ..schemas import (
    RegisterRequest, LoginRequest, ForgotPasswordRequest, 
    VerifyOTPRequest, ResetPasswordRequest, TokenResponse
)
from ..security import hash_password, verify_password, create_access_token
from ..services.otp_service import otp_service

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register")
def register(request: RegisterRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:
    # Check if user already exists
    existing_user = db.query(User).filter(User.email == request.email).first()
    if existing_user:
        # Update user details if re-registering
        existing_user.full_name = request.full_name
        existing_user.mobile = request.mobile or existing_user.mobile
        existing_user.hashed_password = hash_password(request.password)
        db.commit()
        db.refresh(existing_user)
        user = existing_user
    else:
        new_user = User(
            full_name=request.full_name,
            email=request.email,
            mobile=request.mobile,
            hashed_password=hash_password(request.password),
            age="24",
            gender="Male"
        )
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        user = new_user

    # Generate initial verification OTP
    otp_code = otp_service.generate_otp(user.email, db)
    access_token = create_access_token(data={"sub": user.email, "user_id": user.id})

    return {
        "status": "success",
        "message": "User registered successfully",
        "access_token": access_token,
        "token_type": "bearer",
        "demo_otp": otp_code,
        "user": {
            "id": user.id,
            "full_name": user.full_name,
            "email": user.email,
            "mobile": user.mobile
        }
    }

@router.post("/login")
def login(request: LoginRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        # Auto-create demo user if logging in for first time in demo mode
        user = User(
            full_name=request.email.split("@")[0].capitalize(),
            email=request.email,
            mobile="+91 93925 71978",
            hashed_password=hash_password(request.password),
            age="24",
            gender="Male"
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    else:
        if not verify_password(request.password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid email or password"
            )

    access_token = create_access_token(data={"sub": user.email, "user_id": user.id})

    return {
        "status": "success",
        "message": "Login successful",
        "access_token": access_token,
        "token_type": "bearer",
        "user": {
            "id": user.id,
            "full_name": user.full_name,
            "email": user.email,
            "mobile": user.mobile,
            "age": user.age,
            "gender": user.gender,
            "profile_photo_url": user.profile_photo_url
        }
    }

@router.post("/forgot-password")
def forgot_password(request: ForgotPasswordRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:
    user = db.query(User).filter(User.email == request.email).first()
    otp_code = otp_service.generate_otp(request.email, db)
    return {
        "status": "success",
        "message": f"Verification code sent to {request.email}",
        "demo_otp": otp_code
    }

@router.post("/verify-otp")
def verify_otp(request: VerifyOTPRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:
    is_valid = otp_service.verify_otp(request.email, request.otp, db)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid or expired verification code"
        )
    return {
        "status": "success",
        "message": "OTP verified successfully"
    }

@router.post("/reset-password")
def reset_password(request: ResetPasswordRequest, db: Session = Depends(get_db)) -> Dict[str, Any]:
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    user.hashed_password = hash_password(request.new_password)
    db.commit()

    return {
        "status": "success",
        "message": "Password reset successfully. You can now log in with your new password."
    }
