"""
Rusaint 라이브러리를 사용한 유세인트 데이터 크롤링 서비스 (파사드).

- 학적/성적 이력: RusaintAcademicService 위임
- 졸업사정표: RusaintGraduationService 위임
- 전체 스냅샷(학적+졸업): 이 파일에서 직접 처리 (4세션 병렬)
"""

import asyncio
import logging
import time
from typing import List, Optional, Tuple

import rusaint

from app.schemas.usaint_schemas import UsaintSnapshotResponse
from app.services.constants import SEMESTER_TYPE_MAP
from app.services import session as session_module
from app.services import fetchers
from app.services.rusaint_academic_service import RusaintAcademicService
from app.services.rusaint_graduation_service import RusaintGraduationService

logger = logging.getLogger(__name__)


class RusaintService:
    """
    유세인트 크롤링 파사드.

    - fetch_usaint_snapshot: 전체 스냅샷 (학적+졸업, 4세션)
    - fetch_usaint_snapshot_academic: 학적/성적만 → Academic 서비스 위임
    - fetch_usaint_graduation_info: 졸업사정표만 → Graduation 서비스 위임
    """

    SEMESTER_TYPE_MAP = SEMESTER_TYPE_MAP

    def __init__(self) -> None:
        self._academic = RusaintAcademicService()
        self._graduation = RusaintGraduationService()

    async def fetch_usaint_snapshot(
        self,
        student_id: str,
        s_token: str,
    ) -> UsaintSnapshotResponse:
        """
        SSO 토큰으로 유세인트 전체 스냅샷 조회 (학적+졸업, 4세션 병렬).
        """
        start_time = time.time()
        logger.info(f"유세인트 데이터 조회 시작: student_id={student_id[:4]}****")

        sessions: List[Tuple[str, Optional[rusaint.USaintSession]]] = []

        try:
            session_start = time.time()
            session_grad, session_course1, session_course2, session_student = (
                await asyncio.gather(
                    session_module.create_session(student_id, s_token),
                    session_module.create_session(student_id, s_token),
                    session_module.create_session(student_id, s_token),
                    session_module.create_session(student_id, s_token),
                )
            )
            logger.info(f"세션 생성 완료: {time.time() - session_start:.2f}초")

            sessions = [
                ("grad", session_grad),
                ("course1", session_course1),
                ("course2", session_course2),
                ("student", session_student),
            ]

            try:
                app_start = time.time()
                grad_app, course_grades_app1, course_grades_app2, student_info_app = (
                    await asyncio.gather(
                        session_module.get_graduation_app(session_grad),
                        session_module.get_course_grades_app(session_course1),
                        session_module.get_course_grades_app(session_course2),
                        session_module.get_student_info_app(session_student),
                    )
                )
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__} - {str(e)}",
                    exc_info=True,
                )
                await session_module.cleanup_sessions(sessions)
                raise
            logger.info(f"Application 생성 완료: {time.time() - app_start:.2f}초")

            data_start = time.time()
            (
                basic_info,
                (taken_courses, low_grade_codes),
                flags,
            ) = await asyncio.gather(
                fetchers.fetch_basic_info(student_info_app),
                fetchers.fetch_all_course_data_parallel(
                    course_grades_app1, course_grades_app2, SEMESTER_TYPE_MAP
                ),
                fetchers.fetch_flags(student_info_app),
            )
            logger.info(f"데이터 조회 완료: {time.time() - data_start:.2f}초")

            total_time = time.time() - start_time
            logger.info(
                f"유세인트 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)"
            )

            return UsaintSnapshotResponse(
                takenCourses=taken_courses,
                lowGradeSubjectCodes=low_grade_codes,
                flags=flags,
                basicInfo=basic_info,
            )

        except rusaint.RusaintError as e:
            logger.error(
                f"Rusaint 오류 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True,
            )
            raise ValueError("유세인트 로그인에 실패했습니다. SSO 토큰을 확인해주세요.")
        except ValueError as e:
            logger.error(f"SSO 토큰 오류 (student_id={student_id[:4]}****): {str(e)}")
            raise
        except asyncio.TimeoutError:
            logger.error(f"유세인트 연결 시간 초과 (student_id={student_id[:4]}****)")
            raise ValueError("유세인트 연결 시간이 초과되었습니다.")
        except Exception as e:
            logger.error(
                f"유세인트 데이터 조회 중 오류 발생 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True,
            )
            raise
        finally:
            await session_module.cleanup_sessions(sessions)

    async def fetch_usaint_snapshot_academic(
        self,
        student_id: str,
        s_token: str,
    ) -> dict:
        """학적/성적 이력 조회 (졸업사정표 제외). → Academic 서비스 위임."""
        return await self._academic.fetch_usaint_snapshot_academic(
            student_id, s_token
        )

    async def fetch_usaint_graduation_info(
        self,
        student_id: str,
        s_token: str,
    ) -> dict:
        """졸업사정표 조회. → Graduation 서비스 위임."""
        return await self._graduation.fetch_usaint_graduation_info(
            student_id, s_token
        )
