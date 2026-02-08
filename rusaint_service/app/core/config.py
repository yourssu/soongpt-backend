"""
rusaint-service 설정 관리.

환경 변수를 통해 설정을 로드합니다.
.env는 rusaint_service/ 폴더 기준으로 로드 (실행 위치와 무관).
"""

from pathlib import Path

from pydantic_settings import BaseSettings
from typing import Optional

# rusaint_service/ 디렉터리 (config.py → app/core → app → rusaint_service)
_RUSAINT_ROOT = Path(__file__).resolve().parent.parent.parent


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
    # WAS와 동일한 시크릿 사용 (pseudonym = HMAC-SHA256(student_id)). 미설정 시 서버 기동 실패.
    pseudonym_secret: str = ""

    # 성적 등급 상수
    LOW_GRADE_RANKS: set[str] = {"C+", "C0", "C-", "D+", "D0", "D-"}
    FAIL_GRADE: str = "F"

    # CORS 설정 (환경 변수: 콤마 구분 문자열, e.g. "https://a.com,https://b.com")
    allowed_origins_raw: str = "http://localhost:8080"

    @property
    def allowed_origins(self) -> list[str]:
        return [o.strip() for o in self.allowed_origins_raw.split(",") if o.strip()]

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
        if not (self.pseudonym_secret or "").strip():
            raise ValueError(
                "PSEUDONYM_SECRET is required. "
                "Please set the environment variable or add it to .env file."
            )

    class Config:
        # 실행 위치(cwd)가 아니라 rusaint_service/ 폴더의 .env 로드
        env_file = _RUSAINT_ROOT / ".env"
        env_file_encoding = "utf-8"


# 전역 설정 인스턴스
settings = Settings()
