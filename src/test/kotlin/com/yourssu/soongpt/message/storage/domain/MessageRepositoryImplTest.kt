package com.yourssu.soongpt.message.storage.domain

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.MessageFixture.HELLO_WORLD
import com.yourssu.soongpt.message.implement.domain.Message
import com.yourssu.soongpt.message.implement.domain.MessageRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class MessageRepositoryImplTest {

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class get_메서드는 {
        private var message: Message? = null

        @BeforeEach
        fun setUp() {
            message = messageRepository.save(HELLO_WORLD.toDomain())
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 메세지_아이디를_받으면 {
            @Test
            @DisplayName("해당하는 메서드 객체를 반환한다.")
            fun success() {
                val actual = messageRepository.get(message!!.id!!)

                assertEquals(message!!.id!! , actual.id)
            }
        }
    }
}