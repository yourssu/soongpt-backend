package com.yourssu.soongpt.domain.course.implement.utils

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * GeneralElectiveFieldDisplayMapper의 부분 매칭 로직 검증.
 * - 정확히 매칭되는 항목이 우선 적용되는지
 * - 부분 매칭 시 예상 가능한 동작(맵 순서 기반)이 유지되는지
 * - 매칭 실패 시 원문 반환
 */
class GeneralElectiveFieldDisplayMapperTest : BehaviorSpec({

    given("mapForCourseField ~22학번") {

        `when`("정확히 매칭되는 raw가 있으면") {
            then("해당 표시용 값만 반환한다") {
                // 정확 매칭 우선 검증
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-인성과리더십", 2022) shouldBe "인성과 리더십"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-자기계발과진로탐색", 2022) shouldBe "자기계발과 진로탐색"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-자기계발과진로", 2022) shouldBe "자기계발과 진로탐색"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-한국어의사소통", 2022) shouldBe "한국어의사소통"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-문학·예술", 2022) shouldBe "문학·예술"
            }
        }

        `when`("정확 매칭이 없고 부분 매칭만 가능할 때") {
            then("afterDash가 raw의 접두사인 경우(예: 인성) 의도대로 한 항목에 매칭된다") {
                // "인성" -> "인성과리더십" (raw.startsWith("인성"))
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-인성", 2022) shouldBe "인성과 리더십"
            }
            then("afterDash가 raw의 접두사인 경우(예: 자기) 맵에 정의된 순서상 첫 번째 매칭 항목의 표시값을 반환한다") {
                // "자기" -> map 순서상 "자기계발과진로탐색"이 "자기계발과진로"보다 먼저이므로 동일 표시 "자기계발과 진로탐색"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-자기", 2022) shouldBe "자기계발과 진로탐색"
            }
            then("raw가 afterDash의 접두사인 경우(예: 짧은 raw) 해당 표시값을 반환한다") {
                // "국제어문" 정확 매칭 있음. 부분만 보면 "한국어의사소통과국제어문".startsWith("국제어문") false, "국제어문".startsWith("한국어의사소통과국제어문") false
                // "국제어문" 정확 매칭 -> "국제어문"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-국제어문", 2022) shouldBe "국제어문"
            }
        }

        `when`("정확 매칭도 부분 매칭도 없으면") {
            then("afterDash를 그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-알수없는분야", 2022) shouldBe "알수없는분야"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("x-존재하지않는키", 2022) shouldBe "존재하지않는키"
            }
        }

        `when`("rawField가 빈 문자열이면") {
            then("그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForCourseField("", 2022) shouldBe ""
            }
        }

        `when`("대시 없이 소분류만 있는 rawField면") {
            then("전체가 afterDash로 사용되어 매칭된다") {
                GeneralElectiveFieldDisplayMapper.mapForCourseField("인성과리더십", 2022) shouldBe "인성과 리더십"
            }
        }
    }

    given("mapForCourseField 23학번 이상") {
        `when`("rawField가 주어지면") {
            then("매핑 없이 raw 그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForCourseField("인간·언어", 2023) shouldBe "인간·언어"
                GeneralElectiveFieldDisplayMapper.mapForCourseField("문화·예술", 2024) shouldBe "문화·예술"
            }
        }
    }
})
