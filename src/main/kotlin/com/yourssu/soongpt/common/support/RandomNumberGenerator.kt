package com.yourssu.soongpt.common.support

class RandomNumberGenerator {
    companion object {
        fun generateRandomNumber(maximum: Long): Long {
            return bound(maximum).random()
        }

        private fun bound(maximum: Long): LongRange {
            return 1L..maximum
        }
    }
}
