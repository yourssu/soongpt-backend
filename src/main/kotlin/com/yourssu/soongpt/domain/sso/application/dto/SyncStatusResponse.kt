package com.yourssu.soongpt.domain.sso.application.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동기화 상태 응답")
data class SyncStatusResponse(
    @Schema(description = "동기화 상태", example = "COMPLETED", allowableValues = ["PROCESSING", "COMPLETED", "REQUIRES_REAUTH", "FAILED", "ERROR"])
    val status: String,
    @Schema(description = "에러/실패 사유 (에러 시에만 포함)", example = "token_expired", nullable = true)
    val reason: String? = null,
    @Schema(description = "학적정보 (COMPLETED 시에만 포함)", nullable = true)
    val studentInfo: StudentInfoResponse? = null,
)

@Schema(description = "학적정보 응답")
data class StudentInfoResponse(
    @Schema(description = "학년", example = "3")
    val grade: Int,
    @Schema(description = "학기 차수", example = "5")
    val semester: Int,
    @Schema(description = "입학년도", example = "2022")
    val year: Int,
    @Schema(description = "소속 학과", example = "컴퓨터학부")
    val department: String,
    @Schema(description = "복수전공 학과", example = "경영학부", nullable = true)
    val doubleMajorDepartment: String?,
    @Schema(description = "부전공 학과", nullable = true)
    val minorDepartment: String?,
    @Schema(description = "교직 이수 여부", example = "false")
    val teaching: Boolean,
)

@Schema(description = "학적정보 수정 요청")
data class StudentInfoUpdateRequest(
    @Schema(description = "학년", example = "3")
    val grade: Int,
    @Schema(description = "학기 차수", example = "5")
    val semester: Int,
    @Schema(description = "입학년도", example = "2022")
    val year: Int,
    @Schema(description = "소속 학과", example = "컴퓨터학부")
    val department: String,
    @Schema(description = "복수전공 학과", example = "경영학부", nullable = true)
    val doubleMajorDepartment: String?,
    @Schema(description = "부전공 학과", nullable = true)
    val minorDepartment: String?,
    @Schema(description = "교직 이수 여부", example = "false")
    val teaching: Boolean,
)
