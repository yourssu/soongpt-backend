package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes

/** 분반 코드 자릿수 (10자리 중 뒤 2자리) */
const val DIVISION_DIVISOR = 100

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

/**
 * scheduleRoom에서 강의실 정보를 제외한 요일/시간만 추출
 * 예: "목 15:00-16:15 (정보과학관 21601-)" → "목 15:00-16:15"
 * 파싱 실패 시 scheduleRoom 원본 반환
 */
fun Course.scheduleWithoutRoom(): String {
    val courseTimes = CourseTimes.from(scheduleRoom).toList()
    if (courseTimes.isEmpty()) return scheduleRoom
    return courseTimes.joinToString(", ") {
        "${it.week.displayName} ${it.startTime.toTimeFormat()}-${it.endTime.toTimeFormat()}"
    }
}
