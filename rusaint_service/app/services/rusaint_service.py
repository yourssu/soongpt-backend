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
            logger.error(f"Rusaint 오류: {str(e)}")
            raise ValueError(f"유세인트 로그인 실패: {str(e)}")
        except ValueError as e:
            logger.error(f"SSO 토큰 오류: {str(e)}")
            raise
        except Exception as e:
            logger.error(f"유세인트 데이터 조회 중 오류 발생: {str(e)}", exc_info=True)
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
            logger.error(f"세션 생성 실패: {str(e)}")
            raise ValueError(f"SSO 토큰이 유효하지 않거나 만료되었습니다: {str(e)}")

    async def _fetch_basic_info(self, session: rusaint.USaintSession) -> BasicInfo:
        """
        기본 학적 정보를 조회합니다.
        
        **민감 정보는 조회하지 않음**: 이름, 주민번호, 주소, 전화번호 등은 가져오지 않습니다.
        """
        try:
            # StudentInformationApplication 생성
            app_builder = rusaint.StudentInformationApplicationBuilder()
            app = await app_builder.build(session)

            # 학적 상태 정보 조회 (학년/학기만 필요)
            academic_records = await app.academic_record()

            # 현재 학적 상태 가져오기 (마지막 레코드)
            current_record = academic_records.records[-1] if academic_records.records else None

            if not current_record:
                raise ValueError("학적 정보를 찾을 수 없습니다")

            # year는 "2023학년도" 형식이므로 숫자만 추출
            year_str = current_record.year.replace("학년도", "").strip()
            year = int(year_str)

            # term은 "1학기" 형식
            term_str = current_record.term.replace("학기", "").strip()
            
            # 학년과 학기 계산 (1학기=1, 2학기=2, ... 8학기=8)
            # term_number는 재학 중 몇 번째 학기인지
            # grade는 학년 (1~4)
            
            # 일반 학생 정보에서 학과명만 조회 (민감정보 제외)
            student_info = await app.general()
            department = getattr(student_info, 'department_name', None) or \
                        getattr(student_info, 'department', None) or \
                        getattr(student_info, 'dept', None) or "알 수 없음"

            # 학년 계산: 1학기→1학년, 2학기→1학년, 3학기→2학년, ...
            # academic_records에서 전체 레코드 수로 계산
            total_semesters = len([r for r in academic_records.records if "학기" in r.term])
            grade = (total_semesters + 1) // 2  # 1-2학기→1학년, 3-4학기→2학년
            if grade > 4:
                grade = 4  # 최대 4학년

            return BasicInfo(
                year=year,
                grade=grade,
                semester=total_semesters,  # 재학 누적 학기
                department=department,
            )
        except Exception as e:
            logger.error(f"기본 학적 정보 조회 실패: {str(e)}")
            raise

    async def _fetch_taken_courses(self, session: rusaint.USaintSession) -> list[TakenCourse]:
        """
        수강 내역을 조회합니다.
        
        **과목 코드만 조회**: 과목명, 교수명, 성적 등 민감정보는 제외
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

                # SemesterType.ONE → 1, SemesterType.TWO → 2
                semester_num = 1 if semester_grade.semester == rusaint.SemesterType.ONE else 2

                taken_courses.append(
                    TakenCourse(
                        year=semester_grade.year,
                        semester=semester_num,
                        subjectCodes=subject_codes,
                    )
                )

            logger.debug(f"수강 내역 조회 완료: {len(taken_courses)}개 학기")
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
                    grade = getattr(cls, 'grade', None) or getattr(cls, 'score', None)
                    if not grade:
                        continue

                    grade_str = str(grade).upper()

                    # 성적이 F인 경우
                    if grade_str == "F":
                        self._classify_subject_by_type(cls, fail_dict)
                    # 성적이 C 또는 D인 경우 (P/F 제외)
                    elif any(g in grade_str for g in ["C+", "C0", "C-", "D+", "D0", "D-"]):
                        self._classify_subject_by_type(cls, pass_low_dict)

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

    def _classify_subject_by_type(self, cls, target_dict: Dict[str, List[str]]):
        """
        과목을 이수 구분에 따라 분류합니다.
        """
        # TODO: rusaint의 ClassGrade 객체에서 이수 구분(전필/전선/교필/교선) 정보를 추출
        # 현재는 임시로 과목명이나 코드로 추정
        # 실제로는 cls.category 또는 cls.type 같은 속성이 있을 수 있음
        code = cls.code
        
        # 간단한 휴리스틱 (실제로는 더 정확한 분류 필요)
        if hasattr(cls, 'category') and cls.category:
            category = str(cls.category).lower()
            if '전공필수' in category or 'major required' in category:
                target_dict["major_required"].append(code)
            elif '전공선택' in category or 'major elective' in category:
                target_dict["major_elective"].append(code)
            elif '교양필수' in category or 'general required' in category:
                target_dict["general_required"].append(code)
            elif '교양선택' in category or 'general elective' in category:
                target_dict["general_elective"].append(code)
            else:
                # 기본값: 전공선택으로 분류
                target_dict["major_elective"].append(code)
        else:
            # 카테고리 정보가 없으면 일단 전공선택으로 분류
            target_dict["major_elective"].append(code)

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

            # 최대 신청 가능 학점 계산 (숭실대 규정)
            max_credits = 18  # 기본값
            if previous_gpa >= 4.0:
                max_credits = 21
            elif previous_gpa >= 3.5:
                max_credits = 19
            
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

            # requirements.requirements가 리스트인지 딕셔너리인지 확인
            if isinstance(requirements.requirements, dict):
                # 딕셔너리인 경우
                for key, req in requirements.requirements.items():
                    category = str(key).lower() if isinstance(key, str) else ""
                    remaining = 0
                    
                    # remaining 필드 확인
                    for attr in ['remaining', 'left', 'required']:
                        if hasattr(req, attr):
                            value = getattr(req, attr)
                            if value is not None and value > 0:
                                remaining = int(value)
                                break

                    # 카테고리별 분류
                    if '전공필수' in category or 'major_required' in category:
                        major_required = remaining
                    elif '전공선택' in category or 'major_elective' in category:
                        major_elective = remaining
                    elif '교양필수' in category or 'general_required' in category:
                        general_required = remaining
                    elif '교양선택' in category or 'general_elective' in category:
                        general_elective = remaining
            elif isinstance(requirements.requirements, list):
                # 리스트인 경우
                for req in requirements.requirements:
                    if hasattr(req, 'category') and hasattr(req, 'remaining'):
                        category = str(req.category).lower()
                        remaining = req.remaining if req.remaining > 0 else 0

                        if '전공필수' in category or 'major required' in category:
                            major_required = remaining
                        elif '전공선택' in category or 'major elective' in category:
                            major_elective = remaining
                        elif '교양필수' in category or 'general required' in category:
                            general_required = remaining
                        elif '교양선택' in category or 'general elective' in category:
                            general_elective = remaining

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
