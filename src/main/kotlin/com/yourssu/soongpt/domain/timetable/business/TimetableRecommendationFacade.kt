package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.DeletableCourseDto
import com.yourssu.soongpt.domain.timetable.business.dto.FinalTimetableRecommendationResponse
import com.yourssu.soongpt.domain.timetable.business.dto.GroupedTimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.RecommendationDto
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateFactory
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateProvider
import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.TimetableCombinationGenerator
import com.yourssu.soongpt.domain.timetable.implement.TimetablePersister
import com.yourssu.soongpt.domain.timetable.implement.TimetableRanker
import com.yourssu.soongpt.domain.timetable.implement.UserContextProvider
import com.yourssu.soongpt.domain.timetable.implement.SwapCourseProvider
import com.yourssu.soongpt.domain.timetable.implement.SwapTrack
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val EXCLUDED_DEPARTMENTS_FOR_CHAPEL = setOf("국제법무학과", "금융학부", "자유전공학부")
private const val MAX_RECOMMENDATIONS_PER_TAG = 4
private const val MAX_SWAP_ALTERNATIVES = 2
private const val SWAP_SAMPLE_POOL_SIZE = 20
private const val MAX_SWAPPED_COMBINATIONS = 50
private const val MAX_BASELINE_FOR_SWAPS = 2
private const val MAX_SWAP_ATTEMPTS = 10

