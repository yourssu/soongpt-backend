"""
유세인트에서 데이터를 조회하는 함수들.

기본 정보, 수강/성적, 복수전공·교직, 졸업 요건 등을 가져옵니다.
"""

import asyncio
import logging
from typing import Any, Dict

import rusaint

from app.core.config import settings
from app.services.constants import CHAPEL_CODES, RETAKE_GENERAL_REQUIRED_MAPPING
from app.schemas.usaint_schemas import (
    BasicInfo,
    Flags,
    GraduationRequirementItem,
    GraduationRequirements,
    TakenCourse,
)

logger = logging.getLogger(__name__)


async def fetch_basic_info(student_info_app) -> tuple[BasicInfo, list[str]]:
    """
    기본 학적 정보를 조회합니다.

    **민감 정보는 조회하지 않음**: 이름, 주민번호, 주소, 전화번호 등은 가져오지 않습니다.
    **휴학/엇학기/졸업유예 고려**: 계산이 아니라 유세인트에서 직접 크롤링

    Args:
        student_info_app: 학생정보 애플리케이션

    Returns:
        tuple: (BasicInfo, warnings) — warnings에 NO_SEMESTER_INFO가 포함될 수 있음
    """
    try:
        warnings: list[str] = []
        student_info = await student_info_app.general()

        # basicInfo.semester = 재학 누적 학기 (1~8). 학년+현재학기(1/2)로 계산 (예: 3학년 2학기 → 6).
        admission_year = getattr(student_info, "apply_year", None) or getattr(
            student_info, "admission_year", None
        )
        if admission_year is None:
            logger.error("입학년도 정보를 찾을 수 없습니다")
            raise ValueError("필수 학적 정보(입학년도)를 조회할 수 없습니다")

        grade = getattr(student_info, "grade", None)
        if grade is None:
            logger.error("학년 정보를 찾을 수 없습니다")
            raise ValueError("필수 학적 정보(학년)를 조회할 수 없습니다")

        # 재학 누적 학기: 3학년 2학기 → 6학기. (학년, 현재학기 1/2)로 계산.
        # rusaint의 term/semester는 "현재 학기(1학기=1, 2학기=2)" 또는 이미 "누적 학기(1~8)"일 수 있음.
        term_raw = getattr(student_info, "term", None) or getattr(
            student_info, "semester", None
        )
        if term_raw is None:
            logger.warning(
                "학기 정보가 없어 기본값(1학기) 사용. "
                "새내기일 가능성이 높지만, 데이터 누락일 수도 있음"
            )
            warnings.append("NO_SEMESTER_INFO")
            term_raw = 1
        if 1 <= term_raw <= 2:
            # 현재 학기(1/2) → 재학 누적 학기 계산: (학년-1)*2 + 현재학기
            semester = (grade - 1) * 2 + term_raw
        else:
            # 이미 1~8 누적 학기로 온 경우 그대로 사용
            semester = term_raw
        semester = max(1, min(8, semester))

        # TODO(PT-87): 2025년 3월 이후 삭제 예정 - 숭피티 출시 전까지 다음 학기 추천을 위한 임시 +1학기 보정
        # 현재 유세인트는 이전 학기(예: 3학년 6학기)를 반환하지만, 숭피티는 다음 학기(4학년 7학기) 기준으로 추천함
        # 1학년 1학기(semester=1)이거나 학기 정보가 없었던 경우는 보정하지 않음
        if semester > 1:
            semester = min(8, semester + 1)
        grade = min(4, (semester - 1) // 2 + 1)

        # rusaint StudentInformation: department=학과, major=세부전공(Optional)
        department = getattr(student_info, "major", None) or getattr(
            student_info, "department", None
        )
        if not department:
            logger.error("학과 정보를 찾을 수 없습니다")
            raise ValueError("필수 학적 정보(학과)를 조회할 수 없습니다")

        return BasicInfo(
            year=admission_year,
            grade=grade,
            semester=semester,
            department=department,
        ), warnings
    except ValueError:
        raise
    except Exception as e:
        logger.error(f"기본 학적 정보 조회 실패: {type(e).__name__}")
        raise


async def fetch_all_course_data_parallel(
    course_grades_app1,
    course_grades_app2,
    semester_type_map: Dict[Any, str],
) -> tuple[list[TakenCourse], list[str], list[str]]:
    """
    2개의 CourseGradesApplication으로 학기를 나눠서 병렬 조회합니다.

    Args:
        course_grades_app1: CourseGradesApplication 인스턴스 #1
        course_grades_app2: CourseGradesApplication 인스턴스 #2
        semester_type_map: 학기 타입 매핑 (SEMESTER_TYPE_MAP)

    Returns:
        tuple: (taken_courses, low_grade_subject_codes, warnings)
    """
    try:
        semesters = await course_grades_app1.semesters(rusaint.CourseType.BACHELOR)

        if not semesters:
            logger.warning("수강 이력 없음 (빈 학기 목록) — 새내기 가능성")
            return [], [], ["NO_COURSE_HISTORY"]

        if len(semesters) <= 1:
            semesters_group1 = semesters
            semesters_group2 = []
        else:
            mid_point = (len(semesters) + 1) // 2
            semesters_group1 = semesters[:mid_point]
            semesters_group2 = semesters[mid_point:]

        tasks_group1 = [
            course_grades_app1.classes(
                rusaint.CourseType.BACHELOR,
                sem.year,
                sem.semester,
                include_details=False,
            )
            for sem in semesters_group1
        ] if semesters_group1 else []

        tasks_group2 = [
            course_grades_app2.classes(
                rusaint.CourseType.BACHELOR,
                sem.year,
                sem.semester,
                include_details=False,
            )
            for sem in semesters_group2
        ] if semesters_group2 else []

        if tasks_group1 and tasks_group2:
            classes_group1, classes_group2 = await asyncio.gather(
                asyncio.gather(*tasks_group1),
                asyncio.gather(*tasks_group2),
            )
        elif tasks_group1:
            classes_group1 = await asyncio.gather(*tasks_group1)
            classes_group2 = []
        else:
            classes_group1 = []
            classes_group2 = await asyncio.gather(*tasks_group2)

        all_semester_classes = list(classes_group1) + list(classes_group2)

        taken_courses = []
        # 과목코드별 최신 성적 추적: {code: (year, semester_index, rank_str)}
        # semester_index는 semesters 리스트 내 인덱스 (시간순 비교용)
        latest_grades: Dict[str, tuple[int, int, str]] = {}
        # 과목코드 → 과목명 매핑 (재수강 매핑용, 가장 최근 학기 기준)
        code_to_name: Dict[str, str] = {}

        for idx, (semester_grade, classes) in enumerate(zip(semesters, all_semester_classes)):
            # 채플 과목 제외
            subject_codes = [cls.code for cls in classes if cls.code not in CHAPEL_CODES]
            semester_str = semester_type_map.get(semester_grade.semester, "1")

            taken_courses.append(
                TakenCourse(
                    year=semester_grade.year,
                    semester=semester_str,
                    subjectCodes=subject_codes,
                )
            )

            for cls in classes:
                code = cls.code
                # 채플 과목 제외
                if code in CHAPEL_CODES:
                    continue

                # 과목명 캐시 (최신 학기 기준으로 갱신)
                class_name = getattr(cls, "class_name", None) or ""
                if class_name:
                    code_to_name[code] = class_name

                rank = getattr(cls, "rank", None)
                if not rank:
                    continue

                rank_str = str(rank).upper().strip()

                # 최신 성적으로 갱신 (semesters가 시간순이라고 가정)
                if code not in latest_grades:
                    latest_grades[code] = (semester_grade.year, idx, rank_str)
                else:
                    prev_year, prev_idx, _ = latest_grades[code]
                    # 더 최근 학기면 갱신 (year 비교 후 semester index 비교)
                    if (semester_grade.year, idx) > (prev_year, prev_idx):
                        latest_grades[code] = (semester_grade.year, idx, rank_str)

        # 최신 성적 기준으로 C 이하인 과목만 low_grade_codes에 추가 (중복 없음)
        low_grade_codes = []
        for code, (_, _, rank_str) in latest_grades.items():
            if rank_str == settings.FAIL_GRADE or rank_str in settings.LOW_GRADE_RANKS:
                low_grade_codes.append(code)

        # 교양필수 재수강 매핑: 폐강된 구과목 → 대체 신과목 baseCode 추가
        # 구과목 코드로는 현재 학기 DB에 매칭 안 되므로, 신과목 baseCode를 함께 전달
        low_grade_code_set = set(low_grade_codes)
        for code in list(low_grade_codes):
            name = code_to_name.get(code, "")
            replacement_code = RETAKE_GENERAL_REQUIRED_MAPPING.get(name)
            if replacement_code and replacement_code not in low_grade_code_set:
                low_grade_codes.append(replacement_code)
                low_grade_code_set.add(replacement_code)

        return taken_courses, low_grade_codes, []

    except Exception as e:
        logger.error(f"성적 관련 데이터 조회 실패 (병렬): {type(e).__name__}")
        raise


async def fetch_flags(student_info_app) -> Flags:
    """
    복수전공/부전공 및 교직 이수 정보를 조회합니다.

    **학과명만 조회**: 자격증 번호, 날짜 등 민감정보는 제외

    Args:
        student_info_app: StudentInformationApplication 인스턴스 (재사용)
    """
    try:
        qualifications = await student_info_app.qualifications()

        teaching = False
        if qualifications.teaching_major:
            teaching_info = qualifications.teaching_major
            teaching = teaching_info.major_name is not None

        student_info = await student_info_app.general()

        double_major = None
        minor = None

        # rusaint StudentInformation: plural_major(복수전공), sub_major(부전공)
        for attr in ["plural_major", "second_major", "double_major", "dual_major", "major_double"]:
            if hasattr(student_info, attr):
                value = getattr(student_info, attr)
                if value and str(value).strip():
                    double_major = value
                    break

        for attr in ["sub_major", "minor", "minor_major", "submajor"]:
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
        return Flags(
            doubleMajorDepartment=None,
            minorDepartment=None,
            teaching=False,
        )


async def fetch_graduation_requirements(grad_app) -> GraduationRequirements:
    """
    졸업 요건 상세 정보를 조회합니다 (raw 데이터).

    **개별 요건 정보 포함**: 각 요건의 이름, 기준학점, 이수학점, 충족여부 등

    Args:
        grad_app: 졸업요건 애플리케이션

    Returns:
        GraduationRequirements: 개별 요건 목록 (raw 데이터)
    """
    try:
        requirements = await grad_app.requirements()
        requirement_list = []

        if isinstance(requirements.requirements, dict):
            for key, req in requirements.requirements.items():
                name = str(key)
                requirement_value = getattr(req, "requirement", None)
                calculation_value = getattr(req, "calculation", None) or getattr(
                    req, "calcuation", None
                )
                difference_value = getattr(req, "difference", None)
                result_value = getattr(req, "result", False)
                category = getattr(req, "category", str(key))

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

        return GraduationRequirements(requirements=requirement_list)

    except Exception as e:
        logger.error(f"졸업 요건 조회 실패: {type(e).__name__}")
        raise
