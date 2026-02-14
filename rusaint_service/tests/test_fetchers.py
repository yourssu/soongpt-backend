"""
fetchers.py 로직 단위 테스트.

mock 데이터로 다음 시나리오를 검증:
1. 채플 과목 제외 (takenCourses, lowGradeSubjectCodes)
2. lowGradeSubjectCodes 중복 제거
3. 재수강 후 성적 개선 시 lowGradeSubjectCodes에서 제외
4. 여러 번 수강해도 저성적이면 1회만 포함
"""

import pytest
from unittest.mock import AsyncMock, MagicMock
from dataclasses import dataclass
from typing import List

import rusaint

from app.services.fetchers import fetch_all_course_data_parallel
from app.services.constants import SEMESTER_TYPE_MAP, CHAPEL_CODES


@dataclass
class MockSemesterGrade:
    """학기 정보 mock"""
    year: int
    semester: rusaint.SemesterType


@dataclass
class MockClassGrade:
    """수강 과목 정보 mock"""
    code: str
    rank: str = None
    class_name: str = ""


def create_mock_apps(semesters: List[MockSemesterGrade], classes_by_semester: List[List[MockClassGrade]]):
    """
    CourseGradesApplication mock 쌍 생성.

    fetch_all_course_data_parallel은 학기를 두 그룹으로 나눠서 처리하므로,
    app1은 앞쪽 학기, app2는 뒤쪽 학기를 담당하도록 mock 구성.
    """
    # 학기 분할 로직 (fetchers.py와 동일)
    if len(semesters) <= 1:
        semesters_group1 = semesters
        semesters_group2 = []
    else:
        mid_point = (len(semesters) + 1) // 2
        semesters_group1 = semesters[:mid_point]
        semesters_group2 = semesters[mid_point:]

    classes_group1 = classes_by_semester[:len(semesters_group1)]
    classes_group2 = classes_by_semester[len(semesters_group1):]

    # app1: semesters 전체 반환 (첫 번째 앱이 semesters 조회)
    app1 = AsyncMock()
    app1.semesters = AsyncMock(return_value=semesters)

    call_count1 = [0]
    async def mock_classes1(*args, **kwargs):
        idx = call_count1[0]
        call_count1[0] += 1
        if idx < len(classes_group1):
            return classes_group1[idx]
        return []
    app1.classes = mock_classes1

    # app2: 뒤쪽 학기 담당
    app2 = AsyncMock()
    app2.semesters = AsyncMock(return_value=[])

    call_count2 = [0]
    async def mock_classes2(*args, **kwargs):
        idx = call_count2[0]
        call_count2[0] += 1
        if idx < len(classes_group2):
            return classes_group2[idx]
        return []
    app2.classes = mock_classes2

    return app1, app2


@pytest.mark.asyncio
async def test_chapel_courses_excluded_from_taken_courses():
    """채플 과목이 takenCourses에서 제외되는지 검증"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="12345678", rank="A"),
            MockClassGrade(code="21501015", rank="P"),  # 채플
            MockClassGrade(code="21500785", rank="P"),  # 소그룹채플
            MockClassGrade(code="87654321", rank="B"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    taken_courses, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    assert len(taken_courses) == 1
    subject_codes = taken_courses[0].subjectCodes

    # 채플 과목 제외 확인
    assert "21501015" not in subject_codes
    assert "21500785" not in subject_codes
    # 일반 과목은 포함
    assert "12345678" in subject_codes
    assert "87654321" in subject_codes


@pytest.mark.asyncio
async def test_chapel_courses_excluded_from_low_grade_codes():
    """채플 과목이 lowGradeSubjectCodes에서 제외되는지 검증 (F 성적이어도)"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="12345678", rank="F"),
            MockClassGrade(code="21501015", rank="F"),  # 채플 F (있을 수 없지만 테스트용)
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 채플은 F여도 제외
    assert "21501015" not in low_grade_codes
    # 일반 과목은 포함
    assert "12345678" in low_grade_codes


