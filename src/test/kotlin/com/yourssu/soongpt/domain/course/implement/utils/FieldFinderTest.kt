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
            val result = FieldFinder.findFieldBySchoolId(sampleField, 23)

            then("자기개발·진로탐색을 반환해야 한다") {
                result shouldBe "자기개발·진로탐색"
            }
        }

//        `when`("schoolId 14로 호출할 때") {
//            val result = FieldFinder.findFieldBySchoolId(sampleField, 14)
//
//            then("학문과진로탐색(실용-생활)을 반환해야 한다") {
//                result shouldBe "학문과진로탐색(실용-생활)"
//            }
//        }

        `when`("schoolId 20으로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(sampleField, 20)

            then("범위에 포함되는 결과를 반환해야 한다") {
                result shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
            }
        }

        `when`("schoolId 22로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId(sampleField, 22)

            then("범위에 포함되는 결과를 반환해야 한다") {
                result shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
            }
        }

//        `when`("schoolId 19로 호출할 때") {
//            val result = FieldFinder.findFieldBySchoolId(sampleField, 19)
//
//            then("정확히 19에 해당하는 규칙을 반환해야 한다") {
//                result shouldBe "균형교양-사회과학(사회/역사)"
//            }
//        }

        `when`("빈 field로 호출할 때") {
            val result = FieldFinder.findFieldBySchoolId("", 20)

            then("빈 문자열을 반환해야 한다") {
                result shouldBe ""
            }
        }

        `when`("잘못된 형식의 field로 호출할 때") {
            val invalidField = "잘못된 형식의 데이터"
            val result = FieldFinder.findFieldBySchoolId(invalidField, 20)

            then("빈 문자열을 반환해야 한다") {
                result shouldBe ""
            }
        }

//        `when`("여러 규칙이 겹치는 경우") {
//            val overlappingField = """['18]실용교양(자기개발과진로탐색)
//['16-'18]균형교양(사회과학-사회/정치/경제)
//['15이전]학문과진로탐색(실용-생활)"""
//
//            val result = FieldFinder.findFieldBySchoolId(overlappingField, 18)
//
//            then("더 높은 시작 연도를 가진 규칙을 반환해야 한다") {
//                result shouldBe "실용교양(자기개발과진로탐색)"
//            }
//        }

        `when`("이후 키워드가 포함된 경우") {
            val afterField = "['25이후]미래교육과정"
            val result = FieldFinder.findFieldBySchoolId(afterField, 30)

            then("해당 필드를 반환해야 한다") {
                result shouldBe "미래교육과정"
            }
        }

        `when`("이전 키워드가 포함된 경우") {
            val beforeField = "['10이전]과거교육과정"
            val result = FieldFinder.findFieldBySchoolId(beforeField, 5)

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
                val result = FieldFinder.findFieldBySchoolId(realField, 24)
                result shouldBe "인간·언어"
            }

            then("schoolId 21은 범위 내 규칙을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(realField, 21)
                result shouldBe "의사소통/글로벌,기초역량-국제어문"
            }

            then("schoolId 19는 정확히 매칭되는 규칙을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(realField, 19)
                result shouldBe "기초역량-한국어의사소통과국제어문"
            }

//            then("schoolId 17은 범위 패턴을 반환해야 한다") {
//                val result = FieldFinder.findFieldBySchoolId(realField, 17)
//                result shouldBe "기초역량(국제어문-국제어)"
//            }
//
//            then("schoolId 12는 이전 패턴을 반환해야 한다") {
//                val result = FieldFinder.findFieldBySchoolId(realField, 12)
//                result shouldBe "세계의언어(핵심-창의)"
//            }
        }

//        `when`("자기개발·진로탐색 분야 복합 패턴 테스트") {
//            val careerField = """['23이후]자기개발·진로탐색
//['20,'21~'22]공동체/리더십,숭실품성-자기계발과진로탐색
//['19]숭실품성-인성과리더십
//['16-'18]실용교양(자기개발과진로탐색)
//['15이전]학문과진로탐색(실용-생활)"""
//
//            then("각 연도별로 정확한 분류를 반환해야 한다") {
//                FieldFinder.findFieldBySchoolId(careerField, 25) shouldBe "자기개발·진로탐색"
//                FieldFinder.findFieldBySchoolId(careerField, 22) shouldBe "공동체/리더십,숭실품성-자기계발과진로탐색"
//                FieldFinder.findFieldBySchoolId(careerField, 19) shouldBe "숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(careerField, 18) shouldBe "실용교양(자기개발과진로탐색)"
//                FieldFinder.findFieldBySchoolId(careerField, 13) shouldBe "학문과진로탐색(실용-생활)"
//            }
//        }

