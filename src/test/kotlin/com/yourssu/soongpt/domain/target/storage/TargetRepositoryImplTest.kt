package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.exception.TargetNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class TargetRepositoryImplTest {
    @Autowired
    private lateinit var targetRepository: TargetRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 아이디에_해당하는_수강_대상이_없으면 {
            @Test
            @DisplayName("TargetNotFound 예외를 던진다.")
            fun failure() {
                assertThrows<TargetNotFoundException> { targetRepository.get(0L) }
            }
        }
    }
}