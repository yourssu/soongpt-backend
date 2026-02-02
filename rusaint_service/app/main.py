from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from app.api.usaint_router import router as usaint_router
from app.core.config import settings
import logging

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

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.allowed_origins,
        allow_credentials=True,
        allow_methods=["POST", "GET"],
        allow_headers=["Content-Type", "Authorization"],
    )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(_request, exc: RequestValidationError):
        return JSONResponse(status_code=422, content={"detail": exc.errors()})

    app.include_router(usaint_router)

    @app.get("/api/health")
    async def health_check() -> dict:
        return {
            "status": "ok",
            "service": settings.app_name,
            "version": settings.app_version,
        }

    @app.get("/api/health/ready")
    async def readiness_check() -> dict:
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

    return app


app = create_app()
