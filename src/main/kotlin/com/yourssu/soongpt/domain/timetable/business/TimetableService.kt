package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.course.application.RecommendContext
import com.yourssu.soongpt.domain.course.application.RecommendContextResolver
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.utils.GeneralElectiveFieldDisplayMapper
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.timetable.implement.*
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

    fun recommendTimetable(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        return timetableRecommendationFacade.recommend(command)
    }

    fun getAvailableGeneralElectives(timetableId: Long): AvailableGeneralElectivesResponse {
        ensureTimetableExists(timetableId)
        val ctx = recommendContextResolver.resolveOptional()
        val summary = ctx?.graduationSummary?.generalElective
        if (ctx != null && ctx.graduationSummary != null && summary == null) {
            logger.warn { "generalElective null, progress 0/0/true 적용" }
        }
        val progress = buildGeneralElectiveProgress(ctx, summary)
        val courses = getAvailableGeneralElectiveCourses(timetableId, ctx?.admissionYear)
        return AvailableGeneralElectivesResponse(progress = progress, courses = courses)
    }

    private fun buildGeneralElectiveProgress(
        ctx: RecommendContext?,
        summary: RusaintCreditSummaryItemDto?,
    ): GeneralElectiveProgress {
        if (ctx == null || ctx.graduationSummary == null) {
            return GeneralElectiveProgress(required = -2, completed = -2, satisfied = false, fieldCredits = null)
        }
        if (summary == null) {
            return GeneralElectiveProgress(required = 0, completed = 0, satisfied = true, fieldCredits = null)
        }
        val fieldCredits = if (ctx.admissionYear <= 2019) null
        else {
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
        return GeneralElectiveProgress(
            required = summary.required,
            completed = summary.completed,
            satisfied = summary.satisfied,
            fieldCredits = fieldCredits
        )
    }

    private fun getAvailableGeneralElectiveCourses(timetableId: Long, admissionYear: Int?): List<GeneralElectiveDto> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val requiredGeMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
        val scienceBaseCode = GeneralElectiveFieldDisplayMapper.SCIENCE_HARDCODED_COURSE_CODE.toLong() / 100

        // 표시용 trackName 기준으로 묶어서 중복 제거 (raw가 "자연과학공학기술"/"자연과학·공학·기술" 등 여러 형태여도 한 track으로)
        val byDisplayName = requiredGeMap.entries
            .map { (raw, codes) ->
                val display = admissionYear?.let {
                    GeneralElectiveFieldDisplayMapper.mapForCourseField(raw, it)
                } ?: raw
                display to codes
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, listOfCodes) -> listOfCodes.flatten().distinct() }

        // ~22학번: 해당 학번에 속하지 않는 분야(23 전용 "인간언어", "문화예술" 등) 제외
        val allowedNames = admissionYear?.let { GeneralElectiveFieldDisplayMapper.allowedTrackNamesForDisplay(it) }
        val filteredEntries = if (!allowedNames.isNullOrEmpty()) {
            byDisplayName.filter { (displayName, _) -> displayName in allowedNames }
        } else {
            byDisplayName
        }

        // ~22학번(19학번 이하 포함): 9개 track을 고정 순서로 전부 노출. 미수강 과목 없는 분야는 courses=[]
        val orderedNames = admissionYear?.let { GeneralElectiveFieldDisplayMapper.allowedTrackNamesOrdered(it) }
        val result = mutableListOf<GeneralElectiveDto>()
        val entriesMap = filteredEntries

        val trackNamesToIterate = orderedNames?.takeIf { it.isNotEmpty() } ?: entriesMap.keys.toList()

        for (displayTrackName in trackNamesToIterate) {
            val courseCodes = entriesMap[displayTrackName] ?: emptyList()
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

    fun getAvailableChapels(timetableId: Long): AvailableChapelsResponse {
        ensureTimetableExists(timetableId)
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

    private fun getAvailableChapelCourses(timetableId: Long): List<TimetableCourseResponse> {
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)
        val chapelCodes = untakenCourseCodeService.getUntakenCourseCodes(Category.CHAPEL)
        if (chapelCodes.isEmpty()) return emptyList()
        val chapels = courseReader.findAllByCode(chapelCodes)
        val availableChapels = chapels.filter { course ->
            val courseCandidate = courseCandidateFactory.create(course)
            !timetableBitSet.intersects(courseCandidate.timeSlot)
        }
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
        val baseTimetable = timetableReader.get(command.timetableId)
        val baseCourses = timetableCourseReader.findAllCourseByTimetableId(command.timetableId)
        val courseCodesToAdd = command.generalElectiveCourseCodes + listOfNotNull(command.chapelCourseCode)
        val coursesToAdd = courseReader.findAllByCode(courseCodesToAdd)
        finalizeTimetableValidator.validate(command.timetableId, coursesToAdd)
        val newTimetable = timetableWriter.save(
            Timetable(tag = baseTimetable.tag, score = baseTimetable.score)
        )
        val allCourses = baseCourses + coursesToAdd
        val timetableCourses = allCourses.map { course ->
            TimetableCourse(timetableId = newTimetable.id!!, courseId = course.id!!)
        }
        timetableCourseWriter.saveAll(timetableCourses)
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
