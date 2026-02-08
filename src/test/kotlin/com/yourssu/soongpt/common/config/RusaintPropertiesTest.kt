package com.yourssu.soongpt.common.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class RusaintPropertiesTest @Autowired constructor(
    private val rusaintProperties: RusaintProperties,
) {

    @Test
    fun `RusaintProperties 설정_바인딩이_정상동작한다`() {
        assertEquals("https://rusaint-service.test", rusaintProperties.baseUrl)
        assertEquals("test-secret-at-least-48-bytes-for-validation-ok!!!!!!!!", rusaintProperties.pseudonymSecret)
        assertEquals("test-secret-at-least-48-bytes-for-validation-ok!!!!!!!!", rusaintProperties.internalJwtSecret)
    }
}
