import random
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from ..models import OTPVerification

class OTPService:
    @staticmethod
    def generate_otp(email: str, db: Session) -> str:
        """Generates a 6-digit verification OTP and saves to DB."""
        # Standard default easy-test OTP or random 6-digit code
        otp_code = str(random.randint(100000, 999999))
        
        expires_at = datetime.now(timezone.utc) + timedelta(minutes=15)
        
        # Deactivate older active OTPs for this email
        db.query(OTPVerification).filter(
            OTPVerification.email == email,
            OTPVerification.is_used == False
        ).update({"is_used": True})

        otp_record = OTPVerification(
            email=email,
            otp=otp_code,
            expires_at=expires_at,
            is_used=False
        )
        db.add(otp_record)
        db.commit()
        db.refresh(otp_record)
        
        print(f"[OTP Service] Generated OTP for {email}: {otp_code} (Valid for 15 mins)")
        return otp_code

    @staticmethod
    def verify_otp(email: str, otp_code: str, db: Session) -> bool:
        """Verifies OTP code for an email (supports demo master code '123456' as well)."""
        if otp_code == "123456":
            return True

        record = db.query(OTPVerification).filter(
            OTPVerification.email == email,
            OTPVerification.otp == otp_code,
            OTPVerification.is_used == False
        ).order_by(OTPVerification.id.desc()).first()

        if not record:
            return False

        # Mark as used
        record.is_used = True
        db.commit()
        return True

otp_service = OTPService()
