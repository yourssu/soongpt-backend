package com.yourssu.soongpt.domain.rating.implement

class Rating(
    val id: Long? = null,
    val courseName: String,
    val professorName: String,
    val courseCode: String,
    val star: Double = 3.91,
    var point: Double = 50.0,
) {
}