"""
졸업사정표 요약 빌더.

name 필드 기반으로 졸업 요건을 분류하여 GraduationSummary를 생성합니다.
분류 규칙은 docs/graduate_business.md 참조.
"""

from typing import Optional

from app.schemas.graduation_summary import (
    ChapelSummaryItem,
    CreditSummaryItem,
    GraduationSummary,
)
from app.schemas.usaint_schemas import GraduationRequirementItem


def _safe_int(value: Optional[float]) -> int:
    """null 값을 0으로 변환"""
    return int(value) if value is not None else 0


def _safe_bool(value: Optional[bool]) -> bool:
    """null 값을 False로 변환"""
    return value if value is not None else False


def _is_general_required(name: str) -> bool:
    """교양필수 여부"""
    return "교양필수" in name


def _is_general_elective(name: str) -> bool:
    """교양선택 여부"""
    return "교양선택" in name


def _is_major_foundation(name: str) -> bool:
    """전공기초 여부"""
    return "전기" in name or "전공기초" in name


def _is_major_required_only(name: str) -> bool:
    """전공필수 단독 여부 (전선/진선 미포함)"""
    return "전필" in name and "전선" not in name and "진선" not in name


def _is_major_elective_only(name: str) -> bool:
    """전공선택 단독 여부 (전필 미포함)"""
    has_elective = "전선" in name or "진선" in name or "전공선택" in name
    return has_elective and "전필" not in name


def _is_major_combined(name: str) -> bool:
    """전필+전선 복합 여부"""
    has_required = "전필" in name
    has_elective = "전선" in name or "진선" in name
    return has_required and has_elective


def _is_double_major_required_only(name: str) -> bool:
    """복수전공필수 단독 여부 (복필만 있고 복선/복수전공 없음)"""
    return "복필" in name and "복선" not in name and "복수전공" not in name


def _is_double_major_combined(name: str) -> bool:
    """복수전공 복합 여부 (복필+복선 합계)"""
    return "복수전공" in name


def _is_minor(name: str) -> bool:
    """부전공 여부 (필수/선택 구분 없이 전체 부전공)"""
    return "부전공" in name


def _is_christian(name: str) -> bool:
    """기독교과목 여부"""
    return "기독교" in name


def _is_chapel(name: str) -> bool:
    """채플 여부"""
    return "채플" in name


def build_graduation_summary(
    requirements: list[GraduationRequirementItem],
) -> GraduationSummary:
    """
    졸업 요건 목록에서 GraduationSummary를 생성합니다.

    Args:
        requirements: 졸업 요건 항목 목록 (raw 데이터)

    Returns:
        GraduationSummary: name 기반 분류 결과
    """
    # 임시 저장소
    general_required: Optional[CreditSummaryItem] = None
    general_elective: Optional[CreditSummaryItem] = None
    major_foundation: Optional[CreditSummaryItem] = None
    major_required: Optional[CreditSummaryItem] = None
    major_elective: Optional[CreditSummaryItem] = None
    major_combined: Optional[GraduationRequirementItem] = None
    double_major_required: Optional[CreditSummaryItem] = None
    double_major_combined: Optional[GraduationRequirementItem] = None
    minor: Optional[CreditSummaryItem] = None
    christian: Optional[CreditSummaryItem] = None
    chapel: Optional[ChapelSummaryItem] = None

    for req in requirements:
        name = req.name

        # 1. 교양필수
        if _is_general_required(name):
            general_required = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 2. 교양선택
        if _is_general_elective(name):
            general_elective = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 3. 전공기초
        if _is_major_foundation(name):
            major_foundation = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 4. 전필+전선 복합 (우선 처리)
        if _is_major_combined(name):
            major_combined = req
            continue

        # 5. 전공필수 단독
        if _is_major_required_only(name):
            major_required = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 6. 전공선택 단독
        if _is_major_elective_only(name):
            major_elective = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 7. 복수전공 복합 (복필+복선 합계)
        if _is_double_major_combined(name):
            double_major_combined = req
            continue

        # 8. 복수전공필수 단독
        if _is_double_major_required_only(name):
            double_major_required = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 9. 부전공 (필수/선택 구분 없이 전체 부전공)
        if _is_minor(name):
            minor = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 10. 기독교과목
        if _is_christian(name):
            christian = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 11. 채플 (학점 없이 충족 여부만)
        if _is_chapel(name):
            chapel = ChapelSummaryItem(satisfied=_safe_bool(req.result))
            continue

    # 복합 항목 후처리: 전필+전선
    if major_combined is not None:
        combined_req = _safe_int(major_combined.requirement)
        combined_calc = _safe_int(major_combined.calculation)
        combined_result = _safe_bool(major_combined.result)

        if major_required is not None:
            elective_req = max(0, combined_req - major_required.required)
            elective_calc = max(0, combined_calc - major_required.completed)
            if combined_result:
                major_required = CreditSummaryItem(
                    required=major_required.required,
                    completed=major_required.completed,
                    satisfied=True,
                )
                major_elective = CreditSummaryItem(
                    required=elective_req,
                    completed=elective_calc,
                    satisfied=True,
                )
            else:
                major_required = CreditSummaryItem(
                    required=major_required.required,
                    completed=major_required.completed,
                    satisfied=major_required.completed >= major_required.required,
                )
                major_elective = CreditSummaryItem(
                    required=elective_req,
                    completed=elective_calc,
                    satisfied=elective_calc >= elective_req,
                )
        else:
            major_elective = CreditSummaryItem(
                required=combined_req,
                completed=combined_calc,
                satisfied=combined_result,
            )

    # 복합 항목 후처리: 복수전공
    double_major_elective: Optional[CreditSummaryItem] = None
    if double_major_combined is not None:
        combined_req = _safe_int(double_major_combined.requirement)
        combined_calc = _safe_int(double_major_combined.calculation)
        combined_result = _safe_bool(double_major_combined.result)

        if double_major_required is not None:
            elective_req = max(0, combined_req - double_major_required.required)
            elective_calc = max(0, combined_calc - double_major_required.completed)
            if combined_result:
                double_major_required = CreditSummaryItem(
                    required=double_major_required.required,
                    completed=double_major_required.completed,
                    satisfied=True,
                )
                double_major_elective = CreditSummaryItem(
                    required=elective_req,
                    completed=elective_calc,
                    satisfied=True,
                )
            else:
                double_major_required = CreditSummaryItem(
                    required=double_major_required.required,
                    completed=double_major_required.completed,
                    satisfied=double_major_required.completed >= double_major_required.required,
                )
                double_major_elective = CreditSummaryItem(
                    required=elective_req,
                    completed=elective_calc,
                    satisfied=elective_calc >= elective_req,
                )
        else:
            double_major_elective = CreditSummaryItem(
                required=combined_req,
                completed=combined_calc,
                satisfied=combined_result,
            )

    return GraduationSummary(
        generalRequired=general_required,
        generalElective=general_elective,
        majorFoundation=major_foundation,
        majorRequired=major_required,
        majorElective=major_elective,
        minor=minor,
        doubleMajorRequired=double_major_required,
        doubleMajorElective=double_major_elective,
        christianCourses=christian,
        chapel=chapel,
    )
