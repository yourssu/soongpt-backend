package com.yourssu.soongpt.domain.sso.application

import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoResponse
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoUpdateRequest
import com.yourssu.soongpt.domain.sso.implement.SyncSession
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.sso.business.SsoService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SsoControllerTest : BehaviorSpec({

    val ssoService = mock<SsoService>()
    val clientJwtProvider = mock<ClientJwtProvider>()
    val controller = SsoController(ssoService, clientJwtProvider)

    beforeTest {
        reset(ssoService, clientJwtProvider)
    }

    fun usaintData(pseudonym: String): RusaintUsaintDataResponse {
        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = listOf(
                RusaintTakenCourseDto(year = 2024, semester = "1", subjectCodes = listOf("21505455")),
            ),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = "경영학부",
                minorDepartment = null,
                teaching = true,
            ),
            basicInfo = RusaintBasicInfoDto(
                year = 2023,
                semester = 5,
                grade = 3,
                department = "컴퓨터학부",
            ),
            graduationRequirements = null,
            graduationSummary = null,
            warnings = listOf("NO_GRADUATION_DATA"),
        )
    }

    given("GET /sync/status") {
        `when`("쿠키가 없으면") {
            then("401 ERROR + invalid_session을 반환한다") {
                val response = controller.getSyncStatus(null)

                response.statusCodeValue shouldBe 401
                response.body.shouldNotBeNull()
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "invalid_session"

                verifyNoInteractions(clientJwtProvider, ssoService)
            }
        }

        `when`("JWT가 유효하지 않으면") {
            then("401 ERROR + invalid_session을 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("bad"))
                    .thenReturn(Result.failure(RuntimeException("invalid")))

                val response = controller.getSyncStatus("bad")

                response.statusCodeValue shouldBe 401
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "invalid_session"

                verify(ssoService, never()).getSyncStatus(any())
            }
        }

        `when`("세션이 만료되었으면") {
            then("401 ERROR + session_expired를 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))
                whenever(ssoService.getSyncStatus("p")).thenReturn(null)

                val response = controller.getSyncStatus("token")

                response.statusCodeValue shouldBe 401
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "session_expired"
            }
        }

        `when`("동기화가 진행 중이면") {
            then("200 PROCESSING을 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))
                whenever(ssoService.getSyncStatus("p")).thenReturn(
                    SyncSession(pseudonym = "p", status = SyncStatus.PROCESSING)
                )

                val response = controller.getSyncStatus("token")

                response.statusCodeValue shouldBe 200
                response.body!!.result.status shouldBe "PROCESSING"
                response.body!!.result.reason.shouldBeNull()
                response.body!!.result.studentInfo.shouldBeNull()
            }
        }

        `when`("동기화가 완료(COMPLETED)되고 usaintData가 있으면") {
            then("200 COMPLETED + studentInfo/warnings를 포함한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))

                val data = usaintData("p")
                whenever(ssoService.getSyncStatus("p")).thenReturn(
                    SyncSession(
                        pseudonym = "p",
                        status = SyncStatus.COMPLETED,
                        usaintData = data,
                    )
                )

                val response = controller.getSyncStatus("token")

                response.statusCodeValue shouldBe 200
                response.body!!.result.status shouldBe "COMPLETED"

                val studentInfo = response.body!!.result.studentInfo.shouldNotBeNull()
                studentInfo.grade shouldBe 3
                studentInfo.semester shouldBe 5
                studentInfo.year shouldBe 2023
                studentInfo.department shouldBe "컴퓨터학부"
                studentInfo.doubleMajorDepartment shouldBe "경영학부"
                studentInfo.minorDepartment shouldBe null
                studentInfo.teaching shouldBe true

                response.body!!.result.warnings.shouldNotBeNull()
                response.body!!.result.warnings!! shouldBe listOf("NO_GRADUATION_DATA")
            }
        }

        `when`("REQUIRES_REAUTH이면") {
            then("200 REQUIRES_REAUTH + reason을 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))
                whenever(ssoService.getSyncStatus("p")).thenReturn(
                    SyncSession(
                        pseudonym = "p",
                        status = SyncStatus.REQUIRES_REAUTH,
                        failReason = "token_expired",
                    )
                )

                val response = controller.getSyncStatus("token")

                response.statusCodeValue shouldBe 200
                response.body!!.result.status shouldBe "REQUIRES_REAUTH"
                response.body!!.result.reason shouldBe "token_expired"
            }
        }

        `when`("FAILED이면") {
            then("200 FAILED + reason을 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))
                whenever(ssoService.getSyncStatus("p")).thenReturn(
                    SyncSession(
                        pseudonym = "p",
                        status = SyncStatus.FAILED,
                        failReason = "server_timeout",
                    )
                )

                val response = controller.getSyncStatus("token")

                response.statusCodeValue shouldBe 200
                response.body!!.result.status shouldBe "FAILED"
                response.body!!.result.reason shouldBe "server_timeout"
            }
        }
    }

    given("PUT /sync/student-info") {
        val updateRequest = StudentInfoUpdateRequest(
            grade = 2,
            semester = 3,
            year = 2022,
            department = "컴퓨터학부",
            doubleMajorDepartment = null,
            minorDepartment = "수학과",
            teaching = false,
        )

        `when`("쿠키가 없으면") {
            then("401 ERROR + invalid_session을 반환한다") {
                val response = controller.updateStudentInfo(null, updateRequest)

                response.statusCodeValue shouldBe 401
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "invalid_session"

                verifyNoInteractions(clientJwtProvider, ssoService)
            }
        }

        `when`("JWT가 유효하지 않으면") {
            then("401 ERROR + invalid_session을 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("bad"))
                    .thenReturn(Result.failure(RuntimeException("invalid")))

                val response = controller.updateStudentInfo("bad", updateRequest)

                response.statusCodeValue shouldBe 401
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "invalid_session"

                verify(ssoService, never()).updateStudentInfo(any(), any())
            }
        }

        `when`("세션이 만료되었으면") {
            then("401 ERROR + session_expired를 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))
                whenever(ssoService.updateStudentInfo(eq("p"), any()))
                    .thenReturn(null)

                val response = controller.updateStudentInfo("token", updateRequest)

                response.statusCodeValue shouldBe 401
                response.body!!.result.status shouldBe "ERROR"
                response.body!!.result.reason shouldBe "session_expired"
            }
        }

        `when`("학적정보 수정이 성공하면") {
            then("200 COMPLETED + studentInfo를 반환한다") {
                whenever(clientJwtProvider.validateAndGetPseudonym("token"))
                    .thenReturn(Result.success("p"))

                whenever(ssoService.updateStudentInfo(eq("p"), eq(updateRequest)))
                    .thenReturn(
                        StudentInfoResponse(
                            grade = updateRequest.grade,
                            semester = updateRequest.semester,
                            year = updateRequest.year,
                            department = updateRequest.department,
                            doubleMajorDepartment = updateRequest.doubleMajorDepartment,
                            minorDepartment = updateRequest.minorDepartment,
                            teaching = updateRequest.teaching,
                        )
                    )

                val response = controller.updateStudentInfo("token", updateRequest)

                response.statusCodeValue shouldBe 200
                response.body!!.result.status shouldBe "COMPLETED"

                val studentInfo = response.body!!.result.studentInfo.shouldNotBeNull()
                studentInfo.grade shouldBe 2
                studentInfo.semester shouldBe 3
                studentInfo.year shouldBe 2022
                studentInfo.department shouldBe "컴퓨터학부"
                studentInfo.minorDepartment shouldBe "수학과"
                studentInfo.teaching shouldBe false

                response.body!!.result.warnings.shouldBeNull()

                verify(ssoService, times(1)).updateStudentInfo("p", updateRequest)
            }
        }
    }
})
