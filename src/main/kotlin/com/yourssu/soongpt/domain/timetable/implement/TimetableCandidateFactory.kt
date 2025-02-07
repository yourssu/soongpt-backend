package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Component

private const val DIFFERENT_GRADE_SCORE = 19
private const val MORNING_CLASSES_SCORE = 10

@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
    private val targetReader: TargetReader,
) {
    fun createTimetableCandidatesWithRule(coursesCandidates: List<Courses>): TimetableCandidates {
        return TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        }).filterRules()
    }

    fun extendWithDifferentGrade(timetableCandidates: TimetableCandidates, addCourses: List<Courses>, departmentGrade: DepartmentGrade): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map {
            Triple(it, CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())), -countDifferentGrades(departmentGrade, it))
        })
    }

    private fun countDifferentGrades(departmentGrade: DepartmentGrade, courses: Courses): Int {
        return courses.values.count { !targetReader.isTarget(it.id!!, departmentGrade) } * DIFFERENT_GRADE_SCORE
    }

    fun extendWithRatings(timetableCandidates: TimetableCandidates, addCourses: List<Pair<Courses, Double>>): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map { (courses, score) ->
            val courseTimes = courseTimeReader.findAllByCourseIds(courses.getAllIds())
        Triple(courses, CourseTimes(courseTimes), score.toInt() - countMorningClasses(CourseTimes(courseTimes)))
        })
    }

    private fun countMorningClasses(courseTimes: CourseTimes): Int {
        return courseTimes.countMorningClasses() * MORNING_CLASSES_SCORE

    }
}