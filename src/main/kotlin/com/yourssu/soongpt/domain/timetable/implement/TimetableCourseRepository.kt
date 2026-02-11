package com.yourssu.soongpt.domain.timetable.implement

interface TimetableCourseRepository {
    fun findAllCourseByTimetableId(timetableId: Long): List<TimetableCourse>
    fun save(timetableCourse: TimetableCourse): TimetableCourse
    fun saveAll(timetableCourses: List<TimetableCourse>): List<TimetableCourse>
}
