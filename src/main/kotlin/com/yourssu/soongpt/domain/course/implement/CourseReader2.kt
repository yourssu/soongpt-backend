package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.stereotype.Component

@Component
class CourseReader2(
    private val jsonCourseRepository: JsonCourseRepository,
) {
    fun findAllByDepartmentGradeInMajorRequired(department: Department, grade: DepartmentGrade): List<Course2> {
        return jsonCourseRepository.findAllByDepartmentAndGrade(department, grade, Category.MAJOR_REQUIRED)
    }
}
