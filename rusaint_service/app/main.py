from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.usaint_router import router as usaint_router
from app.core.config import settings
import logging

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
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
    # 프로덕션에서는 WAS의 origin만 허용하도록 설정
    allowed_origins = ["http://localhost:8080"]  # TODO: 환경 변수로 관리
    app.add_middleware(
        CORSMiddleware,
        allow_origins=allowed_origins,
        allow_credentials=True,
        allow_methods=["POST", "GET"],
        allow_headers=["Content-Type", "Authorization"],
    )

    # 라우터 등록
    app.include_router(usaint_router)

    # 헬스체크
    @app.get("/api/health")
    async def health_check() -> dict:
        return {"status": "ok", "service": settings.app_name}

    logger.info(f"{settings.app_name} v{settings.app_version} 시작")

    return app


app = create_app()
