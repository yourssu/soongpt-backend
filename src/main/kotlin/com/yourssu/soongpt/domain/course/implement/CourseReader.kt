package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.SearchCourseDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val jsonCourseRepository: JsonCourseRepository,
) {
    fun findAllByDepartmentGradeInMajorRequired(department: Department, grade: DepartmentGrade): List<Course> {
        return jsonCourseRepository.findAllByDepartmentAndGrade(department, grade, Category.MAJOR_REQUIRED)
    }

    fun findAllByDepartmentGradeInMajorElective(department: Department, grade: DepartmentGrade): List<Course> {
        return jsonCourseRepository.findAllByDepartmentAndGrade(department, grade, Category.MAJOR_ELECTIVE)
    }

    fun findAllByDepartmentGradeInGeneralRequired(department: Department, grade: DepartmentGrade): List<Course> {
        return jsonCourseRepository.findAllByDepartmentAndGrade(department, grade, Category.GENERAL_REQUIRED)
    }

    fun search(query: String, pageable: Pageable): SearchCourseDto {
        return jsonCourseRepository.search(query, pageable)
    }
}
