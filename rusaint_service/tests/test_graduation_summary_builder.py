"""
graduation_summary_builder 테스트.

학과별 졸업사정표 패턴에 따른 전공 복합 필드 해석을 검증합니다.
"""

import pytest

from app.schemas.usaint_schemas import GraduationRequirementItem
from app.services.graduation_summary_builder import build_graduation_summary


def _req(
    name: str,
    requirement: int,
    calculation: float,
    result: bool,
    category: str = "",
) -> GraduationRequirementItem:
    """테스트용 졸업 요건 항목 생성 헬퍼"""
    return GraduationRequirementItem(
        name=name,
        requirement=requirement,
        calculation=calculation,
        difference=calculation - requirement,
        result=result,
        category=category,
    )


# ======================================================================
# Case 1: 전기 + 전필 + 전선 (전필·전선 복합)
# 예: 컴퓨터학부 — "전기 15", "전필 21", "전필·전선 51"
# "전필·전선" = 전필+전선 → 전선 = 51-21 = 30
# ======================================================================
class TestCase1_전기_전필_전선_복합명시:
    def test_전필전선_복합에서_전선_역산(self):
        reqs = [
            _req("학부-전기 15", 15, 15, True),
            _req("학부-전필 21", 21, 18, False),
            _req("학부-전필·전선 51", 51, 33, False),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorFoundation is not None
        assert summary.majorFoundation.required == 15
        assert summary.majorFoundation.satisfied is True

        assert summary.majorRequired is not None
        assert summary.majorRequired.required == 21
        assert summary.majorRequired.completed == 18

        assert summary.majorElective is not None
        assert summary.majorElective.required == 30  # 51 - 21
        assert summary.majorElective.completed == 15  # 33 - 18
        assert summary.majorElective.satisfied is False  # 15 < 30


# ======================================================================
# Case 2: 전기 + 전필 + 전선 ("전공" 복합)
# 예: 일부 학과 — "전기 15", "전필 21", "전공 51"
# "전공" = 전필+전선 (전필 단독 존재하므로) → 전선 = 51-21 = 30
# ======================================================================
class TestCase2_전기_전필_전공복합:
    def test_전공_복합에서_전필_빼서_전선(self):
        reqs = [
            _req("학부-전기 15", 15, 15, True),
            _req("학부-전필 21", 21, 21, True),
            _req("학부-전공 51", 51, 48, False),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorFoundation is not None
        assert summary.majorFoundation.required == 15

        assert summary.majorRequired is not None
        assert summary.majorRequired.required == 21
        assert summary.majorRequired.completed == 21

        assert summary.majorElective is not None
        assert summary.majorElective.required == 30  # 51 - 21
        assert summary.majorElective.completed == 27  # 48 - 21
        assert summary.majorElective.satisfied is False  # 27 < 30


# ======================================================================
# Case 3: 전기 + 전선 (전필 없음, "전공" 복합)
# 예: 경영학부 — "전기 15", "전공 42"
# "전공" = 전기+전선 (전필 없으므로) → 전선 = 42-15 = 27
# ======================================================================
class TestCase3_전기_전선_전필없음:
    def test_전공_복합에서_전기_빼서_전선(self):
        reqs = [
            _req("학부-전기 15", 15, 15, True),
            _req("학부-전공 42", 42, 30, False),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorFoundation is not None
        assert summary.majorFoundation.required == 15

        assert summary.majorRequired is None  # 전필 없음

        assert summary.majorElective is not None
        assert summary.majorElective.required == 27  # 42 - 15
        assert summary.majorElective.completed == 15  # 30 - 15
        assert summary.majorElective.satisfied is False  # 15 < 27


# ======================================================================
# Case 4: 전필 + 전선 (전기 없음, "전공" 복합)
# 예: 일부 학과 — "전필 21", "전공 51"
# "전공" = 전필+전선 (전필 존재) → 전선 = 51-21 = 30
# ======================================================================
class TestCase4_전필_전선_전기없음:
    def test_전공_복합에서_전필_빼서_전선(self):
        reqs = [
            _req("학부-전필 21", 21, 15, False),
            _req("학부-전공 51", 51, 33, False),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorFoundation is None  # 전기 없음

        assert summary.majorRequired is not None
        assert summary.majorRequired.required == 21
        assert summary.majorRequired.completed == 15

        assert summary.majorElective is not None
        assert summary.majorElective.required == 30  # 51 - 21
        assert summary.majorElective.completed == 18  # 33 - 15
        assert summary.majorElective.satisfied is False  # 18 < 30


# ======================================================================
# Case 5: 전선 satisfied 계산 — 항상 completed >= required
# ======================================================================
class TestCase5_전선_satisfied_계산:
    def test_전선_충족(self):
        reqs = [
            _req("학부-전필 21", 21, 21, True),
            _req("학부-전필·전선 51", 51, 51, True),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorElective is not None
        assert summary.majorElective.required == 30  # 51 - 21
        assert summary.majorElective.completed == 30  # 51 - 21
        assert summary.majorElective.satisfied is True  # 30 >= 30

    def test_전선_미충족_복합_충족이어도(self):
        """복합 전체가 충족이더라도 전선은 자체 required/completed로 판단"""
        reqs = [
            _req("학부-전필 10", 10, 40, True),  # 전필 초과 이수
            _req("학부-전필·전선 50", 50, 50, True),  # 복합 충족
        ]
        summary = build_graduation_summary(reqs)

        assert summary.majorElective is not None
        assert summary.majorElective.required == 40  # 50 - 10
        assert summary.majorElective.completed == 10  # 50 - 40
        assert summary.majorElective.satisfied is False  # 10 < 40


# ======================================================================
# Case 6: 매칭 안 되는 항목은 null
# ======================================================================
class TestCase6_null_처리:
    def test_빈_requirements(self):
        summary = build_graduation_summary([])

        assert summary.majorFoundation is None
        assert summary.majorRequired is None
        assert summary.majorElective is None
        assert summary.generalRequired is None
        assert summary.chapel is None

    def test_교양만_있을때_전공_null(self):
        reqs = [
            _req("학부-교양필수 19", 19, 17, False),
            _req("학부-교양선택 12", 12, 15, True),
        ]
        summary = build_graduation_summary(reqs)

        assert summary.generalRequired is not None
        assert summary.generalElective is not None
        assert summary.majorFoundation is None
        assert summary.majorRequired is None
        assert summary.majorElective is None


# ======================================================================
# Case 7: "전공" 매처가 다른 필드와 충돌하지 않는지
# ======================================================================
class TestCase7_매처_충돌_없음:
    def test_전공기초는_foundation으로(self):
        reqs = [_req("학부-전공기초 15", 15, 15, True)]
        summary = build_graduation_summary(reqs)
        assert summary.majorFoundation is not None
        assert summary.majorElective is None

    def test_전공선택은_elective로(self):
        reqs = [_req("학부-전공선택 27", 27, 20, False)]
        summary = build_graduation_summary(reqs)
        assert summary.majorElective is not None
        assert summary.majorElective.required == 27
        assert summary.majorFoundation is None

    def test_복수전공은_double_major로(self):
        reqs = [_req("학부-복수전공 21", 21, 18, False)]
        summary = build_graduation_summary(reqs)
        assert summary.doubleMajorElective is not None
        assert summary.majorElective is None  # "전공" 매처에 잡히면 안 됨

    def test_부전공은_minor로(self):
        reqs = [_req("학부-부전공 21", 21, 18, False)]
        summary = build_graduation_summary(reqs)
        assert summary.minor is not None
        assert summary.majorElective is None


# ======================================================================
# Case 8: 채플
# ======================================================================
class TestCase8_채플:
    def test_채플_충족(self):
        reqs = [_req("학부-채플", 0, 0, True)]
        summary = build_graduation_summary(reqs)
        assert summary.chapel is not None
        assert summary.chapel.satisfied is True

    def test_채플_미충족(self):
        reqs = [_req("학부-채플", 0, 0, False)]
        summary = build_graduation_summary(reqs)
        assert summary.chapel is not None
        assert summary.chapel.satisfied is False

    def test_채플_없으면_null(self):
        summary = build_graduation_summary([])
        assert summary.chapel is None
