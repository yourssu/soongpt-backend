package com.yourssu.soongpt.domain.sso.application.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동기화 상태 응답")
data class SyncStatusResponse(
    @Schema(description = "동기화 상태", example = "COMPLETED", allowableValues = ["PROCESSING", "COMPLETED", "REQUIRES_REAUTH", "REQUIRES_USER_INPUT", "FAILED", "ERROR"])
    val status: String,
    @Schema(
        description = """에러/실패 사유 (status가 ERROR, FAILED, REQUIRES_REAUTH, REQUIRES_USER_INPUT일 때 포함).
            - invalid_session: 쿠키/JWT 문제 (재로그인 필요)
            - session_expired: 동기화 세션 만료 (재로그인 필요)
            - token_expired: sToken 만료 (재인증 필요, REQUIRES_REAUTH)
            - student_info_mapping_failed: 학년/학과/입학년도 매칭 실패 (사용자 입력 필요, REQUIRES_USER_INPUT)
            - student_info_mapping_failed: basic_info_unavailable: 유세인트 기본 학적 정보 조회 실패(데이터 없음 등) → 사용자 직접 입력 (REQUIRES_USER_INPUT)
            - server_unreachable: 유세인트 서버 접속 불가
            - server_timeout: 유세인트 서버 응답 시간 초과
            - internal_error: 내부 서버 오류""",
        example = "token_expired",
        nullable = true,
        allowableValues = ["invalid_session", "session_expired", "token_expired", "student_info_mapping_failed", "student_info_mapping_failed: basic_info_unavailable", "server_unreachable", "server_timeout", "internal_error"],
    )
    val reason: String? = null,
    @Schema(description = "학적정보 (COMPLETED 시에만 포함)", nullable = true)
    val studentInfo: StudentInfoResponse? = null,
    @Schema(
        description = "빈 데이터 경고 (COMPLETED 시에만). NO_COURSE_HISTORY: 수강 이력 없음. NO_SEMESTER_INFO: 학기 정보 없어 기본값 사용. NO_GRADUATION_DATA: 졸업사정표 조회 불가(동기화 단계·세션 저장). 과목 추천 API에서는 NO_GRADUATION_REPORT도 사용됨.",
        nullable = true,
        example = "[\"NO_COURSE_HISTORY\", \"NO_GRADUATION_DATA\"]",
    )
    val warnings: List<String>? = null,
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
