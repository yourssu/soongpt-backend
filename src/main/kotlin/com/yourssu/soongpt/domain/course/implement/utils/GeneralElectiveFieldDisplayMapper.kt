package com.yourssu.soongpt.domain.course.implement.utils

/**
 * 교양선택(교선) 분야 raw 데이터를 학번별로 이쁜 표시용 문자열로 매핑.
 *
 * - A: progress.fieldCredits 키 (분야별 이수 학점 대분류)
 * - B: courses[].field (과목별 분야 표시)
 *
 * ~22학번: A = 숭실품성 / 균형교양 / 기초역량, B = 인성과 리더십 등 소분야
 * 23학번~: A = 인간 / 문화 / 사회 / 과학 / 자기개발, B = 인간·언어 등
 * schoolId <= 20일 때 A만 매핑 결과 뒤에 "교과" 붙임 (띄어쓰기 없음). 매칭(contains) 판단에는 교과 미사용.
 */
object GeneralElectiveFieldDisplayMapper {

    /**
     * progress.fieldCredits 맵의 키로 쓸 표시용 분야명 (A).
     * ~22: 숭실품성, 균형교양, 기초역량 중 하나 (매칭은 raw 기준, 교과 미포함)
     *      schoolId <= 20이면 위 값 뒤에 "교과" 붙여서 반환 (숭실품성교과 등)
     * 23~: 인간, 문화, 사회, 과학, 자기개발 중 하나
     */
    fun mapForProgressFieldCredits(rawField: String, admissionYear: Int, schoolId: Int): String {
        if (rawField.isBlank()) return rawField
        val base = if (admissionYear <= 2022) {
            when {
                rawField.contains("숭실품성") -> "숭실품성"
                rawField.contains("균형교양") -> "균형교양"
                rawField.contains("기초역량") -> "기초역량"
                else -> rawField
            }
        } else {
            val dotIndex = rawField.indexOf('·')
            if (dotIndex > 0) rawField.substring(0, dotIndex).trim() else rawField
        }
        return if (admissionYear <= 2022 && schoolId <= 20 && base in listOf("숭실품성", "균형교양", "기초역량")) {
            base + "교과"
        } else {
            base
        }
    }

    /**
     * courses[].field 로 쓸 표시용 분야명 (B).
     * ~22: 소분야 이쁘게 (인성과 리더십, 자기계발과 진로탐색 등)
     * 23~: raw 그대로 사용 (이미 인간·언어, 문화·예술 등 표준형)
     */
    fun mapForCourseField(rawField: String, admissionYear: Int): String {
        if (rawField.isBlank()) return rawField
        if (admissionYear > 2022) return rawField

        // ~22: "prefix,대분류-소분류" 형태 → 소분류만 추출 후 표시용 매핑
        val afterDash = rawField.substringAfterLast('-').trim()
        return RAW_TO_DISPLAY_B_22[afterDash]
            ?: RAW_TO_DISPLAY_B_22.entries.firstOrNull { (raw, _) -> afterDash.startsWith(raw) || raw.startsWith(afterDash) }?.value
            ?: afterDash
    }

    /** ~22학번 B: raw 소분야 문자열 → 표시용 (공백 등 보정) */
    private val RAW_TO_DISPLAY_B_22 = mapOf(
        "인성과리더십" to "인성과 리더십",
        "자기계발과진로탐색" to "자기계발과 진로탐색",
        "자기계발과진로" to "자기계발과 진로탐색",
        "한국어의사소통" to "한국어의사소통",
        "한국어의사소통과국제어문" to "한국어의사소통",
        "국제어문" to "국제어문",
        "문학·예술" to "문학·예술",
        "역사·철학·종교" to "역사·철학·종교",
        "정치·경제·경영" to "정치·경제·경영",
        "사회·문화·심리" to "사회·문화·심리",
        "자연과학·공학·기술" to "자연과학·공학·기술",
    )
}
