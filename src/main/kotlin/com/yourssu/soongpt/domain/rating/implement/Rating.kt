package com.yourssu.soongpt.domain.rating.implement

class Rating(
    val id: Long? = null,
    val courseName: String,
    val professorName: String,
    val star: Double,
    var point: Double = 50.0,
) {
}