package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import java.util.*

class GuaranteedLunchTimeStrategy: TagStrategy {
    companion object {
        const val LUNCH_START = 11 * 60 / 5
        const val LUNCH_RANGE = 2 * 60 / 5
        const val LUNCH_TIME = 45 / 5
    }
    override fun isCorrect(timeSlot: BitSet): Boolean {
        for (day in Week.weekdays()) {
            val dayStart = day.ordinal * TIMESLOT_DAY_RANGE
            val startSlot = dayStart + LUNCH_START
            val endSlot = startSlot + LUNCH_RANGE

            var lunchFound = false
            for (slot in startSlot until endSlot - LUNCH_TIME) {
                if (timeSlot.get(slot, slot + LUNCH_TIME).isEmpty) {
                    lunchFound = true
                    break
                }
            }

            if (!lunchFound) {
                return false
            }
        }
        return true
    }
}