package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.implement.TimeTableFactory
import com.yourssu.soongpt.domain.timetable.implement.TimetableCandidateFactory
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseReader
import com.yourssu.soongpt.domain.timetable.implement.TimetableReader
import org.springframework.stereotype.Service

@Service
class TimetableService(
    private val timetableReader: TimetableReader,
    private val timetableCourseReader: TimetableCourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val timetableCandidateFactory: TimetableCandidateFactory,
    private val timeTableFactory: TimeTableFactory,
) {
    fun createTimetable(command: TimetableCreatedCommand): TimetableResponses {
        val timetables = timeTableFactory.createTimetable(command)
        val responses = timetableCandidateFactory.issueTimetables(timetables)
        return TimetableResponses(responses)
    }

    fun getTimeTable(id: Long): TimetableResponse {
        val timetable = timetableReader.get(id)
        val courses = timetableCourseReader.findAllCourseByTimetableId(id)
        val response = courses.map { TimetableCourseResponse.from(it, courseTimeReader.findAllByCourseId(it.id!!)) }
        return TimetableResponse.from(timetable, response)
    }
}