@pytest.mark.asyncio
async def test_low_grade_codes_no_duplicates():
    """같은 과목을 여러 학기 C 이하로 수강해도 lowGradeSubjectCodes에 1회만 포함"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.TWO),
        MockSemesterGrade(year=2025, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [MockClassGrade(code="11111111", rank="D0")],  # 2024-1: D0
        [MockClassGrade(code="11111111", rank="C0")],  # 2024-2: C0 (재수강)
        [MockClassGrade(code="11111111", rank="C0")],  # 2025-1: C0 (또 재수강)
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 중복 없이 1회만 포함
    assert low_grade_codes.count("11111111") == 1


@pytest.mark.asyncio
async def test_retake_improved_grade_excluded():
    """재수강 후 B 이상으로 개선되면 lowGradeSubjectCodes에서 제외"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.TWO),
    ]
    classes_by_semester = [
        [MockClassGrade(code="22222222", rank="D0")],  # 2024-1: D0
        [MockClassGrade(code="22222222", rank="A0")],  # 2024-2: A0 (재수강 성공)
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 최신 성적이 A0이므로 제외
    assert "22222222" not in low_grade_codes


@pytest.mark.asyncio
async def test_retake_still_low_grade_included_once():
    """재수강 후에도 여전히 C 이하면 lowGradeSubjectCodes에 1회만 포함"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.TWO),
        MockSemesterGrade(year=2025, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [MockClassGrade(code="33333333", rank="F")],   # 2024-1: F
        [MockClassGrade(code="33333333", rank="D0")],  # 2024-2: D0 (약간 개선)
        [MockClassGrade(code="33333333", rank="C0")],  # 2025-1: C0 (여전히 저성적)
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 최신 성적이 C0이므로 포함, 단 1회만
    assert "33333333" in low_grade_codes
    assert low_grade_codes.count("33333333") == 1


@pytest.mark.asyncio
async def test_mixed_scenario():
    """복합 시나리오: 채플 제외 + 재수강 개선 + 저성적 유지"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.TWO),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="21501015", rank="P"),    # 채플
            MockClassGrade(code="AAAA1111", rank="D0"),   # 과목A: D0
            MockClassGrade(code="BBBB2222", rank="F"),    # 과목B: F
            MockClassGrade(code="CCCC3333", rank="A+"),   # 과목C: A+ (정상)
        ],
        [
            MockClassGrade(code="21500785", rank="P"),    # 소그룹채플
            MockClassGrade(code="AAAA1111", rank="B+"),   # 과목A: B+ (재수강 성공)
            MockClassGrade(code="BBBB2222", rank="C0"),   # 과목B: C0 (여전히 저성적)
            MockClassGrade(code="DDDD4444", rank="D+"),   # 과목D: D+ (신규 저성적)
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    taken_courses, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # takenCourses 검증
    all_subject_codes = []
    for tc in taken_courses:
        all_subject_codes.extend(tc.subjectCodes)

    # 채플 제외
    assert "21501015" not in all_subject_codes
    assert "21500785" not in all_subject_codes

    # 일반 과목 포함
    assert "AAAA1111" in all_subject_codes
    assert "BBBB2222" in all_subject_codes
    assert "CCCC3333" in all_subject_codes
    assert "DDDD4444" in all_subject_codes

    # lowGradeSubjectCodes 검증
    # 과목A: D0 → B+ (개선됨) → 제외
    assert "AAAA1111" not in low_grade_codes
    # 과목B: F → C0 (여전히 저성적) → 포함
    assert "BBBB2222" in low_grade_codes
    # 과목C: A+ → 제외
    assert "CCCC3333" not in low_grade_codes
    # 과목D: D+ → 포함
    assert "DDDD4444" in low_grade_codes
    # 채플 제외
    assert "21501015" not in low_grade_codes
    assert "21500785" not in low_grade_codes


@pytest.mark.asyncio
async def test_no_rank_courses_excluded_from_low_grade():
    """성적(rank)이 없는 과목은 lowGradeSubjectCodes에서 제외"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="44444444", rank=None),   # 성적 없음 (진행중 등)
            MockClassGrade(code="55555555", rank=""),     # 빈 문자열
            MockClassGrade(code="66666666", rank="F"),    # F
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 성적 없는 과목 제외
    assert "44444444" not in low_grade_codes
    assert "55555555" not in low_grade_codes
    # F는 포함
    assert "66666666" in low_grade_codes


# ============================================================
# 교양필수 재수강 매핑 테스트
# ============================================================

@pytest.mark.asyncio
async def test_retake_mapping_adds_replacement_code():
    """폐강된 교양필수 구과목(현대인과성서)이 저성적이면 대체 신과목 baseCode가 추가된다"""
    semesters = [
        MockSemesterGrade(year=2022, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="99990001", rank="D0", class_name="현대인과성서"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 원본 코드 포함
    assert "99990001" in low_grade_codes
    # 대체 신과목 baseCode (현대사회이슈와기독교) 추가
    assert "21501020" in low_grade_codes


@pytest.mark.asyncio
async def test_retake_mapping_all_old_courses():
    """모든 구과목 → 신과목 매핑이 정상 동작하는지 검증"""
    semesters = [
        MockSemesterGrade(year=2021, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="10000001", rank="F", class_name="독서와토론"),
            MockClassGrade(code="10000002", rank="D0", class_name="대학글쓰기"),
            MockClassGrade(code="10000003", rank="C0", class_name="기업가정신과행동"),
            MockClassGrade(code="10000004", rank="D+", class_name="현대인과성서"),
            MockClassGrade(code="10000005", rank="C+", class_name="컴퓨터사고"),
            MockClassGrade(code="10000006", rank="F", class_name="AI와데이터사회"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 원본 코드 6개 + 대체 baseCode 6개
    assert "21501003" in low_grade_codes  # 독서와토론 → 고전읽기와상상력
    assert "21501006" in low_grade_codes  # 대학글쓰기 → 미디어사회와비평적글쓰기
    assert "21501009" in low_grade_codes  # 기업가정신과행동 → 혁신과기업가정신
    assert "21501020" in low_grade_codes  # 현대인과성서 → 현대사회이슈와기독교
    assert "21501028" in low_grade_codes  # 컴퓨터사고 → 컴퓨팅적사고와코딩기초
    assert "21501034" in low_grade_codes  # AI와데이터사회 → AI와데이터기초


@pytest.mark.asyncio
async def test_retake_mapping_not_added_for_good_grade():
    """구과목이지만 성적이 B 이상이면 대체 코드가 추가되지 않는다"""
    semesters = [
        MockSemesterGrade(year=2022, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="99990001", rank="B+", class_name="현대인과성서"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # B+ → 저성적 아님 → low_grade_codes에 포함 안 됨
    assert "99990001" not in low_grade_codes
    # 대체 코드도 추가 안 됨
    assert "21501020" not in low_grade_codes


@pytest.mark.asyncio
async def test_retake_mapping_no_duplicate_if_already_present():
    """대체 과목 코드가 이미 low_grade_codes에 있으면 중복 추가하지 않는다"""
    semesters = [
        MockSemesterGrade(year=2022, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="99990001", rank="D0", class_name="현대인과성서"),
            MockClassGrade(code="21501020", rank="F", class_name="[인간과성서]현대사회이슈와기독교"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 21501020은 이미 low_grade_codes에 직접 포함 → 중복 추가 안 됨
    assert low_grade_codes.count("21501020") == 1


@pytest.mark.asyncio
async def test_retake_mapping_ignores_non_mapped_courses():
    """매핑에 없는 과목은 대체 코드가 추가되지 않는다"""
    semesters = [
        MockSemesterGrade(year=2024, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="12345678", rank="F", class_name="데이터구조"),
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 데이터구조는 매핑에 없으므로 원본 코드만 포함
    assert low_grade_codes == ["12345678"]


@pytest.mark.asyncio
async def test_retake_mapping_works_without_class_name():
    """class_name이 없는 과목은 매핑 없이 원본 코드만 포함된다"""
    semesters = [
        MockSemesterGrade(year=2022, semester=rusaint.SemesterType.ONE),
    ]
    classes_by_semester = [
        [
            MockClassGrade(code="99990001", rank="D0"),  # class_name 없음 (기본값 "")
        ],
    ]

    app1, app2 = create_mock_apps(semesters, classes_by_semester)

    _, low_grade_codes, _ = await fetch_all_course_data_parallel(
        app1, app2, SEMESTER_TYPE_MAP
    )

    # 원본 코드만 포함, 대체 코드 없음
    assert low_grade_codes == ["99990001"]
