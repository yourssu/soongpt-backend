package com.yourssu.soongpt.domain.timetable.implement

interface TimetableRepository {
    fun save(timetable: Timetable): Timetable
    fun get(id: Long): Timetable
}