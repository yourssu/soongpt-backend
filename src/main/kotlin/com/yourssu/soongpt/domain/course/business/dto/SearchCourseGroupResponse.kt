package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.baseCode

data class SearchCourseGroupResponse(
    val baseCourseCode: Long,
    val courseName: String,
    val credits: Double?,
    val professors: List<String>,
    val department: String,
    val target: String,
    val sections: List<SectionResponse>,
) {
    companion object {
        fun from(courses: List<Course>, isStrictByCode: Map<Long, Boolean>): SearchCourseGroupResponse {
            val first = courses.first()
            val professors = courses.mapNotNull { it.professor }.distinct()
            val sections = courses.map { section ->
                val isStrict = isStrictByCode[section.code] ?: false
                SectionResponse.from(section, isStrict, divisionFromCourseCode = true)
            }
            return SearchCourseGroupResponse(
                baseCourseCode = first.baseCode(),
                courseName = first.name,
                credits = first.credit,
                professors = professors,
                department = first.department,
                target = first.target,
                sections = sections,
            )
        }
    }
}
