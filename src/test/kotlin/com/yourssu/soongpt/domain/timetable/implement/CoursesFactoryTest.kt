package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.common.support.fixture.CourseFixture
import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.course.implement.CoursesFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class CoursesFactoryTest {
    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class generateTimetableCandidates_메서드는 {
        val courses = Courses(
            listOf(
                CourseFixture.MAJOR_REQUIRED.toDomain(),
                CourseFixture.MAJOR_ELECTIVE.toDomain(),
            )
        )

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 과목_그룹들을_받으면 {
            @Test
            @DisplayName("각 그룹에서 과목을 모두 선택한 집합들의 리스트를 반환한다.")
            fun success() {
                val coursesFactory = CoursesFactory(listOf(courses, courses))

                val actual = coursesFactory.generateTimetableCandidates().first
                assertThat(actual).hasSize(4)
            }
        }
    }
}