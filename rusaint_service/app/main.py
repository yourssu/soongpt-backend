from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from app.api.usaint_router import router as usaint_router
from app.core.config import settings
import logging

# 로깅 설정
logging.basicConfig(
    level=logging.INFO if not settings.debug else logging.DEBUG,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)

logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.app_name,
        description="Unofficial u-saint client wrapper using rusaint.",
        version=settings.app_version,
        debug=settings.debug,
    )

    # CORS 설정 (내부 API이므로 최소한으로 제한)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.allowed_origins,
        allow_credentials=True,
        allow_methods=["POST", "GET"],
        allow_headers=["Content-Type", "Authorization"],
    )

    # 라우터 등록
    app.include_router(usaint_router)

    # 헬스체크 (기본)
    @app.get("/api/health")
    async def health_check() -> dict:
        """기본 헬스체크"""
        return {
            "status": "ok",
            "service": settings.app_name,
            "version": settings.app_version,
        }

    # Readiness 체크 (의존성 확인)
    @app.get("/api/health/ready")
    async def readiness_check() -> dict:
        """서비스 준비 상태 확인 (rusaint 라이브러리 로드 확인)"""
        try:
            import rusaint
            return {
                "status": "ready",
                "rusaint_available": True,
                "rusaint_timeout": settings.rusaint_timeout,
            }
        except ImportError:
            logger.error("rusaint 라이브러리를 로드할 수 없습니다")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="rusaint library not available",
            )

    logger.info(f"{settings.app_name} v{settings.app_version} 시작")

    return app


app = create_app()
