package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.course.implement.Category

data class DeletableCourseDto(
    val courseCode: Long,
    val category: Category
)
