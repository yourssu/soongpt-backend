package com.yourssu.soongpt.domain.course.implement.dto

import com.yourssu.soongpt.domain.course.implement.Course

data class GroupedCoursesByCategoryDto(
    val majorRequiredCourses: List<Course>,
    val majorElectiveCourses: List<Course>,
    val generalRequiredCourses: List<Course>,
    val generalElectiveCourses: List<Course>,
) {
    companion object {
        fun from(
            majorRequiredCourses: List<Course>,
            majorElectiveCourses: List<Course>,
            generalRequiredCourses: List<Course>,
            generalElectiveCourses: List<Course>,
        ): GroupedCoursesByCategoryDto {
            return GroupedCoursesByCategoryDto(
                majorRequiredCourses = majorRequiredCourses,
                majorElectiveCourses = majorElectiveCourses,
                generalRequiredCourses = generalRequiredCourses,
                generalElectiveCourses = generalElectiveCourses,
            )
        }
    }
}
