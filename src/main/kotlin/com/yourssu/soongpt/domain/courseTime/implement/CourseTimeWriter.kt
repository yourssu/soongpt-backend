package com.yourssu.soongpt.domain.courseTime.implement

import org.springframework.stereotype.Component

@Component
class CourseTimeWriter(
    private val courseTimeRepository: CourseTimeRepository,
) {
    fun save(courseTime: CourseTime): CourseTime {
        return courseTimeRepository.save(courseTime)
    }
}