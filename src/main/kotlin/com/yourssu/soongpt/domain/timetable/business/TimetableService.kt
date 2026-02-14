package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.course.application.RecommendContextResolver
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.utils.GeneralElectiveFieldDisplayMapper
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
        // 이수현황 조립: progress는 항상 반환. rusaint/졸업사정 없으면 -2/-2/false/빈 맵(recommend/all과 동일 센티널)
        val ctx = recommendContextResolver.resolveOptional()

        // 기존 타임테이블 로직: courses 조회 (하드코딩 과목 field override를 위해 admissionYear 전달)
        val courses = getAvailableGeneralElectiveCourses(timetableId, ctx?.admissionYear)
        val summary = ctx?.graduationSummary?.generalElective
        val progress = if (ctx != null && ctx.graduationSummary != null && summary != null) {
            val fieldCredits = if (ctx.admissionYear <= 2019) {
                // 19학번 이하: fieldCredits null (required/completed/satisfied만, TS에서 if (fieldCredits) 분기)
                null
            } else {
                val rawFieldCounts = generalCourseRecommendService.computeTakenFieldCourseCounts(
                    ctx.takenSubjectCodes,
                    ctx.schoolId
                )
                GeneralElectiveFieldDisplayMapper.buildFieldCreditsStructure(
                    ctx.admissionYear,
                    ctx.schoolId,
                    rawFieldCounts,
                )
            }
            GeneralElectiveProgress(
                required = summary.required,
                completed = summary.completed,
                satisfied = summary.satisfied,
                fieldCredits = fieldCredits
            )
        } else if (ctx != null && ctx.graduationSummary != null && summary == null) {
            // 졸업사정표는 있지만 교양선택 항목이 없음 → 해당 이수구분 없음
            GeneralElectiveProgress(required = 0, completed = 0, satisfied = true, fieldCredits = null)
        } else {
            // 졸업사정표 자체가 없음 (rusaint 미제공 등) → 제공 불가
            GeneralElectiveProgress(required = -2, completed = -2, satisfied = false, fieldCredits = null)
        }
        return AvailableGeneralElectivesResponse(progress = progress, courses = courses)
    }

    private fun getAvailableGeneralElectiveCourses(timetableId: Long, admissionYear: Int?): List<GeneralElectiveDto> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val requiredGeMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
        val result = mutableListOf<GeneralElectiveDto>()
        val scienceBaseCode = GeneralElectiveFieldDisplayMapper.SCIENCE_HARDCODED_COURSE_CODE.toLong() / 100

        // 트랙별로 순회하며 시간 충돌 검사 + trackName 표시용 매핑
        for ((rawTrackName, courseCodes) in requiredGeMap) {
            val displayTrackName = admissionYear?.let {
                GeneralElectiveFieldDisplayMapper.mapForCourseField(rawTrackName, it)
            } ?: rawTrackName

            if (courseCodes.isEmpty()) {
                result.add(GeneralElectiveDto(displayTrackName, emptyList()))
                continue
            }

            val courses = courseReader.findAllByCode(courseCodes)
            val availableCourses = courses.filter { course ->
                val courseCandidate = courseCandidateFactory.create(course)
                !timetableBitSet.intersects(courseCandidate.timeSlot)
            }
            val availableCourseResponses = availableCourses.map { course ->
                val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
                val response = TimetableCourseResponse.from(course, courseTimes)
                // 하드코딩 과목(2150180801): courses[].field도 올바른 분야명으로 override
                if (admissionYear != null && course.baseCode() == scienceBaseCode) {
                    response.copy(field = GeneralElectiveFieldDisplayMapper.scienceFieldForCourseDisplay(admissionYear))
                } else {
                    response
                }
            }
            result.add(GeneralElectiveDto(displayTrackName, availableCourseResponses))
        }
        return result
    }

    // NOTE: 채플 이수현황 조립. available-chapels API에서 사용.
    fun getAvailableChapels(timetableId: Long): AvailableChapelsResponse {
        ensureTimetableExists(timetableId)
        // 기존 타임테이블 로직: courses 조회
        // 이수현황 조립: progress는 항상 반환. rusaint/졸업사정 없으면 satisfied=false
        val ctx = recommendContextResolver.resolveOptional()
        val satisfied = ctx?.graduationSummary?.chapel?.satisfied ?: false
        val progress = ChapelProgress(satisfied = satisfied)
        val courses = if (satisfied) {
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
