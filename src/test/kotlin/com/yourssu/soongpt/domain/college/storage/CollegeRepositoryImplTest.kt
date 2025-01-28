package com.yourssu.soongpt.domain.college.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.college.implement.CollegeRepository
import com.yourssu.soongpt.domain.college.storage.exception.CollegeNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ApplicationTest
class CollegeRepositoryImplTest {
    @Autowired
    private lateinit var collegeRepository: CollegeRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 아이디에_해당하는_단과대가_없으면 {
            @Test
            @DisplayName("CollegeNotFound 예외를 던진다.")
            fun failure() {
                assertThrows<CollegeNotFoundException> { collegeRepository.get(0L) }
            }
        }
    }
}