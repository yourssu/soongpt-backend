package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Category

enum class SwapTrack {
    MAJOR_ELECTIVE,
    DOUBLE_MAJOR,
    MINOR,
    TEACHING,
    ;

    fun toCourseCategory(): Category {
        return when (this) {
            TEACHING -> Category.TEACHING
            MAJOR_ELECTIVE, DOUBLE_MAJOR, MINOR -> Category.MAJOR_ELECTIVE
        }
    }
}
