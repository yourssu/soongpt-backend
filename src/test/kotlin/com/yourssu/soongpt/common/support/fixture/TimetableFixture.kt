package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.Timetable

enum class TimetableFixture(
    val tag: Tag,
) {
    DEFAULT(Tag.DEFAULT),
    ;

    fun toDomain() = Timetable(
        tag = tag,
    )
}