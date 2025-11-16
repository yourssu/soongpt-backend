from fastapi import FastAPI

from app.api.v1.routes import router as api_v1_router


def create_app() -> FastAPI:
    app = FastAPI(
        title="Rusaint Service",
        description="Unofficial u-saint client wrapper using rusaint.",
        version="0.1.0",
    )

    # 헬스체크
    @app.get("/api/health")
    async def health_check() -> dict:
        return {"status": "ok"}

    # v1 API 라우터 마운트
    app.include_router(api_v1_router, prefix="/api/v1")

    return app


app = create_app()
