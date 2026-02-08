package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component

@Component
class CourseCandidateProvider(
    private val userContextProvider: UserContextProvider,
    private val courseReader: CourseReader,
    private val courseCandidateFactory: CourseCandidateFactory
) {
    fun createCourseCandidateGroups(command: PrimaryTimetableCommand): List<List<CourseCandidate>> {
        // 1. UserContext 가져오기
        val userContext = userContextProvider.getContext(command.userId)

        // 2. 각 과목 카테고리별로 분반 후보 그룹을 가져와 하나의 리스트로 합침
        return command.getAllSelectedCourseCommands()
            .map { selectedCommand -> getCourseCandidates(selectedCommand, userContext) }
    }

    private fun getCourseCandidates(
        command: SelectedCourseCommand,
        userContext: UserContext
    ): List<CourseCandidate> {
        val coursesToProcess = if (command.selectedCourseIds.isEmpty()) {
            // 분반 선택 안했을때: 해당 과목의 모든 분반을 후보로 가져옴
            // 교양필수 과목 등은 이 분기에서 department, grade에 따라 올바른 과목으로 변환됨
            courseReader.findAllByClass(userContext.department, command.courseCode, userContext.grade)
        } else {
            // 분반 선택 했을때: 선택한 특정 분반만 후보로 가져옴
            courseReader.findAllByCode(command.selectedCourseIds)
        }

        return coursesToProcess.map { course ->
            courseCandidateFactory.create(course)
        }
    }
}

private fun PrimaryTimetableCommand.getAllSelectedCourseCommands(): List<SelectedCourseCommand> {
    return this.retakeCourses +
            this.addedCourses +
            this.majorRequiredCourses +
            this.generalRequiredCourses +
            this.majorElectiveCourses +
            this.otherMajorCourses
}

