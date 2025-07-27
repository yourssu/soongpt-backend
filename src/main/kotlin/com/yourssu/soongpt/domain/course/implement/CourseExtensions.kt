package com.yourssu.soongpt.domain.course.implement

fun Course.isWeekend(): Boolean =
    scheduleRoom.contains("토") || scheduleRoom.contains("일")

fun List<Course>.preferWeekday(): List<Course> {
    val (weekdays, weekends) = partition { !it.isWeekend() }
    return weekdays.ifEmpty { weekends }
}
