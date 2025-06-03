package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.business.command.SearchCoursesCommand

data class SearchCoursesRequest(
    val schoolId: Int,
    val department: String,
    val grade: Int,
    val q: String = "",
    val page: Int = 0,
    val size: Int = 20,
    val sort: String = "ASC"
) {
    fun toCommand(): SearchCoursesCommand {
        return SearchCoursesCommand(
            schoolId = schoolId,
            department = department,
            grade = grade,
            q = q,
            page = page,
            size = size,
            sort = sort
        )
    }
}
