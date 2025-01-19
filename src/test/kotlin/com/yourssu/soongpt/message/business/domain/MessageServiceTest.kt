package com.yourssu.soongpt.message.business.domain

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.MessageFixture.HELLO_WORLD
import com.yourssu.soongpt.message.implement.domain.Message
import com.yourssu.soongpt.message.implement.domain.MessageReader
import com.yourssu.soongpt.message.implement.domain.MessageWriter
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

@ApplicationTest
class MessageServiceTest {
    @Autowired
    lateinit var messageService: MessageService

    @Autowired
    lateinit var messageReader: MessageReader

    @Autowired
    lateinit var messageWriter: MessageWriter

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class create_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 메세지_본문을_받으면 {
            @Test
            @DisplayName("메세지를 생성한다.")
            fun success() {
                val response = messageService.create(HELLO_WORLD.toCreatedCommand())

                assertNotNull(response)
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findById_메서드는 {
        var message: Message? = null

        @BeforeEach
        fun setUp() {
            message = messageWriter.save(HELLO_WORLD.toDomain())
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 메세지_아이디를_받으면 {
            @Test
            @DisplayName("해당하는 메세지 응답을 반환한다.")
            fun success() {
                val actual = messageService.find(message!!.id!!)

                assertEquals(message!!.id, actual.id)
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findAll_메서드는 {
        @BeforeEach
        fun setUp() {
            messageWriter.save(HELLO_WORLD.toDomain())
            messageWriter.save(HELLO_WORLD.toDomain())
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 요청을_받으면 {
            @Test
            @DisplayName("모든 메세지 응답을 반환한다.")
            fun success() {
                val response = messageService.findAll()

                assertEquals(2, response.size)
            }
        }
    }

}