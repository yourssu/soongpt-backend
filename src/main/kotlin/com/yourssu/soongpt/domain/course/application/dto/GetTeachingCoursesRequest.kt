package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.implement.TeachingArea
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea
import jakarta.validation.constraints.NotBlank

data class GetTeachingCoursesRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    /**
     * 교직 이수 관점 대분류 (선택)
     * - 전공영역 | 교직영역 | 특성화
     */
    val majorArea: String? = null,

    /**
     * (하위 필터, 선택) 기존 teachingArea 파라미터 유지 (하위 호환)
     */
    val teachingArea: String? = null,
) {
    fun toQuery(): FilterTeachingCoursesQuery {
        val majorAreaEnum = majorArea?.let { TeachingMajorArea.from(it) }
        val teachingAreaEnum = teachingArea?.let { TeachingArea.from(it) }

        return FilterTeachingCoursesQuery(
            schoolId = schoolId,
            departmentName = department,
            majorArea = majorAreaEnum,
            teachingArea = teachingAreaEnum,
        )
    }
}
