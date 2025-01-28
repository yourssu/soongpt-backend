package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.TargetFixture.TARGET1
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class TargetRepositoryImplTest {
    @Autowired
    private lateinit var targetRepository: TargetRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findAllByCourseId_메서드는 {
        val courseId = 1L

        @BeforeEach
        fun setUp() {
            targetRepository.save(TARGET1.toDomain(departmentGradeId = 1L, courseId = courseId))
            targetRepository.save(TARGET1.toDomain(departmentGradeId = 2L, courseId = courseId))
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 과목_아이디를_받으면 {
            @Test
            @DisplayName("과목에 해당하는 모든 수강 대상을 반환한다.")
            fun success() {
                val targets = targetRepository.findAllByCourseId(courseId)

                assertThat(targets).hasSize(2)
            }
        }
    }
}
