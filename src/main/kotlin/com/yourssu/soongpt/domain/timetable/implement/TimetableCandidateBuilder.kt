package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import java.util.*

class TimetableCandidateBuilder(
    initialCodes: List<Long> = emptyList(),
   initialTimeSlot: BitSet = BitSet(TIMESLOT_SIZE)
) {
    private val codes: MutableList<Long> = initialCodes.toMutableList()
    private val timeSlot: BitSet = initialTimeSlot.clone() as BitSet
    private var points: Int = 0

    fun intersects(otherTimeSlot: BitSet): Boolean {
        return this.timeSlot.intersects(otherTimeSlot)
    }

    fun add(course: CourseCandidate): Boolean {
        if (intersects(course.timeSlot)) {
            return false
        }
        codes.add(course.code)
        timeSlot.or(course.timeSlot)
        points += course.point
        return true
    }

    fun remove(course: CourseCandidate) {
        codes.removeLast()
        timeSlot.xor(course.timeSlot)
        points -= course.point
    }

    fun build(): TimetableCandidate {
        val finalTags = Tag.entries.filter { tag ->
            tag.strategy.isCorrect(this.timeSlot)
        }

        return TimetableCandidate(
            codes = this.codes.toList(),
            timeSlot = this.timeSlot,
            validTags = finalTags,
            points = this.points
        )
    }
}
