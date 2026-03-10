package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.business.dto.LabCourseItem
import com.yourssu.soongpt.domain.timetable.business.dto.LabCourseTime
import com.yourssu.soongpt.domain.timetable.business.dto.LabTimetableResponse
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
                courseTime = mapCourseTimes(course.scheduleRoom),
            )
        }
        return LabTimetableResponse(
            timetableId = timetable.id!!,
            tag = tagToLabString(timetable.tag),
            totalCredit = labCourses.sumOf { it.credit },
            courses = labCourses,
        )
    }

    /** lab.md TIME_TABLE_TAG 키만 허용; 해당 없으면 DEFAULT */
    private fun tagToLabString(tag: Tag): String {
        val labTag = when (tag) {
            Tag.DEFAULT -> "DEFAULT"
            Tag.FREE_MONDAY, Tag.FREE_TUESDAY, Tag.FREE_WEDNESDAY, Tag.FREE_THURSDAY, Tag.FREE_FRIDAY -> "HAS_FREE_DAY"
            Tag.NO_MORNING_CLASSES -> "NO_MORNING_CLASSES"
            Tag.NO_LONG_BREAKS -> "NO_LONG_BREAKS"
            Tag.GUARANTEED_LUNCH_TIME -> "GUARANTEED_LUNCH_TIME"
            Tag.NO_EVENING_CLASSES -> "NO_EVENING_CLASSES"
            else -> "DEFAULT"
        }
        return if (labTag in LAB_TAG_WHITELIST) labTag else "DEFAULT"
    }

    /** 싸강(courseTime 빈 과목) 1건을 LabCourseItem으로 변환 (courseTime = []) */
    fun toLabCourseItemForSsang(course: Course): LabCourseItem {
        return LabCourseItem(
            courseName = course.name,
            professorName = course.professor,
            classification = course.category.name,
            credit = parseCreditFromTime(course.time),
            courseTime = emptyList(),
        )
    }

    companion object {
        private val LAB_TAG_WHITELIST = setOf(
            "DEFAULT", "HAS_FREE_DAY", "NO_MORNING_CLASSES", "NO_LONG_BREAKS",
            "EVENLY_DISTRIBUTED", "GUARANTEED_LUNCH_TIME", "NO_EVENING_CLASSES",
        )
    }

    private fun parseCreditFromTime(timeString: String): Int {
        return timeString.toDoubleOrNull()?.toInt() ?: 0
    }

    private fun mapCourseTimes(scheduleRoom: String): List<LabCourseTime> {
        val courseTimes = CourseTimes.from(scheduleRoom).toList()
        return courseTimes.map { ct ->
            LabCourseTime(
                week = ct.week.displayName,
                start = ct.startTime.toTimeFormat(),
                end = ct.endTime.toTimeFormat(),
                classroom = normalizeClassroom(ct.classroom),
            )
        }
    }

    private fun normalizeClassroom(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var c = raw.replace(Regex("-.*$"), "").trim()
        c = c.replace(Regex("\\s*\\(.+\\)\\s*$"), "").trim()
        return if (c.isBlank()) null else c
    }
}
