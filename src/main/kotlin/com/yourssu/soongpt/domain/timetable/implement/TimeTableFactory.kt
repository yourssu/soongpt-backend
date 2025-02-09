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
        val chapels =
//            if (command.isChapel) courseReader.findChapelsByDepartmentGradeId(departmentGrade.id!!)
//                .map { Courses(listOf(it)) }
//            else
                emptyList<Courses>()

        validateCreditRule(
            majorRequiredCourses = majorRequiredCourses,
            generalRequiredCourses = generalRequiredCourses,
            majorElectiveCredit = command.majorElectiveCredit,
            generalElectiveCredit = command.generalElectiveCredit,
        )

        val step1 = CoursesFactory(majorRequiredCourses + majorElectiveCourses + generalRequiredCourses + chapels)
            .generateTimetableCandidates()
        val step2 = timetableCandidateFactory.createTimetableCandidatesWithRule(step1)

        val majorElectives = Courses(courseReader.findAllByDepartmentGradeIdInMajorElective(departmentGrade.id!!))
        val availableMajorElectiveCredit = calculateAvailableMajorElective(command, majorElectiveCourses)
        val addMajorElectives =
            CoursesFactory(majorElectives.groupByCourseNames())
                .districtDuplicatedCourses(majorElectiveCourses)
                .allCasesLessThan(availableMajorElectiveCredit, 100)
        val ratingsStep3 = ratingReader.findAllPointPairs(Courses(addMajorElectives.map { it.values }.flatten()))
        val majorCourseScorePairs = CoursesFactory(addMajorElectives).sortByRatingAverage(ratingsStep3, 20)
        val step3 = timetableCandidateFactory.extendWithRatings(step2, majorCourseScorePairs, 0)
        val step3N = timetableCandidateFactory.pickTopNEachTag(step3, 5)

        val generalElectives = Courses(courseReader.findAllByDepartmentGradeIdInGeneralElective(departmentGrade.id))
        val addGeneralElectives =
            CoursesFactory(generalElectives.groupByCourseNames())
                .allCasesLessThan(command.generalElectiveCredit, 100)
        val ratingsStep4 = ratingReader.findAllPointPairs(Courses(addGeneralElectives.map { it.values }.flatten()))
        val generalCourseScorePairs = CoursesFactory(addGeneralElectives).sortByRatingAverage(ratingsStep4, 20)
        val step4 = timetableCandidateFactory.extendWithRatings(step3N, generalCourseScorePairs)
        val step4N = timetableCandidateFactory.pickTopNEachTag(step4, 5)

        val step5 = timetableCandidateFactory.pickFinalTimetables(step4N)

        return step5
    }

    @Transactional
    fun issueTimetables(timetableCandidates: TimetableCandidates): List<TimetableResponse> {
        val responses = ArrayList<TimetableResponse>()
        for (step in timetableCandidates.values) {
            val timetable = timetableWriter.save(Timetable(tag = step.tag))
            saveTimetableCourses(step.courses, timetable)
            responses.add(
                TimetableResponse(
                    timetableId = timetable.id!!,
                    tag = timetable.tag.name,
                    score = step.score,
                    courses = toTimetableCourseResponses(step.courses)
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