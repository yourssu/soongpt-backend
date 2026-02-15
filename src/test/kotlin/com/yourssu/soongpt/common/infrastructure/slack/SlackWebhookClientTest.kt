package com.yourssu.soongpt.common.infrastructure.slack

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import io.mockk.*

class SlackWebhookClientTest : BehaviorSpec({

    val restTemplate = mockk<RestTemplate>(relaxed = true)
    val restTemplateBuilder = mockk<RestTemplateBuilder> {
        every { build() } returns restTemplate
    }

    beforeEach {
        clearAllMocks()
    }

    given("Slack이 활성화되어 있을 때") {
        val client = SlackWebhookClient(
            webhookUrl = "https://hooks.slack.com/services/TEST/WEBHOOK/URL",
            enabled = true,
            restTemplateBuilder = restTemplateBuilder
        )

        `when`("학생 정보 매칭 실패 알림을 보내면") {
            every {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    String::class.java
                )
            } returns ResponseEntity.ok("ok")

            client.notifyStudentInfoMappingFailed(
                studentIdPrefix = "2022",
                rawData = mapOf(
                    "grade" to 6,
                    "semester" to 3,
                    "year" to 2014,
                    "department" to "컴공학부"
                ),
                failureReason = "학년이 유효하지 않음: 6 (예상 범위: 1~5)"
            )

            // 비동기로 전송되므로 잠시 대기
            Thread.sleep(500)

            then("Slack API 호출됨") {
                verify(timeout = 1000) {
                    restTemplate.postForEntity(
                        "https://hooks.slack.com/services/TEST/WEBHOOK/URL",
                        withArg<HttpEntity<Map<String, String>>> { entity ->
                            val body = entity.body!!["text"]!!
                            body.contains("학생 정보 매칭 실패") shouldBe true
                            body.contains("2022****") shouldBe true
                            body.contains("학년이 유효하지 않음: 6") shouldBe true
                        },
                        String::class.java
                    )
                }
            }
        }

        `when`("Rusaint 서비스 에러 알림을 보내면") {
            every {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    String::class.java
                )
            } returns ResponseEntity.ok("ok")

            client.notifyRusaintServiceError(
                operation = "academic",
                statusCode = 502,
                errorMessage = "숭실대 서버 연결 실패",
                studentIdPrefix = "2022"
            )

            Thread.sleep(500)

            then("Slack API 호출됨") {
                verify(timeout = 1000) {
                    restTemplate.postForEntity(
                        "https://hooks.slack.com/services/TEST/WEBHOOK/URL",
                        withArg<HttpEntity<Map<String, String>>> { entity ->
                            val body = entity.body!!["text"]!!
                            body.contains("Rusaint 서비스 에러") shouldBe true
                            body.contains("502") shouldBe true
                            body.contains("academic") shouldBe true
                        },
                        String::class.java
                    )
                }
            }
        }
    }

    given("Slack이 비활성화되어 있을 때") {
        val client = SlackWebhookClient(
            webhookUrl = "https://hooks.slack.com/services/TEST/WEBHOOK/URL",
            enabled = false,
            restTemplateBuilder = restTemplateBuilder
        )

        `when`("알림을 보내면") {
            client.notifyStudentInfoMappingFailed(
                studentIdPrefix = "2022",
                rawData = mapOf("grade" to 6),
                failureReason = "테스트"
            )

            Thread.sleep(500)

            then("Slack API 호출되지 않음") {
                verify(exactly = 0) {
                    restTemplate.postForEntity(any<String>(), any<HttpEntity<*>>(), String::class.java)
                }
            }
        }
    }

    given("Webhook URL이 없을 때") {
        val client = SlackWebhookClient(
            webhookUrl = null,
            enabled = true,
            restTemplateBuilder = restTemplateBuilder
        )

        `when`("알림을 보내면") {
            client.notifyStudentInfoMappingFailed(
                studentIdPrefix = "2022",
                rawData = mapOf("grade" to 6),
                failureReason = "테스트"
            )

            Thread.sleep(500)

            then("Slack API 호출되지 않음") {
                verify(exactly = 0) {
                    restTemplate.postForEntity(any<String>(), any<HttpEntity<*>>(), String::class.java)
                }
            }
        }
    }
})
