package com.yourssu.soongpt.domain.course.implement.utils

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FieldFinderTest : BehaviorSpec({

    given("FieldFinder.findFieldBySchoolId") {
        val sampleField = """['23이후]자기개발·진로탐색
['20,'21~'22]공동체/리더십,숭실품성-자기계발과진로탐색
['19]균형교양-사회과학(사회/역사)
['16-'18]실용교양(자기개발과진로탐색)
['15이전]학문과진로탐색(실용-생활)"""

        `when`("schoolId 23으로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(23, sampleField)

            then("자기개발·진로탐색을 반환해야 한다") {
                result shouldBe "자기개발·진로탐색"
            }
        }

        `when`("schoolId 14로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(14, sampleField)

            then("학문과진로탐색(실용-생활)을 반환해야 한다") {
                result shouldBe "학문과진로탐색(실용-생활)"
            }
        }

        `when`("schoolId 20으로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(20, sampleField)

            then("범위에 포함되는 결과를 반환해야 한다") {
                result shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
            }
        }

        `when`("schoolId 22로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(22, sampleField)

            then("범위에 포함되는 결과를 반환해야 한다") {
                result shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
            }
        }

        `when`("schoolId 19로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(19, sampleField)

            then("정확히 19에 해당하는 규칙을 반환해야 한다") {
                result shouldBe "균형교양-사회과학(사회/역사)"
            }
        }

        `when`("범위에 포함되지 않는 schoolId 100으로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(100, sampleField)

            then("빈 문자열을 반환해야 한다") {
                result shouldBe ""
            }
        }

        `when`("빈 field로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(20, "")

            then("빈 문자열을 반환해야 한다") {
                result shouldBe ""
            }
        }

        `when`("잘못된 형식의 field로 호출할 때") {
            val invalidField = "잘못된 형식의 데이터"
            val result = FieldFinder.findFieldBySchoolId(20, invalidField)

            then("빈 문자열을 반환해야 한다") {
                result shouldBe ""
            }
        }

        `when`("여러 규칙이 겹치는 경우") {
            val overlappingField = """['18]실용교양(자기개발과진로탐색)
['16-'18]균형교양(사회과학-사회/정치/경제)
['15이전]학문과진로탐색(실용-생활)"""
            
            val result = FieldFinder.findFieldBySchoolId(18, overlappingField)

            then("더 높은 시작 연도를 가진 규칙을 반환해야 한다") {
                result shouldBe "실용교양(자기개발과진로탐색)"
            }
        }

        `when`("이후 키워드가 포함된 경우") {
            val afterField = "['25이후]미래교육과정"
            val result = FieldFinder.findFieldBySchoolId(30, afterField)

            then("해당 필드를 반환해야 한다") {
                result shouldBe "미래교육과정"
            }
        }

        `when`("이전 키워드가 포함된 경우") {
            val beforeField = "['10이전]과거교육과정"
            val result = FieldFinder.findFieldBySchoolId(5, beforeField)

            then("해당 필드를 반환해야 한다") {
                result shouldBe "과거교육과정"
            }
        }
    }

    given("실제 JSON 데이터를 사용한 통합 테스트") {
        
        `when`("인간·언어 분야 복합 패턴 테스트") {
            val realField = """['23이후]인간·언어
['20,'21~'22]의사소통/글로벌,기초역량-국제어문
['19]기초역량-한국어의사소통과국제어문
['16-'18]기초역량(국제어문-국제어)
['15이전]세계의언어(핵심-창의)"""

            then("schoolId 24는 가장 최신 규칙을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(24, realField)
                result shouldBe "인간·언어"
            }

            then("schoolId 21은 범위 내 규칙을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(21, realField)
                result shouldBe "의사소통/글로벌,기초역량-국제어문"
            }

            then("schoolId 19는 정확히 매칭되는 규칙을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(19, realField)
                result shouldBe "기초역량-한국어의사소통과국제어문"
            }

            then("schoolId 17은 범위 패턴을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(17, realField)
                result shouldBe "기초역량(국제어문-국제어)"
            }

            then("schoolId 12는 이전 패턴을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(12, realField)
                result shouldBe "세계의언어(핵심-창의)"
            }
        }

        `when`("자기개발·진로탐색 분야 복합 패턴 테스트") {
            val careerField = """['23이후]자기개발·진로탐색
['20,'21~'22]공동체/리더십,숭실품성-자기계발과진로탐색
['19]숭실품성-인성과리더십
['16-'18]실용교양(자기개발과진로탐색)
['15이전]학문과진로탐색(실용-생활)"""

            then("각 연도별로 정확한 분류를 반환해야 한다") {
                FieldFinder.findFieldBySchoolId(25, careerField) shouldBe "자기개발·진로탐색"
                FieldFinder.findFieldBySchoolId(22, careerField) shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
                FieldFinder.findFieldBySchoolId(19, careerField) shouldBe "숭실품성-인성과리더십"
                FieldFinder.findFieldBySchoolId(18, careerField) shouldBe "실용교양(자기개발과진로탐색)"
                FieldFinder.findFieldBySchoolId(13, careerField) shouldBe "학문과진로탐색(실용-생활)"
            }
        }

        `when`("기독교과목이 포함된 복합 패턴 테스트") {
            val christianField = """기독교과목
['23이후]자기개발·진로탐색
['20,'21~'22]공동체/리더십,숭실품성-인성과리더십
['19]숭실품성-인성과리더십
['16-'18]숭실품성(인성-종교가치인성교육)
['15이전]인성과리더쉽(핵심-창의)"""

            then("기독교과목 접두사가 있어도 연도별 분류가 작동해야 한다") {
                FieldFinder.findFieldBySchoolId(24, christianField) shouldBe "자기개발·진로탐색"
                FieldFinder.findFieldBySchoolId(21, christianField) shouldBe "공동체/리더십,숭실품성-인성과리더십"
                FieldFinder.findFieldBySchoolId(19, christianField) shouldBe "숭실품성-인성과리더십" 
                FieldFinder.findFieldBySchoolId(17, christianField) shouldBe "숭실품성(인성-종교가치인성교육)"
                FieldFinder.findFieldBySchoolId(14, christianField) shouldBe "인성과리더쉽(핵심-창의)"
            }
        }

        `when`("숭실사이버대과목이 포함된 패턴 테스트") {
            val cyberField = """숭실사이버대과목
['23이후]사회·정치·경제
['20,'21~'22]창의/융합,균형교양-사회·문화·심리
['19]균형교양-사회과학(사회/역사)
['16-'18]균형교양(사회과학-사회/정치/경제)
['15이전]인간과사회(융합-사회)"""

            then("사이버대 접두사가 있어도 정상 동작해야 한다") {
                FieldFinder.findFieldBySchoolId(23, cyberField) shouldBe "사회·정치·경제"
                FieldFinder.findFieldBySchoolId(20, cyberField) shouldBe "창의/융합,균형교양-사회·문화·심리"
                FieldFinder.findFieldBySchoolId(19, cyberField) shouldBe "균형교양-사회과학(사회/역사)"
            }
        }

        `when`("특수 문자가 포함된 필드명 테스트") {
            val specialCharField = """['23이후]과학·기술
['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술
['19]균형교양-자연/공학(자연/과학/기술)
['16-'18]기초역량(과학정보기술-정보기술)
['15이전]정보와기술(융합-자연)"""

            then("슬래시, 괄호, 하이픈이 포함된 필드명도 정확히 추출해야 한다") {
                FieldFinder.findFieldBySchoolId(23, specialCharField) shouldBe "과학·기술"
                FieldFinder.findFieldBySchoolId(21, specialCharField) shouldBe "창의/융합,균형교양-자연과학·공학·기술"
                FieldFinder.findFieldBySchoolId(19, specialCharField) shouldBe "균형교양-자연/공학(자연/과학/기술)"
                FieldFinder.findFieldBySchoolId(17, specialCharField) shouldBe "기초역량(과학정보기술-정보기술)"
                FieldFinder.findFieldBySchoolId(14, specialCharField) shouldBe "정보와기술(융합-자연)"
            }
        }

        `when`("경계값 테스트") {
            val boundaryField = """['23이후]최신과정
['20,'21~'22]중간과정
['19]정확히19
['16-'18]범위과정
['15이전]과거과정"""

            then("경계값에서 정확히 분류되어야 한다") {
                FieldFinder.findFieldBySchoolId(23, boundaryField) shouldBe "최신과정" // 경계 시작
                FieldFinder.findFieldBySchoolId(22, boundaryField) shouldBe "중간과정" // 범위 끝
                FieldFinder.findFieldBySchoolId(20, boundaryField) shouldBe "중간과정" // 범위 시작
                FieldFinder.findFieldBySchoolId(18, boundaryField) shouldBe "범위과정" // 범위 끝
                FieldFinder.findFieldBySchoolId(16, boundaryField) shouldBe "범위과정" // 범위 시작
                FieldFinder.findFieldBySchoolId(15, boundaryField) shouldBe "과거과정" // 이전 경계
            }
        }

        `when`("빈 라인이 포함된 패턴 테스트") {
            val emptyLineField = """['23이후]테스트과정


['19]중간과정
['15이전]과거과정"""

            then("빈 라인이 있어도 정상 동작해야 한다") {
                FieldFinder.findFieldBySchoolId(24, emptyLineField) shouldBe "테스트과정"
                FieldFinder.findFieldBySchoolId(19, emptyLineField) shouldBe "중간과정"
                FieldFinder.findFieldBySchoolId(14, emptyLineField) shouldBe "과거과정"
            }
        }
    }
})
