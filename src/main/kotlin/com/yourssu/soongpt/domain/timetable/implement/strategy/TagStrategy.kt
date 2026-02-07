package com.yourssu.soongpt.domain.timetable.implement.strategy

import java.util.*

interface TagStrategy {
    fun isCorrect(timeSlot: BitSet): Boolean
}
