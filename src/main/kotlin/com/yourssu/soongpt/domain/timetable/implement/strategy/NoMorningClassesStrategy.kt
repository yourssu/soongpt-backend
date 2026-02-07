package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_UNIT_MINUTES
import java.util.*

class NoMorningClassesStrategy : TagStrategy {
    companion object {
        const val MORNING_END = (11 * 60) / TIMESLOT_UNIT_MINUTES
    }
    override fun isCorrect(timeSlot: BitSet): Boolean {
        for (day in Week.weekdays()) {
            val dayStart = day.ordinal * TIMESLOT_DAY_RANGE
            val dayEnd = dayStart + MORNING_END

            if (!timeSlot.get(dayStart, dayEnd).isEmpty) {
                return false
            }
        }
        return true
    }
}