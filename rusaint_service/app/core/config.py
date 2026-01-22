"""
rusaint-service 설정 관리.

환경 변수를 통해 설정을 로드합니다.
"""

from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """애플리케이션 설정"""

    # 애플리케이션 정보
    app_name: str = "Rusaint Service"
    app_version: str = "0.1.0"
    debug: bool = False

    # 내부 JWT 인증
    internal_jwt_secret: str  # 환경 변수 필수
    internal_jwt_algorithm: str = "HS256"

    # Rusaint 설정
    rusaint_timeout: int = 30  # 초 단위 (Kotlin timeout보다 충분히 길게)

    # Redis 설정 (Phase 3)
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 0
    redis_password: Optional[str] = None

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# 전역 설정 인스턴스
settings = Settings()
