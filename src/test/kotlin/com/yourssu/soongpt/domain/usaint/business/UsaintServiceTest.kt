package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.business.dto.UsaintSyncResponse
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintAvailableCreditsDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintGraduationRequirementsDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintLowGradeSubjectCodesDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintRemainingCreditsDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import com.yourssu.soongpt.domain.usaint.implement.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintUsaintDataResponse
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
        val courseRepository = mock<CourseRepository>()

        val service = UsaintService(
            pseudonymGenerator = pseudonymGenerator,
            rusaintServiceClient = rusaintServiceClient,
            courseRepository = courseRepository,
        )

        val sampleSnapshot = RusaintUsaintDataResponse(
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
        whenever(courseRepository.groupByCategory(any())).thenReturn(
            GroupedCoursesByCategoryDto(
                majorRequiredCourses = emptyList(),
                majorElectiveCourses = emptyList(),
                generalRequiredCourses = emptyList(),
                generalElectiveCourses = emptyList(),
            ),
        )

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

        `when`("rusaint가 저성적 과목 코드를 반환하면") {
            val snapshotWithLowGrades = sampleSnapshot.copy(
                lowGradeSubjectCodes = RusaintLowGradeSubjectCodesDto(
                    passLow = listOf("21505395"),
                    fail = listOf("21501015"),
                ),
            )
            whenever(rusaintServiceClient.syncUsaintData(any(), any())).thenReturn(snapshotWithLowGrades)
            whenever(courseRepository.groupByCategory(any())).thenReturn(
                GroupedCoursesByCategoryDto(
                    majorRequiredCourses = emptyList(),
                    majorElectiveCourses = emptyList(),
                    generalRequiredCourses = emptyList(),
                    generalElectiveCourses = emptyList(),
                ),
            )

            val request = UsaintSyncRequest(studentId = "20233009", sToken = "token")
            val result = service.syncUsaintData(request)

            then("저성적 분류를 위해 courseRepository.groupByCategory가 호출된다") {
                verify(courseRepository).groupByCategory(listOf(21505395L))
                verify(courseRepository).groupByCategory(listOf(21501015L))
            }

            then("summary가 포함된 응답을 반환한다") {
                result.summary shouldBe "usaint data synced"
            }
        }
    }
})
