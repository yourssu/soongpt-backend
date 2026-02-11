package com.yourssu.soongpt.domain.timetable.business.dto

data class PrimaryTimetableCommand(
    val retakeCourses: List<SelectedCourseCommand> = emptyList(),
    val majorRequiredCourses: List<SelectedCourseCommand> = emptyList(),
    val majorElectiveCourses: List<SelectedCourseCommand> = emptyList(),
    val majorBasicCourses: List<SelectedCourseCommand> = emptyList(),
    val doubleMajorCourses: List<SelectedCourseCommand> = emptyList(),
    val minorCourses: List<SelectedCourseCommand> = emptyList(),
    val teachingCourses: List<SelectedCourseCommand> = emptyList(),
    val generalRequiredCourses: List<SelectedCourseCommand> = emptyList(),
    val addedCourses: List<SelectedCourseCommand> = emptyList(),
)

data class SelectedCourseCommand(val courseCode: Long, val selectedCourseIds: List<Long>)
