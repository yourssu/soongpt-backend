"""
Rusaint 라이브러리를 사용하여 유세인트 데이터를 크롤링하는 서비스.

SSO 토큰을 사용하여 유세인트에 로그인하고, 학적/성적 정보를 조회합니다.
공개 API만 정의하며, 세션·앱 생성은 session, 데이터 조회는 fetchers 모듈에 위임합니다.
"""

import asyncio
import logging
import time
from typing import List, Optional, Tuple

import rusaint

from app.core.config import settings
from app.core.security import generate_pseudonym
from app.schemas.usaint_schemas import UsaintSnapshotResponse
from app.services.constants import SEMESTER_TYPE_MAP
from app.services import session as session_module
from app.services import fetchers

logger = logging.getLogger(__name__)


class RusaintService:
    """Rusaint 라이브러리를 사용한 유세인트 데이터 크롤링 서비스"""

    # 하위 호환: 테스트 등에서 RusaintService.SEMESTER_TYPE_MAP 참조 유지
    SEMESTER_TYPE_MAP = SEMESTER_TYPE_MAP

    async def fetch_usaint_snapshot(
        self,
        student_id: str,
        s_token: str,
    ) -> UsaintSnapshotResponse:
        """
        SSO 토큰을 사용하여 유세인트 데이터를 조회합니다.

        Args:
            student_id: 학번
            s_token: SSO 토큰

        Returns:
            UsaintSnapshotResponse: 유세인트 데이터 스냅샷

        Raises:
            ValueError: SSO 토큰이 만료되었거나 유효하지 않을 때
            Exception: 기타 크롤링 중 발생한 예외
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
                (taken_courses, low_grade_codes, available_credits),
                flags,
                remaining_credits,
            ) = await asyncio.gather(
                fetchers.fetch_basic_info(student_info_app),
                fetchers.fetch_all_course_data_parallel(
                    course_grades_app1, course_grades_app2, SEMESTER_TYPE_MAP
                ),
                fetchers.fetch_flags(student_info_app),
                fetchers.fetch_remaining_credits(grad_app),
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
                availableCredits=available_credits,
                basicInfo=basic_info,
                remainingCredits=remaining_credits,
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
        """
        학적/성적 이력 조회 (졸업사정표 제외, 약 4-5초)

        포함 데이터:
        - 수강 내역 (takenCourses)
        - 저성적 과목 (lowGradeSubjectCodes)
        - 신청 가능 학점 (availableCredits)
        - 복수전공/교직 정보 (flags)
        - 기본 정보 (basicInfo)

        Args:
            student_id: 학번
            s_token: SSO 토큰

        Returns:
            dict: 학적/성적 이력 데이터
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

    async def fetch_usaint_graduation_info(
        self,
        student_id: str,
        s_token: str,
    ) -> dict:
        """
        졸업사정표 정보 조회 (약 5-6초)

        포함 데이터:
        - 개별 졸업 요건 상세 정보 (requirements)
        - 남은 졸업 학점 요약 (remainingCredits)

        Args:
            student_id: 학번
            s_token: SSO 토큰

        Returns:
            dict: 졸업사정표 데이터 (GraduationRequirements)
        """
        start_time = time.time()
        logger.info(
            f"유세인트 Graduation 데이터 조회 시작: student_id={student_id[:4]}****"
        )

        sessions: List[Tuple[str, Optional[rusaint.USaintSession]]] = []

        try:
            session_start = time.time()
            session_grad = await session_module.create_session(student_id, s_token)
            logger.info(f"세션 생성 완료: {time.time() - session_start:.2f}초")

            sessions = [("grad", session_grad)]

            try:
                app_start = time.time()
                grad_app = await session_module.get_graduation_app(session_grad)
                logger.info(f"Application 생성 완료: {time.time() - app_start:.2f}초")
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__} - {str(e)}",
                    exc_info=True,
                )
                await session_module.cleanup_sessions(sessions)
                raise

            data_start = time.time()
            graduation_reqs = await fetchers.fetch_graduation_requirements(grad_app)
            logger.info(f"데이터 조회 완료: {time.time() - data_start:.2f}초")

            total_time = time.time() - start_time
            logger.info(
                f"유세인트 Graduation 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)"
            )

            pseudonym = generate_pseudonym(student_id, settings.pseudonym_secret)
            return {
                "pseudonym": pseudonym,
                "graduationRequirements": graduation_reqs,
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
                f"유세인트 Graduation 데이터 조회 중 오류 발생 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True,
            )
            raise
        finally:
            await session_module.cleanup_sessions(sessions)
