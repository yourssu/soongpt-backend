package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.course.application.RecommendContextResolver
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
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
    private val recommendContextResolver: RecommendContextResolver,
    private val generalCourseRecommendService: GeneralCourseRecommendService,
) {
    fun recommendTimetable(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        return timetableRecommendationFacade.recommend(command)
    }

    fun getAvailableGeneralElectives(timetableId: Long, userId: String): AvailableGeneralElectivesResponse {
        // 기존 타임테이블 로직: courses 조회
        val courses = getAvailableGeneralElectiveCourses(timetableId, userId)

        // 이수현황 조립: progress만 별도로 계산해서 응답에 추가
        val ctx = recommendContextResolver.resolveOptional()
        val progress = if (ctx != null) {
            val summary = ctx.graduationSummary?.generalElective
            val fieldCredits = generalCourseRecommendService.computeTakenFieldCredits(
                ctx.takenSubjectCodes,
                ctx.schoolId
            )
            GeneralElectiveProgress(
                required = summary?.required,
                completed = summary?.completed,
                satisfied = summary?.satisfied ?: false,
                fieldCredits = fieldCredits
            )
        } else null

        return AvailableGeneralElectivesResponse(progress = progress, courses = courses)
    }

    private fun getAvailableGeneralElectiveCourses(timetableId: Long, userId: String): List<GeneralElectiveDto> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)

        // 2. Piki의 컴포넌트로부터 트랙별 필수 교양 과목 목록을 가져옴 (현재는 Mock Provider로 대체)
        // val requiredGeMap = pikiCourseProvider.getRequiredGeneralElectives(userContext)
        // 이게 code일지, Course일지는 모른다 아직.
        val requiredGeMap = mapOf(
            "핵심역량-창의" to listOf(2150081501L),
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

    // NOTE: 피키가 만든거!!! 채플 이수현황을 조립할 때 사용합니다. 대충 예시코드..라고 생각해주세요 피키피키~야호~
    fun getAvailableChapels(timetableId: Long, userId: String): AvailableChapelsResponse {
        // 기존 타임테이블 로직: courses 조회
        val courses = getAvailableChapelCourses(timetableId, userId)

        // 이수현황 조립: progress만 별도로 계산해서 응답에 추가
        val ctx = recommendContextResolver.resolveOptional()
        val satisfied = ctx?.graduationSummary?.chapel?.satisfied
        val progress = satisfied?.let { ChapelProgress(satisfied = it) }

        return AvailableChapelsResponse(progress = progress, courses = courses)
    }

    // NOTE: 피키가 분리함!!! 기존에 있던 채플 과목 조회 로직을 분리함
    private fun getAvailableChapelCourses(timetableId: Long, userId: String): List<TimetableCourseResponse> {
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
        val timetableCourses = allCourses.map { course ->
            TimetableCourse(timetableId = newTimetable.id!!, courseId = course.id!!)
        }
        timetableCourseWriter.saveAll(timetableCourses)

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
