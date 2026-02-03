package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType
import com.yourssu.soongpt.domain.target.implement.Target
import jakarta.persistence.*

@Entity
@Table(name = "target")
class TargetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val courseCode: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    val scopeType: ScopeType,

    @Column(nullable = true)
    val collegeId: Long? = null,

    @Column(nullable = true)
    val departmentId: Long? = null,

    @Column(nullable = false)
    val grade1: Boolean = false,

    @Column(nullable = false)
    val grade2: Boolean = false,

    @Column(nullable = false)
    val grade3: Boolean = false,

    @Column(nullable = false)
    val grade4: Boolean = false,

    @Column(nullable = false)
    val grade5: Boolean = false,

    @Column(nullable = false)
    val isDenied: Boolean = false,

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    val studentType: StudentType = StudentType.GENERAL,

    @Column(nullable = false)
    val isStrict: Boolean = false,
) {
    companion object {
        fun from(target: Target) = TargetEntity(
            id = target.id,
            courseCode = target.courseCode,
            scopeType = target.scopeType,
            collegeId = target.collegeId,
            departmentId = target.departmentId,
            grade1 = target.grade1,
            grade2 = target.grade2,
            grade3 = target.grade3,
            grade4 = target.grade4,
            grade5 = target.grade5,
            isDenied = target.isDenied,
            studentType = target.studentType,
            isStrict = target.isStrict,
        )
    }

    fun toDomain() = Target(
        id = id,
        courseCode = courseCode,
        scopeType = scopeType,
        collegeId = collegeId,
        departmentId = departmentId,
        grade1 = grade1,
        grade2 = grade2,
        grade3 = grade3,
        grade4 = grade4,
        grade5 = grade5,
        isDenied = isDenied,
        studentType = studentType,
        isStrict = isStrict,
    )
}
