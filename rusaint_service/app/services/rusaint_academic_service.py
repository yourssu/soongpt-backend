"""
유세인트 학적/성적 이력 조회 서비스.

졸업사정표 제외. 수강 내역, 저성적, 신청가능학점, 복수전공/교직, 기본 정보만 조회.
"""

import asyncio
import logging
import time
from typing import List, Optional, Tuple

import rusaint

from app.core.config import settings
from app.core.security import generate_pseudonym
from app.services.constants import SEMESTER_TYPE_MAP
from app.services import session as session_module
from app.services import fetchers

logger = logging.getLogger(__name__)


class RusaintAcademicService:
    """유세인트 학적/성적 이력 조회 (졸업사정표 제외, 약 4-5초)"""

    async def fetch_usaint_snapshot_academic(
        self,
        student_id: str,
        s_token: str,
    ) -> dict:
        """
        학적/성적 이력 조회 (졸업사정표 제외).

        포함: 수강 내역, 저성적 과목, 신청 가능 학점, 복수전공/교직, 기본 정보.
        """
        start_time = time.time()
        logger.info(
            f"유세인트 Academic 데이터 조회 시작: student_id={student_id[:4]}****"
        )

        sessions: List[Tuple[str, Optional[rusaint.USaintSession]]] = []

        try:
            session_start = time.time()
            session_course1, session_course2, session_student = await asyncio.gather(
                session_module.create_session(student_id, s_token),
                session_module.create_session(student_id, s_token),
                session_module.create_session(student_id, s_token),
            )
            logger.info(f"세션 생성 완료: {time.time() - session_start:.2f}초")

            sessions = [
                ("course1", session_course1),
                ("course2", session_course2),
                ("student", session_student),
            ]

            try:
                app_start = time.time()
                course_grades_app1, course_grades_app2, student_info_app = (
                    await asyncio.gather(
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
                (taken_courses, low_grade_codes, available_credits),
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
                f"유세인트 Academic 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)"
            )

            pseudonym = generate_pseudonym(student_id, settings.pseudonym_secret)
            return {
                "pseudonym": pseudonym,
                "takenCourses": taken_courses,
                "lowGradeSubjectCodes": low_grade_codes,
                "flags": flags,
                "availableCredits": available_credits,
                "basicInfo": basic_info,
            }

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
                f"유세인트 Academic 데이터 조회 중 오류 발생 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True,
            )
            raise
        finally:
            await session_module.cleanup_sessions(sessions)
