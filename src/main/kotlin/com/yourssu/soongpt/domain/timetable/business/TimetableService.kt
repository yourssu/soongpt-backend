package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.timetable.implement.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimetableService(
    private val timetableReader: TimetableReader,
    private val timetableCourseReader: TimetableCourseReader,
    private val timetableRecommendationFacade: TimetableRecommendationFacade,
    private val courseReader: CourseReader,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
    private val userContextProvider: UserContextProvider,
    private val finalizeTimetableValidator: FinalizeTimetableValidator,
    private val takenCourseChecker: TakenCourseChecker,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val timetableBitsetConverter: TimetableBitsetConverter,
) {
    fun recommendTimetable(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        return timetableRecommendationFacade.recommend(command)
    }

    fun getAvailableGeneralElectives(timetableId: Long, userId: String): List<GeneralElectiveDto> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)

        // 2. Piki의 컴포넌트로부터 트랙별 필수 교양 과목 목록을 가져옴 (현재는 Mock Provider로 대체)
        // val requiredGeMap = pikiCourseProvider.getRequiredGeneralElectives(userContext)
        // 이게 code일지, Course일지는 모른다 아직.
        val requiredGeMap = mapOf(
            "핵심역량-창의" to listOf(2150145301L, 2150081501L),
            "균형교양-인문" to listOf(2150152601L),
            "만족한트랙" to emptyList()
        )

        val result = mutableListOf<GeneralElectiveDto>()

        // 3. 트랙별로 순회하며 시간 충돌 검사
        for ((trackName, courseCodes) in requiredGeMap) {
            if (courseCodes.isEmpty()) {
                result.add(GeneralElectiveDto(trackName, emptyList()))
                continue
            }

            val courses = courseReader.findAllByCode(courseCodes)
            val availableCourses = courses.filter { course ->
                val courseCandidate = courseCandidateFactory.create(course)
                !timetableBitSet.intersects(courseCandidate.timeSlot)
            }
            val availableCourseResponses = availableCourses.map { course ->
                val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
                TimetableCourseResponse.from(course, courseTimes)
            }
            result.add(GeneralElectiveDto(trackName, availableCourseResponses))
        }
        return result
    }

    fun getAvailableChapels(timetableId: Long, userId: String): List<TimetableCourseResponse> {
        val userContext = userContextProvider.getContext(userId)
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val chapels = courseReader.findAllBy(Category.CHAPEL, userContext.department, userContext.grade)

        // 3. 필터링: 기수강 과목 제외, 시간 충돌 과목 제외
        // 기수강 여부는 .. 나중에 바꿀예정
        val availableChapels = chapels.filter { course ->
            val courseCandidate = courseCandidateFactory.create(course)
            !takenCourseChecker.isCourseTaken(userId, course.code) &&
                    !timetableBitSet.intersects(courseCandidate.timeSlot)
        }

        // 4. 최종 응답 DTO로 변환하여 반환
        return availableChapels.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            TimetableCourseResponse.from(course, courseTimes)
        }
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
