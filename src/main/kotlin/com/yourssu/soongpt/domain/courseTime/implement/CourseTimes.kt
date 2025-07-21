package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidParseFormatException

class CourseTimes(
    private val values: List<CourseTime>
) {
    companion object {
        fun parseScheduleRoom(scheduleRoom: String): CourseTimes {
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
            val pattern = Regex("""([월화수목금토일\s]+)\s+(\d{1,2}:\d{2})-(\d{1,2}:\d{2})\s+\((.+)\)""")
            val result = pattern.find(entry.trim()) ?: return emptyList()

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
                val classroom = classroomStr.trim()

                return weeks.map { week ->
                    CourseTime(
                        week = week,
                        startTime = startTime,
                        endTime = endTime,
                        classroom = classroom
                    )
                }
            } catch (e: Exception) {
                throw InvalidParseFormatException();
            }


        }
    }

    fun toList(): List<CourseTime> = values.toList()

    fun isEmpty(): Boolean = values.isEmpty()

    fun size(): Int = values.size
}
