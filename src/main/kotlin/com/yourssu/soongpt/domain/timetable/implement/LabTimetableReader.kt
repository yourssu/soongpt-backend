package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.dto.LabTimetableResult
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.stereotype.Component

@Component
class LabTimetableReader(
    private val timetableReader: TimetableReader,
    private val timetableCourseReader: TimetableCourseReader,
    private val labTimetableValidator: LabTimetableValidator,
) {
    private val maxAttempts = 15

    fun getValidRandomTimetable(): LabTimetableResult {
        repeat(maxAttempts) {
            val result = attemptGetValidTimetable() ?: return@repeat
            return result
        }
        throw TimetableNotFoundException()
    }

    private fun attemptGetValidTimetable(): LabTimetableResult? {
        val tag = Tag.entries.random()
        val timetable = timetableReader.getRandomByTag(tag) ?: timetableReader.getRandom() ?: return null
        val rawCourses = timetableCourseReader.findAllCourseByTimetableId(timetable.id!!)
        val filtered = labTimetableValidator.filterValidCourses(rawCourses)

        if (filtered.isEmpty()) return null
        if (!labTimetableValidator.hasChapel(rawCourses)) return null
        if (labTimetableValidator.hasOverlap(filtered)) return null

        return LabTimetableResult(timetable, filtered)
    }
}
