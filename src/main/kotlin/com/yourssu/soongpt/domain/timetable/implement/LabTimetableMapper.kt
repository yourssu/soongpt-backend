package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.implement.Timetable
import org.springframework.stereotype.Component

@Component
class LabTimetableMapper {

    fun mapToFrontend(timetable: Timetable, courses: List<Course>): LabTimetableResponse {
        val labCourses = courses.map { course ->
            LabCourseItem(
                courseName = course.name,
                professorName = course.professor,
                classification = course.category.name,
                credit = parseCreditFromTime(course.time),
                courseTime = mapCourseTimes(course.scheduleRoom)
            )
        }
        return LabTimetableResponse(
            timetableId = timetable.id!!,
            tag = timetable.tag.name,
            totalCredit = labCourses.sumOf { it.credit },
            courses = labCourses,
        )
    }

    private fun parseCreditFromTime(timeString: String): Int {
        // 기존 CourseCandidateFactory의 로직 재사용: point 대신 time 사용
        return timeString.toDoubleOrNull()?.toInt() ?: 0
    }

    private fun mapCourseTimes(scheduleRoom: String): List<LabCourseTime> {
        // 기존 CourseTimes.from() 로직 재사용
        val courseTimes = CourseTimes.from(scheduleRoom)
        return courseTimes.toList().map { courseTime ->
            LabCourseTime(
                week = courseTime.week.displayName,
                start = courseTime.startTime.toTimeFormat(),
                end = courseTime.endTime.toTimeFormat(),
                classroom = normalizeClassroom(courseTime.classroom)
            )
        }
    }

    private fun normalizeClassroom(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        // 1) remove professor tail: "-..."
        var c = raw.replace(Regex("-.*$"), "").trim()
        // 2) prefer building+room before inner parentheses if present: "정보과학관 21204 (김선행강의실)" -> "정보과학관 21204"
        c = c.replace(Regex("\\s*\\(.+\\)\$"), "").trim()
        return if (c.isBlank()) null else c
    }
}
