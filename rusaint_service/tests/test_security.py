"""
내부 JWT 인증 관련 테스트.
"""

import pytest
from fastapi import HTTPException
from app.core.security import verify_internal_jwt
from app.core.config import settings
from fastapi.security import HTTPAuthorizationCredentials
import jwt
from datetime import datetime, timedelta


@pytest.mark.asyncio
async def test_jwt_verification_with_valid_token():
    """유효한 JWT 토큰으로 검증 테스트"""
    # JWT 토큰 생성 (테스트용)
    if settings.internal_jwt_secret:
        payload = {
            "sub": "test",
            "exp": datetime.utcnow() + timedelta(hours=1),
        }
        token = jwt.encode(payload, settings.internal_jwt_secret, algorithm=settings.internal_jwt_algorithm)
        
        credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials=token)
        result = await verify_internal_jwt(credentials)
        
        assert result is not None
        assert result.get("sub") == "test"


@pytest.mark.asyncio
async def test_jwt_verification_with_expired_token():
    """만료된 JWT 토큰 처리 테스트"""
    # 만료된 JWT 토큰 생성
    if settings.internal_jwt_secret:
        payload = {
            "sub": "test",
            "exp": datetime.utcnow() - timedelta(hours=1),  # 1시간 전 만료
        }
        token = jwt.encode(payload, settings.internal_jwt_secret, algorithm=settings.internal_jwt_algorithm)
        
        credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials=token)
        
        with pytest.raises(HTTPException) as exc_info:
            await verify_internal_jwt(credentials)
        
        assert exc_info.value.status_code == 401
        assert "expired" in exc_info.value.detail.lower()


@pytest.mark.asyncio
async def test_jwt_verification_with_invalid_token():
    """유효하지 않은 JWT 토큰 처리 테스트"""
    credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials="invalid-token")
    
    with pytest.raises(HTTPException) as exc_info:
        await verify_internal_jwt(credentials)
    
    assert exc_info.value.status_code == 401
    assert "invalid" in exc_info.value.detail.lower()


@pytest.mark.asyncio
async def test_jwt_verification_with_placeholder_in_debug_mode():
    """개발 모드에서 placeholder 토큰 허용 테스트"""
    if settings.debug:
        credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials="internal-jwt-placeholder")
        result = await verify_internal_jwt(credentials)
        
        assert result is not None
        assert result.get("valid") is True
