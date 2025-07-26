package com.yourssu.soongpt.domain.course.implement.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course

data class GroupedCoursesByCategoryDto(
    val majorRequiredCourses: List<Course>,
    val majorElectiveCourses: List<Course>,
    val generalRequiredCourses: List<Course>,
    val generalElectiveCourses: List<Course>,
) {
    companion object {
        fun from(
            groupedCourses: Map<Category, List<Course>>,
        ): GroupedCoursesByCategoryDto {
            return GroupedCoursesByCategoryDto(
                majorRequiredCourses = groupedCourses[Category.MAJOR_REQUIRED] ?: emptyList(),
                majorElectiveCourses = groupedCourses[Category.MAJOR_ELECTIVE] ?: emptyList(),
                generalRequiredCourses = groupedCourses[Category.GENERAL_REQUIRED] ?: emptyList(),
                generalElectiveCourses = groupedCourses[Category.GENERAL_ELECTIVE] ?: emptyList(),
            )
        }
    }
}
