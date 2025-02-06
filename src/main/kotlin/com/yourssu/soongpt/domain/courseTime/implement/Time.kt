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
        val hours = time / HOUR_TO_MIN
        val minutes = (time % HOUR_TO_MIN).toString().padStart(2, '0')
        return "$hours$TIME_DELIMITER$minutes"
    }

    fun isOverThan(other: Time, minute: Int = 0): Boolean {
        return time + minute > other.time
    }

    fun addMinute(minute: Int): Time {
        return Time(time + minute)
    }

    fun minus(other: Time): Int {
        return time - other.time
    }
}