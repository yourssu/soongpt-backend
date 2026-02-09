"""
유세인트 세션 및 Application 생성/정리.

SSO 토큰으로 세션을 만들고, GraduationRequirements / CourseGrades / StudentInformation
Application을 생성합니다. 사용 후 세션 정리도 담당합니다.
"""

import asyncio
import logging
from typing import List, Optional, Tuple

import rusaint
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

from app.core.config import settings
from app.services.exceptions import (
    SSOTokenError,
    RusaintConnectionError,
    RusaintTimeoutError,
    RusaintInternalError,
)

logger = logging.getLogger(__name__)


async def cleanup_sessions(
    sessions: List[Tuple[str, Optional[rusaint.USaintSession]]],
) -> None:
    """
    세션 리스트를 안전하게 종료합니다.

    모든 세션에 대해 close()를 호출하며, 개별 세션 종료 실패는 로깅만 하고 계속 진행합니다.

    Args:
        sessions: (세션명, 세션객체) 튜플의 리스트
    """
    for name, sess in sessions:
        if sess and hasattr(sess, "close"):
            try:
                await sess.close()
                logger.debug(f"세션 종료 완료: {name}")
            except Exception as e:
                logger.warning(
                    f"세션 종료 중 오류 ({name}): {type(e).__name__} - {str(e)}"
                )


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(asyncio.TimeoutError),
    reraise=True,
)
async def create_session(student_id: str, s_token: str) -> rusaint.USaintSession:
    """
    SSO 토큰으로 유세인트 세션을 생성합니다.

    네트워크 일시적 오류 시 최대 3회 재시도합니다.

    Args:
        student_id: 학번
        s_token: SSO 토큰

    Returns:
        USaintSession: 생성된 세션

    Raises:
        SSOTokenError: SSO 토큰이 유효하지 않거나 만료된 경우
        RusaintTimeoutError: 연결 시간 초과 (3회 재시도 후)
        RusaintConnectionError: 네트워크/DNS/SSL 등 연결 문제
        RusaintInternalError: rusaint 라이브러리 내부 오류
    """
    try:
        builder = rusaint.USaintSessionBuilder()
        session = await asyncio.wait_for(
            builder.with_token(student_id, s_token),
            timeout=settings.rusaint_timeout,
        )
        return session
    except asyncio.TimeoutError:
        logger.warning(
            f"세션 생성 시간 초과 (student_id={student_id[:4]}****) - 재시도 중..."
        )
        raise
    except rusaint.RusaintError as e:
        error_msg = str(e)
        logger.error(
            f"SSO 토큰 인증 실패 (student_id={student_id[:4]}****): {type(e).__name__}\n"
            f"에러 메시지: {error_msg}",
            exc_info=True,
        )
        raise SSOTokenError(f"SSO 토큰이 유효하지 않거나 만료되었습니다: {error_msg}")
    except (ConnectionError, OSError) as e:
        logger.error(
            f"숭실대 서버 연결 실패 (student_id={student_id[:4]}****): {type(e).__name__}\n"
            f"에러 메시지: {str(e)}",
            exc_info=True,
        )
        raise RusaintConnectionError(f"숭실대 서버 연결 실패: {type(e).__name__} - {str(e)}")
    except Exception as e:
        logger.error(
            f"세션 생성 중 예기치 않은 오류 (student_id={student_id[:4]}****): {type(e).__name__}\n"
            f"에러 메시지: {str(e)}",
            exc_info=True,
        )
        raise RusaintInternalError(f"rusaint 내부 오류: {type(e).__name__} - {str(e)}")


async def get_graduation_app(
    session: rusaint.USaintSession,
) -> rusaint.GraduationRequirementsApplication:
    """졸업요건 Application을 생성합니다."""
    grad_builder = rusaint.GraduationRequirementsApplicationBuilder()
    return await grad_builder.build(session)


async def get_course_grades_app(
    session: rusaint.USaintSession,
) -> rusaint.CourseGradesApplication:
    """성적 조회 Application을 생성합니다."""
    app_builder = rusaint.CourseGradesApplicationBuilder()
    return await app_builder.build(session)


async def get_student_info_app(
    session: rusaint.USaintSession,
) -> rusaint.StudentInformationApplication:
    """학생 정보 Application을 생성합니다."""
    app_builder = rusaint.StudentInformationApplicationBuilder()
    return await app_builder.build(session)