//        `when`("기독교과목이 포함된 복합 패턴 테스트") {
//            val christianField = """기독교과목
//['23이후]자기개발·진로탐색
//['20,'21~'22]공동체/리더십,숭실품성-인성과리더십
//['19]숭실품성-인성과리더십
//['16-'18]숭실품성(인성-종교가치인성교육)
//['15이전]인성과리더쉽(핵심-창의)"""
//
//            then("기독교과목 접두사가 있어도 연도별 분류가 작동해야 한다") {
//                FieldFinder.findFieldBySchoolId(christianField, 24) shouldBe "자기개발·진로탐색"
//                FieldFinder.findFieldBySchoolId(christianField, 21) shouldBe "공동체/리더십,숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(christianField, 19) shouldBe "숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(christianField, 17) shouldBe "숭실품성(인성-종교가치인성교육)"
//                FieldFinder.findFieldBySchoolId(christianField, 14) shouldBe "인성과리더쉽(핵심-창의)"
//            }
//        }

//        `when`("숭실사이버대과목이 포함된 패턴 테스트") {
//            val cyberField = """숭실사이버대과목
//['23이후]사회·정치·경제
//['20,'21~'22]창의/융합,균형교양-사회·문화·심리
//['19]균형교양-사회과학(사회/역사)
//['16-'18]균형교양(사회과학-사회/정치/경제)
//['15이전]인간과사회(융합-사회)"""
//
//            then("사이버대 접두사가 있어도 정상 동작해야 한다") {
//                FieldFinder.findFieldBySchoolId(cyberField, 23) shouldBe "사회·정치·경제"
//                FieldFinder.findFieldBySchoolId(cyberField, 20) shouldBe "창의/융합,균형교양-사회·문화·심리"
//                FieldFinder.findFieldBySchoolId(cyberField, 19) shouldBe "균형교양-사회과학(사회/역사)"
//            }
//        }

//        `when`("특수 문자가 포함된 필드명 테스트") {
//            val specialCharField = """['23이후]과학·기술
//['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술
//['19]균형교양-자연/공학(자연/과학/기술)
//['16-'18]기초역량(과학정보기술-정보기술)
//['15이전]정보와기술(융합-자연)"""
//
//            then("슬래시, 괄호, 하이픈이 포함된 필드명도 정확히 추출해야 한다") {
//                FieldFinder.findFieldBySchoolId(specialCharField, 23) shouldBe "과학·기술"
//                FieldFinder.findFieldBySchoolId(specialCharField, 21) shouldBe "창의/융합,균형교양-자연과학·공학·기술"
//                FieldFinder.findFieldBySchoolId(specialCharField, 19) shouldBe "균형교양-자연/공학(자연/과학/기술)"
//                FieldFinder.findFieldBySchoolId(specialCharField, 17) shouldBe "기초역량(과학정보기술-정보기술)"
//                FieldFinder.findFieldBySchoolId(specialCharField, 14) shouldBe "정보와기술(융합-자연)"
//            }
//        }

        `when`("경계값 테스트") {
            val boundaryField = """['23이후]최신과정
['20,'21~'22]중간과정
['19]정확히19
['16-'18]범위과정
['15이전]과거과정"""

            then("경계값에서 정확히 분류되어야 한다") {
                FieldFinder.findFieldBySchoolId(boundaryField, 23) shouldBe "최신과정" // 경계 시작
                FieldFinder.findFieldBySchoolId(boundaryField, 22) shouldBe "중간과정" // 범위 끝
                FieldFinder.findFieldBySchoolId(boundaryField, 20) shouldBe "중간과정" // 범위 시작
                FieldFinder.findFieldBySchoolId(boundaryField, 18) shouldBe "범위과정" // 범위 끝
                FieldFinder.findFieldBySchoolId(boundaryField, 16) shouldBe "범위과정" // 범위 시작
                FieldFinder.findFieldBySchoolId(boundaryField, 15) shouldBe "과거과정" // 이전 경계
            }
        }

        `when`("빈 라인이 포함된 패턴 테스트") {
            val emptyLineField = """['23이후]테스트과정


['19]중간과정
['15이전]과거과정"""

            then("빈 라인이 있어도 정상 동작해야 한다") {
                FieldFinder.findFieldBySchoolId(emptyLineField, 24) shouldBe "테스트과정"
                FieldFinder.findFieldBySchoolId(emptyLineField, 19) shouldBe "중간과정"
                FieldFinder.findFieldBySchoolId(emptyLineField, 14) shouldBe "과거과정"
            }
        }
    }

