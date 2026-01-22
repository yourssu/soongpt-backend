"""
Rusaint 라이브러리를 사용하여 유세인트 데이터를 크롤링하는 서비스.

SSO 토큰을 사용하여 유세인트에 로그인하고, 학적/성적 정보를 조회합니다.
"""

from app.schemas.usaint_schemas import (
    UsaintSnapshotResponse,
    TakenCourse,
    LowGradeSubjectCodes,
    GradeBandSubjectCodes,
    Flags,
    AvailableCredits,
    BasicInfo,
    RemainingCredits,
)
from app.core.config import settings
import rusaint
import logging
from typing import Dict, List

logger = logging.getLogger(__name__)


class RusaintService:
    """Rusaint 라이브러리를 사용한 유세인트 데이터 크롤링 서비스"""

    def __init__(self):
        """RusaintService 초기화"""
        self._cached_graduation_requirements = None

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
        logger.info(f"유세인트 데이터 조회 시작: student_id={student_id[:4]}****")

        try:
            # 1. SSO 토큰으로 세션 생성
            session = await self._create_session(student_id, s_token)

            # 2. 각종 정보 조회 (병렬 처리 가능)
            basic_info = await self._fetch_basic_info(session)
            taken_courses = await self._fetch_taken_courses(session)
            low_grade_codes = await self._fetch_low_grade_subject_codes(session)
            flags = await self._fetch_flags(session)
            available_credits = await self._fetch_available_credits(session)
            remaining_credits = await self._fetch_remaining_credits(session)

            logger.info(f"유세인트 데이터 조회 완료: student_id={student_id[:4]}****")

            return UsaintSnapshotResponse(
                takenCourses=taken_courses,
                lowGradeSubjectCodes=low_grade_codes,
                flags=flags,
                availableCredits=available_credits,
                basicInfo=basic_info,
                remainingCredits=remaining_credits,
            )

        except rusaint.RusaintError as e:
            logger.error(f"Rusaint 오류 (student_id={student_id[:4]}****): {type(e).__name__}")
            raise ValueError("유세인트 로그인에 실패했습니다. SSO 토큰을 확인해주세요.")
        except ValueError as e:
            logger.error(f"SSO 토큰 오류 (student_id={student_id[:4]}****): {type(e).__name__}")
            raise
        except Exception as e:
            logger.error(f"유세인트 데이터 조회 중 오류 발생 (student_id={student_id[:4]}****): {type(e).__name__}", exc_info=True)
            raise

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
        """
        try:
            builder = rusaint.USaintSessionBuilder()
            session = await builder.with_token(student_id, s_token)
            logger.debug(f"유세인트 세션 생성 완료: student_id={student_id[:4]}****")
            return session
        except Exception as e:
            logger.error(f"세션 생성 실패 (student_id={student_id[:4]}****): {type(e).__name__}")
            raise ValueError("SSO 토큰이 유효하지 않거나 만료되었습니다.")

    async def _fetch_basic_info(self, session: rusaint.USaintSession) -> BasicInfo:
        """
        기본 학적 정보를 조회합니다.

        **민감 정보는 조회하지 않음**: 이름, 주민번호, 주소, 전화번호 등은 가져오지 않습니다.
        **휴학/엇학기/졸업유예 고려**: 계산이 아니라 유세인트에서 직접 크롤링
        """
        try:
            # GraduationRequirementsApplication에서 정확한 학년/학기 정보 가져오기
            grad_builder = rusaint.GraduationRequirementsApplicationBuilder()
            grad_app = await grad_builder.build(session)
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
            logger.error(f"기본 학적 정보 조회 실패: {str(e)}")
            raise

    async def _fetch_taken_courses(self, session: rusaint.USaintSession) -> list[TakenCourse]:
        """
        수강 내역을 조회합니다.

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

                # SemesterType 매핑 (문자열로)
                # ONE(1학기)="1", TWO(2학기)="2", SUMMER="SUMMER", WINTER="WINTER"
                semester_map = {
                    rusaint.SemesterType.ONE: "1",
                    rusaint.SemesterType.TWO: "2",
                    rusaint.SemesterType.SUMMER: "SUMMER",
                    rusaint.SemesterType.WINTER: "WINTER",
                }
                semester_str = semester_map.get(semester_grade.semester, "1")

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
            logger.error(f"수강 내역 조회 실패: {str(e)}")
            raise

    async def _fetch_low_grade_subject_codes(
        self,
        session: rusaint.USaintSession,
    ) -> LowGradeSubjectCodes:
        """
        저성적 과목 코드를 조회합니다.

        **과목 코드만 조회**: 성적의 절대값(A+, B+ 등)은 C/D/F 판정에만 사용하고 저장하지 않음
        """
        try:
            # CourseGradesApplication 생성
            app_builder = rusaint.CourseGradesApplicationBuilder()
            app = await app_builder.build(session)

            # 학기별 성적 정보 조회 (학부생 과정)
            semesters = await app.semesters(rusaint.CourseType.BACHELOR)

            # 저성적 과목 분류
            pass_low_dict = {"major_required": [], "major_elective": [], "general_required": [], "general_elective": []}
            fail_dict = {"major_required": [], "major_elective": [], "general_required": [], "general_elective": []}

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

                    # 성적이 F인 경우
                    if rank_str == "F":
                        await self._classify_subject_by_type(session, cls, fail_dict)
                    # 성적이 C 또는 D인 경우 (P/F 제외)
                    elif rank_str in ["C+", "C0", "C-", "D+", "D0", "D-"]:
                        await self._classify_subject_by_type(session, cls, pass_low_dict)

            logger.debug(f"저성적 과목: C/D {sum(len(v) for v in pass_low_dict.values())}개, F {sum(len(v) for v in fail_dict.values())}개")

            return LowGradeSubjectCodes(
                passLow=GradeBandSubjectCodes(
                    majorRequired=pass_low_dict["major_required"],
                    majorElective=pass_low_dict["major_elective"],
                    generalRequired=pass_low_dict["general_required"],
                    generalElective=pass_low_dict["general_elective"],
                ),
                fail=GradeBandSubjectCodes(
                    majorRequired=fail_dict["major_required"],
                    majorElective=fail_dict["major_elective"],
                    generalRequired=fail_dict["general_required"],
                    generalElective=fail_dict["general_elective"],
                ),
            )
        except Exception as e:
            logger.warning(f"저성적 과목 조회 실패 (선택 정보): {str(e)}")
            # 실패 시 빈 데이터 반환 (필수 정보가 아님)
            return LowGradeSubjectCodes(
                passLow=GradeBandSubjectCodes(
                    majorRequired=[],
                    majorElective=[],
                    generalRequired=[],
                    generalElective=[],
                ),
                fail=GradeBandSubjectCodes(
                    majorRequired=[],
                    majorElective=[],
                    generalRequired=[],
                    generalElective=[],
                ),
            )

    async def _get_course_category(
        self,
        session: rusaint.USaintSession,
        course_code: str,
        course_name: str,
    ) -> str:
        """
        졸업요건 정보를 기반으로 과목의 이수구분을 결정합니다.

        Returns:
            "major_required" | "major_elective" | "general_required" | "general_elective"
        """
        try:
            # 졸업요건 조회 (캐싱)
            if self._cached_graduation_requirements is None:
                grad_builder = rusaint.GraduationRequirementsApplicationBuilder()
                grad_app = await grad_builder.build(session)
                self._cached_graduation_requirements = await grad_app.requirements()

            requirements = self._cached_graduation_requirements

            if not hasattr(requirements, 'requirements') or not requirements.requirements:
                return "major_elective"  # Fallback

            # 과목명 정제 (대괄호 제거)
            import re
            course_name_clean = re.sub(r'\[.*?\]', '', course_name).strip()

            # 우선순위: 구체적인 카테고리 먼저 확인
            priority_categories = {
                "교양필수": "general_required",
                "교양선택": "general_elective",
                "전공": None,  # 세부 구분 필요
                "전공기초": "major_required",
            }

            # 각 category별로 lectures를 확인하여 매칭
            for key, req in requirements.requirements.items():
                category = req.category

                # 우선순위 카테고리만 확인
                if category not in priority_categories:
                    continue

                # lectures에서 과목명으로 매칭
                for lecture_name in req.lectures:
                    # 접두사 제거 및 정제
                    clean_lecture = re.sub(r'^\(.*?\)', '', lecture_name).strip()
                    clean_lecture = re.sub(r'^\xa0', '', clean_lecture).strip()

                    if not clean_lecture or not course_name_clean:
                        continue

                    # 부분 문자열 매칭
                    if (clean_lecture in course_name_clean or
                        course_name_clean in clean_lecture or
                        clean_lecture.replace(' ', '') in course_name_clean.replace(' ', '')):

                        # "전공" 카테고리는 세부 구분 필요
                        if category == "전공":
                            # requirement name에서 전필/전선 구분
                            if "전필" in key or "전공필수" in key:
                                return "major_required"
                            else:
                                return "major_elective"

                        return priority_categories[category]

            # 매칭 실패 시 기본값
            return "major_elective"

        except Exception as e:
            logger.debug(f"이수구분 분류 실패 ({course_code}): {e}")
            return "major_elective"  # Fallback

    async def _classify_subject_by_type(
        self,
        session: rusaint.USaintSession,
        cls,
        target_dict: Dict[str, List[str]],
    ):
        """
        졸업요건 정보를 기반으로 과목을 이수구분에 따라 분류합니다.
        """
        code = cls.code
        course_name = cls.class_name

        # 졸업요건에서 이수구분 조회
        category_key = await self._get_course_category(session, code, course_name)

        # 분류
        target_dict[category_key].append(code)

    async def _fetch_flags(self, session: rusaint.USaintSession) -> Flags:
        """
        복수전공/부전공 및 교직 이수 정보를 조회합니다.

        **학과명만 조회**: 자격증 번호, 날짜 등 민감정보는 제외
        """
        try:
            # StudentInformationApplication 생성
            app_builder = rusaint.StudentInformationApplicationBuilder()
            app = await app_builder.build(session)

            # 자격 정보 조회 (교직만 필요)
            qualifications = await app.qualifications()

            # 교직 정보 확인
            teaching = False
            if qualifications.teaching_major:
                teaching_info = qualifications.teaching_major
                # major_name이 None이 아니면 교직 이수 중으로 판단
                teaching = teaching_info.major_name is not None
                logger.debug(f"교직 이수: {teaching}, 전공: {teaching_info.major_name}")

            # 일반 학생 정보에서 복수전공/부전공 확인
            student_info = await app.general()

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
            logger.warning(f"복수전공/교직 정보 조회 실패 (선택 정보): {str(e)}")
            # 실패 시 기본값 반환 (선택 정보)
            return Flags(
                doubleMajorDepartment=None,
                minorDepartment=None,
                teaching=False,
            )

    async def _fetch_available_credits(self, session: rusaint.USaintSession) -> AvailableCredits:
        """
        직전 성적 및 최대 신청 가능 학점 정보를 조회합니다.

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
            logger.error(f"신청 가능 학점 정보 조회 실패: {str(e)}")
            raise

    async def _fetch_remaining_credits(self, session: rusaint.USaintSession) -> RemainingCredits:
        """
        졸업까지 남은 이수 학점 정보를 조회합니다.

        **학점 정보만 조회**: 과목별 상세 정보는 제외
        """
        try:
            # GraduationRequirementsApplication 생성
            app_builder = rusaint.GraduationRequirementsApplicationBuilder()
            app = await app_builder.build(session)

            # 졸업 요건 조회
            requirements = await app.requirements()

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
            logger.error(f"졸업 요건 조회 실패: {str(e)}")
            raise
