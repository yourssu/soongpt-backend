"""
Rusaint 서비스에서 사용하는 상수.
"""

import rusaint

# 학기 타입 매핑 (rusaint.SemesterType → API 문자열)
SEMESTER_TYPE_MAP = {
    rusaint.SemesterType.ONE: "1",
    rusaint.SemesterType.TWO: "2",
    rusaint.SemesterType.SUMMER: "SUMMER",
    rusaint.SemesterType.WINTER: "WINTER",
}

# 채플 관련 과목 코드 (학점 과목이 아님, 졸업 요건은 별도 API에서 확인)
CHAPEL_CODES = frozenset({"21501015", "21500785"})  # 채플, 소그룹채플
