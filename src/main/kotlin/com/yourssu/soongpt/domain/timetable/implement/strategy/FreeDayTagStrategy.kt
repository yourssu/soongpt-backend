package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import java.util.*

class FreeDayTagStrategy : TagStrategy {
    override fun isCorrect(timeSlot: BitSet): Boolean {
        for (day in Week.weekdays()) {
            val startSlot = day.ordinal * TIMESLOT_DAY_RANGE
            val endSlot = startSlot + TIMESLOT_DAY_RANGE

            if (timeSlot.get(startSlot, endSlot).isEmpty) {
                return true
            }
        }
        return false
    }
}
