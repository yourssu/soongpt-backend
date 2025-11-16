from fastapi import FastAPI

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

    return app


app = create_app()
