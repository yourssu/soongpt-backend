package com.yourssu.soongpt.domain.rating.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.rating.implement.Rating
import jakarta.persistence.*

@Entity
@Table(name = "rating")
class RatingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "course_name", nullable = false)
    val courseName: String,

    @Column(name = "professor_name", nullable = true)
    val professorName: String,

    @Column(name = "star", nullable = false)
    val star: Double,

    @Column(name = "point", nullable = false)
    val point: Double,
) : BaseEntity() {
    companion object {
        fun from(rating: Rating): RatingEntity {
            return RatingEntity(
                courseName = rating.courseName,
                professorName = rating.professorName,
                star = rating.star,
                point = rating.point,
            )
        }
    }

    fun toDomain(): Rating {
        return Rating(
            id = id,
            courseName = courseName,
            professorName = professorName,
            star = star,
            point = point,
        )
    }
}