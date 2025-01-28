package com.yourssu.soongpt.domain.departmentGrade.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeRepository
import com.yourssu.soongpt.domain.departmentGrade.storage.exception.DepartmentGradeNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class DepartmentGradeRepositoryImplTest {
    @Autowired
    private lateinit var departmentGradeRepository: DepartmentGradeRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 아이디에_해당하는_학과_학년이_없으면 {
            @Test
            @DisplayName("DepartmentGradeNotFound 예외를 던진다.")
            fun failure() {
                assertThrows<DepartmentGradeNotFoundException> { departmentGradeRepository.get(0L) }
            }
        }
    }
}