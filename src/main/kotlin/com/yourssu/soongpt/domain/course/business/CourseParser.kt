package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Component

@Component
class CourseParser(
    private val departmentReader: DepartmentReader
) {
    fun parseClassifications(input: String): Map<Classification, List<Long>> {
        val mapping = mutableMapOf<Classification, MutableList<Long>>()
        val groups = input.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        for (group in groups) {
            val tokens = group.split("-").map { it.trim() }
            if (tokens.isEmpty()) continue
            val classificationStr = tokens[0]
            val classification = when (classificationStr) {
                "전필", "전기" -> Classification.MAJOR_REQUIRED
                "전선" -> Classification.MAJOR_ELECTIVE
                "교필" -> Classification.GENERAL_REQUIRED
                "교선" -> Classification.GENERAL_ELECTIVE
                "채플" -> Classification.CHAPEL
                else -> continue
            }
            val deptIds = if (tokens.size > 1) {
                departmentReader.getMatchingDepartments(tokens[1]).mapNotNull { it.id }
            } else {
                emptyList()
            }
            mapping.getOrPut(classification) { mutableListOf() }.addAll(deptIds)
        }
        return mapping
    }

    fun parseProfessorNames(professor: String?): String {
        return professor?.split("\n")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.joinToString(", ")
            ?.let { "$it 교수님" }
            ?: ""
    }

    fun parseCredit(timePoints: String?): Int {
        if (timePoints.isNullOrEmpty()) throw IllegalArgumentException("time_points 값이 비어있음")
        val parts = timePoints.split("/")
        return parts[1].toDoubleOrNull()?.toInt() ?: 0
    }

    fun parseCourseTimes(scheduleRoom: String, courseId: Long): List<CourseTime> {
        val courseTimes = mutableListOf<CourseTime>()
        val schedules = scheduleRoom.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        schedules.forEach { sch ->
            try {
                val tokens = sch.split(" ").filter { it.isNotEmpty() }

                val timePattern = Regex("\\d{1,2}:\\d{2}-\\d{1,2}:\\d{2}")
                val timeTokenIndex = tokens.indexOfFirst { token -> timePattern.matches(token) }
                if (timeTokenIndex == -1) {
                    throw IllegalArgumentException("시간 범위 형식 오류 : $sch")
                }

                val dayTokens = tokens.subList(0, timeTokenIndex)
                if (dayTokens.isEmpty()) {
                    throw IllegalArgumentException("요일 정보 누락 : $sch")
                }

                val timeToken = tokens[timeTokenIndex]
                val times = timeToken.split("-")
                if (times.size != 2) {
                    throw IllegalArgumentException("시간 범위 형식 오류 : $sch")
                }
                val startTime = Time.of(times[0])
                val endTime = Time.of(times[1])

                val classroom = parseClassroom(sch)

                dayTokens.forEach { day ->
                    val week = parseWeek(day)
                    val courseTime = CourseTime(
                        week = week,
                        startTime = startTime,
                        endTime = endTime,
                        classroom = classroom,
                        courseId = courseId
                    )
                    courseTimes.add(courseTime)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("schedule_room 파싱 실패 : $sch / ${e.message}")
            }
        }
        return courseTimes
    }

    fun parseTarget(input: String): List<ParsedTarget> {
        val results = mutableListOf<ParsedTarget>()
        if (input.trim().isEmpty()) return results

        val lines = input.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val gradeRegex = Regex("^(전체학년|전체|(\\d+)학년)") // ex. group 0: 1학년, group 1: 1학년, group 2: 1
        val exclusionRegex = Regex("^(.*)\\((.*제외.*)\\)$")
        for (line in lines) {
            var grade = 0
            var remaining = line
            val gradeMatch = gradeRegex.find(line)
            if (gradeMatch != null) {
                val gradePart = gradeMatch.value.trim()
                remaining = line.substring(gradeMatch.range.last + 1).trim()
                grade = if (gradePart.equals("전체학년") || gradePart.equals("전체")) {
                    0
                } else {
                    gradeMatch.groupValues[2].toIntOrNull() ?: 0
                }
            }

            // ex. 1학년 -> 학과는 전체학과로 처리
            if (remaining.isEmpty()) {
                results.add(ParsedTarget(grade, setOf("전체"), emptySet()))
                continue
            }

            var includedStr = remaining
            var excludedStr = remaining
            val exclusionMatch = exclusionRegex.find(remaining)
            if (exclusionMatch != null) { // group 0: (중문 제외), group 1: "", group 2: 중문 제외
                includedStr = exclusionMatch.groupValues[1].trim()
                excludedStr = exclusionMatch.groupValues[2].trim().replace("제외", "").trim()
            }

            val includedDepartments = if (includedStr.isEmpty() || includedStr.equals("전체")) {
                setOf("전체")
            } else {
                includedStr.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
            }

            val excludedDepartments = if (exclusionMatch != null) {
                excludedStr.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
            } else {
                emptySet()
            }

            results.add(ParsedTarget(grade, includedDepartments, excludedDepartments))
        }
        return results
    }

    private fun parseWeek(day: String): Week {
        return when(day) {
            "월" -> Week.MONDAY
            "화" -> Week.TUESDAY
            "수" -> Week.WEDNESDAY
            "목" -> Week.THURSDAY
            "금" -> Week.FRIDAY
            "토" -> Week.SATURDAY
            "일" -> Week.SUNDAY
            else -> throw IllegalArgumentException("알 수 없는 요일 : $day")
        }
    }

    private fun parseClassroom(schedule: String): String? {
        val startIdx = schedule.indexOf("(")
        val endIdx = schedule.indexOf(")")
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            val content = schedule.substring(startIdx + 1, endIdx)
            // 만약 내용에 "("가 포함된다면, 그 앞부분만 사용
            // ex. (정보과학관 21203 (김재상강의실)-김익수)
            val roomPart = content.split("(").first()
            return roomPart.split("-").first().trim()
        }
        return null
    }
}