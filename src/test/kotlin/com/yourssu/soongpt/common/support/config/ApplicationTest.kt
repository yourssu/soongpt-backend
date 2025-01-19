package com.yourssu.soongpt.common.support.config

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(DataClearExtension::class)
annotation class ApplicationTest
