package com.yourssu.soongpt.domain.course.business.command

class MajorRequiredCourseFoundCommand(
    val departmentName: String,
    val subDepartmentName: String? = null,
    val grade: Int,
    val schoolId: Long? = null,
) {
}
