"""
유세인트 졸업사정표 조회 서비스.

졸업 요건 상세(raw) + 핵심 요약(graduationSummary) 조회 (약 5-6초).
"""

import asyncio
import logging
import time
from typing import List, Optional, Tuple

import rusaint

from app.core.config import settings
from app.core.security import generate_pseudonym
from app.services import session as session_module
from app.services import fetchers
from app.services.graduation_summary_builder import build_graduation_summary

logger = logging.getLogger(__name__)


class RusaintGraduationService:
    """유세인트 졸업사정표 조회 (약 5-6초)"""

    async def fetch_usaint_graduation_info(
        self,
        student_id: str,
        s_token: str,
    ) -> dict:
        """
        졸업사정표 정보 조회.

        포함: 개별 졸업 요건 상세(requirements, raw), 핵심 요약(graduationSummary).
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

            # 핵심 요약 생성 (name 기반 분류)
            graduation_summary = build_graduation_summary(graduation_reqs.requirements)

            pseudonym = generate_pseudonym(student_id, settings.pseudonym_secret)
            return {
                "pseudonym": pseudonym,
                "graduationRequirements": graduation_reqs,
                "graduationSummary": graduation_summary,
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
