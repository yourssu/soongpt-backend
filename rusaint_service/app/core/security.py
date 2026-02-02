"""내부 JWT 검증 및 pseudonym 발급."""

import base64
import hmac
import hashlib
from fastapi import HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from app.core.config import settings
import jwt

security = HTTPBearer()


async def verify_internal_jwt(
    credentials: HTTPAuthorizationCredentials = Security(security),
) -> dict:
    """내부 JWT 토큰 검증. DEBUG 시 placeholder 허용."""
    token = credentials.credentials

    if settings.debug and token == "internal-jwt-placeholder":
        return {"valid": True}

    if not settings.internal_jwt_secret:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Internal JWT secret not configured",
            headers={"WWW-Authenticate": "Bearer"},
        )

    try:
        return jwt.decode(
            token,
            settings.internal_jwt_secret,
            algorithms=[settings.internal_jwt_algorithm],
        )
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Internal JWT token expired",
            headers={"WWW-Authenticate": "Bearer"},
        )
    except jwt.InvalidTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid internal JWT token",
            headers={"WWW-Authenticate": "Bearer"},
        )


def generate_pseudonym(student_id: str, secret: str) -> str:
    """HMAC-SHA256(student_id, secret) → base64url."""
    digest = hmac.new(
        secret.encode("utf-8"),
        student_id.encode("utf-8"),
        hashlib.sha256,
    ).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")
