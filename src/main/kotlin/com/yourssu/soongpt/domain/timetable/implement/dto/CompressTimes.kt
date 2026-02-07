package com.yourssu.soongpt.domain.timetable.implement.dto

data class CompressTimes(
    val times: List<CompressTime>
) {
    companion object {
        fun from(times: List<CompressTime>): CompressTimes {
            return CompressTimes(
                times = times
            )
        }
    }
}
