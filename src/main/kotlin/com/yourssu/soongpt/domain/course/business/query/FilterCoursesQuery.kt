package com.yourssu.soongpt.domain.course.business.query

import com.yourssu.soongpt.domain.course.implement.Category

class FilterCoursesQuery(
    val departmentName: String,
    val grade: Int,
    val schoolId: Int,
    val category: Category,
    val field: String? = null,
    val subDepartmentName: String? = null,
)
