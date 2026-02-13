package com.yourssu.soongpt.domain.course.implement.utils

/**
 * 교양선택(교선) 분야 raw 데이터를 학번별로 이쁜 표시용 문자열로 매핑.
 *
 * - A: progress.fieldCredits 키 (분야별 이수 과목 수 대분류)
 * - B: courses[].field (과목별 분야 표시)
 *
 * ~20학번: A = 공동체/리더십 / 의사소통/글로벌 / 창의/융합, schoolId<=20이면 "역량" 붙임
 * 21~22학번: A = 숭실품성 / 균형교양 / 기초역량, schoolId<=20이면 "교과" 붙임
 * 23학번~: A = 인간 / 문화 / 사회 / 과학 / 자기개발
 */
object GeneralElectiveFieldDisplayMapper {

    private val BASE_20_OR_BELOW = listOf("공동체/리더십", "의사소통/글로벌", "창의/융합")
    private val BASE_21_22 = listOf("숭실품성", "균형교양", "기초역량")
    private val FIELDS_23_OR_ABOVE = listOf("인간", "문화", "사회", "과학", "자기개발")

    /** 창의/융합(20), 균형교양(21~22)의 세부필드 5개. 디폴트로 전부 보내고 없으면 0 */
    val BALANCE_SUB_FIELDS = listOf(
        "문학·예술",
        "역사·철학·종교",
        "정치·경제·경영",
        "사회·문화·심리",
        "자연과학·공학·기술",
    )

    /**
     * admissionYear, schoolId에 해당하는 progress.fieldCredits 전체 구조 생성.
     * 균형교양교과(21~22), 창의/융합역량(20)은 객체 안에 세부필드 5개. 나머지는 Int.
     * @param rawFieldCounts raw 분야 → 이수 과목 수
     * @return Map<String, Any> (Int 또는 Map<String, Int>)
     */
    fun buildFieldCreditsStructure(
        admissionYear: Int,
        schoolId: Int,
        rawFieldCounts: Map<String, Int>,
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        when {
            admissionYear <= 2020 -> {
                val suffix = if (schoolId <= 20) "역량" else ""
                val balanceKey = "창의/융합" + suffix
                result["공동체/리더십" + suffix] = 0
                result["의사소통/글로벌" + suffix] = 0
                result[balanceKey] = BALANCE_SUB_FIELDS.associateWith { 0 }.toMutableMap()
            }
            admissionYear <= 2022 -> {
                // 21~22학번은 항상 "교과" (schoolId=year%100 이라 21·22학번은 schoolId>20이므로 별도 분기)
                val suffix = if (admissionYear in 2021..2022) "교과" else if (schoolId <= 20) "교과" else ""
                val balanceKey = "균형교양" + suffix
                result["숭실품성" + suffix] = 0
                result[balanceKey] = BALANCE_SUB_FIELDS.associateWith { 0 }.toMutableMap()
                result["기초역량" + suffix] = 0
            }
            else -> {
                FIELDS_23_OR_ABOVE.forEach { result[it] = 0 }
            }
        }

        for ((raw, count) in rawFieldCounts) {
            val (key, subField) = resolveProgressFieldEntry(raw, admissionYear, schoolId)
            if (subField != null) {
                @Suppress("UNCHECKED_CAST")
                val nested = result[key] as? MutableMap<String, Int> ?: continue
                nested[subField] = (nested[subField] ?: 0) + count
            } else {
                result[key] = (result[key] as? Int ?: 0) + count
            }
        }
        return result
    }

