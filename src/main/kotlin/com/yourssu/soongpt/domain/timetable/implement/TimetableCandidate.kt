package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses

class TimetableCandidate(
    val courses: Courses,
    private val coursesTimes: CourseTimes,
    val tag: Tag,
    ) {
    companion object {
        fun fromAllTags(courses: Courses, coursesTimes: CourseTimes): List<TimetableCandidate> {
            return Tag.entries.map { tag ->
                TimetableCandidate(courses, coursesTimes, tag)
            }
        }
    }

    fun isCorrect(): Boolean {
        return tag.strategy.isCorrect(courses, coursesTimes)
    }
}