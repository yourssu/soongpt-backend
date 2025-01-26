package com.yourssu.soongpt.domain.courseTime.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeRepository
import com.yourssu.soongpt.domain.courseTime.storage.exception.CourseTimeNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ApplicationTest
class CourseTimeRepositoryImplTest {
    @Autowired
    private lateinit var courseTimeRepository: CourseTimeRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 아이디에_해당하는_강의_시간이_없으면 {
            @Test
            @DisplayName("CourseTimeNotFound 예외를 던진다.")
            fun failure() {
                assertThrows<CourseTimeNotFoundException> { courseTimeRepository.get(0L) }
            }
        }
    }
}
