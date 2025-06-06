package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course2
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse

data class SearchCourseResponse(
    val name: String,
    val professor: String?,
    val code: Int?,
    val category: String,
    val credit: Int,
    val target: String,
    val field: String?,
    val courseTime: List<CourseTimeResponse>,
) {
    companion object {
        fun from(
            course: Course2,
        ): SearchCourseResponse {
            return SearchCourseResponse(
                name = course.courseName,
                professor = course.professorName,
                code = course.courseCode,
                category = course.category.name,
                credit = course.credit,
                target = course.target,
                field = course.field,
                courseTime = course.courseTime.map { CourseTimeResponse.from(it) }
            )
        }
    }
}
