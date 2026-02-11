package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_UNIT_MINUTES
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoLongBreaksStrategy
import org.springframework.stereotype.Component
import java.util.*

@Component
class TimetableRanker {
    fun rank(candidates: List<TimetableCandidate>): List<TimetableCandidate> {
        if (candidates.isEmpty()) {
            return emptyList()
        }

        return candidates.sortedWith(
                compareByDescending<TimetableCandidate> { totalScore(it) }
                        .thenBy { getCompactnessScore(it.timeSlot) }
                        .thenByDescending { it.points }
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun rankByPreference(candidates: List<TimetableCandidate>, tag: Tag): List<TimetableCandidate> {
        if (candidates.isEmpty()) {
            return emptyList()
        }

        return candidates.sortedWith(
            compareByDescending<TimetableCandidate> { totalScore(it) }
                .thenBy { getCompactnessScore(it.timeSlot) }
        )
    }

    fun preferenceScore(timeSlot: BitSet): Int {
        return getPreferenceScore(timeSlot)
    }

    fun totalScore(candidate: TimetableCandidate): Int {
        val rawScore = getTagScore(candidate) + getPreferenceScore(candidate.timeSlot)
        return maxOf(0, rawScore)
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

    private fun getPreferenceScore(timeSlot: BitSet): Int {
        var score = 0

        score += getFreeDayScore(timeSlot)
        if (hasGuaranteedLunch(timeSlot)) score += 15
        if (hasNoMorningClasses(timeSlot)) score += 10
        if (hasNoEveningClasses(timeSlot)) score += 10
        if (!hasNoLongBreaks(timeSlot)) score -= 20

        return score
    }

    private fun getFreeDayScore(timeSlot: BitSet): Int {
        var score = 0
        for (dayIndex in WEEKDAY_RANGE) {
            val dayMask = DAY_MASKS[dayIndex]
            if (!timeSlot.intersects(dayMask)) {
                score += if (dayIndex == 0 || dayIndex == WEEKDAYS - 1) 20 else 10
            }
        }
        return score
    }

    private fun hasNoMorningClasses(timeSlot: BitSet): Boolean {
        for (dayIndex in WEEKDAY_RANGE) {
            if (timeSlot.intersects(MORNING_MASKS[dayIndex])) {
                return false
            }
        }
        return true
    }

    private fun hasNoEveningClasses(timeSlot: BitSet): Boolean {
        for (dayIndex in WEEKDAY_RANGE) {
            if (timeSlot.intersects(EVENING_MASKS[dayIndex])) {
                return false
            }
        }
        return true
    }

    private fun hasGuaranteedLunch(timeSlot: BitSet): Boolean {
        for (dayIndex in WEEKDAY_RANGE) {
            var lunchFound = false
            for (mask in LUNCH_SLOT_MASKS[dayIndex]) {
                if (!timeSlot.intersects(mask)) {
                    lunchFound = true
                    break
                }
            }
            if (!lunchFound) return false
        }
        return true
    }

    private fun hasNoLongBreaks(timeSlot: BitSet): Boolean {
        return NoLongBreaksStrategy().isCorrect(timeSlot)
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

private const val WEEKDAYS = 5

private val WEEKDAY_RANGE = 0 until WEEKDAYS

private const val MORNING_END_SLOT = (11 * 60) / TIMESLOT_UNIT_MINUTES
private const val EVENING_START_SLOT = (18 * 60 + 30) / TIMESLOT_UNIT_MINUTES
private const val EVENING_RANGE = (5 * 60 + 30) / TIMESLOT_UNIT_MINUTES

private const val LUNCH_START_SLOT = 11 * 60 / TIMESLOT_UNIT_MINUTES
private const val LUNCH_RANGE = 2 * 60 / TIMESLOT_UNIT_MINUTES
private const val LUNCH_BLOCK = 45 / TIMESLOT_UNIT_MINUTES

private val DAY_MASKS: List<BitSet> = WEEKDAY_RANGE.map { day ->
    buildMask(day * TIMESLOT_DAY_RANGE, (day + 1) * TIMESLOT_DAY_RANGE)
}

private val MORNING_MASKS: List<BitSet> = WEEKDAY_RANGE.map { day ->
    val start = day * TIMESLOT_DAY_RANGE
    buildMask(start, start + MORNING_END_SLOT)
}

private val EVENING_MASKS: List<BitSet> = WEEKDAY_RANGE.map { day ->
    val start = day * TIMESLOT_DAY_RANGE + EVENING_START_SLOT
    buildMask(start, start + EVENING_RANGE)
}

private val LUNCH_SLOT_MASKS: List<List<BitSet>> = WEEKDAY_RANGE.map { day ->
    val dayStart = day * TIMESLOT_DAY_RANGE
    val start = dayStart + LUNCH_START_SLOT
    val end = start + LUNCH_RANGE
    (start until end - LUNCH_BLOCK).map { slot ->
        buildMask(slot, slot + LUNCH_BLOCK)
    }
}

private fun buildMask(startInclusive: Int, endExclusive: Int): BitSet {
    val mask = BitSet(TIMESLOT_SIZE)
    mask.set(startInclusive, endExclusive)
    return mask
}
