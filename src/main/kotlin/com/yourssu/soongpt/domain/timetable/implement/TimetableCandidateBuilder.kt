package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import java.util.*

class TimetableCandidateBuilder(
    initialCodes: List<Long> = emptyList(),
    initialTimeSlot: BitSet = BitSet(TIMESLOT_SIZE),
    private val pinnedTag: Tag = Tag.DEFAULT
) {
    private val codes: MutableList<Long> = initialCodes.toMutableList()
    private val timeSlot: BitSet = initialTimeSlot.clone() as BitSet
    private var points: Int = 0

    fun intersects(otherTimeSlot: BitSet): Boolean {
        return this.timeSlot.intersects(otherTimeSlot)
    }

    fun add(course: CourseCandidate): Boolean {
        if (intersects(course.timeSlot)) return false

        val simulated = timeSlot.clone() as BitSet
        simulated.or(course.timeSlot)

        if (!pinnedTag.strategy.isCorrect(simulated)) return false

        codes += course.code
        timeSlot.or(course.timeSlot)
        points += course.point
        return true
    }

    fun remove(course: CourseCandidate) {
        if (codes.remove(course.code)) {
            timeSlot.xor(course.timeSlot)
            points -= course.point
        }
    }

    fun build(): TimetableCandidate {
        if (pinnedTag.strategy.isCorrect(timeSlot)) {
            return TimetableCandidate(
                codes    = codes.toList(),
                timeSlot = timeSlot.clone() as BitSet,
                validTags = listOf(pinnedTag),
                points   = points
            )
        } else {
            return TimetableCandidate(
                codes    = codes.toList(),
                timeSlot = timeSlot.clone() as BitSet,
                validTags = listOf(Tag.DEFAULT),
                points   = points
            )
        }
    }
}
