package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class StudentInfoValidatorTest : BehaviorSpec({

    val departmentReader = mockk<DepartmentReader>()
    val validator = StudentInfoValidator(departmentReader)

    val mockDepartment = mockk<Department>()

    beforeEach {
        clearAllMocks()
    }

    given("정상적인 학생 정보가 주어졌을 때") {
        val basicInfo = RusaintBasicInfoDto(
            year = 2022,
            semester = 1,
            grade = 3,
            department = "컴퓨터학부"
        )

        every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

        `when`("검증하면") {
            val result = validator.validate(
                basicInfo = basicInfo,
                studentIdPrefix = "2022"
            )

            then("검증 성공") {
                result.isValid shouldBe true
                result.failureReason shouldBe null
            }
        }
    }

    given("학년이 1~5 범위를 벗어났을 때") {
        val testCases = listOf(
            0 to "0 (예상 범위: 1~5)",
            6 to "6 (예상 범위: 1~5)",
            -1 to "-1 (예상 범위: 1~5)",
            10 to "10 (예상 범위: 1~5)"
        )

        testCases.forEach { (invalidGrade, expectedMessage) ->
            `when`("학년이 $invalidGrade 일 때") {
                val basicInfo = RusaintBasicInfoDto(
                    year = 2022,
                    semester = 1,
                    grade = invalidGrade,
                    department = "컴퓨터학부"
                )

                every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

                val result = validator.validate(
                    basicInfo = basicInfo,
                    studentIdPrefix = "2022"
                )

                then("검증 실패") {
                    result.isValid shouldBe false
                    result.failureReason shouldBe "학년이 유효하지 않음: $expectedMessage"
                }
            }
        }
    }

    given("학기가 1~12 범위를 벗어났을 때") {
        val testCases = listOf(0, 13, 99, -1)

        testCases.forEach { invalidSemester ->
            `when`("학기가 $invalidSemester 일 때") {
                val basicInfo = RusaintBasicInfoDto(
                    year = 2022,
                    semester = invalidSemester,
                    grade = 3,
                    department = "컴퓨터학부"
                )

                every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

                val result = validator.validate(
                    basicInfo = basicInfo,
                    studentIdPrefix = "2022"
                )

                then("검증 실패") {
                    result.isValid shouldBe false
                    result.failureReason shouldBe "학기가 유효하지 않음: $invalidSemester (예상 범위: 1~12)"
                }
            }
        }
    }

    given("입학년도가 2015~2026 범위를 벗어났을 때") {
        val testCases = listOf(
            2014 to "2014 (예상 범위: 2015~2026)",
            2027 to "2027 (예상 범위: 2015~2026)",
            2000 to "2000 (예상 범위: 2015~2026)",
            1990 to "1990 (예상 범위: 2015~2026)"
        )

        testCases.forEach { (invalidYear, expectedMessage) ->
            `when`("입학년도가 $invalidYear 일 때") {
                val basicInfo = RusaintBasicInfoDto(
                    year = invalidYear,
                    semester = 1,
                    grade = 3,
                    department = "컴퓨터학부"
                )

                every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

                val result = validator.validate(
                    basicInfo = basicInfo,
                    studentIdPrefix = "2022"
                )

                then("검증 실패") {
                    result.isValid shouldBe false
                    result.failureReason shouldBe "입학년도가 유효하지 않음: $expectedMessage"
                }
            }
        }
    }

    given("학과 매칭에 실패했을 때") {
        `when`("DB에 학과가 없으면") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 3,
                department = "컴공학부"
            )

            every { departmentReader.getByName("컴공학부") } throws RuntimeException("학과 없음")

            val result = validator.validate(
                basicInfo = basicInfo,
                studentIdPrefix = "2022"
            )

            then("검증 실패") {
                result.isValid shouldBe false
                result.failureReason shouldBe "학과 매칭 실패: '컴공학부' (정규화: '컴공학부', DB에서 찾을 수 없음)"
            }
        }

        `when`("학과가 빈 문자열일 때") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 3,
                department = ""
            )

            val result = validator.validate(
                basicInfo = basicInfo,
                studentIdPrefix = "2022"
            )

            then("검증 실패") {
                result.isValid shouldBe false
                result.failureReason shouldBe "학과가 비어있음"
            }
        }
    }

    given("여러 검증이 동시에 실패했을 때") {
        `when`("모든 항목이 잘못되었으면") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2010,
                semester = 99,
                grade = 0,
                department = ""
            )

            val result = validator.validate(
                basicInfo = basicInfo,
                studentIdPrefix = "2010"
            )

            then("검증 실패") {
                result.isValid shouldBe false
            }

            then("모든 에러 메시지 포함") {
                result.failureReason!! shouldBe "학년이 유효하지 않음: 0 (예상 범위: 1~5), 학기가 유효하지 않음: 99 (예상 범위: 1~12), 입학년도가 유효하지 않음: 2010 (예상 범위: 2015~2026), 학과가 비어있음"
            }
        }
    }

    given("rawDataForLogging이 제공되었을 때") {
        `when`("검증 실패 시") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 0,
                grade = 3,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val rawData = mapOf(
                "academic_pseudonym" to "abc123",
                "taken_courses_count" to 45
            )

            val result = validator.validate(
                basicInfo = basicInfo,
                studentIdPrefix = "2022",
                rawDataForLogging = rawData
            )

            then("검증 실패") {
                result.isValid shouldBe false
            }
        }
    }

    given("경계값 테스트") {
        `when`("학년이 1일 때 (최소값)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 1,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2022")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }

        `when`("학년이 5일 때 (최대값)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 1,
                grade = 5,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2022")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }

        `when`("입학년도가 2015일 때 (최소값)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2015,
                semester = 1,
                grade = 3,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2015")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }

        `when`("입학년도가 2026일 때 (최대값)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2026,
                semester = 1,
                grade = 3,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2026")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }

        `when`("학기가 7일 때 (1~12 범위 내)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2022,
                semester = 7,
                grade = 4,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2022")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }

        `when`("학기가 12일 때 (최대값)") {
            val basicInfo = RusaintBasicInfoDto(
                year = 2020,
                semester = 12,
                grade = 5,
                department = "컴퓨터학부"
            )

            every { departmentReader.getByName("컴퓨터학부") } returns mockDepartment

            val result = validator.validate(basicInfo, "2020")

            then("검증 성공") {
                result.isValid shouldBe true
            }
        }
    }
})
