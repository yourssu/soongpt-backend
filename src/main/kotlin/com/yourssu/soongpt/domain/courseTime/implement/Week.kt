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
                "월" -> Week.MONDAY
                "화" -> Week.TUESDAY
                "수" -> Week.WEDNESDAY
                "목" -> Week.THURSDAY
                "금" -> Week.FRIDAY
                "토" -> Week.SATURDAY
                "일" -> Week.SUNDAY
                else -> throw IllegalArgumentException("알 수 없는 요일 : $name")
            }
        }
    }
}
