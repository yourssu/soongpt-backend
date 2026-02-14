package com.yourssu.soongpt.domain.usaint.implement.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * rusaint-service 응답 JSON이 WAS DTO로 정상 역직렬화되는지 검증합니다.
 * 스키마 변경 시 이 테스트가 실패하면 DTO 수정이 필요합니다.
 */
class RusaintDtoDeserializationTest : DescribeSpec({

    val objectMapper = ObjectMapper().registerKotlinModule()

    describe("RusaintAcademicResponseDto 역직렬화") {
        it("rusaint-service /snapshot/academic 응답 JSON을 역직렬화할 수 있다") {
            // rusaint-service가 반환하는 실제 응답 형식
            val json = """
                {
                    "pseudonym": "test-pseudonym",
                    "takenCourses": [
                        {"year": 2024, "semester": "1", "subjectCodes": ["21505455", "21501027"]}
                    ],
                    "lowGradeSubjectCodes": ["21505395", "21501015"],
                    "flags": {
                        "doubleMajorDepartment": null,
                        "minorDepartment": null,
                        "teaching": false
                    },
                    "basicInfo": {
                        "year": 2023,
                        "grade": 3,
                        "semester": 6,
                        "department": "컴퓨터학부"
                    }
                }
            """.trimIndent()

            val dto = objectMapper.readValue<RusaintAcademicResponseDto>(json)

            dto.pseudonym shouldBe "test-pseudonym"
            dto.takenCourses.size shouldBe 1
            dto.takenCourses[0].year shouldBe 2024
            dto.takenCourses[0].semester shouldBe "1"
            dto.lowGradeSubjectCodes shouldBe listOf("21505395", "21501015")
            dto.flags.teaching shouldBe false
            dto.basicInfo.grade shouldBe 3
            dto.basicInfo.semester shouldBe 6
            dto.basicInfo.department shouldBe "컴퓨터학부"
        }
    }

    describe("RusaintGraduationResponseDto 역직렬화") {
        it("rusaint-service /snapshot/graduation 응답 JSON을 역직렬화할 수 있다") {
            // rusaint-service가 반환하는 실제 응답 형식
            val json = """
                {
                    "pseudonym": "test-pseudonym",
                    "graduationRequirements": {
                        "requirements": [
                            {
                                "name": "학부-교양필수 19",
                                "requirement": 19,
                                "calculation": 17.0,
                                "difference": -2.0,
                                "result": false,
                                "category": "교양필수"
                            }
                        ]
                    },
                    "graduationSummary": {
                        "generalRequired": {"required": 19, "completed": 17, "satisfied": false},
                        "generalElective": {"required": 12, "completed": 15, "satisfied": true},
                        "majorFoundation": {"required": 6, "completed": 6, "satisfied": true},
                        "majorRequired": {"required": 12, "completed": 15, "satisfied": true},
                        "majorElective": {"required": 54, "completed": 30, "satisfied": false},
                        "doubleMajorRequired": {"required": 0, "completed": 0, "satisfied": true},
                        "doubleMajorElective": {"required": 0, "completed": 0, "satisfied": true},
                        "minor": {"required": 0, "completed": 0, "satisfied": true},
                        "christianCourses": {"required": 6, "completed": 6, "satisfied": true},
                        "chapel": {"satisfied": true}
                    }
                }
            """.trimIndent()

            val dto = objectMapper.readValue<RusaintGraduationResponseDto>(json)

            dto.pseudonym shouldBe "test-pseudonym"
            dto.graduationRequirements.requirements.size shouldBe 1
            dto.graduationRequirements.requirements[0].name shouldBe "학부-교양필수 19"
            dto.graduationRequirements.requirements[0].requirement shouldBe 19
            dto.graduationRequirements.requirements[0].result shouldBe false

            dto.graduationSummary shouldNotBe null
            dto.graduationSummary.generalRequired!!.required shouldBe 19
            dto.graduationSummary.generalRequired!!.completed shouldBe 17
            dto.graduationSummary.generalRequired!!.satisfied shouldBe false
            dto.graduationSummary.majorElective!!.satisfied shouldBe false
            dto.graduationSummary.chapel!!.satisfied shouldBe true
        }

        it("graduationSummary 필드가 null인 경우도 역직렬화할 수 있다") {
            val json = """
                {
                    "pseudonym": "test-pseudonym",
                    "graduationRequirements": {
                        "requirements": []
                    },
                    "graduationSummary": {
                        "generalRequired": {"required": 19, "completed": 17, "satisfied": false},
                        "generalElective": null,
                        "majorFoundation": null,
                        "majorRequired": {"required": 12, "completed": 15, "satisfied": true},
                        "majorElective": null,
                        "doubleMajorRequired": null,
                        "doubleMajorElective": null,
                        "minor": null,
                        "christianCourses": null,
                        "chapel": null
                    }
                }
            """.trimIndent()

            val dto = objectMapper.readValue<RusaintGraduationResponseDto>(json)

            dto.graduationSummary.generalRequired shouldNotBe null
            dto.graduationSummary.generalRequired!!.required shouldBe 19
            dto.graduationSummary.generalElective shouldBe null
            dto.graduationSummary.majorFoundation shouldBe null
            dto.graduationSummary.majorRequired shouldNotBe null
            dto.graduationSummary.majorElective shouldBe null
            dto.graduationSummary.chapel shouldBe null
        }

        it("requirement/calculation/difference가 null인 경우도 역직렬화할 수 있다") {
            val json = """
                {
                    "pseudonym": "test-pseudonym",
                    "graduationRequirements": {
                        "requirements": [
                            {
                                "name": "학부-졸업논문",
                                "requirement": null,
                                "calculation": null,
                                "difference": null,
                                "result": false,
                                "category": "기타"
                            }
                        ]
                    },
                    "graduationSummary": {
                        "generalRequired": {"required": 0, "completed": 0, "satisfied": true},
                        "generalElective": {"required": 0, "completed": 0, "satisfied": true},
                        "majorFoundation": {"required": 0, "completed": 0, "satisfied": true},
                        "majorRequired": {"required": 0, "completed": 0, "satisfied": true},
                        "majorElective": {"required": 0, "completed": 0, "satisfied": true},
                        "doubleMajorRequired": {"required": 0, "completed": 0, "satisfied": true},
                        "doubleMajorElective": {"required": 0, "completed": 0, "satisfied": true},
                        "minor": {"required": 0, "completed": 0, "satisfied": true},
                        "christianCourses": {"required": 0, "completed": 0, "satisfied": true},
                        "chapel": {"satisfied": true}
                    }
                }
            """.trimIndent()

            val dto = objectMapper.readValue<RusaintGraduationResponseDto>(json)

            dto.graduationRequirements.requirements[0].requirement shouldBe null
            dto.graduationRequirements.requirements[0].calculation shouldBe null
            dto.graduationRequirements.requirements[0].difference shouldBe null
        }

        it("graduationSummary 필드가 null인 경우도 역직렬화할 수 있다 (매칭 안 된 항목)") {
            val json = """
                {
                    "pseudonym": "test-pseudonym",
                    "graduationRequirements": {
                        "requirements": []
                    },
                    "graduationSummary": {
                        "generalRequired": {"required": 19, "completed": 17, "satisfied": false},
                        "majorRequired": {"required": 12, "completed": 12, "satisfied": true},
                        "chapel": {"satisfied": true}
                    }
                }
            """.trimIndent()

            val dto = objectMapper.readValue<RusaintGraduationResponseDto>(json)

            dto.graduationSummary.generalRequired!!.required shouldBe 19
            dto.graduationSummary.majorRequired!!.satisfied shouldBe true
            dto.graduationSummary.chapel!!.satisfied shouldBe true
            dto.graduationSummary.generalElective shouldBe null
            dto.graduationSummary.majorFoundation shouldBe null
            dto.graduationSummary.majorElective shouldBe null
            dto.graduationSummary.minor shouldBe null
            dto.graduationSummary.doubleMajorRequired shouldBe null
            dto.graduationSummary.doubleMajorElective shouldBe null
            dto.graduationSummary.christianCourses shouldBe null
        }
    }
})
