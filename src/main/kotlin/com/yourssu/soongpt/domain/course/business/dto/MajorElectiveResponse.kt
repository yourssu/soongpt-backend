package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course

data class MajorElectiveResponse(
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Long,
    val name: String,
    val professor: String? = null,
    val department: String,
    val timePoints: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
) {
    companion object {
        fun from(
            course: Course,
        ): MajorElectiveResponse {
            return MajorElectiveResponse(
                category = course.category,
                subCategory = course.subCategory,
                field = course.field,
                code = course.code,
                name = course.name,
                professor = course.professor,
                department = course.department,
                timePoints = course.timePoints,
                personeel = course.personeel,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
            )
        }
    }
}
