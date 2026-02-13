package com.yourssu.soongpt.domain.sso.implement

import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SyncSessionStoreTest : BehaviorSpec({

    fun props(sessionTtlMinutes: Long = 60L): SsoProperties {
        return SsoProperties(
            frontendUrl = "https://soongpt.yourssu.com",
            clientJwtSecret = "x".repeat(32),
            allowedRedirectUrls = emptyList(),
            sessionTtlMinutes = sessionTtlMinutes,
            jwtValidityMinutes = 60L,
            cookieSameSite = "Lax",
            cookieSecure = false,
        )
    }

    lateinit var store: SyncSessionStore

    beforeTest {
        store = SyncSessionStore(props())
    }

    fun usaintData(pseudonym: String): RusaintUsaintDataResponse {
        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = listOf(
                RusaintTakenCourseDto(year = 2024, semester = "1", subjectCodes = listOf("21505455")),
            ),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = null,
                minorDepartment = null,
                teaching = false,
            ),
            basicInfo = RusaintBasicInfoDto(
                year = 2023,
                semester = 1,
                grade = 3,
                department = "컴퓨터학부",
            ),
            graduationRequirements = null,
            graduationSummary = null,
            warnings = emptyList(),
        )
    }

    given("SyncSessionStore") {
        `when`("세션을 생성하면") {
            then("status=PROCESSING으로 저장된다") {
                val session = store.createSession("test-pseudonym")

                session.status shouldBe SyncStatus.PROCESSING
                store.hasSession("test-pseudonym") shouldBe true
                store.getSession("test-pseudonym").shouldNotBeNull()
                store.size() shouldBe 1L
            }
        }

        `when`("세션이 없는 pseudonym을 updateStatus하면") {
            then("아무 일도 일어나지 않고 예외도 발생하지 않는다") {
                store.updateStatus(
                    pseudonym = "missing",
                    status = SyncStatus.COMPLETED,
                    usaintData = usaintData("missing"),
                )

                store.hasSession("missing") shouldBe false
                store.getSession("missing").shouldBeNull()
                store.size() shouldBe 0L
            }
        }

        `when`("세션 상태를 COMPLETED로 업데이트하면") {
            then("usaintData가 함께 저장된다") {
                val pseudonym = "test-pseudonym"
                store.createSession(pseudonym)

                val data = usaintData(pseudonym)
                store.updateStatus(
                    pseudonym = pseudonym,
                    status = SyncStatus.COMPLETED,
                    usaintData = data,
                )

                val updated = store.getSession(pseudonym).shouldNotBeNull()
                updated.status shouldBe SyncStatus.COMPLETED
                updated.usaintData.shouldNotBeNull()

                store.getUsaintData(pseudonym).shouldNotBeNull().pseudonym shouldBe pseudonym
            }
        }

        `when`("세션 상태를 FAILED로 업데이트하면") {
            then("failReason이 저장된다") {
                val pseudonym = "test-pseudonym"
                store.createSession(pseudonym)

                store.updateStatus(
                    pseudonym = pseudonym,
                    status = SyncStatus.FAILED,
                    failReason = "timeout",
                )

                val updated = store.getSession(pseudonym).shouldNotBeNull()
                updated.status shouldBe SyncStatus.FAILED
                updated.failReason shouldBe "timeout"
            }
        }
    }
})
