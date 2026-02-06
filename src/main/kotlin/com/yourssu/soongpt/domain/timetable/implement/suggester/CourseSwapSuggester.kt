package com.yourssu.soongpt.domain.timetable.implement.suggester

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.SuggestionCandidate
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateFactory
import com.yourssu.soongpt.domain.timetable.implement.TimetableCandidateBuilder
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component

@Component
class CourseSwapSuggester(
        private val courseReader: CourseReader,
        private val courseCandidateFactory: CourseCandidateFactory
) {
    fun suggest(
            primary: TimetableCandidate,
            command: PrimaryTimetableCommand,
            untakenCourses: List<Course> // 전필>전선 순으로 정렬된, 수강한 적 없는 전공과목 리스트
    ): List<SuggestionCandidate> {
        val suggestions = mutableListOf<SuggestionCandidate>()
        val primaryCourses = courseReader.findAllByCode(primary.codes)

        for (courseToRemove in primaryCourses) {
            if (suggestions.size >= 10) break

            // 1. 기준 시간표에서 현재 과목을 제거하여 빌더 생성
            val courseToRemoveCandidate = courseCandidateFactory.create(courseToRemove)
            val tempBuilder =
                    TimetableCandidateBuilder(
                            initialCodes = primary.codes,
                            initialTimeSlot = primary.timeSlot
                    )
            tempBuilder.remove(courseToRemoveCandidate)
            val remainingCredit =
                    command.maxCredit - (primary.points - courseToRemoveCandidate.point)

            // 2. 정렬된 후보 과목들을 순서대로 넣어봄
            for (courseToAdd in untakenCourses) {
                if (suggestions.size >= 10) break

                // 3. 학점 제약 조건 확인
                if (courseToAdd.credit!! > remainingCredit) continue

                val courseToAddCandidate = courseCandidateFactory.create(courseToAdd)

                // 4. 시간 충돌 확인 및 제안 생성
                if (tempBuilder.add(courseToAddCandidate)) {
                    val newTimetableCandidate = tempBuilder.build()
                    val description =
                            "'${courseToRemove.name}' 대신 '${courseToAdd.category.displayName}' 과목인 '${courseToAdd.name}'을(를) 수강할 수 있습니다."
                    suggestions.add(SuggestionCandidate(newTimetableCandidate, description))

                    // 다음 시뮬레이션을 위해 추가했던 과목 다시 제거 (백트래킹)
                    tempBuilder.remove(courseToAddCandidate)
                }
            }
        }
        return suggestions
    }
}
