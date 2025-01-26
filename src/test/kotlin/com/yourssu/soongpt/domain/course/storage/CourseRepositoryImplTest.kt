package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.storage.exception.CourseNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ApplicationTest
class CourseRepositoryImplTest {
    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 아이디에_해당하는_과목이_없으면 {
            @Test
            @DisplayName("CourseNotFound 예외를 던진다.")
            fun failure() {
                assertThrows<CourseNotFoundException> { courseRepository.get(0L) }
            }
        }
    }
}