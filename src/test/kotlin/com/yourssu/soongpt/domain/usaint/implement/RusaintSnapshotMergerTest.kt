package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.infrastructure.exception.StudentInfoMappingException
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

    val departmentReader = mockk<DepartmentReader>()
    val studentInfoValidator = StudentInfoValidator(departmentReader)
    val merger = RusaintSnapshotMerger(studentInfoValidator)

    val mockDepartment = mockk<com.yourssu.soongpt.domain.department.implement.Department>()

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

        every { departmentReader.getByName("알수없는학과") } throws RuntimeException("학과 없음")

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
        }
    }
})
