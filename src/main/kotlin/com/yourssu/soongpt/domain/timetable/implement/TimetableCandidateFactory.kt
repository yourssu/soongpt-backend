package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import org.springframework.stereotype.Component

@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
) {
    fun createTimetableCandidates(coursesCandidates: List<Courses>): TimetableCandidates {
        return TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        })
    }

    fun extendTimetableCandidates(timetableCandidates: TimetableCandidates, addCourses: List<Courses>): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map {
            Pair(it, CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds()))
            )
        })
    }

    fun issueTimetables(timetableCandidates: TimetableCandidates): ArrayList<TimetableResponse> {
        val responses = ArrayList<TimetableResponse>()
        for (timetableCandidate in timetableCandidates.values) {
            val timetable = timetableWriter.save(Timetable(tag = timetableCandidate.tag))
            saveTimetableCourses(timetableCandidate.courses, timetable)
            responses.add(
                TimetableResponse(
                    timetable.id!!,
                    timetable.tag.name,
                    toTimetableCourseResponses(timetableCandidate.courses)
                )
            )
        }
        return responses
    }

    private fun saveTimetableCourses(courses: Courses, timetable: Timetable) {
        for (course in courses.values) {
            timetableCourseWriter.save(TimetableCourse(timetableId = timetable.id!!, courseId = course.id!!))
        }
    }

    private fun toTimetableCourseResponses(courses: Courses) =
        courses.values.map { TimetableCourseResponse.from(it, courseTimeReader.findAllByCourseId(it.id!!)) }
}