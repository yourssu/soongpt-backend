package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week

enum class CourseTimeFixture(
    val week: Week,
    val startTime: Time,
    val endTime: Time,
    val classroom: String,
) {
    MONDAY_17_19(
        week = Week.MONDAY,
        startTime = Time.of("17:00"),
        endTime = Time.of("19:00"),
        classroom = "형남공학관 1004"
    ),
    TUESDAY_17_19(
        week = Week.TUESDAY,
        startTime = Time.of("17:00"),
        endTime = Time.of("19:00"),
        classroom = "형남공학관 1004"
    ),
    TUESDAY_18_20(
        week = Week.TUESDAY,
        startTime = Time.of("18:00"),
        endTime = Time.of("20:00"),
        classroom = "형남공학관 1004"
    ),
    WEDNESDAY_17_19(
        week = Week.WEDNESDAY,
        startTime = Time.of("17:00"),
        endTime = Time.of("19:00"),
        classroom = "형남공학관 1004"
    ),
    ;

    fun toDomain(courseId: Long): CourseTime {
        return CourseTime(
            week = week,
            startTime = startTime,
            endTime = endTime,
            classroom = classroom,
            courseId = courseId,
        )
    }
}
