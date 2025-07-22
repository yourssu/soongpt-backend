package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.Target
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
)  {
    fun findAllInMajorRequired(targets: List<Target>): List<Course> {
        val courseIds = targets.map { it.courseId }
        return courseRepository.getAllInCategory(Category.MAJOR_REQUIRED, courseIds)
    }

    fun findAllInMajorElective(targets: List<Target>): List<Course> {
        val courseIds = targets.map { it.courseId }
        return courseRepository.getAllInCategory(Category.MAJOR_ELECTIVE, courseIds)
    }

    fun findAllInGeneralRequired(targets: List<Target>): List<Course> {
        val courseIds = targets.map { it.courseId }
        return courseRepository.getAllInCategory(Category.GENERAL_REQUIRED, courseIds)
    }
}
