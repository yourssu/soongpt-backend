package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import com.yourssu.soongpt.domain.usaint.implement.dto.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class UsaintServiceTest : BehaviorSpec({

    given("UsaintService.syncUsaintData") {
        val pseudonymGenerator = mock<PseudonymGenerator>()
        val rusaintServiceClient = mock<RusaintServiceClient>()

        val service = UsaintService(
            pseudonymGenerator = pseudonymGenerator,
            rusaintServiceClient = rusaintServiceClient,
        )

        val sampleSnapshot = RusaintUsaintDataResponse(
            pseudonym = "pseudonym-from-rusaint",
            takenCourses = emptyList(),
            lowGradeSubjectCodes = RusaintLowGradeSubjectCodesDto(
                passLow = emptyList(),
                fail = emptyList(),
            ),
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = null,
                minorDepartment = null,
                teaching = false,
            ),
            availableCredits = RusaintAvailableCreditsDto(
                previousGpa = 4.0,
                carriedOverCredits = 0,
                maxAvailableCredits = 21.0,
            ),
            basicInfo = RusaintBasicInfoDto(
                year = 2025,
                semester = 4,
                grade = 2,
                department = "컴퓨터학부",
            ),
            remainingCredits = RusaintRemainingCreditsDto(
                majorRequired = 12,
                majorElective = 18,
                generalRequired = 6,
                generalElective = 10,
            ),
            graduationRequirements = RusaintGraduationRequirementsDto(
                requirements = emptyList(),
                remainingCredits = RusaintRemainingCreditsDto(
                    majorRequired = 12,
                    majorElective = 18,
                    generalRequired = 6,
                    generalElective = 10,
                ),
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
