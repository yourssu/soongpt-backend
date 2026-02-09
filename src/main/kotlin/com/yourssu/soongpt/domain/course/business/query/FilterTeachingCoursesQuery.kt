package com.yourssu.soongpt.domain.course.business.query

import com.yourssu.soongpt.domain.course.implement.TeachingArea

data class FilterTeachingCoursesQuery(
    val schoolId: Int,
    val departmentName: String,
    val teachingArea: TeachingArea?,
)
