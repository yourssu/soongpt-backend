package com.yourssu.soongpt.domain.timetable.implement

enum class Tag(val description: String) {
    HAS_FREE_DAY("공강 날이 있는 시간표"),
    NO_MORNING_CLASSES("아침 수업이 없는 시간표"),
    NO_LONG_BREAKS("우주 공강이 없는 시간표"),
    EVENLY_DISTRIBUTED("균등하게 배분되어 있는 시간표"),
    GUARANTEED_LUNCH_TIME("점심시간 보장되는 시간표"),
    NO_EVENING_CLASSES("저녁수업이 없는 시간표"),
    DEFAULT("기본 태그")
    ;
}
