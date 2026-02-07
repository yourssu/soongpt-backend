package com.yourssu.soongpt.domain.rating.storage

import com.yourssu.soongpt.domain.rating.implement.Rating
import jakarta.persistence.*

@Entity
@Table(name = "rating")
class RatingEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "code", nullable = false)
    val code: Long,

    @Column(name = "star", nullable = false)
    val star: Double,
) {
    companion object {
        fun from(rating: Rating): RatingEntity {
            return RatingEntity(
                id = rating.id,
                code = rating.code,
                star = rating.star,
            )
        }
    }

    fun toDomain(): Rating {
        return Rating(
            id = id,
            code = code,
            star = star,
        )
    }
}