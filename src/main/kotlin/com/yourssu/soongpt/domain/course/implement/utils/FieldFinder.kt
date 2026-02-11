package com.yourssu.soongpt.domain.course.implement.utils

object FieldFinder {
    fun findFieldBySchoolId(field: String, schoolId: Int): String {
        val allEntries = field
            .split("\n")
            .mapNotNull { line -> parseFieldEntry(line) }

        if (allEntries.isEmpty()) return ""

        val matchingEntries = allEntries.filter { entry -> schoolId in entry.yearRange }

        // 여러 엔트리가 매칭되면(예: '22이후', '23이후' 둘 다) 시작 연도가 더 큰 쪽을 우선
        if (matchingEntries.isNotEmpty()) {
            return matchingEntries.maxBy { it.yearRange.first }.fieldName
        }

        // 일치하는 연도가 없을 때 가장 최근 연도의 필드를 반환
        return allEntries.maxBy { it.yearRange.last }.fieldName
    }

    private fun parseFieldEntry(line: String): FieldEntry? {
        val yearRange = parseYearRange(line) ?: return null
        val fieldName = parseFieldName(line)

        return if (fieldName.isNotBlank()) FieldEntry(yearRange, fieldName) else null
    }

    private fun parseYearRange(rawLine: String): IntRange? {
        // 실제 데이터에 공백/따옴표/리스트 prefix("- ")가 섞여있는 케이스가 있어 normalize 후 파싱
        val line = rawLine.replace(" ", "")

        return when {
            line.contains("이후") -> {
                // 예: ['23이후], [23이후]
                Regex("(\\d{2})(?=이후)").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { it..MAX_YEAR }
            }

            line.contains("이전") -> {
                // 예: ['22이전], [22이전]
                Regex("(\\d{2})(?=이전)").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { MIN_YEAR..it }
            }

            line.contains("~") -> {
                // 예: ['22~'23], [22~23]
                val nums = Regex("(\\d{2})").findAll(line).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
                if (nums.size >= 2) nums.min()..nums.max() else null
            }

            line.contains("-") -> {
                // 예: ['22-'23]
                val match = Regex("(\\d{2})-('?)(\\d{2})").find(line)
                match?.let {
                    val a = it.groupValues[1].toIntOrNull()
                    val b = it.groupValues[3].toIntOrNull()
                    if (a != null && b != null) a..b else null
                }
            }

            else -> {
                // 예: ['23]
                Regex("(\\d{2})(?=\\])").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { it..it }
            }
        }
    }

    private fun parseFieldName(line: String): String {
        // 마지막 ] 이후의 모든 텍스트를 필드명으로 추출
        val lastBracketIndex = line.lastIndexOf(']')
        if (lastBracketIndex != -1 && lastBracketIndex < line.length - 1) {
            return line.substring(lastBracketIndex + 1).trim()
        }

        return ""
    }

    private data class FieldEntry(
        val yearRange: IntRange,
        val fieldName: String,
    )

    private const val MIN_YEAR = 0
    private const val MAX_YEAR = 99
}

