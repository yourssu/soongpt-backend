package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.CourseNotFoundException
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
) {
    fun findAllByDepartmentIdInMajorRequired(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.MAJOR_REQUIRED)
    }

    fun findAllByDepartmentIdInMajorElective(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.MAJOR_ELECTIVE)
    }

    fun findAllByDepartmentIdInGeneralRequired(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.GENERAL_REQUIRED)
    }

    fun findAllByCourseNameInMajorRequired(departmentId: Long, courseName: String): Courses {
        return findAllByCourseName(departmentId, courseName, Classification.MAJOR_REQUIRED)
    }

    fun findAllByCourseNameInMajorElective(departmentId: Long, courseName: String): Courses {
        return findAllByCourseName(departmentId, courseName, Classification.MAJOR_ELECTIVE)
    }

    fun findAllByCourseNameInGeneralRequired(departmentId: Long, courseName: String): Courses {
       return findAllByCourseName(departmentId, courseName, Classification.GENERAL_REQUIRED)
    }

    private fun findAllByCourseName(departmentId: Long, courseName: String, classification: Classification): Courses {
        val courses = courseRepository.findByDepartmentIdAndCourseName(departmentId, courseName, classification)
        if (courses.isEmpty()) {
            throw CourseNotFoundException(courseName = courseName)
        }
        return courses
    }
}
