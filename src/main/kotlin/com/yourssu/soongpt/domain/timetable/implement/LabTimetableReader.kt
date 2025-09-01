package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseReader
import com.yourssu.soongpt.domain.timetable.implement.dto.LabTimetableResult
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.stereotype.Component

@Component
class LabTimetableReader(
    private val timetableReader: TimetableReader,
    private val timetableCourseReader: TimetableCourseReader,
    private val labTimetableValidator: LabTimetableValidator,
) {
    fun getValidRandomTimetable(): LabTimetableResult {
        // 최대 3번까지 시도
        repeat(3) {
            val attempt = attemptGetValidTimetable()
            if (attempt != null) return attempt
        }
        throw TimetableNotFoundException()
    }

    private fun attemptGetValidTimetable(): LabTimetableResult? {
        val timetable = timetableReader.getRandom() ?: return null
        val rawCourses = timetableCourseReader.findAllCourseByTimetableId(timetable.id!!)
        val filteredCourses = labTimetableValidator.filterValidCourses(rawCourses)

        if (filteredCourses.isEmpty()) return null
        if (labTimetableValidator.hasOverlap(filteredCourses)) return null

        return LabTimetableResult(timetable, filteredCourses)
    }
}
