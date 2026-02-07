package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.Timetable

data class LabTimetableResult(
    val timetable: Timetable,
    val courses: List<Course>,
)
