package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.ViolatedTotalCreditRuleException
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.implement.exception.ViolatedMajorElectiveCreditException

class Courses(
    val values: List<Course>,
) {
    companion object {
        fun calculateAvailableMajorElective(
            command: TimetableCreatedCommand,
            majorElectiveCourses: List<Courses>
        ): Int {
            val availableMajorElectiveCredit =
                command.majorElectiveCredit - majorElectiveCourses.sumOf { it.getFirstCredit() }
            if (availableMajorElectiveCredit < 0) {
                throw ViolatedMajorElectiveCreditException()
            }
            return availableMajorElectiveCredit
        }

        fun validateCreditRule(
            majorRequiredCourses: List<Courses>,
            generalRequiredCourses: List<Courses>,
            majorElectiveCredit: Int,
            generalElectiveCredit: Int,
            ) {
            val total = majorRequiredCourses.sumOf { it.getFirstCredit() } + generalRequiredCourses.sumOf { it.getFirstCredit() } + majorElectiveCredit + generalElectiveCredit
            if (total > 22) {
                throw ViolatedTotalCreditRuleException(total.toString())
            }
        }
    }

    fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    fun totalCredit(): Int {
        return values.sumOf { it.credit }
    }

    fun getFirstCredit(): Int {
        if (values.isEmpty()) {
            return 0
        }
        return values.first().credit
    }

    fun unpackNameAndProfessor(): List<Pair<String, String>> {
        return values.map { it.courseName to it.professorName!! }
    }

    fun getAllIds(): List<Long> {
        return values.map { it.id!! }
    }

    fun groupByCourseNames(): List<Courses> {
        return values.groupBy { it.courseName }
            .map { Courses(it.value) }
    }

    fun add(course: Course): Courses {
        return Courses(this.values + course)
    }

    fun extend(courses: Courses): Courses {
        return Courses(this.values + courses.values)
    }
}