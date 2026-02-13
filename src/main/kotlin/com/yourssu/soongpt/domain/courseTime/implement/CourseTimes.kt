package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidParseFormatException

class CourseTimes(
    private val values: List<CourseTime>
) {
    companion object {
        private val SCHEDULE_PATTERN = Regex("""([월화수목금토일\s]+)\s+(\d{1,2}:\d{2})-(\d{1,2}:\d{2})\s+\((.+)\)""")
        fun from(scheduleRoom: String): CourseTimes {
            if (scheduleRoom.isBlank()) return CourseTimes(emptyList())

            val courseTimes = scheduleRoom.split("\n")
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .flatMap { parseScheduleEntry(it) }
                .toList()

            return CourseTimes(courseTimes)
        }

        private fun parseScheduleEntry(entry: String): List<CourseTime> {
            val result = SCHEDULE_PATTERN.find(entry.trim()) ?: return emptyList()

            try {
                val (weeksStr, startTimeStr, endTimeStr, classroomStr) = result.destructured
                val weeks = weeksStr
                    .trim()
                    .split("\\s+".toRegex())
                    .map { Week.fromName(it) }

                if (weeks.isEmpty()) {
                    throw InvalidParseFormatException()
                }

                val startTime = Time.of(startTimeStr)
                val endTime = Time.of(endTimeStr)

                // 괄호 안 문자열은 대개 "강의실-교수" 형식이다.
                // - 강의실이 없는 케이스: "(-교수)" -> null 로 파싱
                // - 교수명은 마지막 '-' 이후로 간주하고 제거
                val classroom = classroomStr
                    .trim()
                    .substringBeforeLast("-", classroomStr.trim())
                    .trim()
                    .let { room ->
                        if (room.isBlank() || room == "-") null else room
                    }

                return weeks.map { week ->
                    CourseTime(
                        week = week,
                        startTime = startTime,
                        endTime = endTime,
                        classroom = classroom
                    )
                }
            } catch (e: Exception) {
                throw InvalidParseFormatException()
            }
        }
    }

    fun toList(): List<CourseTime> = values.toList()

    fun isEmpty(): Boolean = values.isEmpty()

    fun size(): Int = values.size
}
