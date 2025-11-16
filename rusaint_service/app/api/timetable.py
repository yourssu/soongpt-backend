from fastapi import APIRouter, Depends

from app.core.security import verify_internal_jwt
from app.schemas.timetable import TimetableRequest, TimetableResponse
from app.services.filtering import filter_course_data
from app.services.rusaint_client import RusaintClient
from app.services.u_saint_auth import issue_u_saint_token

router = APIRouter()

rusaint_client = RusaintClient()


@router.post("/timetables", response_model=TimetableResponse)
async def create_timetable(
    payload: TimetableRequest,
    _=Depends(verify_internal_jwt),
) -> TimetableResponse:
    """
    WAS(Kotlin)에서 넘어온 pseudonym, studentId, sToken을 기반으로
    u-saint 토큰을 발급받고, rusaint로 시간표를 조회한 후 필터링하여 반환합니다.
    """
    u_saint_token = await issue_u_saint_token(
        student_id=payload.studentId,
        s_token=payload.sToken,
    )

    raw_courses = await rusaint_client.fetch_courses(
        u_saint_token=u_saint_token,
        student_id=payload.studentId,
    )

    filtered_courses = filter_course_data(raw_courses)

    return TimetableResponse(
        pseudonym=payload.pseudonym,
        courses=filtered_courses,
    )
