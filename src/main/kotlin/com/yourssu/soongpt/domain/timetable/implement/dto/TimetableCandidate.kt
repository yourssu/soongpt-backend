package com.yourssu.soongpt.domain.timetable.implement.dto

import java.util.*

data class TimetableCandidate(
    val codes: List<Long>,
    val timeSlot: BitSet,
) {
    companion object {
        fun from(codes: List<Long>, timeSlot: BitSet): TimetableCandidate {
            return TimetableCandidate(codes, timeSlot)
        }
    }
}
