from fastapi import APIRouter

from app.api.v1.timetable import router as timetable_router

router = APIRouter()

# 시간표 관련 엔드포인트
router.include_router(timetable_router, tags=["timetable"])
