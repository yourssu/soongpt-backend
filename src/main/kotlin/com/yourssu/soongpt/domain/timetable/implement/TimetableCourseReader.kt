package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import org.springframework.stereotype.Component

@Component
class TimetableCourseReader(
    private val timetableCourseRepository: TimetableCourseRepository,
    private val courseRepository: CourseRepository,
) {
    fun findAllCourseByTimetableId(timetableId: Long): List<Course> {
        val courseIds = timetableCourseRepository.findAllCourseByTimetableId(timetableId).map { it.courseId }
        return courseRepository.getAll(courseIds)
    }
}