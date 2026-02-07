package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_UNIT_MINUTES
import java.util.*

class NoEveningClassesStrategy: TagStrategy {
    companion object {
        const val EVENING_START = (18 * 60 + 30) / TIMESLOT_UNIT_MINUTES
        const val EVENING_RANGE = (5 * 60 + 30) / TIMESLOT_UNIT_MINUTES
    }
    override fun isCorrect(timeSlot: BitSet): Boolean {
        for (day in Week.weekdays()) {
            val dayStart = day.ordinal * TIMESLOT_DAY_RANGE
            val startSlot = dayStart + EVENING_START
            val endSlot = startSlot + EVENING_RANGE

            if (timeSlot.get(startSlot, endSlot).isEmpty) {
                return true
            }
        }
        return false
    }
}