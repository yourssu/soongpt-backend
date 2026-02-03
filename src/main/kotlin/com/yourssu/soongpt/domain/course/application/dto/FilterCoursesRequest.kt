package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterCoursesQuery
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.exception.InvalidCategoryException
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class FilterCoursesRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

    @field:NotBlank(message = "카테고리는 필수입니다.")
    val category: String,  // MAJOR_REQUIRED, MAJOR_ELECTIVE, GENERAL_REQUIRED, etc.

    val field: String? = null,
    val subDepartment: String? = null
) {
    fun toQuery(): FilterCoursesQuery {
        val categoryEnum = try {
            Category.valueOf(category)
        } catch (e: IllegalArgumentException) {
            throw InvalidCategoryException()
        }

        return FilterCoursesQuery(
            departmentName = department,
            grade = grade,
            schoolId = schoolId,
            category = categoryEnum,
            field = field,
            subDepartmentName = subDepartment
        )
    }
}
