package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.CourseFixture
import com.yourssu.soongpt.common.support.fixture.CourseTimeFixture
import com.yourssu.soongpt.common.support.fixture.TimetableFixture
import com.yourssu.soongpt.domain.course.implement.CourseWriter
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeWriter
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourse
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseWriter
import com.yourssu.soongpt.domain.timetable.implement.TimetableWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class TimetableServiceTest(

) {
    @Autowired
    lateinit var timetableService: TimetableService

    @Autowired
    lateinit var timetableWriter: TimetableWriter

    @Autowired
    lateinit var timetableCourseWriter: TimetableCourseWriter

    @Autowired
    lateinit var courseWriter: CourseWriter

    @Autowired
    lateinit var courseTimeWriter: CourseTimeWriter

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class getTimeTable_메서드는 {
        var timetableId: Long? = null

        @BeforeEach
        fun setUp() {
            val course = courseWriter.save(CourseFixture.MAJOR_REQUIRED.toDomain())
            val timetable = timetableWriter.save(TimetableFixture.DEFAULT.toDomain())
            courseTimeWriter.save(CourseTimeFixture.MONDAY_17_19.toDomain(course.id!!))
            timetableId = timetable.id
            timetableCourseWriter.save(TimetableCourse(timetableId = timetable.id!!, courseId = course.id!!))
        }


        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 시간표_아이디를_받으면 {
            @Test
            @DisplayName("시간표에 소속된 모든 과목을 반환한다.")
            fun success() {
                val timeTable = timetableService.getTimeTable(timetableId!!)
                val courseTime = timeTable.courses.first().courseTime

                assertThat(courseTime).hasSize(1)
            }
        }
    }
}