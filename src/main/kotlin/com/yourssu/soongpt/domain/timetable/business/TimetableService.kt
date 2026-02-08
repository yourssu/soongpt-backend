package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.timetable.implement.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimetableService(
    private val timetableReader: TimetableReader,
    private val timetableGenerator: TimetableGenerator,
    private val timetableCourseReader: TimetableCourseReader,
    private val timetableRecommendationFacade: TimetableRecommendationFacade,
    private val courseReader: CourseReader,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
    private val userContextProvider: UserContextProvider,
    private val finalizeTimetableValidator: FinalizeTimetableValidator
) {
    @Transactional
    fun createTimetable(command: TimetableCreatedCommand): TimetableResponses {
        val timetableCandidates = timetableGenerator.generate(command)
        val responses = timetableGenerator.issueTimetables(timetableCandidates)
        return TimetableResponses(responses)
    }

    fun recommendTimetable(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        return timetableRecommendationFacade.recommend(command)
    }

    @Transactional
    fun finalizeTimetable(command: FinalizeTimetableCommand): TimetableResponse {
        // 1. 기반 시간표의 과목들과 정보 로드
        val baseTimetable = timetableReader.get(command.timetableId)
        val baseCourses = timetableCourseReader.findAllCourseByTimetableId(command.timetableId)

        // 2. 추가할 과목들 리스트 생성
        val courseCodesToAdd = command.generalElectiveCourseCodes + listOfNotNull(command.chapelCourseCode)
        val coursesToAdd = courseReader.findAllByCode(courseCodesToAdd)

        // 3. 시간 충돌 유효성 검사 (기반 과목 + 추가할 과목)
        finalizeTimetableValidator.validate(command.timetableId, coursesToAdd)

        // 4. 새로운 시간표 엔티티 생성 및 저장
        val newTimetable = timetableWriter.save(
            Timetable(
                tag = baseTimetable.tag,
                score = baseTimetable.score
            )
        )

        // 5. 새로운 시간표에 모든 과목(기존+추가) 연결하여 저장
        val allCourses = baseCourses + coursesToAdd
        allCourses.forEach { course ->
            timetableCourseWriter.save(TimetableCourse(timetableId = newTimetable.id!!, courseId = course.id!!))
        }

        // 6. 새로 생성된 최종 시간표 정보 반환
        return getTimeTable(newTimetable.id!!)
    }

    fun getTimeTable(id: Long): TimetableResponse {
        val timetable = timetableReader.get(id)
        val courses = timetableCourseReader.findAllCourseByTimetableId(id)
        val coursesWithTime =
            courses.map { course ->
                val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
                TimetableCourseResponse.from(course, courseTimes)
            }
        return TimetableResponse.from(timetable, coursesWithTime)
    }

    fun createTimetableAlarmRequest(timetableId: Long): TimetableCreatedAlarmRequest {
        val userContext = userContextProvider.getContext("test")
        return TimetableCreatedAlarmRequest(
            schoolId = userContext.schoolId,
            departmentName = userContext.department.name,
            times = timetableId
        )
    }
}
