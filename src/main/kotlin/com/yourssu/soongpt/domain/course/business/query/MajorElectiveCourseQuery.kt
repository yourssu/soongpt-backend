package com.yourssu.soongpt.domain.course.business.query

class MajorElectiveCourseQuery(
    val departmentName: String,
    val subDepartmentName: String? = null,
    val grade: Int,
    val schoolId: Int,
) {
}
