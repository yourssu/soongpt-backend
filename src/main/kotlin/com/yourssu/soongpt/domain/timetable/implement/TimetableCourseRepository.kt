package com.yourssu.soongpt.domain.timetable.implement

interface TimetableCourseRepository {
    fun save(timetableCourse: TimetableCourse): TimetableCourse
    fun findAllCourseByTimetableId(id: Long): List<TimetableCourse>
}