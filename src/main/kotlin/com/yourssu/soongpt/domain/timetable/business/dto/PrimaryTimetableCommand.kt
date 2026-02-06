package com.yourssu.soongpt.domain.timetable.business.dto

data class PrimaryTimetableCommand(
        val departmentName: String, // Todo: userContext
        val grade: Int, // TODO: userContext
        val division: String, // TODO: userContext - 가반/나반 || 01 02?
        val entryYear: Int, // TODO: userContext

        val maxCredit: Int,
        val userId: String = "anonymous", // TODO: userContext 가져오기 위한 key (sToken + ?)
        val retakeCourses: List<SelectedCourseCommand> = emptyList(),
        val majorRequiredCourses: List<SelectedCourseCommand> = emptyList(),
        val majorElectiveCourses: List<SelectedCourseCommand> = emptyList(),
        val otherMajorCourses: List<SelectedCourseCommand> = emptyList(),
        val generalRequiredCourses: List<SelectedCourseCommand> = emptyList(),
        val addedCourses: List<SelectedCourseCommand> = emptyList(),
)

data class SelectedCourseCommand(val courseCode: Long, val selectedCourseIds: List<Long>)
