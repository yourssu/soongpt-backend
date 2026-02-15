package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.infrastructure.exception.StudentInfoMappingException
import com.yourssu.soongpt.common.infrastructure.slack.SlackWebhookClient
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*

class RusaintSnapshotMergerTest : BehaviorSpec({

    val slackWebhookClient = mockk<SlackWebhookClient>(relaxed = true)
    val departmentReader = mockk<DepartmentReader>()
    val studentInfoValidator = StudentInfoValidator(slackWebhookClient, departmentReader)
    val merger = RusaintSnapshotMerger(studentInfoValidator)

    val mockDepartment = mockk<com.yourssu.soongpt.domain.department.model.Department>()

    beforeEach {
        clearAllMocks()
    }

    given("정상적인 학생 정보가 주어졌을 때") {
        val academicResponse = RusaintAcademicResponseDto(
            pseudonym = "test-pseudonym",
            takenCourses = listOf(
                RusaintTakenCourseDto(2022, "1학기", listOf("COM001"))
            ),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(null, null, false),
            basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 3,
                department = "컴퓨터학부"
            ),
            warnings = emptyList()
        )

        every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

        `when`("검증 포함 병합하면") {
            val result = merger.mergeWithValidation(
                academic = academicResponse,
                graduation = null,
                studentIdPrefix = "2022"
            )

            then("병합 성공") {
                result.data.shouldNotBeNull()
                result.validationError.shouldBeNull()
                result.data!!.basicInfo.grade shouldBe 3
                result.data!!.basicInfo.department shouldBe "컴퓨터학부"
            }

            then("Slack 알림이 가지 않음") {
                verify(exactly = 0) {
                    slackWebhookClient.notifyStudentInfoMappingFailed(any(), any(), any(), any())
                }
            }
        }
    }

    given("학생 정보가 유효하지 않을 때") {
        val academicResponse = RusaintAcademicResponseDto(
            pseudonym = "test-pseudonym",
            takenCourses = emptyList(),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(null, null, false),
            basicInfo = RusaintBasicInfoDto(
                year = 2010,      // ❌ 2015~2026 범위 밖
                semester = 99,    // ❌ 1, 2만 허용
                grade = 0,        // ❌ 1~5 범위 밖
                department = ""   // ❌ 빈 문자열
            ),
            warnings = emptyList()
        )

        `when`("검증 포함 병합하면") {
            val result = merger.mergeWithValidation(
                academic = academicResponse,
                graduation = null,
                studentIdPrefix = "2010"
            )

            then("병합 실패") {
                result.data.shouldBeNull()
                result.validationError.shouldNotBeNull()
            }

            then("Slack 알림 전송") {
                verify(exactly = 1) {
                    slackWebhookClient.notifyStudentInfoMappingFailed(
                        studentIdPrefix = "2010",
                        rawData = match { data ->
                            data["parsed_grade"] == 0 &&
                            data["parsed_semester"] == 99 &&
                            data["parsed_year"] == 2010 &&
                            data["parsed_department"] == ""
                        },
                        failureReason = match { it.contains("학년이 유효하지 않음") }
                    )
                }
            }
        }
    }

    given("학과 매칭만 실패했을 때") {
        val academicResponse = RusaintAcademicResponseDto(
            pseudonym = "test-pseudonym",
            takenCourses = emptyList(),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(null, null, false),
            basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 3,
                department = "알수없는학과"  // ❌ DB에 없음
            ),
            warnings = emptyList()
        )

        every { departmentReader.getByName("알수없는학과") } returns null

        `when`("검증 포함 병합하면") {
            val result = merger.mergeWithValidation(
                academic = academicResponse,
                graduation = null,
                studentIdPrefix = "2022"
            )

            then("병합 실패") {
                result.data.shouldBeNull()
                result.validationError shouldBe "학과 매칭 실패: '알수없는학과' (정규화: '알수없는학과', DB에서 찾을 수 없음)"
            }

            then("Slack 알림 전송") {
                verify(exactly = 1) {
                    slackWebhookClient.notifyStudentInfoMappingFailed(
                        studentIdPrefix = "2022",
                        rawData = any(),
                        failureReason = "학과 매칭 실패: '알수없는학과' (정규화: '알수없는학과', DB에서 찾을 수 없음)"
                    )
                }
            }
        }
    }
})
