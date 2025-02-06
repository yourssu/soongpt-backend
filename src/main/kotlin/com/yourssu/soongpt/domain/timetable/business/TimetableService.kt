package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.course.implement.CoursesFactory
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.implement.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimetableService(
    private val timetableReader: TimetableReader,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
    private val timetableCourseReader: TimetableCourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val departmentReader: DepartmentReader,
    private val courseReader: CourseReader,
    private val timetableCandidateFactory: TimetableCandidateFactory,
) {
    @Transactional
    fun createTimetable(command: TimetableCreatedCommand): TimetableResponses {
        val department = departmentReader.getByName(command.departmentName)
        val majorRequiredCourses =
            command.majorRequiredCourses.map { courseReader.findAllByCourseNameInMajorRequired(department.id!!, it) }
        val majorElectiveCourses =
            command.majorElectiveCourses.map { courseReader.findAllByCourseNameInMajorElective(department.id!!, it) }
        val generalRequiredCourses =
            command.generalRequiredCourses.map {
                courseReader.findAllByCourseNameInGeneralRequired(department.id!!, it)
            }

        val coursesCandidates = CoursesFactory(majorRequiredCourses + majorElectiveCourses + generalRequiredCourses).generateTimetableCandidates()
        val timetableCandidates = timetableCandidateFactory.createTimetableCandidates(coursesCandidates).filterTagStrategy()

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
        return TimetableResponses(responses)
    }

    private fun toTimetableCourseResponses(courses: Courses) =
        courses.values.map { TimetableCourseResponse.from(it, courseTimeReader.findAllByCourseId(it.id!!)) }

    private fun saveTimetableCourses(courses: Courses, timetable: Timetable) {
        for (course in courses.values) {
            timetableCourseWriter.save(TimetableCourse(timetableId = timetable.id!!, courseId = course.id!!))
        }
    }

    fun getTimeTable(id: Long): TimetableResponse {
        val timetable = timetableReader.get(id)
        val courses = timetableCourseReader.findAllCourseByTimetableId(id)
        val response = courses.map { TimetableCourseResponse.from(it, courseTimeReader.findAllByCourseId(it.id!!)) }
        return TimetableResponse.from(timetable, response)
    }
}