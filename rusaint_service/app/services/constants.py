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

# ============================================================
# 교양필수 재수강 매핑 (2022학년도 이전 입학자 구과목 → 신과목)
# ============================================================
# 폐강된 구과목의 과목명(rusaint class_name) → 대체 신과목의 baseCode(8자리)
# 구과목 코드로 현재 학기 DB 조회 시 매칭 불가 → 신과목 baseCode를 추가하여 대체 과목 추천
#
# 독서와토론        → [인문적상상력과소통] 고전읽기와상상력
# 대학글쓰기        → [비판적사고와표현] 미디어사회와비평적글쓰기
# 기업가정신과행동   → [창의적사고와혁신] 혁신과기업가정신
# 현대인과성서       → [인간과성서] 현대사회이슈와기독교
# 컴퓨터사고        → [컴퓨팅적사고] 컴퓨팅적사고와코딩기초
# AI와데이터사회     → [SW와AI] AI와데이터기초
# Academic and Professional English 1, 2 → 재수강반 없음 (학점 포기만 가능)
RETAKE_GENERAL_REQUIRED_MAPPING: dict[str, str] = {
    "독서와토론": "21501003",
    "대학글쓰기": "21501006",
    "기업가정신과행동": "21501009",
    "현대인과성서": "21501020",
    "컴퓨터사고": "21501028",
    "AI와데이터사회": "21501034",
}
