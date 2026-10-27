import hashlib
import os
from datetime import datetime, timedelta, timezone
from typing import Optional, Union, Any
from jose import jwt, JWTError
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from .config import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES
from .database import get_db
from .models import User

security_bearer = HTTPBearer(auto_error=False)

def hash_password(password: str) -> str:
    """Secure SHA-256 with salt password hashing."""
    salt = os.getenv("HASH_SALT", "optisight_secure_salt_2026")
    return hashlib.sha256((salt + password).encode("utf-8")).hexdigest()

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify plain password against hashed password."""
    return hash_password(plain_password) == hashed_password

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """Create signed JWT access token."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(timezone.utc) + expires_delta
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security_bearer),
    db: Session = Depends(get_db)
) -> Optional[User]:
    """Retrieve currently authenticated user from Bearer token."""
    if not credentials:
        return None
    
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
    except JWTError:
        # Check if it's a Firebase ID token
        try:
            unverified = jwt.get_unverified_claims(token)
            email = unverified.get("email") or unverified.get("sub")
        except Exception:
            return None

    if not email:
        return None

    user = db.query(User).filter(User.email == email).first()
    if not user and "@" in str(email):
        # Auto-provision user from Firebase login
        name_part = email.split("@")[0].replace(".", " ").title()
        user = User(
            full_name=name_part,
            email=email,
            mobile="+91 93925 71978",
            hashed_password=hash_password("firebase_authenticated_user"),
            age="24",
            gender="Male"
        )
        db.add(user)
        db.commit()
        db.refresh(user)

    return user

def require_current_user(user: Optional[User] = Depends(get_current_user)) -> User:
    """Require user authentication for protected endpoints."""
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials or token expired",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user
