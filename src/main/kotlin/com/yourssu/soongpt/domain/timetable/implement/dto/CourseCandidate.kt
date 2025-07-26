package com.yourssu.soongpt.domain.timetable.implement.dto

import java.util.*

data class CourseCandidate (
    val code: Long,
    val timeSlot: BitSet,
){
    companion object {
        fun from(code: Long, timeSlot: BitSet): CourseCandidate {
            return CourseCandidate(code, timeSlot)
        }
    }
}