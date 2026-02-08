package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component
import java.util.*

@Component
class TimetableBitsetConverter(
    private val timetableCourseReader: TimetableCourseReader,
    private val courseCandidateFactory: CourseCandidateFactory
) {
    fun convert(timetableId: Long): BitSet {
        val baseCourses = timetableCourseReader.findAllCourseByTimetableId(timetableId)
        val timetableBitSet = BitSet()
        baseCourses.forEach { course ->
            val courseCandidate = courseCandidateFactory.create(course)
            timetableBitSet.or(courseCandidate.timeSlot)
        }
        return timetableBitSet
    }
}
