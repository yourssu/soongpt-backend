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
    internal_jwt_secret: Optional[str] = None  # 개발 환경에서는 None 허용
    internal_jwt_algorithm: str = "HS256"

    # Rusaint 설정
    rusaint_timeout: int = 30  # 초 단위 (Kotlin timeout보다 충분히 길게)

    # 성적 등급 상수
    LOW_GRADE_RANKS: set[str] = {"C+", "C0", "C-", "D+", "D0", "D-"}
    FAIL_GRADE: str = "F"

    # CORS 설정
    allowed_origins: list[str] = ["http://localhost:8080"]  # 환경 변수로 쉼표 구분

    # Redis 설정 (Phase 3)
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 0
    redis_password: Optional[str] = None

    @property
    def is_production(self) -> bool:
        """프로덕션 환경 여부"""
        return not self.debug

    def model_post_init(self, __context) -> None:
        """모델 초기화 후 검증"""
        if self.is_production and not self.internal_jwt_secret:
            raise ValueError(
                "INTERNAL_JWT_SECRET is required in production environment. "
                "Please set the environment variable or create a .env file."
            )

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# 전역 설정 인스턴스
settings = Settings()
