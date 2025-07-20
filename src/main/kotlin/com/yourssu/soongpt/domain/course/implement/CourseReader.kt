package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.Target
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
)  {
    fun findAllByTargetInMajorRequired(target: Target): List<Course> {
        return courseRepository.findAllByCategoryTarget(Category.MAJOR_REQUIRED, target)
    }

    fun findAllByTargetInMajorElective(target: Target): List<Course> {
        return courseRepository.findAllByCategoryTarget(Category.MAJOR_ELECTIVE, target)
    }

    fun findAllByTargetInGeneralRequired(target: Target): List<Course> {
        return courseRepository.findAllByCategoryTarget(Category.GENERAL_REQUIRED, target)
    }
}
