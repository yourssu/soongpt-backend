package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationRequirementsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UsaintServiceTest : BehaviorSpec({

    given("UsaintService.syncUsaintData") {
        val pseudonymGenerator = mock<PseudonymGenerator>()
        val rusaintServiceClient = mock<RusaintServiceClient>()

        val service = UsaintService(
            pseudonymGenerator = pseudonymGenerator,
            rusaintServiceClient = rusaintServiceClient,
        )

        val defaultCreditSummary = RusaintCreditSummaryItemDto(
            required = 0,
            completed = 0,
            satisfied = true,
        )

        val sampleSnapshot = RusaintUsaintDataResponse(
            pseudonym = "pseudonym-from-rusaint",
            takenCourses = emptyList(),
            lowGradeSubjectCodes = emptyList(),
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = null,
                minorDepartment = null,
                teaching = false,
            ),
            basicInfo = RusaintBasicInfoDto(
                year = 2025,
                semester = 4,
                grade = 2,
                department = "컴퓨터학부",
            ),
            graduationRequirements = RusaintGraduationRequirementsDto(
                requirements = emptyList(),
            ),
            graduationSummary = RusaintGraduationSummaryDto(
                generalRequired = defaultCreditSummary,
                generalElective = defaultCreditSummary,
                majorFoundation = defaultCreditSummary,
                majorRequired = defaultCreditSummary,
                majorElective = defaultCreditSummary,
                doubleMajorRequired = defaultCreditSummary,
                doubleMajorElective = defaultCreditSummary,
                christianCourses = defaultCreditSummary,
                chapel = RusaintChapelSummaryItemDto(satisfied = true),
            ),
        )

        whenever(pseudonymGenerator.generate(any())).thenReturn("pseudonym-test")
        whenever(rusaintServiceClient.syncUsaintData(any(), any())).thenReturn(sampleSnapshot)

        `when`("유효한 UsaintSyncRequest로 호출하면") {
            val request = UsaintSyncRequest(
                studentId = "20233009",
                sToken = "valid-s-token",
            )
            val result = service.syncUsaintData(request)

            then("UsaintSyncResponse를 반환하고 summary가 설정된다") {
                result.summary shouldBe "usaint data synced"
            }

            then("rusaintServiceClient.syncUsaintData가 studentId, sToken으로 호출된다") {
                verify(rusaintServiceClient).syncUsaintData(
                    eq("20233009"),
                    eq("valid-s-token"),
                )
            }

            then("pseudonymGenerator.generate가 studentId로 호출된다") {
                verify(pseudonymGenerator).generate(eq("20233009"))
            }
        }
    }
})
