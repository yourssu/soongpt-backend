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
        fun weekdays(): List<Week> {
            return listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        }
    }
}
