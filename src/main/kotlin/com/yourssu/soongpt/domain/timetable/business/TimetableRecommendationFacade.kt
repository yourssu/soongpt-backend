package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.course.application.RecommendContextResolver
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.timetable.business.dto.DeletableCourseDto
import com.yourssu.soongpt.domain.timetable.business.dto.FinalTimetableRecommendationResponse
import com.yourssu.soongpt.domain.timetable.business.dto.GroupedTimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.RecommendationDto
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateFactory
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateProvider
import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.TimetableCombinationGenerator
import com.yourssu.soongpt.domain.timetable.implement.TimetablePersister
import com.yourssu.soongpt.domain.timetable.implement.TimetableRanker
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
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val swapCourseProvider: SwapCourseProvider,
    private val untakenCourseCodeService: UntakenCourseCodeService,
    private val recommendContextResolver: RecommendContextResolver,
) {
    @Transactional
    fun recommend(command: PrimaryTimetableCommand): FinalTimetableRecommendationResponse {
        val ctx = recommendContextResolver.resolve()
//        System.err.println("TimetableRecommendationFacade.recommend called with command: $command and userContext: $userContext")
        val mandatoryChapelCandidate = findMandatoryChapelForFreshman(ctx.userGrade, ctx.departmentName)
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
            val successResponse = processSuccessCase(combinations, command, mandatoryChapelCandidate)

//            System.err.println("Returning success response with ${successResponse.size} grouped recommendations")
            return FinalTimetableRecommendationResponse.success(successResponse)
        }

//        System.err.println("No valid timetable combinations found, checking for single conflict courses")
        val singleConflictCourseCodes = findSingleConflictCourses(commandWithoutChapel, mandatoryChapelCandidate)
        if (singleConflictCourseCodes.isNotEmpty()) {
            return FinalTimetableRecommendationResponse.singleConflict(singleConflictCourseCodes)
        }

//        System.err.println("No single conflict courses found, returning failure response")
        return FinalTimetableRecommendationResponse.failure()
    }

    private fun findMandatoryChapelForFreshman(userGrade: Int, departmentName: String): TimetableCandidate? {
        if (userGrade != 1 || departmentName in EXCLUDED_DEPARTMENTS_FOR_CHAPEL) return null

        val chapelCode = untakenCourseCodeService.getUntakenCourseCodes(Category.CHAPEL).firstOrNull()
            ?: return null
        val chapelCourse = courseReader.findAllByCode(listOf(chapelCode)).firstOrNull()
            ?: return null
        val chapelCandidate = courseCandidateFactory.create(chapelCourse)
        return TimetableCandidate(
            codes = listOf(chapelCandidate.code),
            timeSlot = chapelCandidate.timeSlot,
            validTags = emptyList()
        )
    }

    private fun processSuccessCase(
        baselineCombinations: List<TimetableCandidate>,
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
                    val fetched = swapCourseProvider.findAlternatives(track)
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

            val allowDuplicateByTag = tag in setOf(
                Tag.FREE_MONDAY,
                Tag.FREE_TUESDAY,
                Tag.FREE_WEDNESDAY,
                Tag.FREE_THURSDAY,
                Tag.FREE_FRIDAY
            )

            val candidatesForTag = rankedCandidates
                .asSequence()
                .filter { candidate ->
                    val key = candidate.timeSlotKey()
                    allowDuplicateByTag || usedTimeSlots.add(key)
                }
                .take(MAX_RECOMMENDATIONS_PER_TAG)
                .toList()

            if (candidatesForTag.isNotEmpty()) {
                tagCandidates.add(tag to candidatesForTag)
            }
        }

        val allSelectedCandidates = defaultCandidates + tagCandidates.flatMap { it.second }
        val allCodes = allSelectedCandidates.flatMap { it.codes }.distinct()
        val courseByCode = courseReader.findAllByCode(allCodes).associateBy { it.code }
        val baseNameCache = (courseByCode.values + loadSelectedCourses(command, courseReader))
            .associateBy({ it.baseCode() }, { it.name })
            .mapValues { (_, name) -> name as String? }
            .toMutableMap()

        if (defaultCandidates.isNotEmpty()) {
            val defaultRecommendations = defaultCandidates.map { candidate ->
                val score = timetableRanker.totalScore(candidate)
                val timetableResponse = timetablePersister.persist(candidate, Tag.DEFAULT, score, courseByCode)
                RecommendationDto(
                    description = formatDescription("지금까지 고른 과목으로 만들 수 있는 시간표예요"),
                    timetable = timetableResponse
                )
            }
            resultGroups.add(GroupedTimetableResponse(Tag.DEFAULT.description, defaultRecommendations))
        }

        for ((tag, candidates) in tagCandidates) {
            val recommendationsForTag = candidates.map { candidate ->
                val score = timetableRanker.totalScore(candidate)
                val timetableResponse = timetablePersister.persist(candidate, tag, score, courseByCode)
                RecommendationDto(
                    description = formatDescription(
                        buildRecommendationDescription(
                        candidate,
                        tag,
                        command,
                        courseReader,
                        courseByCode,
                        baseNameCache
                    )
                    ),
                    timetable = timetableResponse
                )
            }
            if (recommendationsForTag.isNotEmpty()) {
                resultGroups.add(GroupedTimetableResponse(tag.description, recommendationsForTag))
            }
        }

        // TODO: 태그 우선순위에 따라 resultGroups 정렬하는 로직 추가
        return resultGroups
    }

    private fun findSingleConflictCourses(
        command: PrimaryTimetableCommand,
        baseTimetable: TimetableCandidate?
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
                    val fullCode = (courseToExclude.courseCode * 100) + 1
                    courseReader.findAllByCode(listOf(fullCode)).firstOrNull()
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

private fun loadSelectedCourses(
    command: PrimaryTimetableCommand,
    courseReader: CourseReader
): List<com.yourssu.soongpt.domain.course.implement.Course> {
    val byFullCode = command.getAllCourseCodes()
        .filter { it.selectedCourseIds.isNotEmpty() }
        .flatMap { selected ->
            selected.selectedCourseIds.map { division ->
                (selected.courseCode * 100) + division
            }
        }
        .distinct()

    val byBaseCode = command.getAllCourseCodes()
        .filter { it.selectedCourseIds.isEmpty() }
        .map { (it.courseCode * 100) + 1 }
        .distinct()

    val allCodes = (byFullCode + byBaseCode).distinct()
    if (allCodes.isEmpty()) return emptyList()
    return allCodes.chunked(500).flatMap { courseReader.findAllByCode(it) }
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
    val newSelection = SelectedCourseCommand(courseCode = newCourseCode.toBaseCode(), selectedCourseIds = emptyList())
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
    courseReader: CourseReader,
    courseByCode: Map<Long, com.yourssu.soongpt.domain.course.implement.Course>,
    baseNameCache: MutableMap<Long, String?>
): String {
//    System.err.println(
//        "buildRecommendationDescription: tag=$tag, candidateCodes=${candidate.codes}, " +
//            "selectedCourseCodes=${command.getAllCourseCodes().map { it.courseCode }}"
//    )
    val selectedCommands = command.getAllCourseCodes()
    val selectedBaseCodes = selectedCommands.map { it.courseCode.toBaseCode() }.toSet()
    val candidateBaseCodes = candidate.codes
        .filter { code -> courseByCode[code]?.category != com.yourssu.soongpt.domain.course.implement.Category.CHAPEL }
        .map { it.toBaseCode() }
        .toSet()

    val removedBaseCodes = selectedBaseCodes - candidateBaseCodes
    val addedBaseCodes = candidateBaseCodes - selectedBaseCodes

    val candidateCourses = candidate.codes.mapNotNull { code -> courseByCode[code] }
    val candidateNameByBase = candidateCourses.associateBy({ it.baseCode() }, { it.name })
    val selectedNameByBase = selectedCommands.associate { commandItem ->
        val baseCode = commandItem.courseCode.toBaseCode()
        val name = if (commandItem.selectedCourseIds.isNotEmpty()) {
            val fullCode = (baseCode * 100) + commandItem.selectedCourseIds.first()
            courseReader.findAllByCode(listOf(fullCode)).firstOrNull()?.name
        } else {
            courseReader.findAllByCode(listOf((baseCode * 100) + 1)).firstOrNull()?.name
        }
        baseCode to name
    }

    if (removedBaseCodes.isNotEmpty() || addedBaseCodes.isNotEmpty()) {
//        System.err.println("Removed base codes=$removedBaseCodes, added base codes=$addedBaseCodes")
        fun nameForBase(baseCode: Long): String {
            return selectedNameByBase[baseCode]
                ?: baseNameCache[baseCode]
                ?: candidateNameByBase[baseCode]
                ?: courseReader.findAllByCode(listOf((baseCode * 100) + 1)).firstOrNull()?.name
                ?: "미상"
        }

        val removedNames = removedBaseCodes.map { baseCode -> nameForBase(baseCode) }
        val addedNames = addedBaseCodes.map { baseCode -> nameForBase(baseCode) }

        val benefit = tag.toBenefitMessage()
        val removed = removedNames.firstOrNull()
        val added = addedNames.firstOrNull()

        return when {
            removed != null && added != null ->
                "${removed}${objParticle(removed)} 빼고 ${added}${objParticle(added)} 넣으면 $benefit"
            removed != null ->
                "${removed}${objParticle(removed)} 빼면 $benefit"
            added != null ->
                "${added}${objParticle(added)} 넣으면 $benefit"
            else -> "과목을 바꾸면 $benefit"
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
            val candidateCodes = candidate.codes.toSet()
            val benefit = tag.toBenefitMessage()
            return "분반을 변경하면 $benefit"
        }
    }

    return defaultDescription(tag)
}

private fun Tag.toBenefitMessage(): String {
    return when (this) {
        Tag.FREE_MONDAY -> "월요일 공강이 생겨요"
        Tag.FREE_TUESDAY -> "화요일 공강이 생겨요"
        Tag.FREE_WEDNESDAY -> "수요일 공강이 생겨요"
        Tag.FREE_THURSDAY -> "목요일 공강이 생겨요"
        Tag.FREE_FRIDAY -> "금요일 공강이 생겨요"
        Tag.NO_MORNING_CLASSES -> "아침 수업이 없어져요"
        Tag.NO_EVENING_CLASSES -> "저녁 수업이 없어져요"
        Tag.NO_LONG_BREAKS -> "우주 공강이 없어져요"
        Tag.GUARANTEED_LUNCH_TIME -> "점심 시간이 보장돼요"
        else -> "${this.description}을(를) 만족해요"
    }
}

private fun defaultDescription(tag: Tag): String {
    val desc = tag.description
    return if (desc.endsWith("시간표")) {
        "현재 고른 과목으로 ${desc}를 만들 수 있어요"
    } else {
        "현재 고른 과목으로 ${desc}을(를) 만족하는 시간표를 만들 수 있어요"
    }
}

private fun PrimaryTimetableCommand.findCategoryLabel(courseCode: Long): String? {
    return when {
        this.majorRequiredCourses.any { it.courseCode == courseCode } -> "전공필수"
        this.majorElectiveCourses.any { it.courseCode == courseCode } -> "전공선택"
        this.majorBasicCourses.any { it.courseCode == courseCode } -> "전공기초"
        this.doubleMajorCourses.any { it.courseCode == courseCode } -> "복수전공"
        this.minorCourses.any { it.courseCode == courseCode } -> "부전공"
        this.teachingCourses.any { it.courseCode == courseCode } -> "교직"
        this.generalRequiredCourses.any { it.courseCode == courseCode } -> "교양필수"
        this.retakeCourses.any { it.courseCode == courseCode } -> "재수강"
        this.addedCourses.any { it.courseCode == courseCode } -> "추가"
        else -> null
    }
}

private fun TimetableCandidate.timeSlotKey(): String {
    return this.timeSlot.toLongArray().joinToString(",")
}

private fun objParticle(word: String): String {
    if (word.isBlank()) return "를"
    val trimmed = word.trimEnd { it.isWhitespace() }
    if (trimmed.isEmpty()) return "를"

    val lastMeaningful = trimmed.trimEnd { isPunctuationChar(it) || it == ')' || it == ']' || it == '}' }
    if (lastMeaningful.isEmpty()) return "를"

    val last = lastMeaningful.last()
    val code = last.code

    // 가-힣 처리해서 종성만 따로..
    if (code in 0xAC00..0xD7A3) {
        val hasFinal = (code - 0xAC00) % 28 != 0
        return if (hasFinal) "을" else "를"
    }

    if (last.isDigit()) {
        return when (last) {
            '0', '1', '3', '6', '7', '8' -> "을"
            '2', '4', '5', '9' -> "를"
            else -> "를"
        }
    }

    // English letter
    if (last.isLetter()) {
        val lower = last.lowercaseChar()
        val isVowel = lower in setOf('a', 'e', 'i', 'o', 'u', 'y')
        return if (isVowel) "를" else "을"
    }

    return "를"
}

private fun formatDescription(raw: String): String = wrapToTwoLines(raw, 25)

private fun wrapToTwoLines(text: String, maxPerLine: Int): String {
    if (text.length <= maxPerLine) return text

    val keywordIndex = text.indexOf("시간표")
    if (keywordIndex > 0 && keywordIndex <= maxPerLine) {
        val before = text.substring(0, keywordIndex).trimEnd()
        val after = text.substring(keywordIndex).trimStart()
        return if (before.isNotEmpty() && after.isNotEmpty()) {
            "$before\n$after"
        } else {
            text
        }
    }

    val breakAt = text.lastIndexOf(' ', maxPerLine)
    if (breakAt <= 0) {
        val first = text.substring(0, maxPerLine)
        val second = text.substring(maxPerLine)
        return "$first\n$second"
    }

    val first = text.substring(0, breakAt).trimEnd()
    val second = text.substring(breakAt + 1).trimStart()
    if (second.isEmpty()) return text
    return "$first\n$second"
}

private fun isPunctuationChar(ch: Char): Boolean {
    return when (Character.getType(ch)) {
        Character.CONNECTOR_PUNCTUATION.toInt(),
        Character.DASH_PUNCTUATION.toInt(),
        Character.START_PUNCTUATION.toInt(),
        Character.END_PUNCTUATION.toInt(),
        Character.INITIAL_QUOTE_PUNCTUATION.toInt(),
        Character.FINAL_QUOTE_PUNCTUATION.toInt(),
        Character.OTHER_PUNCTUATION.toInt() -> true
        else -> false
    }
}
