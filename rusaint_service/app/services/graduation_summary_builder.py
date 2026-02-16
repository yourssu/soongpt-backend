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


def _is_balance_excluded(name: str) -> bool:
    """Balance 항목 여부. 'Balance (교양선택) 3개 영역 이상' 등은 학점 요약(graduationSummary) 파싱 범위에서 제외."""
    return "Balance" in name


def _is_general_elective(name: str) -> bool:
    """교양선택(학점) 여부. 학부-교양선택 N 항목만 generalElective로 사용."""
    return "학부-교양선택" in name


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
    """전필+전선 또는 전필+전기+전선 복합 여부.

    예: "전필·전선", "전필전선", "전필·전기·전선" (전기 단독 + 이 복합인 학과도 있음).
    복합 행은 전공기초(전기)보다 먼저 매칭되어, "전필·전기·전선"이 전기로만 분류되지 않도록 함.
    """
    has_required = "전필" in name
    has_elective = "전선" in name or "진선" in name
    return has_required and has_elective


def _is_major_total(name: str) -> bool:
    """전공 복합 총합 여부 (예: "전공 42").

    학과별로 의미가 다름:
    - 전기+전필+전선 학과 → 전필+전선 합계
    - 전기+전선 학과 (전필 없음) → 전기+전선 합계
    - 전필+전선 학과 (전기 없음) → 전필+전선 합계
    후처리에서 단독 필드(전필/전기) 존재 여부로 전선을 역산한다.
    """
    if "전공" not in name:
        return False
    # 더 구체적인 매처에서 이미 처리된 패턴 제외
    if any(x in name for x in ["전공기초", "전공선택", "복수전공", "부전공"]):
        return False
    return True


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

        # Balance (교양선택 3개 영역 등) → 파싱 범위에서 제외
        if _is_balance_excluded(name):
            continue

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

        # 3. 전필+전선 복합 / 전필+전기+전선 복합 (전공기초보다 먼저 처리)
        # "전필·전기·전선" 한 줄인 학과: 이 행을 전기로 매칭하면 안 되므로 복합으로 먼저 매칭.
        # progress에는 전필+전선 이수현황(majorElective)을 보내며, 후처리에서 복합-전기로 역산.
        if _is_major_combined(name):
            major_combined = req
            continue

        # 4. "전공" 복합 총합 (전기+전선 또는 전필+전선, 학과별 상이)
        if _is_major_total(name):
            major_combined = req
            continue

        # 5. 전공기초 (전기 단독만 매칭; 위에서 복합 행은 이미 처리됨)
        if _is_major_foundation(name):
            major_foundation = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 6. 전공필수 단독
        if _is_major_required_only(name):
            major_required = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 7. 전공선택 단독
        if _is_major_elective_only(name):
            major_elective = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 8. 복수전공 복합 (복필+복선 합계)
        if _is_double_major_combined(name):
            double_major_combined = req
            continue

        # 9. 복수전공필수 단독
        if _is_double_major_required_only(name):
            double_major_required = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 10. 부전공 (필수/선택 구분 없이 전체 부전공)
        if _is_minor(name):
            minor = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 11. 기독교과목
        if _is_christian(name):
            christian = CreditSummaryItem(
                required=_safe_int(req.requirement),
                completed=_safe_int(req.calculation),
                satisfied=_safe_bool(req.result),
            )
            continue

        # 12. 채플 (학점 없이 충족 여부만)
        if _is_chapel(name):
            chapel = ChapelSummaryItem(satisfied=_safe_bool(req.result))
            continue

    # 복합 항목 후처리: 전공 복합 → 전선(또는 전필+전선) 역산
    #
    # - 전필 존재 → 복합 = 전필+전선, 전필을 빼서 전선
    # - 전필 없고 전기 존재:
    #   - 복합 행 이름에 "전기" 포함(전필·전기·전선, 전공=전기+전선) → 전기를 빼서 전선
    #   - 복합 행이 "전필·전선"만(전기 미포함) → 빼지 않음, 복합 전체 = 전필+전선
    # - 둘 다 없음 → 복합 전체가 전선
    # 복합으로 전필+전선만 나오는 경우(전필 단독 없음): majorRequired = majorElective = 같은값, warning 플래그
    major_required_elective_combined = False
    combined_name = major_combined.name if major_combined else ""

    if major_combined is not None:
        combined_req = _safe_int(major_combined.requirement)
        combined_calc = _safe_int(major_combined.calculation)

        if major_required is not None:
            # 전필 존재 → 전필+전선 복합, 전필을 빼서 전선
            elective_req = max(0, combined_req - major_required.required)
            elective_calc = max(0, combined_calc - major_required.completed)
        elif major_foundation is not None:
            # 전기만 존재 (전필 없음)
            # 복합 행이 "전기" 포함(전필·전기·전선) 또는 "전공 N"(전기+전선 의미) → 전기 빼기
            # 복합 행이 "전필·전선"만(전기 미포함) → 빼지 않음, 복합 전체 = 전필+전선
            combined_includes_foundation = (
                "전기" in combined_name
                or ("전공" in combined_name and "전필" not in combined_name and "전선" not in combined_name)
            )
            if combined_includes_foundation:
                elective_req = max(0, combined_req - major_foundation.required)
                elective_calc = max(0, combined_calc - major_foundation.completed)
            else:
                elective_req = combined_req
                elective_calc = combined_calc
        else:
            # 단독 필드 없음 → 복합 전체가 전선
            elective_req = combined_req
            elective_calc = combined_calc

        major_elective = CreditSummaryItem(
            required=elective_req,
            completed=elective_calc,
            satisfied=elective_calc >= elective_req,
        )

        # MAJOR_REQUIRED_ELECTIVE_COMBINED: 복합 행 이름이 "전필+전선" 형태이고 전필/전선 단독 행이 없을 때만 True.
        # 예: 학부-전필+전선-문예창작 42 (전필·전선, 전필·전기·전선 등). "학부-전기" & "학부-전공" 별도 행은 False.
        major_required_elective_combined = (
            major_required is None
            and "전필" in combined_name
            and ("전선" in combined_name or "진선" in combined_name)
        )
        # 복합일 때 전필 단독 없음 → WAS/프론트 편의로 majorRequired = majorElective
        if major_required is None:
            major_required = major_elective

    # 복합 항목 후처리: 복수전공
    # 복필 단독 필드가 있으면 복합에서 빼서 복선 역산.
    # 복필/복선 satisfied는 각각 자체 required/completed로 계산.
    double_major_elective: Optional[CreditSummaryItem] = None
    if double_major_combined is not None:
        combined_req = _safe_int(double_major_combined.requirement)
        combined_calc = _safe_int(double_major_combined.calculation)

        if double_major_required is not None:
            elective_req = max(0, combined_req - double_major_required.required)
            elective_calc = max(0, combined_calc - double_major_required.completed)
        else:
            elective_req = combined_req
            elective_calc = combined_calc

        double_major_elective = CreditSummaryItem(
            required=elective_req,
            completed=elective_calc,
            satisfied=elective_calc >= elective_req,
        )

    return GraduationSummary(
        generalRequired=general_required,
        generalElective=general_elective,
        majorFoundation=major_foundation,
        majorRequired=major_required,
        majorElective=major_elective,
        majorRequiredElectiveCombined=major_required_elective_combined,
        minor=minor,
        doubleMajorRequired=double_major_required,
        doubleMajorElective=double_major_elective,
        christianCourses=christian,
        chapel=chapel,
    )
