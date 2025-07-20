package com.yourssu.soongpt.domain.departmentGrade.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import jakarta.persistence.*

@Entity
@Table(name = "department_grade")
class DepartmentGradeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val grade: Int,

    @Column(nullable = false)
    val departmentId: Long,
) : BaseEntity() {
    companion object {
        fun from(departmentGrade: DepartmentGrade) = DepartmentGradeEntity(
            id = departmentGrade.id,
            grade = departmentGrade.grade,
            departmentId = departmentGrade.departmentId
        )
    }

    fun toDomain() = DepartmentGrade(
        id = id,
        grade = grade,
        departmentId = departmentId
    )
}