package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import org.springframework.stereotype.Component

@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
) {
    fun createTimetableCandidatesWithRule(coursesCandidates: List<Courses>): TimetableCandidates {
        return TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        }).filterRules()
    }

    fun extendTimetableCandidatesWithRule(timetableCandidates: TimetableCandidates, addCourses: List<Courses>): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map {
            Pair(it, CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds()))
            )
        })
    }
}