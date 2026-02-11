package com.yourssu.soongpt.domain.course.implement.utils

object FieldFinder {
    fun findFieldBySchoolId(field: String, schoolId: Int): String {
        val allEntries = field
            .split("\n")
            .mapNotNull { line -> parseFieldEntry(line) }

        if (allEntries.isEmpty()) return ""

        val matchingEntries = allEntries.filter { entry -> schoolId in entry.yearRange }

        // 여러 엔트리가 매칭되면(예: '22이후', '23이후' 둘 다) 시작 연도가 더 큰 쪽 우선
        if (matchingEntries.isNotEmpty()) {
            val entryWithHighestYear = matchingEntries.maxBy { entry -> entry.yearRange.first }
            return normalizeFieldName(entryWithHighestYear.fieldName)
        }

        // 일치하는 연도가 없을 때 가장 최근 연도의 필드를 반환
        val mostRecentEntry = allEntries.maxBy { entry -> entry.yearRange.last }
        return normalizeFieldName(mostRecentEntry.fieldName)
    }

    /**
     * 접두어(실제분야명) 형태면 괄호 안만 반환.
     * 예: 품격(글로벌시민의식) -> 글로벌시민의식
     * 괄호가 없으면 원본 그대로 반환.
     */
    private fun normalizeFieldName(fieldName: String): String {
        if (fieldName.isBlank()) return fieldName
        val trimmed = fieldName.trim()
        val match = Regex(".*\\(([^)]+)\\)\\s*$").find(trimmed)
        return match?.groupValues?.get(1)?.trim() ?: trimmed
    }

    private fun parseFieldEntry(line: String): FieldEntry? {
        val yearRange = parseYearRange(line) ?: return null
        val fieldName = parseFieldName(line)

        return if (fieldName.isNotBlank()) FieldEntry(yearRange, fieldName) else null
    }

    private fun parseYearRange(rawLine: String): IntRange? {
        // 실제 데이터에 공백/따옴표/리스트 prefix("- ")가 섞여있는 케이스 정규화
        val line = rawLine.replace(" ", "")

        return when {
            line.contains("이후") -> {
                Regex("(\\d{2})(?=이후)").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { it..MAX_YEAR }
            }

            line.contains("이전") -> {
                Regex("(\\d{2})(?=이전)").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { MIN_YEAR..it }
            }

            line.contains("~") -> {
                val nums = Regex("(\\d{2})")
                    .findAll(line)
                    .mapNotNull { it.groupValues[1].toIntOrNull() }
                    .toList()
                if (nums.size >= 2) nums.min()..nums.max() else null
            }

            line.contains("-") -> {
                val match = Regex("(\\d{2})-('?)(\\d{2})").find(line)
                match?.let {
                    val a = it.groupValues[1].toIntOrNull()
                    val b = it.groupValues[3].toIntOrNull()
                    if (a != null && b != null) a..b else null
                }
            }

            else -> {
                Regex("(\\d{2})(?=\\])").find(line)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull()
                    ?.let { it..it }
            }
        }
    }

    private fun parseFieldName(line: String): String {
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
