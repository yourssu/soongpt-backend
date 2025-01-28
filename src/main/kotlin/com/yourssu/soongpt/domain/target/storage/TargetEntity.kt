package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.domain.target.implement.Target
import jakarta.persistence.*

@Entity
@Table(name = "target")
class TargetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val departmentGradeId: Long,

    @Column(nullable = false)
    val courseId: Long,
) {
    companion object {
        fun from(target: Target) = TargetEntity(
            id = target.id,
            departmentGradeId = target.departmentGradeId,
            courseId = target.courseId
        )
    }

    fun toDomain() = Target(
        id = id,
        departmentGradeId = departmentGradeId,
        courseId = courseId
    )
}
