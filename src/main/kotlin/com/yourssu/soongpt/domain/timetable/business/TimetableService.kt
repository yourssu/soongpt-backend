package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.course.application.RecommendContextResolver
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
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
    private val finalizeTimetableValidator: FinalizeTimetableValidator,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val timetableBitsetConverter: TimetableBitsetConverter,
    private val recommendContextResolver: RecommendContextResolver,
    private val generalCourseRecommendService: GeneralCourseRecommendService,
    private val untakenCourseCodeService: UntakenCourseCodeService,
) {
    fun recommendTimetable(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        return timetableRecommendationFacade.recommend(command)
    }

    fun getAvailableGeneralElectives(timetableId: Long): AvailableGeneralElectivesResponse {
        ensureTimetableExists(timetableId)
        // 기존 타임테이블 로직: courses 조회
        val courses = getAvailableGeneralElectiveCourses(timetableId)

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

    private fun getAvailableGeneralElectiveCourses(timetableId: Long): List<GeneralElectiveDto> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val requiredGeMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
        val result = mutableListOf<GeneralElectiveDto>()

        // 트랙별로 순회하며 시간 충돌 검사
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
    fun getAvailableChapels(timetableId: Long): AvailableChapelsResponse {
        ensureTimetableExists(timetableId)
        // 기존 타임테이블 로직: courses 조회
        // 이수현황 조립: progress만 별도로 계산해서 응답에 추가
        val ctx = recommendContextResolver.resolveOptional()
        val satisfied = ctx?.graduationSummary?.chapel?.satisfied
        val progress = satisfied?.let { ChapelProgress(satisfied = it) }
        val courses = if (satisfied == true) {
            emptyList()
        } else {
            getAvailableChapelCourses(timetableId)
        }

        return AvailableChapelsResponse(progress = progress, courses = courses)
    }

    // NOTE: 피키가 분리함!!! 기존에 있던 채플 과목 조회 로직을 분리함
    private fun getAvailableChapelCourses(timetableId: Long): List<TimetableCourseResponse> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val chapelCodes = untakenCourseCodeService.getUntakenCourseCodes(Category.CHAPEL)
        if (chapelCodes.isEmpty()) return emptyList()
        val chapels = courseReader.findAllByCode(chapelCodes)

        // 필터링: 시간 충돌 과목 제외
        val availableChapels = chapels.filter { course ->
            val courseCandidate = courseCandidateFactory.create(course)
            !timetableBitSet.intersects(courseCandidate.timeSlot)
        }

        // 4. 최종 응답 DTO로 변환하여 반환
        return availableChapels.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            TimetableCourseResponse.from(course, courseTimes)
        }
    }

    private fun ensureTimetableExists(timetableId: Long) {
        timetableReader.get(timetableId)
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
        val ctx = recommendContextResolver.resolveOptional()
        val schoolId = ctx?.schoolId ?: 0
        val departmentName = ctx?.departmentName ?: "UNKNOWN"
        return TimetableCreatedAlarmRequest(
            schoolId = schoolId,
            departmentName = departmentName,
            times = timetableId
        )
    }
}
