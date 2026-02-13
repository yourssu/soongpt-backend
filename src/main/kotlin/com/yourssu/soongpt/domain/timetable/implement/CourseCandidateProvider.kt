package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component

@Component
class CourseCandidateProvider(
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val untakenCourseCodeService: UntakenCourseCodeService,
) {
    fun createCourseCandidateGroups(command: PrimaryTimetableCommand): List<List<CourseCandidate>> {
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
            .map { (selectedCommand, category) ->
                getCourseCandidates(selectedCommand, getUntakenCodes(category))
            }
    }

    private fun getCourseCandidates(
        command: SelectedCourseCommand,
        untakenCodes: List<Long>
    ): List<CourseCandidate> {
        val coursesToProcess = if (command.selectedCourseIds.isEmpty()) {
            val matchedCodes = untakenCodes.filter { it.toBaseCode() == command.courseCode }
            val resolvedCourses = courseReader.findAllByCode(matchedCodes)
                .take(2)
            if (resolvedCourses.isNotEmpty()) {
                resolvedCourses
            } else {
                // 선택한 과목이 기수강 등으로 untaken 목록에 없더라도, 최소 1개 분반은 후보로 넣어야 함
                courseReader.findAllByCode(listOf((command.courseCode * 100) + 1))
            }
        } else {
            // 분반 선택 했을때: 8자리 과목 코드와 분반 번호를 조합하여 10자리 코드를 생성하고, 해당 분반들만 후보로 가져옴
            val fullCourseCodes = command.selectedCourseIds.map { division ->
                (command.courseCode * 100) + division
            }
            courseReader.findAllByCode(fullCourseCodes)
        }

        return coursesToProcess.map { course ->
            courseCandidateFactory.create(course)
        }
    }
}

private fun PrimaryTimetableCommand.getAllSelectedCourseCommands(): List<Pair<SelectedCourseCommand, Category>> {
    return this.retakeCourses.map { it to Category.MAJOR_ELECTIVE } +
            this.addedCourses.map { it to Category.MAJOR_ELECTIVE } +
            this.majorRequiredCourses.map { it to Category.MAJOR_REQUIRED } +
            this.generalRequiredCourses.map { it to Category.GENERAL_REQUIRED } +
            this.majorElectiveCourses.map { it to Category.MAJOR_ELECTIVE } +
            this.doubleMajorCourses.map { it to Category.MAJOR_ELECTIVE } +
            this.minorCourses.map { it to Category.MAJOR_ELECTIVE } +
            this.teachingCourses.map { it to Category.TEACHING }
}
