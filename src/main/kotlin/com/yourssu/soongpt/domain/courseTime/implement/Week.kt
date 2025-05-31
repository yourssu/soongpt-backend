package com.yourssu.soongpt.domain.courseTime.implement

enum class Week(
    val displayName: String,
) {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일"),
    UNKNOWN("알 수 없는 요일"),
    ;

    companion object {
        fun fromName(name: String): Week {
            return entries.find { it.displayName == name } ?: UNKNOWN
        }

        fun weekdays(): List<Week> {
            return listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        }
    }
}
