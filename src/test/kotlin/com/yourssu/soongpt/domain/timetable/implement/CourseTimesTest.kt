package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.common.support.fixture.CourseTimeFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class CourseTimesTest {
    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class hasOverlappingCourseTimes_메서드는 {


        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 강의시간이_겹치지_않으면 {
            val courseTimes = CourseTimes(
                listOf(
                    CourseTimeFixture.MONDAY_17_19.toDomain(1),
                    CourseTimeFixture.TUESDAY_17_19.toDomain(2),
                )
            )
            @Test
            @DisplayName("False를 반환한다.")
            fun success() {
                assertThat(courseTimes.hasOverlappingCourseTimes()).isFalse()
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 강의시간이_겹치면 {
            val courseTimes = CourseTimes(
                listOf(
                    CourseTimeFixture.TUESDAY_18_20.toDomain(1),
                    CourseTimeFixture.TUESDAY_17_19.toDomain(2),
                )
            )
            @Test
            @DisplayName("True를 반환한다.")
            fun success() {
                assertThat(courseTimes.hasOverlappingCourseTimes()).isTrue()
            }
        }
    }
}