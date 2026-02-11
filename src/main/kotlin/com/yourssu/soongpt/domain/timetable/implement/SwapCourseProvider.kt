package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.CourseReader
import org.springframework.stereotype.Component

@Component
class SwapCourseProvider(
    private val courseReader: CourseReader,
    private val untakenCourseCodeService: UntakenCourseCodeService,
) {
    fun findAlternatives(track: SwapTrack): List<Course> {
        val category = track.toCourseCategory()
        val courseCodes = untakenCourseCodeService.getUntakenCourseCodes(category)
        if (courseCodes.isEmpty()) return emptyList()
        return courseReader.findAllByCode(courseCodes)
    }
}
