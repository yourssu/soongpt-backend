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
