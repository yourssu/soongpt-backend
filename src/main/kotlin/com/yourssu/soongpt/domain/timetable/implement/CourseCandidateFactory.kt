package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.dto.CompressTime
import com.yourssu.soongpt.domain.timetable.implement.dto.CompressTimes
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component
import java.util.*

@Component
class CourseCandidateFactory {
    fun create(course: Course): CourseCandidate {
        val courseTimes = CourseTimes.from(course.scheduleRoom)
        val compressTimes = compressTimes(courseTimes)
        val timeSlot = generateTimeSlot(compressTimes)
        val point = course.point.toDouble().toInt()
        return CourseCandidate.from(course.code, timeSlot, point)
    }

    private fun compressTimes(
        courseTimes: CourseTimes
    ): CompressTimes {
        if (courseTimes.isEmpty()) {
            return CompressTimes.from(emptyList())
        }

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