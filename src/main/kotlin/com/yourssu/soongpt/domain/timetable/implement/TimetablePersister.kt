package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component

@Component
class TimetablePersister(
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
    private val courseReader: CourseReader
) {
    fun persist(candidate: TimetableCandidate): TimetableResponse {
        // 1. Timetable 엔티티 저장
        val newTimetable = timetableWriter.save(
            Timetable(
                tag = candidate.validTags.firstOrNull() ?: Tag.DEFAULT,
                score = 0 // TODO: 점수 계산 로직은 추후 반영 가능
            )
        )

        // 2. TimetableCourse 연관관계 저장
        val courses = courseReader.findAllByCode(candidate.codes)
        courses.forEach { course ->
            timetableCourseWriter.save(
                TimetableCourse(
                    timetableId = newTimetable.id!!,
                    courseId = course.id!!
                )
            )
        }

        // 3. 최종 응답 DTO 생성
        val courseResponses = courses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            TimetableCourseResponse.from(course, courseTimes)
        }

        return TimetableResponse.from(newTimetable, courseResponses)
    }
}
