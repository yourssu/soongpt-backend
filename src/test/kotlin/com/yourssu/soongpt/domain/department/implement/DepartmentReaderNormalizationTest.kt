package com.yourssu.soongpt.domain.department.implement

import com.yourssu.soongpt.common.util.DepartmentNameNormalizer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DepartmentReaderNormalizationTest {

    @AfterEach
    fun tearDown() {
        DepartmentNameNormalizer.resetForTest()
    }

    @Test
    fun `getByName resolves alias before repository lookup`() {
        DepartmentNameNormalizer.initialize(
            canonicalDepartments = setOf("전자정보공학부 IT융합전공"),
            aliases = mapOf("전자정보공학부(IT융합전공)" to "전자정보공학부 IT융합전공"),
        )

        val repository = mock<DepartmentRepository>()
        val department = Department(id = 6L, name = "전자정보공학부 IT융합전공", collegeId = 2L)

        whenever(repository.getByName("전자정보공학부 IT융합전공")).thenReturn(department)

        val reader = DepartmentReader(repository)
        val result = reader.getByName("전자정보공학부(IT융합전공)")

        assertSame(department, result)
        verify(repository).getByName("전자정보공학부 IT융합전공")
    }
}
