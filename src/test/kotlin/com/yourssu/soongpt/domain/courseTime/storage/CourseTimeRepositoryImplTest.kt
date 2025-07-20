package com.yourssu.soongpt.domain.courseTime.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.CourseTimeFixture.MONDAY_17_19
import com.yourssu.soongpt.common.support.fixture.CourseTimeFixture.TUESDAY_17_19
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ApplicationTest
class CourseTimeRepositoryImplTest {
    @Autowired
    private lateinit var courseTimeRepository: CourseTimeRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findAllByCourseId_메서드는 {
        private val courseId = 1L
        @BeforeEach
        fun setUp() {
            courseTimeRepository.save(TUESDAY_17_19.toDomain(courseId))
            courseTimeRepository.save(MONDAY_17_19.toDomain(courseId))
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 과목_아이디를_받으면 {
            @Test
            @DisplayName("수강 시간 목록을 반환한다.")
            fun success() {
                val courseTimes = courseTimeRepository.findAllByCourseId(courseId)
                assertThat(courseTimes).hasSize(2)
            }
        }
    }
}
