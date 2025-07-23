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
    val departmentId: Long,

    @Column(nullable = false)
    val courseId: Long,

    @Column(nullable = false)
    val grade: Int,
) {
    companion object {
        fun from(target: Target) = TargetEntity(
            id = target.id,
            departmentId = target.departmentId,
            courseId = target.courseId,
            grade = target.grade,
        )
    }

    fun toDomain() = Target(
        id = id,
        departmentId = departmentId,
        courseId = courseId,
        grade = grade,
    )
}
