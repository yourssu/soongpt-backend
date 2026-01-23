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

        session: Optional[rusaint.USaintSession] = None
        try:
            # 1. SSO 토큰으로 세션 생성 (타임아웃 적용)
            t1 = time.time()
            session = await self._create_session(student_id, s_token)
            logger.info(f"[성능] 세션 생성: {time.time() - t1:.2f}초")

            # 2. Application 객체 생성 (2+1 전략: 안정성 확보)
            t2 = time.time()

            try:
                # 핵심 2개는 병렬로
                logger.debug("Application 생성 시작: GraduationRequirements + CourseGrades (병렬)")
                grad_app, course_grades_app = await asyncio.gather(
                    self._get_graduation_app(session),
                    self._get_course_grades_app(session),
                )
                logger.debug("병렬 생성 완료")

                # 짧은 딜레이로 서버 안정화
                await asyncio.sleep(0.08)

                # 마지막 1개는 순차로
                logger.debug("Application 생성 시작: StudentInformation (순차)")
                student_info_app = await self._get_student_info_app(session)
                logger.debug("순차 생성 완료")

                logger.info(f"[성능] Application 생성 (2+1): {time.time() - t2:.2f}초")
            except Exception as e:
                logger.error(
                    f"Application 생성 실패: {type(e).__name__}\n"
                    f"에러 메시지: {str(e)}",
                    exc_info=True
                )
                raise

            # 3. 각 Application으로 데이터 병렬 조회
            t3 = time.time()
            (
                basic_info,
                (taken_courses, low_grade_codes, available_credits),
                flags,
                remaining_credits,
            ) = await asyncio.gather(
                self._fetch_basic_info(grad_app),
                self._fetch_all_course_data(course_grades_app),  # 통합!
                self._fetch_flags(student_info_app),
                self._fetch_remaining_credits(grad_app),
            )
            logger.info(f"[성능] 데이터 조회 (병렬): {time.time() - t3:.2f}초")

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
            # 세션 명시적 종료
            if session and hasattr(session, 'close'):
                try:
                    await session.close()
                    logger.debug("유세인트 세션 종료 완료")
                except Exception as e:
                    logger.warning(f"세션 종료 중 오류: {type(e).__name__}")

    # ============================================================
    # Private Methods - rusaint 라이브러리 실제 구현
    # ============================================================

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
            logger.debug(f"유세인트 세션 생성 완료: student_id={student_id[:4]}****")
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
        logger.debug("GraduationRequirementsApplication 생성 완료")
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
        logger.debug("CourseGradesApplication 생성 완료")
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
        logger.debug("StudentInformationApplication 생성 완료")
        return app

    async def _fetch_basic_info(self, grad_app) -> BasicInfo:
        """
        기본 학적 정보를 조회합니다.

        **민감 정보는 조회하지 않음**: 이름, 주민번호, 주소, 전화번호 등은 가져오지 않습니다.
        **휴학/엇학기/졸업유예 고려**: 계산이 아니라 유세인트에서 직접 크롤링

        Args:
            grad_app: 졸업요건 애플리케이션 (중복 API 호출 방지)
        """
        try:
            grad_student = await grad_app.student_info()

            # 입학년도, 현재 학년, 재학 누적 학기 (유세인트에서 직접 제공)
            admission_year = grad_student.apply_year  # 입학년도
            grade = grad_student.grade  # 현재 학년 (휴학/엇학기 고려됨)
            semester = grad_student.semester  # 재학 누적 학기

            # 학과 정보 (majors 리스트에서 첫 번째 전공)
            department = grad_student.majors[0] if grad_student.majors else "알 수 없음"

            logger.debug(f"기본 정보: {admission_year}학번 {grade}학년 {semester}학기, {department}")

            return BasicInfo(
                year=admission_year,  # 입학년도
                grade=grade,  # 현재 학년 (유세인트에서 직접 제공)
                semester=semester,  # 재학 누적 학기 (유세인트에서 직접 제공)
                department=department,
            )
        except Exception as e:
            logger.error(f"기본 학적 정보 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_all_course_data(
        self,
        course_grades_app,
    ) -> tuple[list[TakenCourse], LowGradeSubjectCodes, AvailableCredits]:
        """
        CourseGradesApplication을 재사용하여 모든 성적 관련 데이터를 조회합니다.

        **최대 최적화**: Application 재사용 + semesters() 한 번만 호출
        - 수강 내역 (takenCourses)
        - 저성적 과목 (lowGradeSubjectCodes)
        - 신청 가능 학점 (availableCredits)

        Args:
            course_grades_app: CourseGradesApplication 인스턴스

        Returns:
            tuple: (taken_courses, low_grade_codes, available_credits)
        """
        try:
            # 학기별 성적 정보 조회 (한 번만!)
            semesters = await course_grades_app.semesters(rusaint.CourseType.BACHELOR)

            if not semesters:
                raise ValueError("학기 정보가 없습니다")

            # 🚀 핵심 최적화: 모든 학기의 classes를 병렬로 조회
            tasks = [
                course_grades_app.classes(
                    rusaint.CourseType.BACHELOR,
                    semester_grade.year,
                    semester_grade.semester,
                    include_details=False,
                )
                for semester_grade in semesters
            ]
            all_semester_classes = await asyncio.gather(*tasks)

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

            logger.debug(
                f"통합 조회 완료 - 수강: {len(taken_courses)}개 학기, "
                f"저성적: C/D {len(pass_low_codes)}개 F {len(fail_codes)}개, "
                f"평점: {previous_gpa}, 최대학점: {max_credits}"
            )

            return taken_courses, low_grade_codes, available_credits

        except Exception as e:
            logger.error(f"성적 관련 데이터 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_courses_and_grades(
        self,
        session: rusaint.USaintSession,
    ) -> tuple[list[TakenCourse], LowGradeSubjectCodes]:
        """
        수강 내역과 저성적 과목을 한 번에 조회합니다 (성능 최적화).

        **DEPRECATED**: _fetch_all_course_data() 사용 권장
        **중복 제거**: CourseGradesApplication과 semesters() API 호출을 한 번만 수행
        **과목 코드만 조회**: 과목명, 교수명 등 민감정보는 제외
        **계절학기 포함**: SUMMER(3), WINTER(4)로 구분

        Returns:
            tuple: (taken_courses, low_grade_codes)
        """
        try:
            # CourseGradesApplication 생성 (한 번만)
            app_builder = rusaint.CourseGradesApplicationBuilder()
            app = await app_builder.build(session)

            # 학기별 성적 정보 조회 (한 번만)
            semesters = await app.semesters(rusaint.CourseType.BACHELOR)

            taken_courses = []
            pass_low_codes = []
            fail_codes = []

            for semester_grade in semesters:
                # 각 학기의 수업 목록 조회 (상세 성적 제외)
                classes = await app.classes(
                    rusaint.CourseType.BACHELOR,
                    semester_grade.year,
                    semester_grade.semester,
                    include_details=False,
                )

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

            logger.debug(
                f"수강 내역: {len(taken_courses)}개 학기, "
                f"저성적: C/D {len(pass_low_codes)}개, F {len(fail_codes)}개"
            )

            low_grade_codes = LowGradeSubjectCodes(
                passLow=pass_low_codes,
                fail=fail_codes,
            )

            return taken_courses, low_grade_codes

        except Exception as e:
            logger.error(f"수강 내역/저성적 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_taken_courses(self, session: rusaint.USaintSession) -> list[TakenCourse]:
        """
        수강 내역을 조회합니다.

        **DEPRECATED**: _fetch_courses_and_grades() 사용 권장
        **과목 코드만 조회**: 과목명, 교수명, 성적 등 민감정보는 제외
        **계절학기 포함**: SUMMER(3), WINTER(4)로 구분
        """
        try:
            # CourseGradesApplication 생성
            app_builder = rusaint.CourseGradesApplicationBuilder()
            app = await app_builder.build(session)

            # 학기별 성적 정보 조회 (학부생 과정)
            semesters = await app.semesters(rusaint.CourseType.BACHELOR)

            taken_courses = []
            for semester_grade in semesters:
                # 각 학기의 수업 목록 조회 (상세 성적 제외)
                classes = await app.classes(
                    rusaint.CourseType.BACHELOR,
                    semester_grade.year,
                    semester_grade.semester,
                    include_details=False,  # 상세 성적은 불필요
                )

                # 과목 코드만 추출 (과목명, 교수명 등은 제외)
                subject_codes = [cls.code for cls in classes]

                # SemesterType 매핑 (클래스 상수 사용)
                semester_str = self.SEMESTER_TYPE_MAP.get(semester_grade.semester, "1")

                taken_courses.append(
                    TakenCourse(
                        year=semester_grade.year,
                        semester=semester_str,
                        subjectCodes=subject_codes,
                    )
                )

            logger.debug(f"수강 내역 조회 완료: {len(taken_courses)}개 학기 (계절학기 포함)")
            return taken_courses
        except Exception as e:
            logger.error(f"수강 내역 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_low_grade_subject_codes(
        self,
        session: rusaint.USaintSession,
    ) -> LowGradeSubjectCodes:
        """
        저성적 과목 코드를 조회합니다.

        **과목 코드만 조회**: 성적의 절대값(A+, B+ 등)은 C/D/F 판정에만 사용하고 저장하지 않음

        Args:
            session: 유세인트 세션
        """
        try:
            # CourseGradesApplication 생성
            app_builder = rusaint.CourseGradesApplicationBuilder()
            app = await app_builder.build(session)

            # 학기별 성적 정보 조회 (학부생 과정)
            semesters = await app.semesters(rusaint.CourseType.BACHELOR)

            # 저성적 과목 코드 리스트 (이수구분 없이 과목 코드만)
            pass_low_codes = []
            fail_codes = []

            for semester_grade in semesters:
                # 각 학기의 수업 목록 조회 (성적 포함, 상세정보 제외)
                classes = await app.classes(
                    rusaint.CourseType.BACHELOR,
                    semester_grade.year,
                    semester_grade.semester,
                    include_details=False,
                )

                for cls in classes:
                    # rank 필드에서 성적 등급 확인 (F, C+, D0 등)
                    rank = getattr(cls, 'rank', None)
                    if not rank:
                        continue

                    rank_str = str(rank).upper().strip()
                    code = cls.code

                    # 성적이 F인 경우
                    if rank_str == settings.FAIL_GRADE:
                        fail_codes.append(code)
                    # 성적이 C 또는 D인 경우 (P/F 제외)
                    elif rank_str in settings.LOW_GRADE_RANKS:
                        pass_low_codes.append(code)

            logger.debug(f"저성적 과목: C/D {len(pass_low_codes)}개, F {len(fail_codes)}개")

            return LowGradeSubjectCodes(
                passLow=pass_low_codes,
                fail=fail_codes,
            )
        except Exception as e:
            logger.warning(f"저성적 과목 조회 실패 (선택 정보): {type(e).__name__}")
            # 실패 시 빈 데이터 반환 (필수 정보가 아님)
            return LowGradeSubjectCodes(
                passLow=[],
                fail=[],
            )

    # ============================================================
    # DEPRECATED: 이수구분 분류 로직은 Kotlin 서버로 이관
    # ============================================================
    # async def _get_course_category(...) - 제거됨
    # async def _classify_subject_by_type(...) - 제거됨
    #
    # 이유: Course DB가 이미 이수구분 정보를 가지고 있으므로
    #       Python에서 불완전한 휴리스틱으로 분류하는 것보다
    #       Kotlin에서 DB 조회로 정확하게 분류
    # ============================================================

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
                logger.debug(f"교직 이수: {teaching}, 전공: {teaching_info.major_name}")

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

            logger.debug(f"복수전공: {double_major}, 부전공: {minor}")

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

    async def _fetch_available_credits(self, session: rusaint.USaintSession) -> AvailableCredits:
        """
        직전 성적 및 최대 신청 가능 학점 정보를 조회합니다.

        **DEPRECATED**: _fetch_all_course_data() 사용 권장
        **평점과 학점만 조회**: 개별 과목 성적은 제외
        """
        try:
            # CourseGradesApplication 생성
            app_builder = rusaint.CourseGradesApplicationBuilder()
            app = await app_builder.build(session)

            # 학기별 성적 조회 (학부생 과정)
            semesters = await app.semesters(rusaint.CourseType.BACHELOR)

            if not semesters:
                raise ValueError("학기 정보가 없습니다")

            # 직전 학기 성적
            last_semester = semesters[-1]

            # gpa 필드 확인 (여러 가능성 시도)
            previous_gpa = 0.0
            for attr in ['gpa', 'grade_point_average', 'average']:
                if hasattr(last_semester, attr):
                    value = getattr(last_semester, attr)
                    if value is not None:
                        previous_gpa = float(value)
                        break

            # 이월 학점 확인 (rusaint API에서 제공하지 않을 수 있음)
            carried_over = 0
            for attr in ['carried_over', 'carry_over', 'transferred_credits']:
                if hasattr(last_semester, attr):
                    value = getattr(last_semester, attr)
                    if value is not None:
                        carried_over = int(value)
                        break

            # 최대 신청 가능 학점 계산
            max_credits = 19.5  # 기본값
            if previous_gpa >= 4.0:
                max_credits = 22.5
            max_credits += carried_over

            logger.debug(f"직전 평점: {previous_gpa}, 최대 신청 가능: {max_credits}학점")

            return AvailableCredits(
                previousGpa=previous_gpa,
                carriedOverCredits=carried_over,
                maxAvailableCredits=max_credits,
            )
        except Exception as e:
            logger.error(f"신청 가능 학점 정보 조회 실패: {type(e).__name__}")
            raise

    async def _fetch_remaining_credits(self, grad_app) -> RemainingCredits:
        """
        졸업까지 남은 이수 학점 정보를 조회합니다.

        **학점 정보만 조회**: 과목별 상세 정보는 제외

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

                    logger.debug(f"졸업요건: {key} - diff={diff}, remaining={remaining}")

            logger.debug(f"남은 졸업 학점 - 전필:{major_required} 전선:{major_elective} 교필:{general_required} 교선:{general_elective}")

            return RemainingCredits(
                majorRequired=major_required,
                majorElective=major_elective,
                generalRequired=general_required,
                generalElective=general_elective,
            )
        except Exception as e:
            logger.error(f"졸업 요건 조회 실패: {type(e).__name__}")
            raise