//    given("교필 접두사가 있는 필드 테스트") {
//        `when`("교필- 접두사 패턴들을 테스트할 때") {
//            val testCases = listOf(
//                "교필-['23이후]창의(창의적사고와혁신)" to "창의(창의적사고와혁신)",
//                "교필-['23이후]디지털테크놀로지(SW와AI)" to "디지털테크놀로지(SW와AI)",
//                "교필-['23이후]디지털테크놀로지(컴퓨팅적사고)" to "디지털테크놀로지(컴퓨팅적사고)",
//                "교필-['23이후]품격(글로벌소통과언어)" to "품격(글로벌소통과언어)"
//            )
//
//            then("각 패턴이 올바른 필드명을 반환해야 한다") {
//                testCases.forEach { (input, expected) ->
//                    val result = FieldFinder.findFieldBySchoolId(input, 23)
//                    result shouldBe expected
//                }
//            }
//        }
//    }

    given("실제 JSON 데이터에서 추출한 추가 패턴들") {

//        `when`("교필 접두사 패턴들을 테스트할 때") {
//            val testCases = mapOf(
//                "교필-['23이후]디지털테크놀로지(SW와AI)" to "디지털테크놀로지(SW와AI)",
//                "교필-['23이후]품격(글로벌소통과언어)" to "품격(글로벌소통과언어)",
//                "교필-['23이후]창의(인문적상상력과소통)" to "창의(인문적상상력과소통)",
//                "교필-['23이후]창의(비판적사고와표현)" to "창의(비판적사고와표현)",
//                "교필-['23이후]창의(창의적사고와혁신)" to "창의(창의적사고와혁신)",
//                "교필-['23이후]품격(글로벌시민의식)" to "품격(글로벌시민의식)",
//                "교필-['23이후]디지털테크놀로지(컴퓨팅적사고)" to "디지털테크놀로지(컴퓨팅적사고)"
//            )
//
//            then("각 패턴이 올바른 필드명을 반환해야 한다") {
//                testCases.forEach { (input, expected) ->
//                    val result = FieldFinder.findFieldBySchoolId(input, 23)
//                    result shouldBe expected
//                }
//            }
//        }

//        `when`("기독교과목 접두사가 있는 멀티라인 패턴을 테스트할 때") {
//            val christianPattern = """기독교과목
//교필-['23이후]품격(인간과성서)"""
//
//            then("접두사를 무시하고 올바른 필드명을 반환해야 한다") {
//                val result = FieldFinder.findFieldBySchoolId(christianPattern, 23)
//                result shouldBe "품격(인간과성서)"
//            }
//        }

//        `when`("복잡한 기독교과목 멀티라인 패턴을 테스트할 때") {
//            val complexChristianPattern = """기독교과목
//['23이후]자기개발·진로탐색
//['20,'21~'22]공동체/리더십,숭실품성-인성과리더십
//['19]숭실품성-인성과리더십
//['16-'18]숭실품성(인성-종교가치인성교육)
//['15이전]인성과리더쉽(핵심-창의)"""
//
//            then("각 연도에 맞는 올바른 필드명을 반환해야 한다") {
//                FieldFinder.findFieldBySchoolId(complexChristianPattern, 24) shouldBe "자기개발·진로탐색"
//                FieldFinder.findFieldBySchoolId(complexChristianPattern, 21) shouldBe "공동체/리더십,숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(complexChristianPattern, 19) shouldBe "숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(complexChristianPattern, 17) shouldBe "숭실품성(인성-종교가치인성교육)"
//                FieldFinder.findFieldBySchoolId(complexChristianPattern, 14) shouldBe "인성과리더쉽(핵심-창의)"
//            }
//        }

//        `when`("숭실사이버대과목 접두사 패턴을 테스트할 때") {
//            val cyberPattern = """숭실사이버대과목
//['23이후]사회·정치·경제
//['20,'21~'22]창의/융합,균형교양-사회·문화·심리
//['19]균형교양-사회과학(사회/역사)
//['16-'18]균형교양(사회과학-사회/정치/경제)
//['15이전]인간과사회(융합-사회)"""
//
//            then("사이버대 접두사를 무시하고 올바른 분류를 반환해야 한다") {
//                FieldFinder.findFieldBySchoolId(cyberPattern, 24) shouldBe "사회·정치·경제"
//                FieldFinder.findFieldBySchoolId(cyberPattern, 21) shouldBe "창의/융합,균형교양-사회·문화·심리"
//                FieldFinder.findFieldBySchoolId(cyberPattern, 19) shouldBe "균형교양-사회과학(사회/역사)"
//                FieldFinder.findFieldBySchoolId(cyberPattern, 17) shouldBe "균형교양(사회과학-사회/정치/경제)"
//                FieldFinder.findFieldBySchoolId(cyberPattern, 12) shouldBe "인간과사회(융합-사회)"
//            }
//        }

//        `when`("복합 기독교과목 패턴을 테스트할 때") {
//            val multiChristianPattern = """기독교과목
//['23이후]인간·언어
//['20,'21~'22]창의/융합,균형교양-역사·철학·종교
//['19]숭실품성-인성과리더십
//['16-'18]숭실품성(인성-가치관및윤리교육)
//['15이전]인성과리더쉽(핵심-창의)"""
//
//            then("기독교과목 접두사와 연도별 분류가 모두 작동해야 한다") {
//                FieldFinder.findFieldBySchoolId(multiChristianPattern, 25) shouldBe "인간·언어"
//                FieldFinder.findFieldBySchoolId(multiChristianPattern, 22) shouldBe "창의/융합,균형교양-역사·철학·종교"
//                FieldFinder.findFieldBySchoolId(multiChristianPattern, 19) shouldBe "숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(multiChristianPattern, 18) shouldBe "숭실품성(인성-가치관및윤리교육)"
//                FieldFinder.findFieldBySchoolId(multiChristianPattern, 13) shouldBe "인성과리더쉽(핵심-창의)"
//            }
//        }

        `when`("연도 패턴이 없는 필드들을 테스트할 때") {
            val invalidFields = listOf(
                "null",
                "법학과목",
                "채플과목",
                "교직이론영역",
                "교과교육영역",
                "교육실습영역"
            )

            then("모든 연도에 대해 빈 문자열을 반환해야 한다") {
                invalidFields.forEach { field ->
                    FieldFinder.findFieldBySchoolId(field, 23) shouldBe ""
                    FieldFinder.findFieldBySchoolId(field, 19) shouldBe ""
                    FieldFinder.findFieldBySchoolId(field, 15) shouldBe ""
                }
            }
        }

//        `when`("누락된 연도 패턴을 테스트할 때") {
//            val missingYearPattern = """['23이후]사회·정치·경제
//['19]균형교양-사회과학(사회/역사)
//['16-'18]균형교양(사회과학-사회/정치/경제)
//['15이전]정치와경제(융합-사회)"""
//
//            then("누락된 범위의 연도에 대해 빈 문자열을 반환해야 한다") {
//                FieldFinder.findFieldBySchoolId(missingYearPattern, 24) shouldBe "사회·정치·경제"
//                FieldFinder.findFieldBySchoolId(missingYearPattern, 19) shouldBe "균형교양-사회과학(사회/역사)"
//                FieldFinder.findFieldBySchoolId(missingYearPattern, 17) shouldBe "균형교양(사회과학-사회/정치/경제)"
//                FieldFinder.findFieldBySchoolId(missingYearPattern, 14) shouldBe "정치와경제(융합-사회)"
//            }
//        }

//        `when`("우선순위 테스트 - 더 높은 시작 연도가 선택되는지") {
//            val priorityPattern = """['18]실용교양(자기개발과진로탐색)
//['16-'18]균형교양(사회과학-사회/정치/경제)
//['15이전]학문과진로탐색(실용-생활)"""
//
//            then("18년에 대해 더 높은 시작 연도인 단일 18년 규칙이 선택되어야 한다") {
//                val result = FieldFinder.findFieldBySchoolId(priorityPattern, 18)
//                result shouldBe "실용교양(자기개발과진로탐색)"
//            }
//        }

//        `when`("실제 대용량 필드 패턴을 테스트할 때") {
//            val realLargePattern = """['23이후]인간·언어
//['20,'21~'22]의사소통/글로벌,기초역량-국제어문
//['19]기초역량-한국어의사소통과국제어문
//['16-'18]기초역량(국제어문-국제어)
//['15이전]세계의언어(핵심-창의)"""
//
//            then("모든 연도 범위에서 올바른 결과를 반환해야 한다") {
//                FieldFinder.findFieldBySchoolId(realLargePattern, 25) shouldBe "인간·언어"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 20) shouldBe "의사소통/글로벌,기초역량-국제어문"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 21) shouldBe "의사소통/글로벌,기초역량-국제어문"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 22) shouldBe "의사소통/글로벌,기초역량-국제어문"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 19) shouldBe "기초역량-한국어의사소통과국제어문"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 16) shouldBe "기초역량(국제어문-국제어)"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 17) shouldBe "기초역량(국제어문-국제어)"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 18) shouldBe "기초역량(국제어문-국제어)"
//                FieldFinder.findFieldBySchoolId(realLargePattern, 10) shouldBe "세계의언어(핵심-창의)"
//            }
//        }

        `when`("특수 문자가 포함된 필드명들을 테스트할 때") {
            val specialCharFields = mapOf(
                "['23이후]인간·언어" to "인간·언어",
                "['23이후]사회·정치·경제" to "사회·정치·경제",
                "['23이후]과학·기술" to "과학·기술",
                "['23이후]문화·예술" to "문화·예술",
                "['23이후]자기개발·진로탐색" to "자기개발·진로탐색"
            )

            then("중점(·) 문자가 포함된 필드명을 올바르게 추출해야 한다") {
                specialCharFields.forEach { (input, expected) ->
                    val result = FieldFinder.findFieldBySchoolId(input, 24)
                    result shouldBe expected
                }
            }
        }

//        `when`("괄호와 슬래시가 포함된 복잡한 필드명을 테스트할 때") {
//            val complexFieldNames = mapOf(
//                "['20,'21~'22]의사소통/글로벌,기초역량-국제어문" to "의사소통/글로벌,기초역량-국제어문",
//                "['19]기초역량-한국어의사소통과국제어문" to "기초역량-한국어의사소통과국제어문",
//                "['16-'18]기초역량(국제어문-국제어)" to "기초역량(국제어문-국제어)",
//                "['15이전]세계의언어(핵심-창의)" to "세계의언어(핵심-창의)",
//                "['19]균형교양-자연/공학(자연/과학/기술)" to "균형교양-자연/공학(자연/과학/기술)",
//                "['16-'18]균형교양(사회과학-사회/정치/경제)" to "균형교양(사회과학-사회/정치/경제)"
//            )
//
//            then("복잡한 특수문자 조합도 올바르게 추출해야 한다") {
//                complexFieldNames.forEach { (input, expected) ->
//                    val schoolId = when {
//                        input.contains("'20,'21~'22") -> 21
//                        input.contains("'19]") -> 19
//                        input.contains("'16-'18") -> 17
//                        input.contains("'15이전") -> 14
//                        else -> 23
//                    }
//                    val result = FieldFinder.findFieldBySchoolId(input, schoolId)
//                    result shouldBe expected
//                }
//            }
//        }
    }

    given("에지 케이스 테스트") {

        `when`("매우 긴 필드명을 테스트할 때") {
            val longFieldName = "['23이후]매우긴필드명을가진교육과정으로서학습자들의역량개발과진로탐색및창의적사고력향상을위한종합적교육프로그램"

            then("긴 필드명도 올바르게 추출해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(longFieldName, 23)
                result shouldBe "매우긴필드명을가진교육과정으로서학습자들의역량개발과진로탐색및창의적사고력향상을위한종합적교육프로그램"
            }
        }

        `when`("연속된 대괄호가 있는 패턴을 테스트할 때") {
            val consecutiveBrackets = "['23이후]['연속대괄호']테스트과정"

            then("마지막 대괄호 이후의 텍스트를 추출해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(consecutiveBrackets, 23)
                result shouldBe "테스트과정"
            }
        }

        `when`("숫자가 포함된 필드명을 테스트할 때") {
            val numbersInField = "['23이후]프로그래밍1단계"

            then("필드명의 숫자도 올바르게 포함해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(numbersInField, 23)
                result shouldBe "프로그래밍1단계"
            }
        }

        `when`("영어가 포함된 필드명을 테스트할 때") {
            val englishInField = "['23이후]Digital Technology & AI"

            then("영어가 포함된 필드명도 올바르게 추출해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(englishInField, 23)
                result shouldBe "Digital Technology & AI"
            }
        }

        `when`("특수문자가 많이 포함된 필드명을 테스트할 때") {
            val specialCharsField = "['23이후]과학&기술@창의#융합\$교육%프로그램"

            then("모든 특수문자를 포함해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(specialCharsField, 23)
                result shouldBe "과학&기술@창의#융합\$교육%프로그램"
            }
        }

        `when`("공백으로 시작하거나 끝나는 필드명을 테스트할 때") {
            val whitespaceField = "['23이후]  앞뒤공백있는과정  "

            then("앞뒤 공백이 제거되어야 한다") {
                val result = FieldFinder.findFieldBySchoolId(whitespaceField, 23)
                result shouldBe "앞뒤공백있는과정"
            }
        }

        `when`("탭 문자가 포함된 필드명을 테스트할 때") {
            val tabField = "['23이후]\t탭문자포함\t과정\t"

            then("탭 문자도 올바르게 처리해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(tabField, 23)
                result shouldBe "탭문자포함\t과정"
            }
        }

        `when`("동일한 시작 연도를 가진 패턴들을 테스트할 때") {
            val sameStartYear = """['18]첫번째과정
['18-'20]두번째과정"""

            then("먼저 나타나는 패턴이 선택되어야 한다") {
                val result = FieldFinder.findFieldBySchoolId(sameStartYear, 18)
                result shouldBe "첫번째과정"
            }
        }

        `when`("모든 연도 패턴 타입이 섞인 복합 케이스를 테스트할 때") {
            val allPatternTypes = """['25이후]최신과정
['22,'23~'24]최근과정
['21]정확한과정
['18-'20]범위과정
['15이전]과거과정"""

            then("각 패턴 타입별로 올바른 결과를 반환해야 한다") {
                FieldFinder.findFieldBySchoolId(allPatternTypes, 26) shouldBe "최신과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 22) shouldBe "최근과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 23) shouldBe "최근과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 24) shouldBe "최근과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 21) shouldBe "정확한과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 18) shouldBe "범위과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 19) shouldBe "범위과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 20) shouldBe "범위과정"
                FieldFinder.findFieldBySchoolId(allPatternTypes, 10) shouldBe "과거과정"
            }
        }

        `when`("필드명이 완전히 비어있는 경우를 테스트할 때") {
            val emptyFieldName = "['23이후]"

            then("빈 문자열을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(emptyFieldName, 23)
                result shouldBe ""
            }
        }

        `when`("필드명이 공백만 있는 경우를 테스트할 때") {
            val whitespaceOnlyField = "['23이후]   "

            then("빈 문자열을 반환해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(whitespaceOnlyField, 23)
                result shouldBe ""
            }
        }

