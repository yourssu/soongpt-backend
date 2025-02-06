package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import org.springframework.stereotype.Component

@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
) {
    fun createTimetableCandidates(coursesCandidates: List<Courses>): TimetableCandidates {
        return TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        })
    }
}