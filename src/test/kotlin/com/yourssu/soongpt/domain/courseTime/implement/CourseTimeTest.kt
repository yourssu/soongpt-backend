package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidCourseTimeException
import org.junit.jupiter.api.*
import kotlin.test.Test

class CourseTimeTest {
    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class 생성자는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 시작_시간이_종료_시간보다_늦으면 {
            @Test
            @DisplayName("InvalidCourseTime 예외를 반환한다.")
            fun failure() {
                assertThrows<InvalidCourseTimeException> {
                    CourseTime(
                        week = Week.MONDAY,
                        startTime = Time(1000),
                        endTime = Time(900),
                        courseId = 1
                    )
                }
            }
        }
    }
}
