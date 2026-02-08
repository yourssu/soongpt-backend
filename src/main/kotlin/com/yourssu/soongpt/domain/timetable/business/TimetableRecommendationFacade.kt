package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.timetable.implement.*
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import com.yourssu.soongpt.domain.timetable.implement.suggester.CourseSwapSuggester
import com.yourssu.soongpt.domain.timetable.implement.suggester.DivisionChangeSuggester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.BitSet

private val EXCLUDED_DEPARTMENTS_FOR_CHAPEL = setOf("국제법무학과", "금융학부", "자유전공학부")

@Component
class TimetableRecommendationFacade(
    private val courseCandidateProvider: CourseCandidateProvider,
    private val timetableCombinationGenerator: TimetableCombinationGenerator,
    private val timetableRanker: TimetableRanker,
    private val divisionChangeSuggester: DivisionChangeSuggester,
    private val courseSwapSuggester: CourseSwapSuggester,
    private val untakenCourseFetcher: UntakenCourseFetcher,
    private val timetablePersister: TimetablePersister,
    private val userContextProvider: UserContextProvider,
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory
) {
    @Transactional
    fun recommend(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        val userContext = userContextProvider.getContext(command.userId)

        // 1. 1학년 필수 채플 처리
        val mandatoryChapelCandidate = findMandatoryChapelForFreshman(userContext)
        val commandWithoutChapel = if (mandatoryChapelCandidate != null) {
            command.copyWithoutCourse(mandatoryChapelCandidate.codes.first())
        } else {
            command
        }

        val courseCandidateGroups = courseCandidateProvider.createCourseCandidateGroups(commandWithoutChapel)

        // 2. 성공 케이스 시도
        val combinations = timetableCombinationGenerator.generate(courseCandidateGroups, mandatoryChapelCandidate)
        if (combinations.isNotEmpty()) {
            val successResponse = processSuccessCase(combinations, userContext, command)
            return FinalTimetableRecommendationResponse.success(successResponse)
        }

        // 3. 단일 충돌 케이스 시도
        val singleConflictCourseCodes = findSingleConflictCourses(commandWithoutChapel, mandatoryChapelCandidate)
        if (singleConflictCourseCodes.isNotEmpty()) {
            return FinalTimetableRecommendationResponse.singleConflict(singleConflictCourseCodes)
        }

        // 4. 실패 케이스
        return FinalTimetableRecommendationResponse.failure()
    }

    private fun findMandatoryChapelForFreshman(userContext: UserContext): TimetableCandidate? {
        if (userContext.grade == 1 && userContext.department.name !in EXCLUDED_DEPARTMENTS_FOR_CHAPEL) {
            val chapelCourse = courseReader.findAllBy(
                category = Category.CHAPEL,
                department = userContext.department,
                grade = userContext.grade
            ).firstOrNull()

            if (chapelCourse != null) {
                val chapelCandidate = courseCandidateFactory.create(chapelCourse)
                return TimetableCandidate(
                    codes = listOf(chapelCandidate.code),
                    timeSlot = chapelCandidate.timeSlot,
                    validTags = emptyList()
                )
            }
        }
        return null
    }

    private fun processSuccessCase(
        combinations: List<TimetableCandidate>,
        userContext: UserContext,
        command: PrimaryTimetableCommand
    ): FullTimetableRecommendationResponse {
        val rankedCombinations = timetableRanker.rank(combinations)
        val primaryTimetableCandidate = rankedCombinations.first()
        val remainingCandidates = rankedCombinations.drop(1)

        val divisionChangeSuggestions: List<SuggestionCandidate> =
            divisionChangeSuggester.suggest(primaryTimetableCandidate, remainingCandidates)

        val untakenMajorCourses =
            untakenCourseFetcher.fetchUntakenMajorCourses(userContext, primaryTimetableCandidate)
        val courseSwapSuggestions: List<SuggestionCandidate> =
            courseSwapSuggester.suggest(primaryTimetableCandidate, command, untakenMajorCourses)

        val allSuggestions = divisionChangeSuggestions + courseSwapSuggestions
        val rankedSuggestions = timetableRanker.rankSuggestions(allSuggestions).take(6)

        val primaryResponse = timetablePersister.persist(primaryTimetableCandidate)
        val recommendationDtos =
            rankedSuggestions.map { suggestion ->
                RecommendationDto(
                    description = suggestion.description,
                    timetable = timetablePersister.persist(suggestion.resultingTimetableCandidate)
                )
            }

        return FullTimetableRecommendationResponse(
            primaryTimetable = primaryResponse,
            alternativeSuggestions = recommendationDtos
        )
    }

    private fun findSingleConflictCourses(command: PrimaryTimetableCommand, baseTimetable: TimetableCandidate?): List<DeletableCourseDto> {
        val allSelectedCourses = command.getAllCourseCodes()
        val conflictingCourses = mutableListOf<DeletableCourseDto>()

        for (courseToExclude in allSelectedCourses) {
            val modifiedCommand = command.copyWithoutCourse(courseToExclude.courseCode)
            val courseCandidateGroups = courseCandidateProvider.createCourseCandidateGroups(modifiedCommand)
            val combinations = timetableCombinationGenerator.generate(courseCandidateGroups, baseTimetable)

            if (combinations.isNotEmpty()) {
                // 충돌 원인 과목의 전체 정보를 가져와 Category를 포함한 DTO를 생성
                val course = courseReader.findByCode(courseToExclude.courseCode)
                conflictingCourses.add(DeletableCourseDto(course.code, course.category))
            }
        }
        return conflictingCourses.distinct()
    }
}

private fun PrimaryTimetableCommand.getAllCourseCodes(): List<SelectedCourseCommand> {
    return this.retakeCourses +
            this.majorRequiredCourses +
            this.majorElectiveCourses +
            this.otherMajorCourses +
            this.generalRequiredCourses +
            this.addedCourses
}

private fun PrimaryTimetableCommand.copyWithoutCourse(courseCode: Long): PrimaryTimetableCommand {
    return this.copy(
        retakeCourses = this.retakeCourses.filterNot { it.courseCode == courseCode },
        majorRequiredCourses = this.majorRequiredCourses.filterNot { it.courseCode == courseCode },
        majorElectiveCourses = this.majorElectiveCourses.filterNot { it.courseCode == courseCode },
        otherMajorCourses = this.otherMajorCourses.filterNot { it.courseCode == courseCode },
        generalRequiredCourses = this.generalRequiredCourses.filterNot { it.courseCode == courseCode },
        addedCourses = this.addedCourses.filterNot { it.courseCode == courseCode }
    )
}


