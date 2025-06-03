package com.yourssu.soongpt.domain.course.business.command

class GeneralRequiredCourseFoundCommand(
    val departmentName: String,
    val subDepartmentName: String? = null,
    val grade: Int,
    val schoolId: Long? = null,
) {
}
