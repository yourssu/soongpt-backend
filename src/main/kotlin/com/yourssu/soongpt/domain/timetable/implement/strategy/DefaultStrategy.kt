package com.yourssu.soongpt.domain.timetable.implement.strategy

import java.util.*

class DefaultStrategy: TagStrategy {
    override fun isCorrect(timeSlot: BitSet): Boolean {
        return true
    }
}