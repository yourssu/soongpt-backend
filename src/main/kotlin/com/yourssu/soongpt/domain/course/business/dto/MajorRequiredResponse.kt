package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course

data class MajorRequiredResponse(
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Long,
    val name: String,
    val professor: String? = null,
    val department: String,
    val division: String? = null,
    val time: String,
    val point: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
) {
    companion object {
        fun from(
            course: Course,
        ): MajorRequiredResponse {
            return MajorRequiredResponse(
                category = course.category,
                subCategory = course.subCategory,
                field = course.field,
                code = course.code,
                name = course.name,
                professor = course.professor,
                department = course.department,
                division = course.division,
                time = course.time,
                point = course.point,
                personeel = course.personeel,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
            )
        }
    }
}
