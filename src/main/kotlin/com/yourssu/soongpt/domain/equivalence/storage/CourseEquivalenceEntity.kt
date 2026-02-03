package com.yourssu.soongpt.domain.equivalence.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.equivalence.implement.CourseEquivalence
import jakarta.persistence.*

@Entity
@Table(name = "course_equivalence")
class CourseEquivalenceEntity(
    @Id
    @Column(nullable = false)
    val courseCode: Long,

    @Column(nullable = false)
    val groupId: Long,
) : BaseEntity() {
    companion object {
        fun from(courseEquivalence: CourseEquivalence) = CourseEquivalenceEntity(
            courseCode = courseEquivalence.courseCode,
            groupId = courseEquivalence.groupId,
        )
    }

    fun toDomain() = CourseEquivalence(
        courseCode = courseCode,
        groupId = groupId,
    )
}
