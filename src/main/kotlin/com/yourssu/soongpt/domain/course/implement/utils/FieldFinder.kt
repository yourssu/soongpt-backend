package com.yourssu.soongpt.domain.course.implement.utils

object FieldFinder {
    fun findFieldBySchoolId(field: String, schoolId: Int): String {
        val allEntries = field.split("\n")
            .mapNotNull { line -> parseFieldEntry(line) }
        
        if (allEntries.isEmpty()) {
            return ""
        }

        val matchingEntries = allEntries.filter { entry -> schoolId in entry.yearRange }

        if (matchingEntries.isNotEmpty()) {
            val entryWithHighestYear = matchingEntries.maxBy { entry -> entry.yearRange.first }
            return entryWithHighestYear.fieldName
        }

        // 일치하는 연도가 없을 때 가장 최근 연도의 필드를 반환
        val mostRecentEntry = allEntries.maxBy { entry -> entry.yearRange.last }
        return mostRecentEntry.fieldName
    }
    
    private fun parseFieldEntry(line: String): FieldEntry? {
        val yearRange = parseYearRange(line) ?: return null
        val fieldName = parseFieldName(line)
        
        return if (fieldName.isNotBlank()) {
            FieldEntry(yearRange, fieldName)
        } else null
    }
    
    private fun parseYearRange(line: String): IntRange? {
        return when {
            line.contains("이후") -> {
                val match = Regex("\\['{0,2}(\\d{2})이후").find(line)
                match?.let { it.groupValues[1].toInt()..MAX_YEAR }
            }
            line.contains("이전") -> {
                val match = Regex("\\['{0,2}(\\d{2})이전").find(line)
                match?.let { MIN_YEAR..it.groupValues[1].toInt() }
            }
            line.contains("~") -> {
                val allNumbers = Regex("(\\d{2})").findAll(line).map { it.groupValues[1].toInt() }.toList()
                if (allNumbers.size >= 2) {
                    allNumbers.min()..allNumbers.max()
                } else null
            }
            Regex("\\['{0,2}\\d{2}-'{0,2}\\d{2}'{0,2}\\]").containsMatchIn(line) -> {
                val match = Regex("\\['{0,2}(\\d{2})-'{0,2}(\\d{2})").find(line)
                match?.let { it.groupValues[1].toInt()..it.groupValues[2].toInt() }
            }
            else -> {
                val match = Regex("'{0,2}(\\d{2})\\]").find(line)
                match?.let {
                    val year = it.groupValues[1].toInt()
                    year..year 
                }
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
        val fieldName: String
    )
    
    private const val MIN_YEAR = 0
    private const val MAX_YEAR = 99
}
