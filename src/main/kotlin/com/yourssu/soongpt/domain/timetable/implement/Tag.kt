package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.strategy.*

enum class Tag (val description: String, val strategy: TagStrategy) {
    DEFAULT("지금까지 고른 과목으로 만든 시간표", DefaultStrategy()),
    NO_MORNING_CLASSES("아침 수업 없는 시간표", NoMorningClassesStrategy()),
    FREE_MONDAY("월요일 공강 보장 시간표", SpecificFreeDayStrategy(Week.MONDAY)),
    FREE_TUESDAY("화요일 공강 보장 시간표", SpecificFreeDayStrategy(Week.TUESDAY)),
    FREE_WEDNESDAY("수요일 공강 보장 시간표", SpecificFreeDayStrategy(Week.WEDNESDAY)),
    FREE_THURSDAY("목요일 공강 보장 시간표", SpecificFreeDayStrategy(Week.THURSDAY)),
    FREE_FRIDAY("금요일 공강 보장 시간표", SpecificFreeDayStrategy(Week.FRIDAY)),
    NO_LONG_BREAKS("우주공강 없는 시간표", NoLongBreaksStrategy()),
    GUARANTEED_LUNCH_TIME("점심시간 보장 시간표", GuaranteedLunchTimeStrategy()),
    NO_EVENING_CLASSES("저녁 수업 없는 시간표", NoEveningClassesStrategy()),
    ;
}
