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
    fun persist(candidate: TimetableCandidate, tag: Tag, score: Int): TimetableResponse {
        val courses = courseReader.findAllByCode(candidate.codes)
        return persistWithCourses(candidate, tag, score, courses)
    }

    fun persist(candidate: TimetableCandidate, tag: Tag, score: Int, courseByCode: Map<Long, com.yourssu.soongpt.domain.course.implement.Course>): TimetableResponse {
        val orderedCourses = candidate.codes.mapNotNull { code -> courseByCode[code] }.toMutableList()
        if (orderedCourses.size != candidate.codes.size) {
            val missingCodes = candidate.codes.filterNot { code -> courseByCode.containsKey(code) }
            if (missingCodes.isNotEmpty()) {
                orderedCourses.addAll(courseReader.findAllByCode(missingCodes))
            }
        }
        return persistWithCourses(candidate, tag, score, orderedCourses)
    }

    private fun persistWithCourses(
        candidate: TimetableCandidate,
        tag: Tag,
        score: Int,
        courses: List<com.yourssu.soongpt.domain.course.implement.Course>
    ): TimetableResponse {
        // 1. Timetable 엔티티 저장
        val newTimetable = timetableWriter.save(
            Timetable(
                tag = tag,
                score = score
            )
        )

        // 2. TimetableCourse 연관관계 저장
        val timetableCourses = courses.map { course ->
            TimetableCourse(
                timetableId = newTimetable.id!!,
                courseId = course.id!!
            )
        }
        timetableCourseWriter.saveAll(timetableCourses)

        // 3. 최종 응답 DTO 생성
        val courseResponses = courses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            TimetableCourseResponse.from(course, courseTimes)
        }

        return TimetableResponse.from(newTimetable, courseResponses)
    }
}