    /**
     * raw → (fieldCredits 키, 세부필드 또는 null)
     * 균형교양/창의·융합이면 (parentKey, subField), 아니면 (key, null)
     */
    private fun resolveProgressFieldEntry(rawField: String, admissionYear: Int, schoolId: Int): Pair<String, String?> {
        if (rawField.isBlank()) return rawField to null
        when {
            admissionYear <= 2020 -> {
                val suffix = if (schoolId <= 20) "역량" else ""
                return when {
                    rawField.contains("공동체") || rawField.contains("리더십") -> "공동체/리더십$suffix" to null
                    rawField.contains("의사소통") || rawField.contains("글로벌") -> "의사소통/글로벌$suffix" to null
                    rawField.contains("창의") || rawField.contains("융합") -> {
                        val sub = if (rawField.contains('-')) resolveSubFieldDisplay(rawField) else BALANCE_SUB_FIELDS.first()
                        "창의/융합$suffix" to sub
                    }
                    else -> rawField to null
                }
            }
            admissionYear <= 2022 -> {
                val suffix = if (admissionYear in 2021..2022) "교과" else if (schoolId <= 20) "교과" else ""
                return when {
                    rawField.contains("숭실품성") -> "숭실품성$suffix" to null
                    rawField.contains("균형교양") -> {
                        val sub = if (rawField.contains('-')) resolveSubFieldDisplay(rawField) else BALANCE_SUB_FIELDS.first()
                        "균형교양$suffix" to sub
                    }
                    rawField.contains("기초역량") -> "기초역량$suffix" to null
                    else -> rawField to null
                }
            }
            else -> {
                val dotIndex = rawField.indexOf('·')
                val key = if (dotIndex > 0) rawField.substring(0, dotIndex).trim() else rawField
                return key to null
            }
        }
    }

    /**
     * progress.fieldCredits 맵의 키로 쓸 표시용 분야명 (A).
     * ~20: 공동체/리더십, 의사소통/글로벌, 창의/융합(→세부필드 5개) (매칭은 raw 기준)
     *      schoolId <= 20이면 위 값 뒤에 "역량" 붙임. 창의/융합은 세부필드(문학·예술 등)로 반환
     * 21~22: 숭실품성, 균형교양(→세부필드 5개), 기초역량
     *        균형교양은 세부필드로 반환. schoolId <= 20이면 숭실품성/기초역량에 "교과" 붙임
     * 23~: 인간, 문화, 사회, 과학, 자기개발 중 하나
     */
    fun mapForProgressFieldCredits(rawField: String, admissionYear: Int, schoolId: Int): String {
        if (rawField.isBlank()) return rawField
        when {
            admissionYear <= 2020 -> {
                val b = when {
                    rawField.contains("공동체") || rawField.contains("리더십") -> "공동체/리더십"
                    rawField.contains("의사소통") || rawField.contains("글로벌") -> "의사소통/글로벌"
                    rawField.contains("창의") || rawField.contains("융합") -> {
                        // 창의/융합 → dash 있으면 세부필드, 없으면 parent+역량
                        if (rawField.contains('-')) return resolveSubFieldDisplay(rawField)
                        val suffix = if (schoolId <= 20) "역량" else ""
                        return "창의/융합" + suffix
                    }
                    else -> return rawField
                }
                val suffix = if (schoolId <= 20 && b in BASE_20_OR_BELOW) "역량" else ""
                return b + suffix
            }
            admissionYear <= 2022 -> {
                val suffix = if (admissionYear in 2021..2022) "교과" else if (schoolId <= 20) "교과" else ""
                return when {
                    rawField.contains("숭실품성") -> "숭실품성$suffix"
                    rawField.contains("균형교양") -> {
                        if (rawField.contains('-')) resolveSubFieldDisplay(rawField)
                        else "균형교양$suffix"
                    }
                    rawField.contains("기초역량") -> "기초역량$suffix"
                    else -> rawField
                }
            }
            else -> {
                val dotIndex = rawField.indexOf('·')
                return if (dotIndex > 0) rawField.substring(0, dotIndex).trim() else rawField
            }
        }
    }

    /** raw에서 dash 이후 소분류 추출 후 표시용 매핑 (균형교양/창의·융합 세부필드) */
    private fun resolveSubFieldDisplay(rawField: String): String {
        val afterDash = rawField.substringAfterLast('-').trim()
        return RAW_TO_DISPLAY_B_22[afterDash]
            ?: RAW_TO_DISPLAY_B_22.entries.firstOrNull { (raw, _) -> afterDash.startsWith(raw) || raw.startsWith(afterDash) }?.value
            ?: afterDash
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
