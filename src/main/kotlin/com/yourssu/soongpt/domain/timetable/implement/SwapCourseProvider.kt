package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import org.springframework.stereotype.Component

@Component
class SwapCourseProvider(
    private val courseReader: CourseReader,
) {
    fun findAlternatives(track: SwapTrack, userContext: UserContext): List<Course> {
        // TODO: secondary major/track별 과목 조회 로직이 준비되면 이 부분을 교체
        System.err.println("SwapCourseProvider.findAlternatives is using a mock implementation.")
        System.err.println("track: $track, userContext: $userContext")
        return courseReader.findAllBy(track.toCourseCategory(), userContext.department, userContext.grade)
    }
}
