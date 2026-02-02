"""
설정 관련 테스트.
"""

import pytest
from app.core.config import Settings


def test_settings_default_values():
    """기본 설정값 테스트"""
    # 개발 모드에서는 internal_jwt_secret이 None이어도 됨. PSEUDONYM_SECRET은 항상 필수.
    settings = Settings(debug=True, pseudonym_secret="test-secret")

    assert settings.app_name == "Rusaint Service"
    assert settings.app_version == "0.1.0"
    assert settings.rusaint_timeout == 30
    assert settings.FAIL_GRADE == "F"
    assert "C+" in settings.LOW_GRADE_RANKS
    assert "D-" in settings.LOW_GRADE_RANKS


def test_settings_production_requires_jwt_secret():
    """프로덕션 환경에서 JWT 시크릿 필수 검증"""
    with pytest.raises(ValueError) as exc_info:
        Settings(debug=False, internal_jwt_secret=None, pseudonym_secret="test")

    assert "INTERNAL_JWT_SECRET" in str(exc_info.value)


def test_settings_requires_pseudonym_secret():
    """PSEUDONYM_SECRET 미설정 시 기동 실패 검증"""
    with pytest.raises(ValueError) as exc_info:
        Settings(debug=True, internal_jwt_secret="test", pseudonym_secret="")

    assert "PSEUDONYM_SECRET" in str(exc_info.value)


def test_settings_is_production_property():
    """is_production 속성 테스트"""
    dev_settings = Settings(debug=True, internal_jwt_secret="test", pseudonym_secret="test")
    prod_settings = Settings(debug=False, internal_jwt_secret="test", pseudonym_secret="test")

    assert not dev_settings.is_production
    assert prod_settings.is_production


def test_settings_cors_origins():
    """CORS 설정 테스트"""
    settings = Settings(
        debug=True,
        pseudonym_secret="test",
        allowed_origins=["http://localhost:8080", "http://localhost:3000"],
    )

    assert len(settings.allowed_origins) == 2
    assert "http://localhost:8080" in settings.allowed_origins
