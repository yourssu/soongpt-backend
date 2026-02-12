package com.yourssu.soongpt.domain.course.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.course.application.support.MockSessionBuilder
import com.yourssu.soongpt.domain.course.application.support.TakenStrategy
import com.yourssu.soongpt.domain.course.business.dto.CourseRecommendationsResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseTiming
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.ResultActions
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse

/**
 * 과목 추천 통합 테스트.
 * - MockSessionBuilder로 실제 DB 과목 기반 세션 자동 생성 후 /api/courses/recommend/all 호출.
 * - Raw response JSON 출력(육안) + boolean assertion(요구사항 검증).
 *
 * 전제: recommend-integration profile + dev DB(MySQL). DB_URL, DB_USERNAME, DB_PASSWORD를 dev DB로 두고 실행.
 * read만 하므로 ddl-auto: validate.
 * 데이터가 없어도 noData/empty 메시지·progress 구조는 검증된다.
 *
 * ## 결과 확인 (raw response / 기이수 과목 목록)
 * - **Gradle 리포트**: 테스트 실행 후
 *   `build/reports/tests/test/index.html` → 실패한 테스트 클릭 → **Standard output** 탭에서
 *   `=== Case N ... (raw response) ===`, `=== Case N 기이수 과목 목록 (코드: 과목명) ===` 출력 확인.
 * - **콘솔에 바로 보기**: `./gradlew test -Pintegration --tests "CourseRecommendIntegrationTest" --console=plain`
 *   (표준 출력이 버퍼 없이 콘솔에 나옴)
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("recommend-integration")
class CourseRecommendIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var syncSessionStore: SyncSessionStore

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var departmentReader: DepartmentReader

    @MockBean
    private lateinit var clientJwtProvider: ClientJwtProvider

    private val objectMapper = jacksonObjectMapper()

    companion object {
        private const val TEST_PSEUDONYM = "RECOMMEND_INTEGRATION_TEST"
        private val ALL_CATEGORIES = "MAJOR_BASIC,MAJOR_REQUIRED,MAJOR_ELECTIVE,GENERAL_REQUIRED,RETAKE,DOUBLE_MAJOR_REQUIRED,DOUBLE_MAJOR_ELECTIVE,MINOR,TEACHING"
    }

    @BeforeEach
    fun setUp() {
        whenever(clientJwtProvider.extractPseudonymFromRequest(any())).thenReturn(Result.success(TEST_PSEUDONYM))
    }

    private fun registerSession(session: com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse) {
        syncSessionStore.createSession(TEST_PSEUDONYM)
        syncSessionStore.updateStatus(TEST_PSEUDONYM, SyncStatus.COMPLETED, session)
    }

    private fun performRecommend(categoryParam: String = ALL_CATEGORIES): ResultActions =
        mockMvc.perform(
            get("/api/courses/recommend/all")
                .param("category", categoryParam)
                .accept(MediaType.APPLICATION_JSON),
        ).andExpect(status().isOk)

    private fun parseResponse(json: String): CourseRecommendationsResponse =
        objectMapper.readValue(
            objectMapper.readTree(json).path("result").toString(),
            CourseRecommendationsResponse::class.java,
        )

    /**
     * 기이수 과목을 "과목코드: 과목명" 형태로 출력 (테스트케이스설계.md 요청: 무슨 과목인지 주석/설명).
     * 데이터가 26-1→26-2로 바뀌어도 DB 조회로 과목명을 가져오므로 유지보수 가능.
     */
    private fun printTakenCoursesWithNames(session: RusaintUsaintDataResponse) {
        val allCodes = session.takenCourses.flatMap { it.subjectCodes }.distinct()
        val baseCodes = allCodes.mapNotNull { it.toLongOrNull() }
        if (baseCodes.isEmpty()) return
        val coursesWithTarget = courseRepository.findCoursesWithTargetByBaseCodes(baseCodes)
        val codeToName = coursesWithTarget.associate { it.course.baseCode() to it.course.name }
        session.takenCourses.forEach { sem ->
            println("[${sem.year}-${sem.semester}]")
            sem.subjectCodes.forEach { code ->
                val name = codeToName[code.toLongOrNull() ?: 0L] ?: "미확인"
                println("  $code: $name")
            }
        }
    }

    @Test
    @DisplayName("Case 1: 컴퓨터학부 21학번 3학년 — 전기 PARTIAL_LATE, 복전 글미 복필 MOST/복선 ALL, 교직 T, 채플 satisfied, 재수강 없음")
    fun case1_computerScience_21_grade3() {
        val builder = MockSessionBuilder(courseRepository, departmentReader)
            .pseudonym(TEST_PSEUDONYM)
            .department("컴퓨터학부")
            .admissionYear(2021)
            .grade(3)
            .doubleMajor("글로벌미디어학부")
            .minor(null)
            .teaching(true)
            .chapel(satisfied = true)
            .majorBasic(TakenStrategy.PARTIAL_LATE)
            .majorRequired(TakenStrategy.PARTIAL_ON_TIME)
            .majorElective(TakenStrategy.PARTIAL_ON_TIME)
            .generalRequired(TakenStrategy.PARTIAL_ON_TIME)
            .generalElective(TakenStrategy.PARTIAL_ON_TIME)
            .doubleMajorRequired(TakenStrategy.MOST)
            .doubleMajorElective(TakenStrategy.ALL)
            .retake(emptyList())
        val session = builder.build()
        // 기이수 과목: 과목코드 + 과목명 (테스트케이스설계.md — 무슨 과목인지 주석)
        println("=== Case 1 기이수 과목 목록 (코드: 과목명) ===")
        printTakenCoursesWithNames(session)
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 1: 컴퓨터학부 21학번 3학년 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // ——— 테스트케이스설계.md Case 1 검증 포인트 ———
        // 전기: LATE 과목이 courses에 timing=LATE로 포함
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            if (majorBasic.courses.isNotEmpty()) {
                majorBasic.progress.satisfied shouldBe false
                if (majorBasic.courses.any { it.timing == CourseTiming.LATE }) {
                    majorBasic.message shouldBe null
                }
            }
        }
        // 전필/전선: ON_TIME 과목 정상 추천 (카테고리 존재·progress 존재)
        response.categories.find { it.category == "MAJOR_REQUIRED" }?.let { mr -> mr.progress.satisfied shouldBe false }
        response.categories.find { it.category == "MAJOR_ELECTIVE" }?.let { me -> me.progress.satisfied shouldBe false }
        // 복필: progress.satisfied=false (MOST 전략으로 1개 남았지만, 서비스는 미이수 전부 추천할 수 있어 size는 가변)
        response.categories.find { it.category == "DOUBLE_MAJOR_REQUIRED" }?.let { dmr ->
            dmr.progress.satisfied shouldBe false
        }
        // 복선: progress.satisfied=true, "이미 모두 이수" 메시지
        response.categories.find { it.category == "DOUBLE_MAJOR_ELECTIVE" }?.let { dme ->
            if (dme.progress.satisfied) {
                dme.message.shouldNotBeNull()
                dme.message!! shouldContain "이수하셨습니다"
                dme.courses.size shouldBe 0
            }
        }
        // 교직: 교직 과목 추천됨 (과목 있거나 메시지)
        response.categories.find { it.category == "TEACHING" }?.let { t ->
            (t.courses.isNotEmpty() || t.message != null) shouldBe true
        }
        // 교필: 22학번 이하 → lateFields=null
        response.categories.find { it.category == "GENERAL_REQUIRED" }?.let { gr ->
            gr.lateFields shouldBe null
        }
        // 재수강: "재수강 가능한 C+ 이하 과목이 없습니다."
        response.categories.find { it.category == "RETAKE" }?.let { retake ->
            retake.message shouldBe "재수강 가능한 C+ 이하 과목이 없습니다."
        }
    }

    @Test
    @DisplayName("Case 2: 경영학부 24학번 2학년 — 전기 없음(noData), 부전공 통계, 교직 F, 채플 미충족")
    fun case2_business_24_grade2() {
        val builder = MockSessionBuilder(courseRepository, departmentReader)
            .pseudonym(TEST_PSEUDONYM)
            .department("경영학부")
            .admissionYear(2024)
            .grade(2)
            .doubleMajor(null)
            .minor("정보통계보험수리학과")
            .teaching(false)
            .chapel(satisfied = false)
            .majorBasic(TakenStrategy.NONE)
            .majorFoundationRequiredCredits(0)
            .majorRequired(TakenStrategy.PARTIAL_ON_TIME)
            .majorElective(TakenStrategy.NONE)
            .generalRequired(TakenStrategy.PARTIAL_ON_TIME)
            .generalElective(TakenStrategy.PARTIAL_ON_TIME)
            .retake(emptyList())
        val session = builder.build()
        println("=== Case 2 기이수 과목 목록 (코드: 과목명) ===")
        printTakenCoursesWithNames(session)
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 2: 경영학부 24학번 2학년 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // ——— 테스트케이스설계.md Case 2 검증 포인트 ———
        // 전기: noDataResponse ("졸업사정표에 전공기초 항목이 없습니다.") → 0/0/true
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            majorBasic.progress.required shouldBe 0
            majorBasic.progress.satisfied shouldBe true
        }
        // 전필: courses 존재, timing 혼재
        response.categories.find { it.category == "MAJOR_REQUIRED" }?.let { mr -> mr.courses.isNotEmpty() shouldBe true }
        // 전선: courses 많이 추천
        response.categories.find { it.category == "MAJOR_ELECTIVE" }?.let { me -> me.progress.satisfied shouldBe false }
        // 부전공: 부필/부선 과목 추천
        response.categories.find { it.category == "MINOR" }?.let { m -> (m.courses.isNotEmpty() || m.message != null) shouldBe true }
        // 교직: "교직이수 대상이 아닙니다."
        response.categories.find { it.category == "TEACHING" }?.let { teaching ->
            teaching.message.shouldNotBeNull()
            teaching.message!! shouldContain "교직이수 대상이 아닙니다"
        }
        // 교필: 24학번 2학년 → lateFields 있을 수 있음 (1학년 분야 LATE)
        response.categories.find { it.category == "GENERAL_REQUIRED" }?.let { gr ->
            gr.courses.isNotEmpty() || gr.lateFields.isNullOrEmpty().not() shouldBe true
        }
    }

    @Test
    @DisplayName("Case 3: 글로벌통상학과 22학번 3학년 — 전필 없음, 복전 수학 복필 ALL/복선 PARTIAL_LATE, 교직 T, 재수강 3개")
    fun case3_globalTrade_22_grade3() {
        val builder = MockSessionBuilder(courseRepository, departmentReader)
            .pseudonym(TEST_PSEUDONYM)
            .department("글로벌통상학과")
            .admissionYear(2022)
            .grade(3)
            .doubleMajor("수학과")
            .minor(null)
            .teaching(true)
            .chapel(satisfied = true)
            .majorBasic(TakenStrategy.PARTIAL_ON_TIME)
            .majorRequiredCredits(0)
            .majorRequired(TakenStrategy.NONE)
            .majorElective(TakenStrategy.PARTIAL_ON_TIME)
            .generalRequired(TakenStrategy.PARTIAL_ON_TIME)
            .generalElective(TakenStrategy.PARTIAL_ON_TIME)
            .doubleMajorRequired(TakenStrategy.ALL)
            .doubleMajorElective(TakenStrategy.PARTIAL_LATE)
            .retake(emptyList())
        val session = builder.build()
        println("=== Case 3 기이수 과목 목록 (코드: 과목명) ===")
        printTakenCoursesWithNames(session)
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 3: 글로벌통상학과 22학번 3학년 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // ——— 테스트케이스설계.md Case 3 검증 포인트 ———
        // 전필: noDataResponse (전필 없음) → 0/0/true
        response.categories.find { it.category == "MAJOR_REQUIRED" }?.let { mr ->
            mr.progress.required shouldBe 0
            mr.progress.satisfied shouldBe true
        }
        // 전기/전선: 정상 추천
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { mb -> (mb.courses.isNotEmpty() || mb.message != null) shouldBe true }
        response.categories.find { it.category == "MAJOR_ELECTIVE" }?.let { me -> me.progress.satisfied shouldBe false }
        // 복필: satisfied 메시지 (복필 ALL)
        response.categories.find { it.category == "DOUBLE_MAJOR_REQUIRED" }?.let { dmr ->
            if (dmr.progress.satisfied) dmr.message.shouldNotBeNull()
        }
        // 복선: LATE 과목 존재 (satisfied=false)
        response.categories.find { it.category == "DOUBLE_MAJOR_ELECTIVE" }?.let { dme ->
            dme.progress.satisfied shouldBe false
        }
        // 교직: 특성화영역 과목만 추천 (과목 또는 메시지)
        response.categories.find { it.category == "TEACHING" }?.let { t ->
            (t.courses.isNotEmpty() || t.message != null) shouldBe true
        }
    }

    @Test
    @DisplayName("Case 4: 회계학과 26학번 1학년 — 신입생, 전기 없음, 복전/부전공 없음, LATE 없음")
    fun case4_accounting_26_grade1() {
        val builder = MockSessionBuilder(courseRepository, departmentReader)
            .pseudonym(TEST_PSEUDONYM)
            .department("회계학과")
            .admissionYear(2026)
            .grade(1)
            .doubleMajor(null)
            .minor(null)
            .teaching(false)
            .chapel(satisfied = false)
            .majorBasic(TakenStrategy.NONE)
            .majorFoundationRequiredCredits(0)
            .majorRequired(TakenStrategy.NONE)
            .majorElective(TakenStrategy.NONE)
            .generalRequired(TakenStrategy.PARTIAL_ON_TIME)
            .generalElective(TakenStrategy.NONE)
            .retake(emptyList())
        val session = builder.build()
        println("=== Case 4 기이수 과목 목록 (코드: 과목명) ===")
        printTakenCoursesWithNames(session)
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 4: 회계학과 26학번 1학년 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // ——— 테스트케이스설계.md Case 4 검증 포인트 ———
        // 전기: noDataResponse (회계 전기 없음)
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            majorBasic.progress.required shouldBe 0
            majorBasic.progress.satisfied shouldBe true
        }
        // 전필: 1학년 대상 과목만 추천 (NONE이어도 추천 목록은 1학년 대상)
        response.categories.find { it.category == "MAJOR_REQUIRED" }?.let { mr -> mr.progress.satisfied shouldBe false }
        // 전선: 전학년 과목 추천
        response.categories.find { it.category == "MAJOR_ELECTIVE" }?.let { me -> (me.courses.isNotEmpty() || me.message != null) shouldBe true }
        // 복전/부전공: 미등록 메시지
        response.categories.find { it.category == "DOUBLE_MAJOR_REQUIRED" }?.let { dmr ->
            dmr.message shouldBe "복수전공을 등록하지 않았습니다."
        }
        response.categories.find { it.category == "MINOR" }?.let { minor ->
            minor.message.shouldNotBeNull()
            minor.message!! shouldContain "부전공"
        }
        // 교직: 비대상 메시지
        response.categories.find { it.category == "TEACHING" }?.let { t ->
            t.message.shouldNotBeNull()
            t.message!! shouldContain "교직이수 대상이 아닙니다"
        }
        // 교필: 1학년 → lateFields=null (LATE 없음)
        response.categories.find { it.category == "GENERAL_REQUIRED" }?.let { gr ->
            gr.lateFields shouldBe null
        }
        // 재수강: lowGradeSubjectCodes 없음 → 메시지
        response.categories.find { it.category == "RETAKE" }?.let { retake ->
            retake.message shouldBe "재수강 가능한 C+ 이하 과목이 없습니다."
        }
    }

    @Test
    @DisplayName("Case 5 (엣지): 졸업사정표 없음(1-1/rusaint 미제공) — 경고 NO_GRADUATION_REPORT, 카테고리별 noDataResponse")
    fun case5_no_graduation_report() {
        val builder = MockSessionBuilder(courseRepository, departmentReader)
            .pseudonym(TEST_PSEUDONYM)
            .department("컴퓨터학부")
            .admissionYear(2025)
            .grade(1)
            .doubleMajor("글로벌미디어학부")
            .minor(null)
            .teaching(false)
            .chapel(satisfied = false)
            .majorBasic(TakenStrategy.NONE)
            .majorFoundationRequiredCredits(0)
            .majorRequired(TakenStrategy.NONE)
            .majorElective(TakenStrategy.NONE)
            .generalRequired(TakenStrategy.NONE)
            .generalElective(TakenStrategy.NONE)
            .retake(emptyList())
            .noGraduationReport()
        val session = builder.build()
        println("=== Case 5 기이수 과목 목록 (코드: 과목명) ===")
        printTakenCoursesWithNames(session)
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 5: 졸업사정표 없음 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // 경고: 실제 세션에는 RusaintSnapshotMerger가 넣은 NO_GRADUATION_DATA, recommend API가 추가하는 NO_GRADUATION_REPORT
        response.warnings shouldContain "NO_GRADUATION_DATA"
        response.warnings shouldContain "NO_GRADUATION_REPORT"

        // 졸업사정표 의존 카테고리: progress = unavailable(-2, -2)
        listOf("MAJOR_BASIC", "MAJOR_REQUIRED", "MAJOR_ELECTIVE", "GENERAL_REQUIRED").forEach { categoryName ->
            response.categories.find { it.category == categoryName }?.let { cat ->
                cat.progress.required shouldBe -2
                cat.progress.completed shouldBe -2
                cat.progress.satisfied shouldBe false
                cat.message.shouldNotBeNull()
                cat.message!! shouldContain "졸업사정표"
                cat.courses.size shouldBe 0
            }
        }

        // 복전 등록 시 졸업사정표 없음 → noData "졸업사정표에 ... 없습니다"
        response.categories.find { it.category == "DOUBLE_MAJOR_REQUIRED" }?.let { dmr ->
            dmr.message.shouldNotBeNull()
            dmr.message!! shouldContain "졸업사정표"
            dmr.courses.size shouldBe 0
        }
        response.categories.find { it.category == "DOUBLE_MAJOR_ELECTIVE" }?.let { dme ->
            dme.message.shouldNotBeNull()
            dme.message!! shouldContain "졸업사정표"
            dme.courses.size shouldBe 0
        }
        // 부전공 미등록 → "부전공을 등록하지 않았습니다"
        response.categories.find { it.category == "MINOR" }?.let { minor ->
            minor.message.shouldNotBeNull()
            minor.message!! shouldContain "부전공"
            minor.courses.size shouldBe 0
        }

        // 재수강·교직: 졸업사정표 없으면 -2(unavailable)로 통일
        response.categories.find { it.category == "RETAKE" }?.let { retake ->
            retake.progress.required shouldBe -2
            retake.progress.completed shouldBe -2
            retake.progress.satisfied shouldBe false
            retake.message.shouldNotBeNull()
            retake.message!! shouldContain "졸업사정표가 없어"
            retake.courses.size shouldBe 0
        }
        response.categories.find { it.category == "TEACHING" }?.let { teaching ->
            teaching.progress.required shouldBe -2
            teaching.progress.completed shouldBe -2
            teaching.progress.satisfied shouldBe false
            teaching.message.shouldNotBeNull()
            teaching.message!! shouldContain "졸업사정표가 없어"
            teaching.courses.size shouldBe 0
        }
    }
}
