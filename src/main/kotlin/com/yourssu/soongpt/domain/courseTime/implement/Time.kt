package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidTimeFormatException

private const val TIME_DELIMITER = ":"
private const val HOUR_TO_MIN = 60
private const val DAY_TO_HOUR = 24

data class Time(
    val time: Int,
) {
    companion object {
        fun of(timeFormat: String): Time {
            try {
                val (hour, minute) = timeFormat.split(TIME_DELIMITER).map { it.toInt() }
                return Time(hour * HOUR_TO_MIN + minute)
            } catch (e: Exception) {
                throw InvalidTimeFormatException()
            }
        }
    }

    init {
        if (time !in 0..<DAY_TO_HOUR * HOUR_TO_MIN) {
            throw InvalidTimeFormatException()
        }
    }

    fun toTimeFormat(): String {
        return "${time / HOUR_TO_MIN}$TIME_DELIMITER${time % HOUR_TO_MIN}"
    }

    fun isOverThan(other: Time): Boolean {
        return time > other.time
    }
}