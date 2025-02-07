package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.course.implement.Courses.Companion.calculateAvailableMajorElective
import com.yourssu.soongpt.domain.course.implement.Courses.Companion.validateCreditRule
import com.yourssu.soongpt.domain.course.implement.CoursesFactory
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeReader
import com.yourssu.soongpt.domain.rating.implement.RatingReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TimeTableFactory(
    private val departmentReader: DepartmentReader,
    private val departmentGradeReader: DepartmentGradeReader,
    private val courseReader: CourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
    private val timetableCandidateFactory: TimetableCandidateFactory,
    private val ratingReader: RatingReader,
) {
    fun createTimetable(command: TimetableCreatedCommand): TimetableCandidates {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)

        val majorRequiredCourses =
            command.majorRequiredCourses.map { courseReader.findAllByCourseNameInMajorRequired(department.id, it) }
        val majorElectiveCourses =
            command.majorElectiveCourses.map { courseReader.findAllByCourseNameInMajorElective(department.id, it) }
        val generalRequiredCourses =
            command.generalRequiredCourses.map {
                courseReader.findAllByCourseNameInGeneralRequired(department.id, it)
            }
        validateCreditRule(
            majorRequiredCourses = majorRequiredCourses,
            generalRequiredCourses = generalRequiredCourses,
            majorElectiveCredit = command.majorElectiveCredit,
            generalElectiveCredit = command.generalElectiveCredit,
        )

        val step1 = CoursesFactory(majorRequiredCourses + majorElectiveCourses + generalRequiredCourses)
            .generateTimetableCandidates()
        val step2 = timetableCandidateFactory.createTimetableCandidatesWithRule(step1)

        val availableMajorElectiveCredit = calculateAvailableMajorElective(command, majorElectiveCourses)
        val addMajorElectives =
            CoursesFactory(Courses(courseReader.findAllByDepartmentGradeIdInMajorElective(departmentGrade.id!!)).groupByCourseNames()).districtDuplicatedCourses(
                majorElectiveCourses
            )
                .allCases()
                .filterLessThanTotalCredit(availableMajorElectiveCredit)
        val step3 = timetableCandidateFactory.extendTimetableCandidatesWithRule(step2, addMajorElectives)

        val generalElectives = Courses(courseReader.findAllByDepartmentGradeIdInGeneralElective(departmentGrade.id))
        val ratings = ratingReader.findAllPointPairs(generalElectives)
        val addGeneralElectives =
            CoursesFactory(generalElectives.groupByCourseNames())
                .allCasesLessThan(command.generalElectiveCredit)
        val second = CoursesFactory(addGeneralElectives).sortByRatingAverage(ratings)
        val step4 = timetableCandidateFactory.extendTimetableCandidates(step3, second)

        return step4
    }

    @Transactional
    fun issueTimetables(timetableCandidates: TimetableCandidates): List<TimetableResponse> {
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