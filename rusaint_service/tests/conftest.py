"""
Pytest 설정 및 공통 픽스처.
"""

import pytest
import os


@pytest.fixture(scope="session", autouse=True)
def setup_test_environment():
    """테스트 환경 설정"""
    # 테스트용 환경 변수 설정
    os.environ["DEBUG"] = "true"
    os.environ["INTERNAL_JWT_SECRET"] = "test-secret-key-for-testing"
    
    yield
    
    # 테스트 후 정리
    pass


@pytest.fixture
def mock_student_id():
    """테스트용 학번"""
    return "20231234"


@pytest.fixture
def mock_sso_token():
    """테스트용 SSO 토큰"""
    return "mock-sso-token"
