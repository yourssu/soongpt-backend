package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.business.dto.*
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.Timetable
import org.springframework.stereotype.Component

@Component
class LabTimetableMapper {
    private val schedulePattern = Regex("""([월화수목금토일\s]+)\s+(\d{1,2}:\d{2})-(\d{1,2}:\d{2})\s+\((.+)\)""")

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
        return timeString.toDoubleOrNull()?.toInt() ?: 0
    }

    private fun mapCourseTimes(scheduleRoom: String): List<LabCourseTime> {
        if (scheduleRoom.isBlank()) return emptyList()
        val entries = scheduleRoom.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val results = mutableListOf<LabCourseTime>()
        for (entry in entries) {
            val m = schedulePattern.find(entry) ?: continue
            val (weeksStr, start, end, rawClassroom) = m.destructured
            val weeks = weeksStr.trim().split("\\s+".toRegex())
            val classroom = normalizeClassroom(rawClassroom)
            weeks.forEach { week ->
                results.add(
                    LabCourseTime(
                        week = week,
                        start = start,
                        end = end,
                        classroom = classroom
                    )
                )
            }
        }
        return results
    }

    private fun normalizeClassroom(raw: String): String? {
        if (raw.isBlank()) return null
        // 1) remove professor tail: "-..."
        var c = raw.replace(Regex("-.*$"), "").trim()
        // 2) prefer building+room before inner parentheses if present: "정보과학관 21204 (김선행강의실)" -> "정보과학관 21204"
        c = c.replace(Regex("\\s*\\(.+\\)\$"), "").trim()
        return if (c.isBlank()) null else c
    }
}
