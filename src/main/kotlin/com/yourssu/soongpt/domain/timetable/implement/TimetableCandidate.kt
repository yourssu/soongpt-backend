package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses

class TimetableCandidate(
    val courses: Courses,
    private val coursesTimes: CourseTimes,
    val tag: Tag,
    private val score: Int = 0,
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

    fun isCorrectCreditRule(): Boolean {
        return courses.totalCredit() < 23
    }

    fun hasOverlappingCourseTimes(): Boolean {
        return coursesTimes.hasOverlappingCourseTimes()
    }

    fun generateNewTimetableCandidate(
        courses: Courses,
        courseTimes: CourseTimes,
        score: Int,
    ): TimetableCandidate {
        return TimetableCandidate(
            courses = this.courses.extend(courses),
            coursesTimes = this.coursesTimes.extend(courseTimes),
            tag = tag,
            score = this.score + score,
        )
    }
}