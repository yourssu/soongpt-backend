from typing import Any

from app.schemas.timetable import FilteredCourse


def filter_course_data(raw_courses: list[dict[str, Any]]) -> list[FilteredCourse]:
    """
    rusaint에서 받아온 raw 과목 데이터에서 필요한 정보만 추출합니다.

    요구사항:
    - 과목코드 등 최소한의 정보만 유지
    - 성적 등 민감 정보는 제거
    """
    # TODO: 실제 데이터 구조에 맞게 필드 매핑/필터링 구현
    filtered: list[FilteredCourse] = []
    for course in raw_courses:
        subject_code = str(course.get("subjectCode") or course.get("code") or "")
        if not subject_code:
            continue
        filtered.append(FilteredCourse(subjectCode=subject_code))
    return filtered