@Component
class TimetableRecommendationFacade(
    private val courseCandidateProvider: CourseCandidateProvider,
    private val timetableCombinationGenerator: TimetableCombinationGenerator,
    private val timetableRanker: TimetableRanker,
    private val timetablePersister: TimetablePersister,
    private val userContextProvider: UserContextProvider,
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val swapCourseProvider: SwapCourseProvider,
) {
    @Transactional
    fun recommend(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        val userContext = userContextProvider.getContext(command.userId)

//        System.err.println("TimetableRecommendationFacade.recommend called with command: $command and userContext: $userContext")
        val mandatoryChapelCandidate = findMandatoryChapelForFreshman(userContext)
        val commandWithoutChapel = if (mandatoryChapelCandidate != null) {
            command.copyWithoutCourse(mandatoryChapelCandidate.codes.first())
        } else {
            command
        }

//        System.err.println("Mandatory chapel candidate: $mandatoryChapelCandidate")
        val courseCandidateGroups = courseCandidateProvider.createCourseCandidateGroups(commandWithoutChapel)
        val combinations = timetableCombinationGenerator.generate(courseCandidateGroups, mandatoryChapelCandidate)

//        System.err.println("Generated ${combinations.size} timetable combinations")
        if (combinations.isNotEmpty()) {
//            System.err.println(
//                "Success path inputs: userId=${command.userId}, totalSelectedCourses=${command.getAllCourseCodes().size}, " +
//                    "selectedCourseCodes=${command.getAllCourseCodes().map { it.courseCode }}"
//            )
            val successResponse = processSuccessCase(combinations, userContext, command, mandatoryChapelCandidate)

//            System.err.println("Returning success response with ${successResponse.size} grouped recommendations")
            return FinalTimetableRecommendationResponse.success(successResponse)
        }

//        System.err.println("No valid timetable combinations found, checking for single conflict courses")
        val singleConflictCourseCodes = findSingleConflictCourses(commandWithoutChapel, mandatoryChapelCandidate, userContext)
        if (singleConflictCourseCodes.isNotEmpty()) {
            return FinalTimetableRecommendationResponse.singleConflict(singleConflictCourseCodes)
        }

//        System.err.println("No single conflict courses found, returning failure response")
        return FinalTimetableRecommendationResponse.failure()
    }

    private fun findMandatoryChapelForFreshman(userContext: UserContext): TimetableCandidate? {
        if (userContext.grade == 1 && userContext.department.name !in EXCLUDED_DEPARTMENTS_FOR_CHAPEL) {
            val chapelCourse = courseReader.findAllBy(
                category = com.yourssu.soongpt.domain.course.implement.Category.CHAPEL,
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
        baselineCombinations: List<TimetableCandidate>,
        userContext: UserContext,
        command: PrimaryTimetableCommand,
        mandatoryChapelCandidate: TimetableCandidate?
    ): List<GroupedTimetableResponse> {
        // STEP 1: '과목 교체' 조합 생성
        val swappedCombinations = mutableListOf<TimetableCandidate>()
        val baselineForSwaps = baselineCombinations
            .sortedBy { it.timeSlot.cardinality() }
            .take(MAX_BASELINE_FOR_SWAPS)

        val alternativesByTrack = mutableMapOf<SwapTrack, List<com.yourssu.soongpt.domain.course.implement.Course>>()
        val alternateQueuesByTrack = mutableMapOf<SwapTrack, ArrayDeque<com.yourssu.soongpt.domain.course.implement.Course>>()
        var swapAttempts = 0

        swapLoop@ for (baseline in baselineForSwaps) {
            val fixedCommand = command.withFixedDivisions(baseline.codes)
            val allSelectedCourses = fixedCommand.getAllCourseCodesWithCategory()
                .filter { (_, track) -> track == SwapTrack.MAJOR_ELECTIVE }

            val courseByTrack = allSelectedCourses.groupBy { it.second }
            val activeTracks = courseByTrack.keys.shuffled().toMutableList()
            if (activeTracks.isEmpty()) continue

            while (swapAttempts < MAX_SWAP_ATTEMPTS && activeTracks.isNotEmpty()) {
                val track = activeTracks.removeAt(0)
                val coursesForTrack = courseByTrack[track].orEmpty()
                if (coursesForTrack.isEmpty()) {
                    continue
                }

                val selectedPair = coursesForTrack.random()
                val selectedCourse = selectedPair.first
                val alternativeCourses = alternativesByTrack.getOrPut(track) {
                    val fetched = swapCourseProvider.findAlternatives(track, userContext)
                    fetched
                }.filter { it.code.toBaseCode() != selectedCourse.courseCode }

                val queue = alternateQueuesByTrack.getOrPut(track) {
                    val sampled = alternativeCourses
                        .take(SWAP_SAMPLE_POOL_SIZE)
                        .shuffled()
                    ArrayDeque(sampled)
                }

                var picked = 0
                while (picked < MAX_SWAP_ALTERNATIVES && queue.isNotEmpty()) {
                    if (swapAttempts >= MAX_SWAP_ATTEMPTS) {
                        break@swapLoop
                    }
                    val altCourse = queue.removeFirst()
                    val swappedCommand = fixedCommand.copyAndSwap(selectedCourse.courseCode, track, altCourse.code)
                    val swappedGroups = courseCandidateProvider.createCourseCandidateGroups(swappedCommand)
                    val newCombinations = timetableCombinationGenerator.generate(swappedGroups, mandatoryChapelCandidate)
                    swappedCombinations.addAll(newCombinations)
                    swapAttempts++
                    picked++
                    if (swappedCombinations.size >= MAX_SWAPPED_COMBINATIONS) {
                        break@swapLoop
                    }
                }

                // If this track still has alternatives, put it back for round-robin.
                if (queue.isNotEmpty()) {
                    activeTracks.add(track)
                }
            }
        }
        // STEP 2: Master List 생성
        val masterList = (baselineCombinations + swappedCombinations).distinctBy { it.timeSlot }
//        System.err.println(
//            "Master list sizes: baseline=${baselineCombinations.size}, swapped=${swappedCombinations.size}, master=${masterList.size}"
//        )

        // STEP 3: 태그별 그룹화 및 최종 응답 DTO 생성
        val resultGroups = mutableListOf<GroupedTimetableResponse>()

        // 3-1. 'DEFAULT' 태그 그룹 처리 (공강 기준 점수)
        val usedTimeSlots = mutableSetOf<String>()
        val defaultCandidates = baselineCombinations
            .sortedBy { it.timeSlot.cardinality() } // 공강 점수: 채워진 칸이 적을수록 좋음 (오름차순)
            .asSequence()
            .filter { candidate -> usedTimeSlots.add(candidate.timeSlotKey()) }
            .take(MAX_RECOMMENDATIONS_PER_TAG)
            .toList()

        // 3-2. 조건부 태그 그룹 처리 (비트셋 매칭 점수)
        val conditionalTags = Tag.values().filter { it != Tag.DEFAULT }
        val tagCandidates = mutableListOf<Pair<Tag, List<TimetableCandidate>>>()
        for (tag in conditionalTags) {
            // 1. 필터링: 해당 태그의 조건을 만족하는 시간표만 masterList에서 걸러냄
            val filteredCandidates = masterList.filter { candidate -> tag.strategy.isCorrect(candidate.timeSlot) }

            // 2. 점수 계산 및 랭킹: 필터링된 후보 내에서 점수 계산 후 정렬
            val rankedCandidates = timetableRanker.rankByPreference(filteredCandidates, tag)

            val candidatesForTag = rankedCandidates
                .asSequence()
                .filter { candidate -> usedTimeSlots.add(candidate.timeSlotKey()) }
                .take(MAX_RECOMMENDATIONS_PER_TAG)
                .toList()

            if (candidatesForTag.isNotEmpty()) {
                tagCandidates.add(tag to candidatesForTag)
            }
        }

        val allSelectedCandidates = defaultCandidates + tagCandidates.flatMap { it.second }
        val allCodes = allSelectedCandidates.flatMap { it.codes }.distinct()
        val courseByCode = courseReader.findAllByCode(allCodes).associateBy { it.code }
        val baseNameCache = mutableMapOf<Long, String?>()

        if (defaultCandidates.isNotEmpty()) {
            val defaultRecommendations = defaultCandidates.map { candidate ->
                val score = timetableRanker.totalScore(candidate)
                val timetableResponse = timetablePersister.persist(candidate, Tag.DEFAULT, score, courseByCode)
                RecommendationDto(
                    description = "선택하신 과목으로 만들 수 있는 시간표입니다.",
                    timetable = timetableResponse
                )
            }
            resultGroups.add(GroupedTimetableResponse(Tag.DEFAULT.name, defaultRecommendations))
        }

        for ((tag, candidates) in tagCandidates) {
            val recommendationsForTag = candidates.map { candidate ->
                val score = timetableRanker.totalScore(candidate)
                val timetableResponse = timetablePersister.persist(candidate, tag, score, courseByCode)
                RecommendationDto(
                    description = buildRecommendationDescription(
                        candidate,
                        tag,
                        command,
                        userContext,
                        courseReader,
                        courseByCode,
                        baseNameCache
                    ),
                    timetable = timetableResponse
                )
            }
            if (recommendationsForTag.isNotEmpty()) {
                resultGroups.add(GroupedTimetableResponse(tag.name, recommendationsForTag))
            }
        }

        // TODO: 태그 우선순위에 따라 resultGroups 정렬하는 로직 추가
        return resultGroups
    }

    private fun findSingleConflictCourses(
        command: PrimaryTimetableCommand,
        baseTimetable: TimetableCandidate?,
        userContext: UserContext
    ): List<DeletableCourseDto> {
        val allSelectedCourses = command.getAllCourseCodes()
        val conflictingCourses = mutableListOf<DeletableCourseDto>()

        for (courseToExclude in allSelectedCourses) {
            val modifiedCommand = command.copyWithoutCourse(courseToExclude.courseCode)
            val courseCandidateGroups = courseCandidateProvider.createCourseCandidateGroups(modifiedCommand)
            val combinations = timetableCombinationGenerator.generate(courseCandidateGroups, baseTimetable)

            if (combinations.isNotEmpty()) {
                val resolvedCourse = if (courseToExclude.selectedCourseIds.isNotEmpty()) {
                    val fullCode = (courseToExclude.courseCode * 100) + courseToExclude.selectedCourseIds.first()
                    courseReader.findAllByCode(listOf(fullCode)).firstOrNull()
                } else {
                    courseReader.findAllByClass(userContext.department, courseToExclude.courseCode, userContext.grade)
                        .firstOrNull()
                }

                if (resolvedCourse != null) {
                    // TODO: rename field to baseCode in response contract
                    conflictingCourses.add(DeletableCourseDto(resolvedCourse.code, resolvedCourse.category))
                }
            }
        }
        return conflictingCourses.distinct()
    }
}

private fun PrimaryTimetableCommand.getAllCourseCodes(): List<SelectedCourseCommand> {
    return this.retakeCourses +
            this.majorRequiredCourses +
            this.majorElectiveCourses +
            this.majorBasicCourses +
            this.doubleMajorCourses +
            this.minorCourses +
            this.teachingCourses +
            this.generalRequiredCourses +
            this.addedCourses
}

private fun PrimaryTimetableCommand.getAllCourseCodesWithCategory(): List<Pair<SelectedCourseCommand, SwapTrack>> {
    return this.majorElectiveCourses.map { it to SwapTrack.MAJOR_ELECTIVE } +
            this.doubleMajorCourses.map { it to SwapTrack.DOUBLE_MAJOR } +
            this.minorCourses.map { it to SwapTrack.MINOR } +
            this.teachingCourses.map { it to SwapTrack.TEACHING }
}

private fun PrimaryTimetableCommand.copyWithoutCourse(courseCode: Long): PrimaryTimetableCommand {
    return this.copy(
        retakeCourses = this.retakeCourses.filterNot { it.courseCode == courseCode },
        majorRequiredCourses = this.majorRequiredCourses.filterNot { it.courseCode == courseCode },
        majorElectiveCourses = this.majorElectiveCourses.filterNot { it.courseCode == courseCode },
        majorBasicCourses = this.majorBasicCourses.filterNot { it.courseCode == courseCode },
        doubleMajorCourses = this.doubleMajorCourses.filterNot { it.courseCode == courseCode },
        minorCourses = this.minorCourses.filterNot { it.courseCode == courseCode },
        teachingCourses = this.teachingCourses.filterNot { it.courseCode == courseCode },
        generalRequiredCourses = this.generalRequiredCourses.filterNot { it.courseCode == courseCode },
        addedCourses = this.addedCourses.filterNot { it.courseCode == courseCode }
    )
}

private fun PrimaryTimetableCommand.copyAndSwap(
    codeToRemove: Long,
    track: SwapTrack,
    newCourseCode: Long
): PrimaryTimetableCommand {
    val newSelection = SelectedCourseCommand(courseCode = newCourseCode, selectedCourseIds = emptyList())
    return when (track) {
        SwapTrack.MAJOR_ELECTIVE -> this.copy(
            majorElectiveCourses = this.majorElectiveCourses.replaceCourse(codeToRemove, newSelection)
        )
        SwapTrack.DOUBLE_MAJOR -> this.copy(
            doubleMajorCourses = this.doubleMajorCourses.replaceCourse(codeToRemove, newSelection)
        )
        SwapTrack.MINOR -> this.copy(
            minorCourses = this.minorCourses.replaceCourse(codeToRemove, newSelection)
        )
        SwapTrack.TEACHING -> this.copy(
            teachingCourses = this.teachingCourses.replaceCourse(codeToRemove, newSelection)
        )
    }
}

private fun PrimaryTimetableCommand.withFixedDivisions(candidateCodes: List<Long>): PrimaryTimetableCommand {
    val divisionByBase = candidateCodes
        .associate { code -> code.toBaseCode() to (code % 100) }

    fun List<SelectedCourseCommand>.fixDivisions(): List<SelectedCourseCommand> {
        return this.map { command ->
            if (command.selectedCourseIds.isNotEmpty()) return@map command
            val division = divisionByBase[command.courseCode]
            if (division == null || division <= 0) return@map command
            command.copy(selectedCourseIds = listOf(division))
        }
    }

    return this.copy(
        retakeCourses = this.retakeCourses.fixDivisions(),
        majorRequiredCourses = this.majorRequiredCourses.fixDivisions(),
        majorElectiveCourses = this.majorElectiveCourses.fixDivisions(),
        majorBasicCourses = this.majorBasicCourses.fixDivisions(),
        doubleMajorCourses = this.doubleMajorCourses.fixDivisions(),
        minorCourses = this.minorCourses.fixDivisions(),
        teachingCourses = this.teachingCourses.fixDivisions(),
        generalRequiredCourses = this.generalRequiredCourses.fixDivisions(),
        addedCourses = this.addedCourses.fixDivisions()
    )
}

private fun List<SelectedCourseCommand>.replaceCourse(
    codeToRemove: Long,
    newSelection: SelectedCourseCommand
): List<SelectedCourseCommand> {
    var replaced = false
    val updated = this.map { existing ->
        if (existing.courseCode == codeToRemove) {
            replaced = true
            newSelection
        } else {
            existing
        }
    }
    return if (replaced) updated else this
}

private fun buildRecommendationDescription(
    candidate: TimetableCandidate,
    tag: Tag,
    command: PrimaryTimetableCommand,
    userContext: UserContext,
    courseReader: CourseReader,
    courseByCode: Map<Long, com.yourssu.soongpt.domain.course.implement.Course>,
    baseNameCache: MutableMap<Long, String?>
): String {
//    System.err.println(
//        "buildRecommendationDescription: tag=$tag, candidateCodes=${candidate.codes}, " +
//            "selectedCourseCodes=${command.getAllCourseCodes().map { it.courseCode }}"
//    )
    val selectedCommands = command.getAllCourseCodes()
    val selectedBaseCodes = selectedCommands.map { it.courseCode }.toSet()
    val candidateBaseCodes = candidate.codes
        .filter { code -> courseByCode[code]?.category != com.yourssu.soongpt.domain.course.implement.Category.CHAPEL }
        .map { it.toBaseCode() }
        .toSet()

    val removedBaseCodes = selectedBaseCodes - candidateBaseCodes
    val addedBaseCodes = candidateBaseCodes - selectedBaseCodes

    val candidateCourses = candidate.codes.mapNotNull { code -> courseByCode[code] }
    val candidateNameByBase = candidateCourses.associateBy({ it.baseCode() }, { it.name })

    if (removedBaseCodes.isNotEmpty() || addedBaseCodes.isNotEmpty()) {
//        System.err.println("Removed base codes=$removedBaseCodes, added base codes=$addedBaseCodes")
        val removedNames = removedBaseCodes.mapNotNull { baseCode ->
            baseNameCache.getOrPut(baseCode) {
                courseReader.findAllByClass(userContext.department, baseCode, userContext.grade)
                    .firstOrNull()
                    ?.name
            }
        }
        val addedNames = addedBaseCodes.mapNotNull { baseCode ->
            candidateNameByBase[baseCode]
        }

        val pairs = removedNames.zip(addedNames)
        val summary = pairs.take(2).joinToString(", ") { (removed, added) -> "$removed -> $added" }
        val suffix = if (pairs.size > 2) " 외 ${pairs.size - 2}건" else ""

        return if (summary.isNotBlank()) {
            "전공선택 과목 교체로 ${tag.description}를 만족한 시간표입니다. ($summary$suffix)"
        } else {
            "전공선택 과목 교체로 ${tag.description}를 만족한 시간표입니다."
        }
    }

    val selectedDivisionCodes = selectedCommands.flatMap { commandItem ->
        if (commandItem.selectedCourseIds.isEmpty()) {
            emptyList()
        } else {
            commandItem.selectedCourseIds.map { division ->
                (commandItem.courseCode * 100) + division
            }
        }
    }.toSet()

    if (selectedDivisionCodes.isNotEmpty()) {
        val candidateCodes = candidate.codes.toSet()
        val changedDivisionBaseCodes = selectedCommands.mapNotNull { commandItem ->
            if (commandItem.selectedCourseIds.isEmpty()) return@mapNotNull null
            val baseCode = commandItem.courseCode
            val selectedFullCodes = commandItem.selectedCourseIds.map { division ->
                (baseCode * 100) + division
            }
            val hasSameBase = candidateBaseCodes.contains(baseCode)
            val hasSelectedDivision = selectedFullCodes.any { candidateCodes.contains(it) }
            if (hasSameBase && !hasSelectedDivision) baseCode else null
        }

        if (changedDivisionBaseCodes.isNotEmpty()) {
            val changedNames = changedDivisionBaseCodes.mapNotNull { baseCode ->
                candidateNameByBase[baseCode]
                    ?: baseNameCache.getOrPut(baseCode) {
                        courseReader.findAllByClass(userContext.department, baseCode, userContext.grade)
                            .firstOrNull()
                            ?.name
                    }
            }
            val summary = changedNames.take(2).joinToString(", ")
            val suffix = if (changedNames.size > 2) " 외 ${changedNames.size - 2}건" else ""
            return if (summary.isNotBlank()) {
                "분반 변경으로 ${tag.description}를 만족한 시간표입니다. ($summary$suffix)"
            } else {
                "분반 변경으로 ${tag.description}를 만족한 시간표입니다."
            }
        }
    }

    return tag.description
}

private fun TimetableCandidate.timeSlotKey(): String {
    return this.timeSlot.toLongArray().joinToString(",")
}
