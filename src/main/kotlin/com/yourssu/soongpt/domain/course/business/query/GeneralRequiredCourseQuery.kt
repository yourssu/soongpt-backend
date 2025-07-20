package com.yourssu.soongpt.domain.course.business.query

class GeneralRequiredCourseQuery(
    val departmentName: String,
    val subDepartmentName: String? = null,
    val grade: Int,
    val schoolId: Long? = null,
    val field: String? = null,
) {
}
