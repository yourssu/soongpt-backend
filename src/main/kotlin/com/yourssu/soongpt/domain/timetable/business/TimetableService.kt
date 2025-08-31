package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.application.dto.TimetableCreatedRequest
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.LabTimetableResponse
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
    private val labTimetableMapper: LabTimetableMapper,
    private val labTimetableValidator: LabTimetableValidator,
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

    fun getRandomTimetable(): LabTimetableResponse {
        val timetable = timetableReader.getRandom()
            ?: throw com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException()
        // 원시 코스 불러오기
        val rawCourses = timetableCourseReader.findAllCourseByTimetableId(timetable.id!!)
        // 실험실 전용 필터: credit=0 또는 courseTime 비어있으면 제외
        val filtered = labTimetableValidator.filterValidCourses(rawCourses)
        if (filtered.isEmpty()) {
            // 다른 시간표 재시도 (간단하게 한 번 더 시도)
            val another = timetableReader.getRandom()
                ?: throw com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException()
            val alt = labTimetableValidator.filterValidCourses(
                timetableCourseReader.findAllCourseByTimetableId(another.id!!)
            )
            if (alt.isEmpty()) throw com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException()
            return labTimetableMapper.mapToFrontend(another, alt)
        }
        // 시간 겹침 검증 (데이터는 겹치지 않는다고 가정이나, 강제 체크)
        if (labTimetableValidator.hasOverlap(filtered)) {
            // 겹치면 다른 시간표로 재시도
            val another = timetableReader.getRandom()
                ?: throw com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException()
            val alt = labTimetableValidator.filterValidCourses(
                timetableCourseReader.findAllCourseByTimetableId(another.id!!)
            )
            if (alt.isEmpty() || labTimetableValidator.hasOverlap(alt)) {
                throw com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException()
            }
            return labTimetableMapper.mapToFrontend(another, alt)
        }
        return labTimetableMapper.mapToFrontend(timetable, filtered)
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
