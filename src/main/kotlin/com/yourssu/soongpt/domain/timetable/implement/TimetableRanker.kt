package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.rating.implement.Rating
import com.yourssu.soongpt.domain.rating.implement.RatingReader
import com.yourssu.soongpt.domain.timetable.business.dto.SuggestionCandidate
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import java.util.BitSet
import kotlin.collections.List
import org.springframework.stereotype.Component

@Component
class TimetableRanker(private val ratingReader: RatingReader) {
    fun rank(candidates: List<TimetableCandidate>): List<TimetableCandidate> {
        if (candidates.isEmpty()) {
            return emptyList()
        }

        val allCourseCodes = candidates.flatMap { it.codes }.distinct()
        val ratingsMap = ratingReader.findAllByCourseCodes(allCourseCodes).associateBy { it.code }

        return candidates.sortedWith(
                compareByDescending<TimetableCandidate> { getTagScore(it) }
                        .thenByDescending { getAverageRating(it, ratingsMap) }
                        .thenBy { getCompactnessScore(it.timeSlot) }
                        .thenByDescending { it.points }
        )
    }

    fun rankSuggestions(suggestions: List<SuggestionCandidate>): List<SuggestionCandidate> {
        if (suggestions.isEmpty()) {
            return emptyList()
        }

        val allCourseCodes = suggestions.flatMap { it.resultingTimetableCandidate.codes }.distinct()
        val ratingsMap = ratingReader.findAllByCourseCodes(allCourseCodes).associateBy { it.code }

        return suggestions.sortedWith(
                compareByDescending<SuggestionCandidate> {
                    getTagScore(it.resultingTimetableCandidate)
                }
                        .thenByDescending {
                            getAverageRating(it.resultingTimetableCandidate, ratingsMap)
                        }
                        .thenBy { getCompactnessScore(it.resultingTimetableCandidate.timeSlot) }
                        .thenByDescending { it.resultingTimetableCandidate.points }
        )
    }

    private fun getTagScore(candidate: TimetableCandidate): Int {
        return candidate.validTags.sumOf { tag ->
            when (tag) {
                Tag.HAS_FREE_DAY -> 30
                Tag.GUARANTEED_LUNCH_TIME -> 20
                Tag.NO_MORNING_CLASSES -> 15
                Tag.NO_LONG_BREAKS -> 15
                Tag.NO_EVENING_CLASSES -> 10
                else -> 5
            }.toInt()
        }
    }

    private fun getAverageRating(
            candidate: TimetableCandidate,
            ratingsMap: Map<Long, Rating>
    ): Double {
        if (candidate.codes.isEmpty()) return 0.0
        val totalRating =
                candidate.codes.sumOf { code ->
                    ratingsMap[code]?.star ?: 3.0 // 평점 정보가 없으면 중간값 3.0으로 계산
                }
        return totalRating / candidate.codes.size
    }

    private fun getCompactnessScore(timeSlot: BitSet): Int {
        var totalSpan = 0
        for (day in 0 until 5) { // 월요일부터 금요일까지
            val dayStart = day * TIMESLOT_DAY_RANGE
            val dayEnd = dayStart + TIMESLOT_DAY_RANGE

            val firstClass = timeSlot.nextSetBit(dayStart)
            val lastClass = timeSlot.previousSetBit(dayEnd - 1)

            if (firstClass != -1 && firstClass < dayEnd) {
                totalSpan += (lastClass - firstClass)
            }
        }
        return totalSpan
    }
}
