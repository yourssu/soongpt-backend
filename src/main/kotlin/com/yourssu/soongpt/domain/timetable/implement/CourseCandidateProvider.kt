package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CourseCandidateProvider(
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val untakenCourseCodeService: UntakenCourseCodeService,
) {
    private val logger = LoggerFactory.getLogger(CourseCandidateProvider::class.java)
    companion object {
        private const val MAX_DIVISIONS_PER_COURSE = 4
    }
    fun createCourseCandidateGroups(
        command: PrimaryTimetableCommand,
        useAllDivisions: Boolean = false
    ): List<List<CourseCandidate>> {
        val untakenCodesByCategory = mutableMapOf<Category, List<Long>>()

        fun getUntakenCodes(category: Category): List<Long> {
            return untakenCodesByCategory.getOrPut(category) {
                when (category) {
                    Category.GENERAL_REQUIRED, Category.GENERAL_ELECTIVE -> untakenCourseCodeService
                        .getUntakenCourseCodesByField(category)
                        .values
                        .flatten()
                    else -> untakenCourseCodeService.getUntakenCourseCodes(category)
                }
            }
        }

        // 2. 각 과목 카테고리별로 분반 후보 그룹을 가져와 하나의 리스트로 합침
        return command.getAllSelectedCourseCommands()
            .map { (selectedCommand, category, allowAllDivisions) ->
                getCourseCandidates(
                    selectedCommand,
                    getUntakenCodes(category),
                    allowAllDivisions || useAllDivisions
                )
            }
    }

    private fun getCourseCandidates(
        command: SelectedCourseCommand,
        untakenCodes: List<Long>,
        allowAllDivisions: Boolean
    ): List<CourseCandidate> {
        val coursesToProcess = if (command.selectedCourseIds.isEmpty()) {
            val allDivisions = courseReader.findAllByBaseCode(command.courseCode)
            if (allowAllDivisions) {
                if (allDivisions.isNotEmpty()) {
                    allDivisions.take(MAX_DIVISIONS_PER_COURSE)
                } else {
                    courseReader.findAllByCode(listOf((command.courseCode * 100) + 1))
                }
            } else {
                val matchedCodes = untakenCodes.filter { it.toBaseCode() == command.courseCode }
                val resolvedCourses = courseReader.findAllByCode(matchedCodes)
                if (resolvedCourses.isNotEmpty()) {
                    resolvedCourses.take(2)
                } else if (allDivisions.isNotEmpty()) {
                    allDivisions.take(2)
                } else {
                    // 선택한 과목이 기수강 등으로 untaken 목록에 없더라도, 최소 1개 분반은 후보로 넣어야 함
                    courseReader.findAllByCode(listOf((command.courseCode * 100) + 1))
                }
            }
        } else {
            // 분반 선택 했을때: 8자리 과목 코드와 분반 번호를 조합하여 10자리 코드를 생성하고, 해당 분반들만 후보로 가져옴
            val fullCourseCodes = command.selectedCourseIds.map { division ->
                (command.courseCode * 100) + division
            }
            val resolved = courseReader.findAllByCode(fullCourseCodes)
            if (resolved.isNotEmpty()) {
                resolved
            } else {
                // 선택한 분반 코드가 없을 경우: baseCode 기준으로 후보 복구
                val fallback = courseReader.findAllByBaseCode(command.courseCode)
                if (fallback.isNotEmpty()) {
                    if (allowAllDivisions) fallback.take(MAX_DIVISIONS_PER_COURSE) else fallback.take(2)
                } else {
                    logger.warn(
                        "CourseCandidateProvider: no candidates found. baseCode={}, selectedDivisions={}, requestedFullCodes={}",
                        command.courseCode,
                        command.selectedCourseIds,
                        fullCourseCodes
                    )
                    emptyList()
                }
            }
        }

        return coursesToProcess.map { course ->
            courseCandidateFactory.create(course)
        }
    }
}

private fun PrimaryTimetableCommand.getAllSelectedCourseCommands(): List<Triple<SelectedCourseCommand, Category, Boolean>> {
    return this.retakeCourses.map { Triple(it, Category.MAJOR_ELECTIVE, false) } +
            this.addedCourses.map { Triple(it, Category.MAJOR_ELECTIVE, true) } +
            this.majorRequiredCourses.map { Triple(it, Category.MAJOR_REQUIRED, false) } +
            this.majorBasicCourses.map { Triple(it, Category.MAJOR_BASIC, true) } +
            this.generalRequiredCourses.map { Triple(it, Category.GENERAL_REQUIRED, false) } +
            this.majorElectiveCourses.map { Triple(it, Category.MAJOR_ELECTIVE, false) } +
            this.doubleMajorCourses.map { Triple(it, Category.MAJOR_ELECTIVE, false) } +
            this.minorCourses.map { Triple(it, Category.MAJOR_ELECTIVE, false) } +
            this.teachingCourses.map { Triple(it, Category.TEACHING, false) }
}
