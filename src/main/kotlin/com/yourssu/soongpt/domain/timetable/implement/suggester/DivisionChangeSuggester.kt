package com.yourssu.soongpt.domain.timetable.implement.suggester

import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.SuggestionCandidate
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component

@Component
class DivisionChangeSuggester(private val courseReader: CourseReader) {
    fun suggest(
            primary: TimetableCandidate,
            alternatives: List<TimetableCandidate>
    ): List<SuggestionCandidate> {
        val suggestions = mutableListOf<SuggestionCandidate>()
        val primaryCourseIds = primary.codes.toSet()

        // 최대 10개 시간표 분석
        for (alternative in alternatives.take(10)) {
            val alternativeCourseIds = alternative.codes.toSet()

            // 1등 시간표와 비교하여 변경된 분반 ID를 찾음
            val removedCourseId = primaryCourseIds.minus(alternativeCourseIds).firstOrNull()
            val addedCourseId = alternativeCourseIds.minus(primaryCourseIds).firstOrNull()

            if (removedCourseId != null && addedCourseId != null) {
                // 변경 전/후 분반 정보를 한 번에 조회
                val courses =
                        courseReader.findAllByCode(listOf(removedCourseId, addedCourseId))
                                .associateBy { it.id }
                val fromCourse = courses[removedCourseId]
                val toCourse = courses[addedCourseId]

                if (fromCourse != null && toCourse != null) {
                    // 1등 시간표에 비해 새로 생긴 태그가 있는지 확인
                    val newTags = alternative.validTags.filterNot { primary.validTags.contains(it) }
                    val improvement =
                            if (newTags.isNotEmpty()) {
                                " '${newTags.first().description}' 효과를 얻을 수 있습니다."
                            } else {
                                " 다른 시간표를 만들 수 있습니다."
                            }

                    val description =
                            "'${fromCourse.name}'을(를) '${getDivisionName(fromCourse.division)}'에서 '${getDivisionName(toCourse.division)}'(으)로 변경하여$improvement"

                    suggestions.add(SuggestionCandidate(alternative, description))
                }
            }
        }
        return suggestions
    }

    private fun getDivisionName(division: String?): String {
        return when (division) {
            "01" -> "가반"
            "02" -> "나반"
            "03" -> "다반"
            "04" -> "라반"
            "05" -> "마반"
            "06" -> "바반"
            "07" -> "사반"
            else -> "${division}반"
        }
    }
}
