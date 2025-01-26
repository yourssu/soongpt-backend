package com.yourssu.soongpt.domain.department.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.department.storage.exception.DepartmentNotFoundException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class DepartmentRepositoryImplTest {
 @Autowired
 private lateinit var departmentRepository: DepartmentRepository

 @Nested
 @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
 inner class get_메서드는 {
  @Nested
  @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
  inner class 아이디에_해당하는_학과가_없으면 {
   @Test
   @DisplayName("DepartmentNotFound 예외를 던진다.")
   fun failure() {
    assertThrows<DepartmentNotFoundException> { departmentRepository.get(0L) }
   }
  }
 }
}