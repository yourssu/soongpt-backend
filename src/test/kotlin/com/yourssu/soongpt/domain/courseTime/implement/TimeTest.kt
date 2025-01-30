package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidTimeFormatException
import org.junit.jupiter.api.*

class TimeTest {
    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class 생성자는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 하루를_분으로_환산한_값의_범위가_아니면 {
            @Test
            @DisplayName("InvalidTimeFormat 예외를 반환한다.")
            fun failure() {
                assertThrows<InvalidTimeFormatException> { Time(1440) }
            }
        }
    }
}