package com.yourssu.soongpt.domain.rating.implement

class Rating(
    val id: Long? = null,
    val courseName: String,
    val professorName: String,
    val star: Double,
    var point: Double = INIT,
) {
    companion object {
        const val INIT = 50.0
    }
}