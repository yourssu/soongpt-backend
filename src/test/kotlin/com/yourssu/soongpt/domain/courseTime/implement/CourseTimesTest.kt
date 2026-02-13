package com.yourssu.soongpt.domain.courseTime.implement

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CourseTimesTest : BehaviorSpec({

    given("CourseTimes.from") {

        `when`("강의실이 없고 교수명만 남아있는 경우: (-교수)") {
            then("classroom은 null로 파싱된다") {
                val schedule = "토 20:00-21:00 (-전삼현)"

                val times = CourseTimes.from(schedule).toList()

                times.size shouldBe 1
                times[0].week.displayName shouldBe "토"
                times[0].startTime.toTimeFormat() shouldBe "20:00"
                times[0].endTime.toTimeFormat() shouldBe "21:00"
                times[0].classroom shouldBe null
            }
        }

        `when`("강의실 문자열에 괄호가 포함된 경우: (PC실습실)") {
            then("교수명을 제거하고 강의실만 유지한다") {
                val schedule = "화 09:00-10:15 (진리관 11310 (PC실습실)-한윤영)"

                val times = CourseTimes.from(schedule).toList()

                times.size shouldBe 1
                times[0].classroom shouldBe "진리관 11310 (PC실습실)"
            }
        }

        `when`("여러 요일이 한 줄에 포함된 경우") {
            then("요일 수만큼 CourseTime이 생성되고 강의실은 동일하게 파싱된다") {
                val schedule = "월 목 13:30-14:45 (미래관 20108-최동일)"

                val times = CourseTimes.from(schedule).toList()

                times.size shouldBe 2
                times[0].classroom shouldBe "미래관 20108"
                times[1].classroom shouldBe "미래관 20108"
            }
        }
    }
})
