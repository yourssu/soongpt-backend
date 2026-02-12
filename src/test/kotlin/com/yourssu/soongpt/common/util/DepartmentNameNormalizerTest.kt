package com.yourssu.soongpt.common.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DepartmentNameNormalizerTest {

    @AfterEach
    fun tearDown() {
        DepartmentNameNormalizer.resetForTest()
    }

    @Test
    fun `canonical department name is returned as is`() {
        DepartmentNameNormalizer.initialize(
            canonicalDepartments = setOf("컴퓨터학부"),
            aliases = emptyMap(),
        )

        val normalized = DepartmentNameNormalizer.normalize("컴퓨터학부")

        assertEquals("컴퓨터학부", normalized)
    }

    @Test
    fun `parenthesized alias is normalized to canonical department name`() {
        DepartmentNameNormalizer.initialize(
            canonicalDepartments = setOf(
                "전자정보공학부 IT융합전공",
                "전자정보공학부 전자공학전공",
            ),
            aliases = mapOf(
                "전자정보공학부(IT융합전공)" to "전자정보공학부 IT융합전공",
            ),
        )

        val normalized = DepartmentNameNormalizer.normalize("전자정보공학부 ( IT융합전공 )")

        assertEquals("전자정보공학부 IT융합전공", normalized)
    }

    @Test
    fun `initializer fails when alias target is not in canonical departments`() {
        assertThrows(IllegalStateException::class.java) {
            DepartmentNameNormalizer.initialize(
                canonicalDepartments = setOf("컴퓨터학부"),
                aliases = mapOf("컴공" to "컴퓨터공학과"),
            )
        }
    }
}
