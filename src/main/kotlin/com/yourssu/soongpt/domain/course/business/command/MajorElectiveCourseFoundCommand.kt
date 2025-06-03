package com.yourssu.soongpt.domain.course.business.command

class MajorElectiveCourseFoundCommand(
    val departmentName: String,
    val subDepartmentName: String? = null,
    val grade: Int,
    val schoolId: Long? = null,
) {
}
