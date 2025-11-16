"""
rusaint 서비스 엔트리포인트.

uvicorn 실행 시 편의를 위해 `main:app` 형태를 유지하고,
실제 애플리케이션 객체는 `app.main`에서 생성합니다.
"""

from app.main import app  # noqa: F401
