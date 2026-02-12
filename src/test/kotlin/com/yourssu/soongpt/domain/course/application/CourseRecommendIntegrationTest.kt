package com.yourssu.soongpt.domain.course.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.course.application.support.MockSessionBuilder
import com.yourssu.soongpt.domain.course.application.support.TakenStrategy
import com.yourssu.soongpt.domain.course.business.dto.CourseRecommendationsResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseTiming
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
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

/**
 * 과목 추천 통합 테스트.
 * - MockSessionBuilder로 실제 DB 과목 기반 세션 자동 생성 후 /api/courses/recommend/all 호출.
 * - Raw response JSON 출력(육안) + boolean assertion(요구사항 검증).
 *
 * 전제: recommend-integration profile + dev DB(MySQL). DB_URL, DB_USERNAME, DB_PASSWORD를 dev DB로 두고 실행.
 * read만 하므로 ddl-auto: validate.
 * 데이터가 없어도 noData/empty 메시지·progress 구조는 검증된다.
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
        // 기이수 과목 목록 (유지보수·디버깅용; 설계서 요청)
        println("=== Case 1 기이수 과목 목록 ===")
        session.takenCourses.forEach { sem ->
            println("[${sem.year}-${sem.semester}] ${sem.subjectCodes.joinToString(", ")}")
        }
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 1: 컴퓨터학부 21학번 3학년 (raw response) ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // 전기: PARTIAL_LATE 시나리오 — 과목이 있으면 아직 미충족 (progress nullable 호환)
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            if (majorBasic.courses.isNotEmpty()) {
                majorBasic.progress?.satisfied shouldBe false
                // LATE 포함 여부는 DB/학번별 데이터 의존적이므로 선택 검증
                if (majorBasic.courses.any { it.timing == CourseTiming.LATE }) {
                    majorBasic.message shouldBe null
                }
            }
        }

        // 복선: 데이터 있을 때 satisfied면 "이미 모두 이수" 메시지·과목 없음
        response.categories.find { it.category == "DOUBLE_MAJOR_ELECTIVE" }?.let { dme ->
            if (dme.progress?.satisfied == true) {
                dme.message.shouldNotBeNull()
                dme.message!! shouldContain "이수하셨습니다"
                dme.courses.size shouldBe 0
            }
        }

        // 재수강: 없음
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
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 2: 경영학부 24학번 2학년 ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // 전기: 해당 없음 (0/0/true) → satisfied 메시지
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            majorBasic.progress?.required shouldBe 0
            majorBasic.progress?.satisfied shouldBe true
        }

        // 교직: 비대상
        response.categories.find { it.category == "TEACHING" }?.let { teaching ->
            teaching.message.shouldNotBeNull()
            teaching.message!! shouldContain "교직이수 대상이 아닙니다"
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
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 3: 글로벌통상학과 22학번 3학년 ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // 전필: 해당 없음
        response.categories.find { it.category == "MAJOR_REQUIRED" }?.let { mr ->
            mr.progress?.required shouldBe 0
            mr.progress?.satisfied shouldBe true
        }

        // 복선: LATE 있을 수 있음
        response.categories.find { it.category == "DOUBLE_MAJOR_ELECTIVE" }?.let { dme ->
            dme.progress?.satisfied shouldBe false
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
        registerSession(session)

        val result = performRecommend()
        val responseBody = result.andReturn().response.contentAsString
        println("=== Case 4: 회계학과 26학번 1학년 ===")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(responseBody)))

        val response = parseResponse(responseBody)

        // 전기: 해당 없음
        response.categories.find { it.category == "MAJOR_BASIC" }?.let { majorBasic ->
            majorBasic.progress?.required shouldBe 0
            majorBasic.progress?.satisfied shouldBe true
        }

        // 복전/부전공: 미등록 메시지
        response.categories.find { it.category == "DOUBLE_MAJOR_REQUIRED" }?.let { dmr ->
            dmr.message shouldBe "복수전공을 등록하지 않았습니다."
        }
        response.categories.find { it.category == "MINOR" }?.let { minor ->
            minor.message.shouldNotBeNull()
            minor.message!! shouldContain "부전공"
        }

        // 재수강: 없음
        response.categories.find { it.category == "RETAKE" }?.let { retake ->
            retake.message shouldBe "재수강 가능한 C+ 이하 과목이 없습니다."
        }
    }
}
