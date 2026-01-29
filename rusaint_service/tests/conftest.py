"""
Pytest 설정 및 공통 픽스처.
"""

import pytest
import os
import sys
import jwt
from pathlib import Path
from datetime import datetime, timedelta
from fastapi.testclient import TestClient

# PYTHONPATH에 rusaint_service 디렉토리 추가
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


@pytest.fixture(scope="session", autouse=True)
def setup_test_environment():
    """테스트 환경 설정"""
    # 테스트용 환경 변수 설정
    os.environ["DEBUG"] = "true"
    os.environ["INTERNAL_JWT_SECRET"] = "test-secret-key-for-testing"
    yield


@pytest.fixture(scope="session")
def test_settings():
    """테스트용 설정"""
    from app.core.config import settings
    return settings


@pytest.fixture
def client():
    """FastAPI 테스트 클라이언트"""
    from app.main import app
    return TestClient(app)


@pytest.fixture
def valid_jwt_token(test_settings):
    """유효한 JWT 토큰"""
    payload = {
        "sub": "test-user",
        "exp": datetime.utcnow() + timedelta(hours=1),
    }
    return jwt.encode(
        payload,
        test_settings.internal_jwt_secret,
        test_settings.internal_jwt_algorithm
    )


@pytest.fixture
def expired_jwt_token(test_settings):
    """만료된 JWT 토큰"""
    payload = {
        "sub": "test-user",
        "exp": datetime.utcnow() - timedelta(hours=1),
    }
    return jwt.encode(
        payload,
        test_settings.internal_jwt_secret,
        test_settings.internal_jwt_algorithm
    )


@pytest.fixture
def placeholder_jwt_token():
    """개발 모드용 placeholder JWT 토큰"""
    return "internal-jwt-placeholder"


@pytest.fixture
def valid_request_body():
    """유효한 요청 본문"""
    return {
        "studentId": "20231234",
        "sToken": "test-sso-token"
    }


@pytest.fixture
def auth_headers(valid_jwt_token):
    """인증 헤더"""
    return {"Authorization": f"Bearer {valid_jwt_token}"}
