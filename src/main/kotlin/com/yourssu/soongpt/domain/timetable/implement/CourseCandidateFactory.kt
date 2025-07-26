package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.implement.dto.CompressTime
import com.yourssu.soongpt.domain.timetable.implement.dto.CompressTimes
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component
import java.util.*

@Component
class CourseCandidateFactory {
    companion object {
        private const val WEEKLY_MINUTES = 24 * 7 * 60
        private const val TIMESLOT_UNIT_MINUTES = 5
        private const val TIMESLOT_SIZE = WEEKLY_MINUTES / TIMESLOT_UNIT_MINUTES
    }

    fun create(course: Course): CourseCandidate {
        val courseTimes = CourseTimes.from(course.scheduleRoom)
        val compressTimes = compressTimes(courseTimes)
        val timeSlot = generateTimeSlot(compressTimes)
        return CourseCandidate.from(course.code, timeSlot)
    }

    private fun compressTimes(
        courseTimes: CourseTimes
    ): CompressTimes {
        val compressedTimes = courseTimes.toList().map { courseTime ->
            CompressTime.from(courseTime)
        }
        return CompressTimes.from(compressedTimes)
    }

    private fun generateTimeSlot(
        compressTimes: CompressTimes
    ): BitSet {
        val timeSlot = BitSet(TIMESLOT_SIZE)
        for (time in compressTimes.times) {
            val startIndex = time.compressedStartTime
            val endIndex = time.compressedEndTime
            timeSlot.set(startIndex, endIndex)
        }
        return timeSlot
    }
}