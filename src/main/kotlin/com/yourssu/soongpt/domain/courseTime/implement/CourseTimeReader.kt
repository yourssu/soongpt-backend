package com.yourssu.soongpt.domain.courseTime.implement

import org.springframework.stereotype.Component

@Component
class CourseTimeReader(
    private val courseTimeRepository: CourseTimeRepository,
) {
    fun findAllByCourseId(courseId: Long): List<CourseTime> {
        return courseTimeRepository.findAllByCourseId(courseId)
    }
}
