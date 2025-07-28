package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.strategy.*

enum class Tag (val description: String, val strategy: TagStrategy) {
    DEFAULT("기본 태그", DefaultStrategy()),
    NO_MORNING_CLASSES("아침 수업이 없는 시간표", NoMorningClassesStrategy()),
    HAS_FREE_DAY("공강 날이 있는 시간표", FreeDayTagStrategy()),
    NO_LONG_BREAKS("우주 공강이 없는 시간표", NoLongBreaksStrategy()),
    GUARANTEED_LUNCH_TIME("점심시간 보장되는 시간표", GuaranteedLunchTimeStrategy()),
    NO_EVENING_CLASSES("저녁수업이 없는 시간표", NoEveningClassesStrategy()),
    ;
}
