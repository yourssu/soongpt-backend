package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseReader
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.stereotype.Component

@Component
class LabTimetableReader(
    private val timetableReader: TimetableReader,
    private val timetableCourseReader: TimetableCourseReader,
    private val labTimetableValidator: LabTimetableValidator,
) {
    fun getValidRandomTimetable(): Pair<Timetable, List<Course>> {
        // 첫 번째 시도
        val firstAttempt = attemptGetValidTimetable()
        if (firstAttempt != null) return firstAttempt

        // 두 번째 시도
        val secondAttempt = attemptGetValidTimetable()
        if (secondAttempt != null) return secondAttempt

        throw TimetableNotFoundException()
    }

    private fun attemptGetValidTimetable(): Pair<Timetable, List<Course>>? {
        val timetable = timetableReader.getRandom() ?: return null
        val rawCourses = timetableCourseReader.findAllCourseByTimetableId(timetable.id!!)
        val filteredCourses = labTimetableValidator.filterValidCourses(rawCourses)

        if (filteredCourses.isEmpty()) return null
        if (labTimetableValidator.hasOverlap(filteredCourses)) return null

        return Pair(timetable, filteredCourses)
    }
}
