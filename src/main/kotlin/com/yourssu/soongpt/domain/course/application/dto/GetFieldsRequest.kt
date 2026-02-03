package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId

data class GetFieldsRequest(
    @field:ValidSchoolId
    val schoolId: Int? = null
)
