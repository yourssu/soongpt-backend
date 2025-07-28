package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.dto.CompressTime
import java.util.*
import kotlin.math.abs

class NoLongBreaksStrategy: TagStrategy {
    companion object {
        const val BREAKS_MINUTE = 60
    }

    override fun isCorrect(timeSlot: BitSet): Boolean {
        for (day in 0 until 5) {
            val breakList = mutableListOf<CompressTime>()
            val dayStart = day * TIMESLOT_DAY_RANGE
            val dayEnd = (day + 1) * TIMESLOT_DAY_RANGE

            var currentIndex = dayStart
            while (currentIndex < dayEnd) {
                val breakStart = timeSlot.nextClearBit(currentIndex)
                if (breakStart >= dayEnd) break

                var breakEnd = timeSlot.nextSetBit(breakStart)
                if (breakEnd == -1 || breakEnd > dayEnd) {
                    breakEnd = dayEnd
                }

                breakList.add(CompressTime(breakStart, breakEnd))
                currentIndex = breakEnd
            }

            if (breakList.isNotEmpty()) {
                breakList.removeFirst()
            }
            if (breakList.isNotEmpty()) {
                breakList.removeLast()
            }

            for (breakTime in breakList) {
                val breakDuration = abs(breakTime.compressedEndTime - breakTime.compressedStartTime)
                if (breakDuration >= BREAKS_MINUTE / 5) {
                    return false
                }
            }
        }
        return true
    }
}