//        `when`("매우 복잡한 실제 데이터 케이스를 테스트할 때") {
//            val realComplexCase = """기독교과목
//['23이후]문화·예술
//['20,'21~'22]창의/융합,균형교양-역사·철학·종교
//['19]숭실품성-인성과리더십
//['16-'18]숭실품성(인성-종교가치인성교육)
//['15이전]인성과리더쉽(핵심-창의)"""
//
//            then("복잡한 실제 케이스도 완벽하게 처리해야 한다") {
//                FieldFinder.findFieldBySchoolId(realComplexCase, 25) shouldBe "문화·예술"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 22) shouldBe "창의/융합,균형교양-역사·철학·종교"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 20) shouldBe "창의/융합,균형교양-역사·철학·종교"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 21) shouldBe "창의/융합,균형교양-역사·철학·종교"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 19) shouldBe "숭실품성-인성과리더십"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 17) shouldBe "숭실품성(인성-종교가치인성교육)"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 16) shouldBe "숭실품성(인성-종교가치인성교육)"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 18) shouldBe "숭실품성(인성-종교가치인성교육)"
//                FieldFinder.findFieldBySchoolId(realComplexCase, 12) shouldBe "인성과리더쉽(핵심-창의)"
//            }
//        }

        `when`("대소문자가 섞인 영어 필드명을 테스트할 때") {
            val mixedCaseField = "['23이후]Computer Science & Engineering"

            then("대소문자를 그대로 유지해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(mixedCaseField, 23)
                result shouldBe "Computer Science & Engineering"
            }
        }

        `when`("유니코드 이모지가 포함된 필드명을 테스트할 때") {
            val emojiField = "['23이후]창의적사고 🧠 혁신교육 💡"

            then("이모지도 올바르게 포함해야 한다") {
                val result = FieldFinder.findFieldBySchoolId(emojiField, 23)
                result shouldBe "창의적사고 🧠 혁신교육 💡"
            }
        }
    }
})
