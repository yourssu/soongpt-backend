package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.support.config.ApplicationTest
import org.junit.jupiter.api.*
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ApplicationTest
class SlackAlarmProducerTest {
    @Autowired
    private lateinit var slackAlarmProducer: SlackAlarmProducer

    @MockitoBean
    private lateinit var slackAlarmFeignClient: SlackAlarmFeignClient

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class sendAlarm_메서드는 {
        private val message = "메세지 형식 테스트"

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 메세지가_주어지면 {
            @Test
            @DisplayName("yaml 파일에 설정된 채널로 슬랙 메세지를 비동기 방식으로 보낸다.")
            fun success() {
                slackAlarmProducer.sendAlarm(message)

                verify(slackAlarmFeignClient, times(1))
                    .sendAlarm(any())
            }
        }
    }
}