"""
유세인트 데이터 조회 API 라우터.

WAS(Kotlin) <-> rusaint-service(Python) 간 내부 통신용 엔드포인트.
WAS는 two-track으로 /snapshot/academic → 0.5초 후 /snapshot/graduation 호출 후 병합합니다.
"""

import asyncio
from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.usaint_schemas import UsaintSnapshotRequest
from app.services.rusaint_service import RusaintService
from app.services.exceptions import (
    SSOTokenError,
    RusaintConnectionError,
    RusaintTimeoutError,
    RusaintInternalError,
)
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
    "/validate-token",
    status_code=status.HTTP_200_OK,
    summary="SSO 토큰 유효성 검증",
    description="SSO 토큰으로 세션 생성만 시도하여 유효성을 검증합니다 (약 1-2초).",
)
async def validate_token(
    request: UsaintSnapshotRequest,
    _: dict = Depends(verify_internal_jwt),
    rusaint_service: RusaintService = Depends(get_rusaint_service),
) -> dict:
    """
    SSO 토큰 유효성을 검증합니다.

    세션 생성만 시도하고 즉시 종료합니다 (데이터 조회 없음).
    콜백 시점에 sToken 만료 여부를 빠르게 확인하는 용도입니다.

    Returns:
        {"valid": true} - 토큰이 유효한 경우

    Raises:
        401 - 토큰이 유효하지 않거나 만료된 경우
    """
    try:
        await rusaint_service.validate_token(
            student_id=request.studentId,
            s_token=request.sToken,
        )
        return {"valid": True}

    except SSOTokenError as e:
        logger.warning(f"SSO 토큰 만료/무효: student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"SSO token is invalid or expired: {str(e)}",
        )
    except RusaintConnectionError as e:
        logger.error(f"숭실대 서버 연결 실패: student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to connect to university server: {str(e)}",
        )
    except (RusaintTimeoutError, asyncio.TimeoutError) as e:
        logger.error(f"숭실대 서버 응답 시간 초과: student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail=f"University server response timeout: {str(e)}",
        )
    except RusaintInternalError as e:
        logger.error(f"rusaint 내부 오류: student_id={request.studentId[:4]}****, detail={str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal rusaint error: {str(e)}",
        )
    except Exception as e:
        logger.error(
            f"SSO 토큰 검증 중 예기치 않은 오류: student_id={request.studentId[:4]}****, error_type={type(e).__name__}, detail={str(e)}",
            exc_info=True,
        )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Unexpected error during token validation: {type(e).__name__}",
        )



@router.post(
    "/snapshot/academic",
    status_code=status.HTTP_200_OK,
    summary="유세인트 학적/성적 이력 조회 (졸업사정표 제외)",
    description="SSO 토큰을 사용하여 학적/성적 이력을 조회합니다 (약 4-5초). 졸업사정표는 제외됩니다.",
)
async def fetch_usaint_snapshot_academic(
    request: UsaintSnapshotRequest,
    _: dict = Depends(verify_internal_jwt),
    rusaint_service: RusaintService = Depends(get_rusaint_service),
) -> dict:
    """
    유세인트 학적/성적 이력을 조회합니다 (졸업사정표 제외).

    포함 데이터:
    - 수강 내역 (takenCourses)
    - 저성적 과목 (lowGradeSubjectCodes)
    - 복수전공/교직 정보 (flags)
    - 기본 정보 (basicInfo)

    내부 JWT 인증이 필요합니다.
    """
    try:
        snapshot = await rusaint_service.fetch_usaint_snapshot_academic(
            student_id=request.studentId,
            s_token=request.sToken,
        )
        return snapshot

    except SSOTokenError as e:
        logger.warning(f"SSO 토큰 만료/무효 (academic): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"SSO token is invalid or expired: {str(e)}",
        )
    except RusaintConnectionError as e:
        logger.error(f"숭실대 서버 연결 실패 (academic): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to connect to university server: {str(e)}",
        )
    except (RusaintTimeoutError, asyncio.TimeoutError) as e:
        logger.error(f"숭실대 서버 응답 시간 초과 (academic): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail=f"University server response timeout: {str(e)}",
        )
    except RusaintInternalError as e:
        logger.error(f"rusaint 내부 오류 (academic): student_id={request.studentId[:4]}****, detail={str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal rusaint error: {str(e)}",
        )
    except Exception as e:
        logger.error(
            f"유세인트 Academic 데이터 조회 중 예기치 않은 오류: student_id={request.studentId[:4]}****, error_type={type(e).__name__}, detail={str(e)}",
            exc_info=True,
        )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch usaint academic data: {type(e).__name__}",
        )


@router.post(
    "/snapshot/graduation",
    status_code=status.HTTP_200_OK,
    summary="유세인트 졸업사정표 조회",
    description="SSO 토큰을 사용하여 졸업사정표 정보를 조회합니다 (약 5-6초).",
)
async def fetch_usaint_graduation_info(
    request: UsaintSnapshotRequest,
    _: dict = Depends(verify_internal_jwt),
    rusaint_service: RusaintService = Depends(get_rusaint_service),
) -> dict:
    """
    유세인트 졸업사정표 정보를 조회합니다.

    포함 데이터:
    - 개별 졸업 요건 상세 정보 (requirements: 이름, 기준학점, 이수학점, 차이, 충족여부, 카테고리)
    - 남은 졸업 학점 요약 (remainingCredits: 하위 호환성)

    내부 JWT 인증이 필요합니다.
    """
    try:
        graduation_info = await rusaint_service.fetch_usaint_graduation_info(
            student_id=request.studentId,
            s_token=request.sToken,
        )
        return graduation_info

    except SSOTokenError as e:
        logger.warning(f"SSO 토큰 만료/무효 (graduation): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"SSO token is invalid or expired: {str(e)}",
        )
    except RusaintConnectionError as e:
        logger.error(f"숭실대 서버 연결 실패 (graduation): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Failed to connect to university server: {str(e)}",
        )
    except (RusaintTimeoutError, asyncio.TimeoutError) as e:
        logger.error(f"숭실대 서버 응답 시간 초과 (graduation): student_id={request.studentId[:4]}****, detail={str(e)}")
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail=f"University server response timeout: {str(e)}",
        )
    except RusaintInternalError as e:
        logger.error(f"rusaint 내부 오류 (graduation): student_id={request.studentId[:4]}****, detail={str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal rusaint error: {str(e)}",
        )
    except Exception as e:
        logger.error(
            f"유세인트 Graduation 데이터 조회 중 예기치 않은 오류: student_id={request.studentId[:4]}****, error_type={type(e).__name__}, detail={str(e)}",
            exc_info=True,
        )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch usaint graduation data: {type(e).__name__}",
        )
