package com.yourssu.soongpt.domain.timetable.implement.constant

const val WEEKLY_MINUTES = 24 * 7 * 60
const val TIMESLOT_UNIT_MINUTES = 5
const val TIMESLOT_SIZE = WEEKLY_MINUTES / TIMESLOT_UNIT_MINUTES
const val TIMESLOT_DAY_RANGE = 24 * 60 / TIMESLOT_UNIT_MINUTES
