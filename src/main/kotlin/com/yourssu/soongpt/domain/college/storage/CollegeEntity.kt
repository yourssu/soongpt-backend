package com.yourssu.soongpt.domain.college.storage

import com.yourssu.soongpt.domain.college.implement.College
import jakarta.persistence.*

@Entity
@Table(name = "college")
class CollegeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String,
) {
        companion object {
            fun from(college: College) = CollegeEntity(
                id = college.id,
                name = college.name,
            )
        }

        fun toDomain() = College(
            id = id,
            name = name,
        )
}