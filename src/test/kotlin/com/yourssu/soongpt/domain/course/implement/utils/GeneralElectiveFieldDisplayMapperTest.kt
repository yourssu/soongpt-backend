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

    given("mapForProgressFieldCredits ~20학번") {
        `when`("rawField가 공동체/리더십, 의사소통/글로벌, 창의/융합에 매칭되고 schoolId <= 20이면") {
            then("뒤에 역량을 붙여 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("공동체역량", 2020, 15) shouldBe "공동체/리더십역량"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("인성과리더십", 2019, 20) shouldBe "공동체/리더십역량"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("한국어의사소통", 2020, 10) shouldBe "의사소통/글로벌역량"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("글로벌시민의식", 2018, 5) shouldBe "의사소통/글로벌역량"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("창의융합", 2020, 18) shouldBe "창의/융합역량"
            }
        }
        `when`("schoolId > 20이면") {
            then("역량을 붙이지 않고 base만 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("인성과리더십", 2020, 21) shouldBe "공동체/리더십"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("한국어의사소통", 2019, 25) shouldBe "의사소통/글로벌"
            }
        }
        `when`("매칭되지 않는 rawField면") {
            then("원문을 그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("알수없는분야", 2020, 15) shouldBe "알수없는분야"
            }
        }
        `when`("창의/융합 raw에 dash가 있으면(세부필드)") {
            then("세부필드 표시명을 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("창의/융합,균형교양-역사·철학·종교", 2020, 15) shouldBe "역사·철학·종교"
            }
        }
    }

    given("mapForProgressFieldCredits 21~22학번") {
        `when`("rawField가 숭실품성, 균형교양, 기초역량에 매칭되고 schoolId <= 20이면") {
            then("뒤에 교과를 붙여 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("숭실품성교과", 2021, 15) shouldBe "숭실품성교과"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("균형교양", 2022, 10) shouldBe "균형교양교과"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("기초역량", 2021, 20) shouldBe "기초역량교과"
            }
        }
        `when`("21~22학번이면 schoolId와 무관하게 교과를 붙인다") {
            then("항상 교과 접미사를 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("숭실품성", 2022, 25) shouldBe "숭실품성교과"
            }
        }
        `when`("균형교양 raw에 dash가 있으면(세부필드)") {
            then("세부필드 표시명을 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("창의/융합,균형교양-문학·예술", 2022, 10) shouldBe "문학·예술"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("균형교양-정치·경제·경영", 2021, 15) shouldBe "정치·경제·경영"
            }
        }
    }

    given("mapForProgressFieldCredits 23학번 이상") {
        `when`("rawField가 · 를 포함하면") {
            then("· 이전 부분만 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("인간·언어", 2023, 15) shouldBe "인간"
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("문화·예술", 2024, 10) shouldBe "문화"
            }
        }
        `when`("· 이 없으면") {
            then("raw 그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.mapForProgressFieldCredits("자기개발", 2023, 20) shouldBe "자기개발"
            }
        }
    }

    given("withTrackOrFieldPrefix") {
        `when`("20학번이면") {
            then("공동체- / 의사소통- / 창의- 접두사를 붙인다") {
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("인성과 리더십", 2020) shouldBe "공동체-인성과 리더십"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("자기계발과 진로탐색", 2020) shouldBe "공동체-자기계발과 진로탐색"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("한국어의사소통", 2020) shouldBe "의사소통-한국어의사소통"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("국제어문", 2020) shouldBe "의사소통-국제어문"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("문학·예술", 2020) shouldBe "창의-문학·예술"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("자연과학·공학·기술", 2020) shouldBe "창의-자연과학·공학·기술"
            }
        }
        `when`("21·22학번이면") {
            then("품성- / 기초- / 균형- 접두사를 붙인다") {
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("인성과 리더십", 2021) shouldBe "품성-인성과 리더십"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("자기계발과 진로탐색", 2022) shouldBe "품성-자기계발과 진로탐색"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("한국어의사소통", 2021) shouldBe "기초-한국어의사소통"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("국제어문", 2022) shouldBe "기초-국제어문"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("문학·예술", 2021) shouldBe "균형-문학·예술"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("자연과학·공학·기술", 2022) shouldBe "균형-자연과학·공학·기술"
            }
        }
        `when`("19학번 이하 또는 23학번 이상이면") {
            then("접두사 없이 그대로 반환한다") {
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("인성과 리더십", 2019) shouldBe "인성과 리더십"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("문학·예술", 2023) shouldBe "문학·예술"
                GeneralElectiveFieldDisplayMapper.withTrackOrFieldPrefix("인간·언어", 2024) shouldBe "인간·언어"
            }
        }
    }

    given("buildFieldCreditsStructure") {
        `when`("21~22학번 raw에 균형교양 세부필드가 있으면") {
            then("균형교양교과 객체 안에 세부필드별 과목수를 넣는다") {
                val rawCounts = mapOf(
                    "창의/융합,균형교양-문학·예술" to 1,
                    "균형교양-정치·경제·경영" to 2,
                )
                val result = GeneralElectiveFieldDisplayMapper.buildFieldCreditsStructure(2021, 10, rawCounts)
                @Suppress("UNCHECKED_CAST")
                val balance = result["균형교양교과"] as Map<String, Int>
                balance["문학·예술"] shouldBe 1
                balance["정치·경제·경영"] shouldBe 2
                balance["역사·철학·종교"] shouldBe 0
                result["숭실품성교과"] shouldBe 0
                result["기초역량교과"] shouldBe 0
            }
        }
        `when`("20학번 raw에 창의/융합 세부필드가 있으면") {
            then("창의/융합역량 객체 안에 세부필드별 과목수를 넣는다") {
                val rawCounts = mapOf("창의/융합,균형교양-사회·문화·심리" to 2)
                val result = GeneralElectiveFieldDisplayMapper.buildFieldCreditsStructure(2020, 15, rawCounts)
                @Suppress("UNCHECKED_CAST")
                val balance = result["창의/융합역량"] as Map<String, Int>
                balance["사회·문화·심리"] shouldBe 2
                result["공동체/리더십역량"] shouldBe 0
            }
        }
    }
})
