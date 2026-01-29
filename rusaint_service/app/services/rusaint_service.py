"""
Rusaint 라이브러리를 사용하여 유세인트 데이터를 크롤링하는 서비스.

SSO 토큰을 사용하여 유세인트에 로그인하고, 학적/성적 정보를 조회합니다.
"""

from app.schemas.usaint_schemas import (
    UsaintSnapshotResponse,
    TakenCourse,
    LowGradeSubjectCodes,
    Flags,
    AvailableCredits,
    BasicInfo,
    RemainingCredits,
    GraduationRequirementItem,
    GraduationRequirements,
)
from app.core.config import settings
import rusaint
import logging
import asyncio
import time
from typing import Dict, List, Optional

logger = logging.getLogger(__name__)


class RusaintService:
    """Rusaint 라이브러리를 사용한 유세인트 데이터 크롤링 서비스"""

    # 학기 타입 매핑 상수
    SEMESTER_TYPE_MAP = {
        rusaint.SemesterType.ONE: "1",
        rusaint.SemesterType.TWO: "2",
        rusaint.SemesterType.SUMMER: "SUMMER",
        rusaint.SemesterType.WINTER: "WINTER",
    }

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

        # 세션 명시적 추적
        sessions: List[tuple[str, Optional[rusaint.USaintSession]]] = []
        
        try:
            # 1. SSO 토큰으로 세션 4개 병렬 생성
            session_grad, session_course1, session_course2, session_student = await asyncio.gather(
                self._create_session(student_id, s_token),
                self._create_session(student_id, s_token),
                self._create_session(student_id, s_token),
                self._create_session(student_id, s_token),
            )
            
            # 생성된 세션 즉시 추적 리스트에 추가
            sessions = [
                ('grad', session_grad),
                ('course1', session_course1),
                ('course2', session_course2),
                ('student', session_student),
            ]

            # 2. Application 4개 병렬 생성 (GraduationRequirements, CourseGrades x2, StudentInformation)
            try:
                grad_app, course_grades_app1, course_grades_app2, student_info_app = await asyncio.gather(
                    self._get_graduation_app(session_grad),
                    self._get_course_grades_app(session_course1),
                    self._get_course_grades_app(session_course2),
                    self._get_student_info_app(session_student),
                )
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__} - {str(e)}",
                    exc_info=True
                )
                # Application 생성 실패 시 명시적 세션 정리
                await self._cleanup_sessions(sessions)
                raise

            # 3. 데이터 병렬 조회
            (
                basic_info,
                (taken_courses, low_grade_codes, available_credits),
                flags,
                remaining_credits,
            ) = await asyncio.gather(
                self._fetch_basic_info(student_info_app),
                self._fetch_all_course_data_parallel(course_grades_app1, course_grades_app2),
                self._fetch_flags(student_info_app),
                self._fetch_remaining_credits(grad_app),
            )

            total_time = time.time() - start_time
            logger.info(f"유세인트 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)")

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
                exc_info=True
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
                exc_info=True
            )
            raise
        finally:
            # 모든 세션 명시적 종료 (예외 발생 여부와 무관하게 실행)
            await self._cleanup_sessions(sessions)

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
        logger.info(f"유세인트 Academic 데이터 조회 시작: student_id={student_id[:4]}****")

        # 세션 명시적 추적
        sessions: List[tuple[str, Optional[rusaint.USaintSession]]] = []
        
        try:
            # 1. 세션 3개 병렬 생성 (GraduationRequirements 제외!)
            session_course1, session_course2, session_student = await asyncio.gather(
                self._create_session(student_id, s_token),
                self._create_session(student_id, s_token),
                self._create_session(student_id, s_token),
            )
            
            # 생성된 세션 즉시 추적 리스트에 추가
            sessions = [
                ('course1', session_course1),
                ('course2', session_course2),
                ('student', session_student),
            ]

            # 2. Application 3개 병렬 생성
            try:
                course_grades_app1, course_grades_app2, student_info_app = await asyncio.gather(
                    self._get_course_grades_app(session_course1),
                    self._get_course_grades_app(session_course2),
                    self._get_student_info_app(session_student),
                )
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__} - {str(e)}",
                    exc_info=True
                )
                # Application 생성 실패 시 명시적 세션 정리
                await self._cleanup_sessions(sessions)
                raise

            # 3. 데이터 병렬 조회
            (
                basic_info,
                (taken_courses, low_grade_codes, available_credits),
                flags,
            ) = await asyncio.gather(
                self._fetch_basic_info(student_info_app),
                self._fetch_all_course_data_parallel(course_grades_app1, course_grades_app2),
                self._fetch_flags(student_info_app),
            )

            total_time = time.time() - start_time
            logger.info(f"유세인트 Academic 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)")

            return {
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
                exc_info=True
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
                exc_info=True
            )
            raise
        finally:
            # 모든 세션 명시적 종료 (예외 발생 여부와 무관하게 실행)
            await self._cleanup_sessions(sessions)

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
        logger.info(f"유세인트 Graduation 데이터 조회 시작: student_id={student_id[:4]}****")

        # 세션 명시적 추적
        sessions: List[tuple[str, Optional[rusaint.USaintSession]]] = []
        
        try:
            # 1. 세션 1개 생성
            session_grad = await self._create_session(student_id, s_token)
            
            # 생성된 세션 즉시 추적 리스트에 추가
            sessions = [('grad', session_grad)]

            # 2. GraduationRequirements Application 생성
            try:
                grad_app = await self._get_graduation_app(session_grad)
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__} - {str(e)}",
                    exc_info=True
                )
                # Application 생성 실패 시 명시적 세션 정리
                await self._cleanup_sessions(sessions)
                raise

            # 3. 졸업 요건 상세 정보 조회
            graduation_reqs = await self._fetch_graduation_requirements(grad_app)

            total_time = time.time() - start_time
            logger.info(f"유세인트 Graduation 데이터 조회 완료: student_id={student_id[:4]}**** (총 {total_time:.2f}초)")

            return {
                "graduationRequirements": graduation_reqs,
            }

        except rusaint.RusaintError as e:
            logger.error(
                f"Rusaint 오류 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True
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
                exc_info=True
            )
            raise
        finally:
            # 모든 세션 명시적 종료 (예외 발생 여부와 무관하게 실행)
            await self._cleanup_sessions(sessions)

    # ============================================================
    # Private Methods - rusaint 라이브러리 실제 구현
    # ============================================================

    async def _cleanup_sessions(self, sessions: List[tuple[str, Optional[rusaint.USaintSession]]]) -> None:
        """
        세션 리스트를 안전하게 종료합니다.
        
        모든 세션에 대해 close()를 호출하며, 개별 세션 종료 실패는 로깅만 하고 계속 진행합니다.
        
        Args:
            sessions: (세션명, 세션객체) 튜플의 리스트
        """
        for name, sess in sessions:
            if sess and hasattr(sess, 'close'):
                try:
                    await sess.close()
                    logger.debug(f"세션 종료 완료: {name}")
                except Exception as e:
                    logger.warning(f"세션 종료 중 오류 ({name}): {type(e).__name__} - {str(e)}")

    async def _create_session(self, student_id: str, s_token: str) -> rusaint.USaintSession:
        """
        SSO 토큰으로 유세인트 세션을 생성합니다.

        Args:
            student_id: 학번
            s_token: SSO 토큰

        Returns:
            USaintSession: 생성된 세션

        Raises:
            ValueError: SSO 토큰이 유효하지 않을 때
            asyncio.TimeoutError: 연결 시간 초과
        """
        try:
            builder = rusaint.USaintSessionBuilder()
            session = await asyncio.wait_for(
                builder.with_token(student_id, s_token),
                timeout=settings.rusaint_timeout,
            )
            return session
        except asyncio.TimeoutError:
            logger.error(f"세션 생성 시간 초과 (student_id={student_id[:4]}****)")
            raise
        except Exception as e:
            logger.error(
                f"세션 생성 실패 (student_id={student_id[:4]}****): {type(e).__name__}\n"
                f"에러 메시지: {str(e)}",
                exc_info=True
            )
            raise ValueError("SSO 토큰이 유효하지 않거나 만료되었습니다.")

    async def _get_graduation_app(self, session: rusaint.USaintSession):
        """
        졸업요건 애플리케이션을 생성합니다.

        기본 정보와 남은 학점 조회에서 재사용하여 중복 API 호출을 방지합니다.

        Args:
            session: 유세인트 세션

        Returns:
            GraduationRequirementsApplication: 졸업요건 애플리케이션
        """
        grad_builder = rusaint.GraduationRequirementsApplicationBuilder()
        grad_app = await grad_builder.build(session)
        return grad_app

    async def _get_course_grades_app(self, session: rusaint.USaintSession):
        """
        성적 조회 애플리케이션을 생성합니다.

        수강 내역, 저성적 과목, 신청 가능 학점 조회에서 재사용합니다.

        Args:
            session: 유세인트 세션

        Returns:
            CourseGradesApplication: 성적 조회 애플리케이션
        """
        app_builder = rusaint.CourseGradesApplicationBuilder()
        app = await app_builder.build(session)
        return app

    async def _get_student_info_app(self, session: rusaint.USaintSession):
        """
        학생 정보 애플리케이션을 생성합니다.

        복수전공/부전공/교직 정보 조회에서 사용합니다.

        Args:
            session: 유세인트 세션

        Returns:
            StudentInformationApplication: 학생 정보 애플리케이션
        """
        app_builder = rusaint.StudentInformationApplicationBuilder()
        app = await app_builder.build(session)
        return app

    async def _fetch_basic_info(self, student_info_app) -> BasicInfo:
        """
        기본 학적 정보를 조회합니다.

        **민감 정보는 조회하지 않음**: 이름, 주민번호, 주소, 전화번호 등은 가져오지 않습니다.
        **휴학/엇학기/졸업유예 고려**: 계산이 아니라 유세인트에서 직접 크롤링

        Args:
            student_info_app: 학생정보 애플리케이션
        """
        try:
            student_info = await student_info_app.general()

            # 입학년도, 현재 학년, 재학 누적 학기
            admission_year = getattr(student_info, 'apply_year', None) or getattr(student_info, 'admission_year', 2020)
            grade = getattr(student_info, 'grade', 1)
            semester = getattr(student_info, 'semester', 1)

            # 학과 정보
            department = getattr(student_info, 'major', None) or getattr(student_info, 'department', "알 수 없음")
            if hasattr(student_info, 'majors') and student_info.majors:
                department = student_info.majors[0]

            return BasicInfo(
                year=admission_year,
                grade=grade,
                semester=semester,
                department=department,
            )
        except Exception as e:
            logger.error(f"기본 학적 정보 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_all_course_data_parallel(
        self,
        course_grades_app1,
        course_grades_app2,
    ) -> tuple[list[TakenCourse], LowGradeSubjectCodes, AvailableCredits]:
        """
        2개의 CourseGradesApplication으로 학기를 나눠서 병렬 조회합니다.

        **최대 성능 최적화**:
        - App 1: 학기 전반부 담당
        - App 2: 학기 후반부 담당
        - 진짜 병렬 처리로 classes() 호출 시간 단축!

        Args:
            course_grades_app1: CourseGradesApplication 인스턴스 #1
            course_grades_app2: CourseGradesApplication 인스턴스 #2

        Returns:
            tuple: (taken_courses, low_grade_codes, available_credits)
        """
        try:
            # 먼저 App 1에서 학기 목록 조회
            semesters = await course_grades_app1.semesters(rusaint.CourseType.BACHELOR)

            if not semesters:
                raise ValueError("학기 정보가 없습니다")

            # 학기를 2개 그룹으로 분할하여 각 Application에서 병렬 조회
            mid_point = len(semesters) // 2
            semesters_group1 = semesters[:mid_point]
            semesters_group2 = semesters[mid_point:]

            # 각 그룹별로 classes() 호출 준비
            tasks_group1 = [
                course_grades_app1.classes(
                    rusaint.CourseType.BACHELOR,
                    sem.year,
                    sem.semester,
                    include_details=False,
                )
                for sem in semesters_group1
            ]

            tasks_group2 = [
                course_grades_app2.classes(
                    rusaint.CourseType.BACHELOR,
                    sem.year,
                    sem.semester,
                    include_details=False,
                )
                for sem in semesters_group2
            ]

            # 두 그룹을 병렬 실행
            classes_group1, classes_group2 = await asyncio.gather(
                asyncio.gather(*tasks_group1),
                asyncio.gather(*tasks_group2),
            )

            # 결과 합치기
            all_semester_classes = list(classes_group1) + list(classes_group2)

            taken_courses = []
            pass_low_codes = []
            fail_codes = []

            # 각 학기별로 데이터 처리
            for semester_grade, classes in zip(semesters, all_semester_classes):
                # 과목 코드 추출
                subject_codes = [cls.code for cls in classes]

                # SemesterType 매핑
                semester_str = self.SEMESTER_TYPE_MAP.get(semester_grade.semester, "1")

                # 수강 내역 추가
                taken_courses.append(
                    TakenCourse(
                        year=semester_grade.year,
                        semester=semester_str,
                        subjectCodes=subject_codes,
                    )
                )

                # 저성적 과목 분류
                for cls in classes:
                    rank = getattr(cls, 'rank', None)
                    if not rank:
                        continue

                    rank_str = str(rank).upper().strip()
                    code = cls.code

                    if rank_str == settings.FAIL_GRADE:
                        fail_codes.append(code)
                    elif rank_str in settings.LOW_GRADE_RANKS:
                        pass_low_codes.append(code)

            # 저성적 과목 결과
            low_grade_codes = LowGradeSubjectCodes(
                passLow=pass_low_codes,
                fail=fail_codes,
            )

            # 직전 학기 정보로 신청 가능 학점 계산
            last_semester = semesters[-1]

            previous_gpa = 0.0
            for attr in ['gpa', 'grade_point_average', 'average']:
                if hasattr(last_semester, attr):
                    value = getattr(last_semester, attr)
                    if value is not None:
                        previous_gpa = float(value)
                        break

            carried_over = 0
            for attr in ['carried_over', 'carry_over', 'transferred_credits']:
                if hasattr(last_semester, attr):
                    value = getattr(last_semester, attr)
                    if value is not None:
                        carried_over = int(value)
                        break

            # 최대 신청 가능 학점 계산
            max_credits = 19.5
            if previous_gpa >= 4.0:
                max_credits = 22.5
            max_credits += carried_over

            available_credits = AvailableCredits(
                previousGpa=previous_gpa,
                carriedOverCredits=carried_over,
                maxAvailableCredits=max_credits,
            )

            return taken_courses, low_grade_codes, available_credits

        except Exception as e:
            logger.error(f"성적 관련 데이터 조회 실패 (병렬): {type(e).__name__}")
            raise

    async def _fetch_flags(self, student_info_app) -> Flags:
        """
        복수전공/부전공 및 교직 이수 정보를 조회합니다.

        **학과명만 조회**: 자격증 번호, 날짜 등 민감정보는 제외

        Args:
            student_info_app: StudentInformationApplication 인스턴스 (재사용)
        """
        try:
            # 자격 정보 조회 (교직만 필요)
            qualifications = await student_info_app.qualifications()

            # 교직 정보 확인
            teaching = False
            if qualifications.teaching_major:
                teaching_info = qualifications.teaching_major
                # major_name이 None이 아니면 교직 이수 중으로 판단
                teaching = teaching_info.major_name is not None

            # 일반 학생 정보에서 복수전공/부전공 확인
            student_info = await student_info_app.general()

            # rusaint의 StudentInformation 구조에서 복수전공/부전공 필드 탐색
            double_major = None
            minor = None

            # 가능한 필드명들 시도
            for attr in ['second_major', 'double_major', 'dual_major', 'major_double']:
                if hasattr(student_info, attr):
                    value = getattr(student_info, attr)
                    if value and str(value).strip():
                        double_major = value
                        break

            for attr in ['minor', 'minor_major', 'submajor']:
                if hasattr(student_info, attr):
                    value = getattr(student_info, attr)
                    if value and str(value).strip():
                        minor = value
                        break

            return Flags(
                doubleMajorDepartment=double_major,
                minorDepartment=minor,
                teaching=teaching,
            )
        except Exception as e:
            logger.warning(f"복수전공/교직 정보 조회 실패 (선택 정보): {type(e).__name__}")
            # 실패 시 기본값 반환 (선택 정보)
            return Flags(
                doubleMajorDepartment=None,
                minorDepartment=None,
                teaching=False,
            )

    async def _fetch_graduation_requirements(self, grad_app) -> GraduationRequirements:
        """
        졸업 요건 상세 정보를 조회합니다.

        **개별 요건 정보 포함**: 각 요건의 이름, 기준학점, 이수학점, 충족여부 등

        Args:
            grad_app: 졸업요건 애플리케이션

        Returns:
            GraduationRequirements: 개별 요건 목록 + 남은 학점 요약
        """
        try:
            # 졸업 요건 조회
            requirements = await grad_app.requirements()

            # 개별 요건 리스트
            requirement_list = []

            # 남은 학점 요약 (하위 호환성)
            major_required = 0
            major_elective = 0
            general_required = 0
            general_elective = 0

            # requirements.requirements는 딕셔너리
            if isinstance(requirements.requirements, dict):
                for key, req in requirements.requirements.items():
                    # 필드 추출 (rusaint GraduationRequirement 객체)
                    # key 예시: "학부-교양필수 19"
                    name = str(key)
                    requirement_value = getattr(req, 'requirement', None)
                    # 주의: rusaint 라이브러리의 오타 "calcuation" 그대로 사용
                    calculation_value = getattr(req, 'calcuation', None)
                    difference_value = getattr(req, 'difference', None)
                    result_value = getattr(req, 'result', False)
                    category = getattr(req, 'category', str(key))
                    # lectures 필드는 의도적으로 제외

                    # GraduationRequirementItem 객체 생성
                    requirement_list.append(
                        GraduationRequirementItem(
                            name=name,
                            requirement=requirement_value,
                            calculation=calculation_value,
                            difference=difference_value,
                            result=result_value,
                            category=category,
                        )
                    )

                    # 남은 학점 계산 (기존 로직 유지)
                    key_lower = name.lower()
                    if difference_value is not None:
                        remaining = int(abs(difference_value)) if difference_value < 0 else 0

                        if '전필' in key_lower:
                            major_required += remaining
                        elif '전선' in key_lower or '전공선택' in key_lower:
                            major_elective += remaining
                        elif '교필' in key_lower or '교양필수' in key_lower:
                            general_required += remaining
                        elif '교선' in key_lower or '교양선택' in key_lower:
                            general_elective += remaining

            # 남은 학점 요약
            remaining_credits = RemainingCredits(
                majorRequired=major_required,
                majorElective=major_elective,
                generalRequired=general_required,
                generalElective=general_elective,
            )

            return GraduationRequirements(
                requirements=requirement_list,
                remainingCredits=remaining_credits,
            )

        except Exception as e:
            logger.error(f"졸업 요건 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_remaining_credits(self, grad_app) -> RemainingCredits:
        """
        졸업까지 남은 이수 학점 정보를 조회합니다.

        **학점 정보만 조회**: 과목별 상세 정보는 제외
        **Deprecated**: _fetch_graduation_requirements 사용 권장

        Args:
            grad_app: 졸업요건 애플리케이션 (중복 API 호출 방지)
        """
        try:
            # 졸업 요건 조회
            requirements = await grad_app.requirements()

            # 남은 학점 추출
            major_required = 0
            major_elective = 0
            general_required = 0
            general_elective = 0

            # requirements.requirements는 딕셔너리
            if isinstance(requirements.requirements, dict):
                for key, req in requirements.requirements.items():
                    key_lower = str(key).lower()

                    # difference 필드 확인 (음수면 부족한 학점)
                    diff = getattr(req, 'difference', None)
                    if diff is None:
                        continue

                    # 음수면 절대값 (부족한 학점), 양수면 0 (이미 충족)
                    remaining = int(abs(diff)) if diff < 0 else 0

                    # 카테고리별 분류
                    if '전필' in key_lower:
                        major_required += remaining
                    elif '전선' in key_lower or '전공선택' in key_lower:
                        major_elective += remaining
                    elif '교필' in key_lower or '교양필수' in key_lower:
                        general_required += remaining
                    elif '교선' in key_lower or '교양선택' in key_lower:
                        general_elective += remaining

            return RemainingCredits(
                majorRequired=major_required,
                majorElective=major_elective,
                generalRequired=general_required,
                generalElective=general_elective,
            )
        except Exception as e:
            logger.error(f"졸업 요건 조회 실패: {type(e).__name__}")
            raise
