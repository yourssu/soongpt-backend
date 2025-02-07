package com.yourssu.soongpt.domain.courseTime.implement

enum class Week(
    val displayName: String,
) {
    MONDAY("mon"),
    TUESDAY("tue"),
    WEDNESDAY("wed"),
    THURSDAY("thu"),
    FRIDAY("fri"),
    SATURDAY("sat"),
    SUNDAY("sun");

    companion object {
        fun fromName(name: String): Week {
            return when(name) {
                "월" -> MONDAY
                "화" -> TUESDAY
                "수" -> WEDNESDAY
                "목" -> THURSDAY
                "금" -> FRIDAY
                "토" -> SATURDAY
                "일" -> SUNDAY
                else -> throw IllegalArgumentException("알 수 없는 요일 : $name")
            }
        }

        fun weekdays(): List<Week> {
            return listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        }
    }
}
