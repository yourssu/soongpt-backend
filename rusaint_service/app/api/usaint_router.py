"""
유세인트 데이터 조회 API 라우터.

WAS(Kotlin) <-> rusaint-service(Python) 간 내부 통신용 엔드포인트.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.usaint_schemas import UsaintSyncRequest, UsaintSnapshotResponse
from app.services.rusaint_service import RusaintService
from app.core.security import verify_internal_jwt
import logging

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api/usaint",
    tags=["usaint"],
)


def get_rusaint_service() -> RusaintService:
    """RusaintService 인스턴스를 반환하는 의존성 함수"""
    return RusaintService()


@router.post(
    "/snapshot",
    response_model=UsaintSnapshotResponse,
    status_code=status.HTTP_200_OK,
    summary="유세인트 데이터 스냅샷 조회",
    description="SSO 토큰을 사용하여 유세인트 학적/성적 정보를 조회합니다. (내부 API)",
)
async def fetch_usaint_snapshot(
    request: UsaintSyncRequest,
    _: dict = Depends(verify_internal_jwt),
    rusaint_service: RusaintService = Depends(get_rusaint_service),
) -> UsaintSnapshotResponse:
    """
    유세인트 데이터 스냅샷을 조회합니다.

    - **studentId**: 학번 (8자리)
    - **sToken**: SSO 토큰

    내부 JWT 인증이 필요합니다 (Authorization: Bearer {internal-jwt}).
    """
    try:
        snapshot = await rusaint_service.fetch_usaint_snapshot(
            student_id=request.studentId,
            s_token=request.sToken,
        )
        return snapshot

    except ValueError as e:
        # SSO 토큰 만료 또는 유효하지 않음
        logger.error(f"SSO 토큰 오류: student_id={request.studentId[:4]}****, error={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="SSO token is invalid or expired",
        )
    except Exception as e:
        # 기타 서버 오류
        logger.error(f"유세인트 데이터 조회 중 오류 발생: student_id={request.studentId[:4]}****, error={str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch usaint data",
        )
