package com.yourssu.soongpt.domain.sso.implement

import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse

/**
 * 로컬/개발 환경에서 추천 API 테스트용 mock usaint 데이터.
 * 필드는 아래 주석을 참고해 채운 뒤, POST /api/dev/mock-user-token 으로 세션 생성 후
 * 동일 쿠키로 GET /api/courses/recommend/all?category=... 호출하면 됨.
 */
object MockUsaintData {

    /** 추천 API에서 사용하는 pseudonym. SyncSessionStore 에 이 키로 세션이 들어감. */
    const val MOCK_USER_PSEUDONYM = "MOCK_USER"

    fun build(): RusaintUsaintDataResponse = RusaintUsaintDataResponse(
        pseudonym = MOCK_USER_PSEUDONYM,
        // ---------- takenCourses: 이수한 과목 목록. 교필 분야 제외·전공 이수 제외 등에 사용됨.
        // subjectCodes: rusaint 기준 8자리 과목코드 문자열. 예: "21501021" (글로벌시민의식)
        takenCourses = listOf(
            // RusaintTakenCourseDto(year = 이수 연도, semester = "1"|"2", subjectCodes = listOf("8자리코드"))
            // RusaintTakenCourseDto(year = 2024, semester = "1", subjectCodes = listOf("21501021")),
        ),
        // ---------- lowGradeSubjectCodes: D등급 이하 과목 코드 (재수강 추천용)
        lowGradeSubjectCodes = emptyList(),
        // ---------- flags: 복수전공/부전공/교직 여부
        flags = RusaintStudentFlagsDto(
            doubleMajorDepartment = null,
            minorDepartment = null,
            teaching = false,
        ),
        // ---------- basicInfo: 학적 기반. 추천 시 학과·학년·schoolId(학년도) 로 사용됨.
        basicInfo = RusaintBasicInfoDto(
            year = 2023,       // 입학년도. schoolId = year % 100 (23학번 → 23)
            semester = 5,      // 현재까지 수강 학기 수 (참고용)
            grade = 3,         // 현재 학년. 전공선택 학년별 그룹·교필 LATE/ON_TIME 판단에 사용
            department = "",   // 학과명. departmentReader.getByName(department) 에 사용 (DB에 있는 학과명으로)
        ),
        graduationRequirements = null,
        // ---------- graduationSummary: 졸업사정표 요약. progress·이수충족 여부(satisfied) 판단에 사용
        graduationSummary = RusaintGraduationSummaryDto(
            generalRequired = RusaintCreditSummaryItemDto(required = 12, completed = 0, satisfied = false),
            generalElective = RusaintCreditSummaryItemDto(required = 12, completed = 0, satisfied = false),
            majorFoundation = RusaintCreditSummaryItemDto(required = 15, completed = 0, satisfied = false),
            majorRequired = RusaintCreditSummaryItemDto(required = 21, completed = 0, satisfied = false),
            majorElective = RusaintCreditSummaryItemDto(required = 30, completed = 0, satisfied = false),
            minor = RusaintCreditSummaryItemDto(required = 0, completed = 0, satisfied = true),
            doubleMajorRequired = RusaintCreditSummaryItemDto(required = 0, completed = 0, satisfied = true),
            doubleMajorElective = RusaintCreditSummaryItemDto(required = 0, completed = 0, satisfied = true),
            christianCourses = RusaintCreditSummaryItemDto(required = 6, completed = 0, satisfied = false),
            chapel = RusaintChapelSummaryItemDto(satisfied = false),
        ),
        warnings = emptyList(),
    )
}
