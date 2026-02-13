package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import java.util.*

class SpecificFreeDayStrategy(
    private val day: Week
) : TagStrategy {
    override fun isCorrect(timeSlot: BitSet): Boolean {
        val startSlot = day.ordinal * TIMESLOT_DAY_RANGE
        val endSlot = startSlot + TIMESLOT_DAY_RANGE
        return timeSlot.get(startSlot, endSlot).isEmpty
    }
}
