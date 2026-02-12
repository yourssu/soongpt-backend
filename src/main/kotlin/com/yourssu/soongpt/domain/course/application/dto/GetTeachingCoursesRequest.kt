package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.handler.BadRequestException
import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class GetTeachingCoursesRequest(
        @field:ValidSchoolId
        @field:Schema(description = "학번 앞 2자리", example = "26")
        val schoolId: Int,
        @field:NotBlank
        @field:Schema(description = "학과명", example = "컴퓨터학부")
        val department: String,
        @field:Schema(
                description = "교직 대분류 영역 (선택)",
                allowableValues = ["전공영역", "MAJOR", "교직영역", "TEACHING", "특성화영역", "SPECIAL"],
                example = "교직영역"
        )
        val majorArea: String? = null,
) {
    fun toQuery(): FilterTeachingCoursesQuery {
        val majorAreaEnum = majorArea?.let {
            try {
                TeachingMajorArea.from(it)
            } catch (e: IllegalArgumentException) {
                throw BadRequestException(message = "잘못된 majorArea 값입니다: $it")
            }
        }

        return FilterTeachingCoursesQuery(
                schoolId = schoolId,
                departmentName = department,
                majorArea = majorAreaEnum,
        )
    }
}
