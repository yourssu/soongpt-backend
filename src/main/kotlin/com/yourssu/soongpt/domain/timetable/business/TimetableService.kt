package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.application.dto.TimetableCreatedRequest
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseReader
import com.yourssu.soongpt.domain.timetable.implement.TimetableGenerator
import com.yourssu.soongpt.domain.timetable.implement.TimetableReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimetableService(
    private val timetableReader: TimetableReader,
    private val timetableGenerator: TimetableGenerator,
    private val timetableCourseReader: TimetableCourseReader,
) {
    @Transactional
    fun createTimetable(command: TimetableCreatedCommand): TimetableResponses {
        val timetableCandidates = timetableGenerator.generate(command)
        val responses = timetableGenerator.issueTimetables(timetableCandidates)
        return TimetableResponses(responses)
    }

    fun getTimeTable(id: Long): TimetableResponse {
        val timetable = timetableReader.get(id)
        val courses = timetableCourseReader.findAllCourseByTimetableId(id)
        val coursesWithTime = courses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList();
            TimetableCourseResponse.from(course, courseTimes)
        }
        return TimetableResponse.from(timetable, coursesWithTime)
    }

    fun createTimetableAlarmRequest(
        request: TimetableCreatedRequest,
        responses: TimetableResponses
    ): TimetableCreatedAlarmRequest {
        val response = TimetableCreatedAlarmRequest(
            schoolId = request.schoolId,
            departmentName = request.department,
            times = responses.timetables.last().timetableId
        )
        return response
    }
}
