package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.TimetableCandidateBuilder
import java.util.*

data class TimetableCandidate(
    val codes: List<Long>,
    val timeSlot: BitSet,
    val validTags: List<Tag>,
    val points: Int = 0,
) {
    companion object {
        fun from(codes: List<Long>, timeSlot: BitSet): TimetableCandidate {
            return TimetableCandidate(codes, timeSlot, listOf(), 0)
        }

    }
    fun toBuilders(): List<TimetableCandidateBuilder> {
        if (validTags.isNotEmpty()) {
            return validTags.map { TimetableCandidateBuilder(codes, timeSlot, it) }
        } else {
            return listOf()
        }
    }
}