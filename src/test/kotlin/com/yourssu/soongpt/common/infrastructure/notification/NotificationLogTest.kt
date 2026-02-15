package com.yourssu.soongpt.common.infrastructure.notification

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.slf4j.LoggerFactory

/**
 * Notification 슬랙 알림 메서드 호출 시 로그가 찍히는지 검증.
 * (로그 → observer.py → Slack 전송 흐름의 1단계: 로그 출력 확인)
 */
class NotificationLogTest : BehaviorSpec({

    val loggerName = "com.yourssu.soongpt.common.infrastructure.notification.Notification"
    val logbackLogger = LoggerFactory.getLogger(loggerName) as Logger

    fun withListAppender(block: (ListAppender<ILoggingEvent>) -> Unit) {
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        logbackLogger.addAppender(appender)
        logbackLogger.level = Level.TRACE
        try {
            block(appender)
        } finally {
            logbackLogger.detachAppender(appender)
            appender.stop()
        }
    }

    given("notifyStudentInfoMappingFailed 호출 시") {
        val events = mutableListOf<ILoggingEvent>()
        `when`("호출하면") {
            withListAppender { appender ->
                Notification.notifyStudentInfoMappingFailed(
                    studentIdPrefix = "2024",
                    failureReason = "학과 매칭 실패: DB에서 찾을 수 없음",
                )
                events.addAll(appender.list)
            }
        }
        then("WARN 레벨로 StudentInfoMappingAlert&... 로그가 찍힌다") {
            events shouldHaveSize 1
            events[0].level shouldBe Level.WARN
            events[0].formattedMessage shouldContain "StudentInfoMappingAlert&"
            events[0].formattedMessage shouldContain "2024"
            events[0].formattedMessage shouldContain "학과 매칭 실패"
        }
    }

    given("notifyRusaintServiceError 호출 시") {
        val events = mutableListOf<ILoggingEvent>()
        `when`("호출하면") {
            withListAppender { appender ->
                Notification.notifyRusaintServiceError(
                    operation = "graduation",
                    statusCode = 500,
                    errorMessage = "Internal Server Error",
                    studentIdPrefix = "2022",
                )
                events.addAll(appender.list)
            }
        }
        then("WARN 레벨로 RusaintServiceError&... 로그가 찍힌다") {
            events shouldHaveSize 1
            events[0].level shouldBe Level.WARN
            events[0].formattedMessage shouldContain "RusaintServiceError&"
            events[0].formattedMessage shouldContain "graduation"
            events[0].formattedMessage shouldContain "500"
            events[0].formattedMessage shouldContain "2022"
        }
    }

    given("notifyRusaintServiceError (statusCode null, 연결 실패) 호출 시") {
        val events = mutableListOf<ILoggingEvent>()
        `when`("호출하면") {
            withListAppender { appender ->
                Notification.notifyRusaintServiceError(
                    operation = "academic",
                    statusCode = null,
                    errorMessage = "Connection refused",
                    studentIdPrefix = null,
                )
                events.addAll(appender.list)
            }
        }
        then("WARN 레벨로 RusaintServiceError&... 로그가 찍힌다") {
            events shouldHaveSize 1
            events[0].formattedMessage shouldContain "RusaintServiceError&"
            events[0].formattedMessage shouldContain "Connection refused"
        }
    }

    given("notifyGraduationSummaryParsingFailed 호출 시") {
        val events = mutableListOf<ILoggingEvent>()
        `when`("호출하면") {
            withListAppender { appender ->
                Notification.notifyGraduationSummaryParsingFailed(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    missingItems = listOf("전공선택(MAJOR_ELECTIVE)", "교양필수(GENERAL_REQUIRED)"),
                    rawRequirements = null,
                )
                events.addAll(appender.list)
            }
        }
        then("WARN 레벨로 GraduationSummaryAlert&... 로그가 찍힌다") {
            events shouldHaveSize 1
            events[0].level shouldBe Level.WARN
            events[0].formattedMessage shouldContain "GraduationSummaryAlert&"
            events[0].formattedMessage shouldContain "컴퓨터학부"
            events[0].formattedMessage shouldContain "userGrade:3"
            events[0].formattedMessage shouldContain "MAJOR_ELECTIVE"
        }
    }
})
