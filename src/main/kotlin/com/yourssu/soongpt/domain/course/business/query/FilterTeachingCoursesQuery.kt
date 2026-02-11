package com.yourssu.soongpt.domain.course.business.query

import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea

data class FilterTeachingCoursesQuery(
        val schoolId: Int,
        val departmentName: String,
        val majorArea: TeachingMajorArea? = null,
)
