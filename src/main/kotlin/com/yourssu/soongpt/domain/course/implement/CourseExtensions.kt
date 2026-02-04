package com.yourssu.soongpt.domain.course.implement

private const val DIVISION_DIVISOR = 100

fun Course.isWeekend(): Boolean =
    scheduleRoom.contains("토") || scheduleRoom.contains("일")

fun List<Course>.preferWeekday(): List<Course> {
    val (weekdays, weekends) = partition { !it.isWeekend() }
    return weekdays.ifEmpty { weekends }
}

/** 10자리 과목코드에서 8자리 기본코드 추출 (분반 제거) */
fun Course.baseCode(): Long = code / DIVISION_DIVISOR

/** Long 코드에서 8자리 기본코드 추출 */
fun Long.toBaseCode(): Long = this / DIVISION_DIVISOR
