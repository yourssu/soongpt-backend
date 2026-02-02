"""
내부 인증 관련 보안 유틸리티.

WAS(Kotlin) <-> rusaint-service(Python) 간 내부 JWT 검증 및 pseudonym 발급을 담당합니다.
"""

import base64
import hmac
import hashlib
from fastapi import HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from app.core.config import settings
import jwt
import logging

logger = logging.getLogger(__name__)

security = HTTPBearer()


async def verify_internal_jwt(
    credentials: HTTPAuthorizationCredentials = Security(security),
) -> dict:
    """
    내부 JWT 토큰을 검증합니다.

    Args:
        credentials: HTTP Authorization 헤더에서 추출한 Bearer 토큰

    Returns:
        dict: 디코딩된 JWT 페이로드

    Raises:
        HTTPException: 토큰이 유효하지 않거나 만료된 경우 401 에러
    """
    token = credentials.credentials

    # 개발 모드: placeholder 토큰 허용
    if settings.debug and token == "internal-jwt-placeholder":
        logger.warning("개발 모드: placeholder JWT 토큰 사용 중")
        return {"valid": True}

    try:
        payload = jwt.decode(
            token,
            settings.internal_jwt_secret,
            algorithms=[settings.internal_jwt_algorithm],
        )
        logger.debug("JWT 토큰 검증 성공")
        return payload
    except jwt.ExpiredSignatureError:
        logger.error("JWT 토큰이 만료되었습니다")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Internal JWT token expired",
            headers={"WWW-Authenticate": "Bearer"},
        )
    except jwt.InvalidTokenError as e:
        logger.error(f"유효하지 않은 JWT 토큰: error_type={type(e).__name__}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid internal JWT token",
            headers={"WWW-Authenticate": "Bearer"},
        )


def generate_pseudonym(student_id: str, secret: str) -> str:
    """
    WAS PseudonymGenerator와 동일한 방식으로 pseudonym 생성.
    HMAC-SHA256(student_id, secret) → base64url (패딩 제거).
    """
    digest = hmac.new(
        secret.encode("utf-8"),
        student_id.encode("utf-8"),
        hashlib.sha256,
    ).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")
