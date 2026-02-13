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

/**
 * Long 코드에서 8자리 기본코드 추출.
 * 10자리(분반 포함)면 /100, 이미 8자리면 그대로 반환.
 */
fun Long.toBaseCode(): Long =
    if (this >= 1_000_000_000L) this / DIVISION_DIVISOR else this

/**
 * rusaint 수강 과목 코드 리스트 → 이수 판정용 Set (8자리 baseCode 비교 가능).
 * rusaint가 10자리(분반 포함)로 줄 수 있어, 원본 + code/100 둘 다 넣어서
 * course.baseCode() in takenSet 비교가 항상 매칭되도록 함.
 */
fun toTakenBaseCodeSet(takenSubjectCodes: List<String>): Set<Long> =
    takenSubjectCodes.mapNotNull { it.toLongOrNull() }.flatMap { code ->
        if (code >= DIVISION_DIVISOR) listOf(code, code / DIVISION_DIVISOR) else listOf(code)
    }.toSet()

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
