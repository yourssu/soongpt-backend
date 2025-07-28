package com.yourssu.soongpt.domain.timetable.implement.dto

import java.util.*

data class CourseCandidate (
    val code: Long,
    val timeSlot: BitSet,
    val point: Int,
){
    companion object {
        fun from(code: Long, timeSlot: BitSet, point: Int): CourseCandidate {
            return CourseCandidate(code, timeSlot, point)
        }
    }
}