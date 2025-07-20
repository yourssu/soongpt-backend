package com.yourssu.soongpt.domain.department.storage

import com.yourssu.soongpt.domain.department.implement.Department
import jakarta.persistence.*

@Entity
@Table(name = "department")
class DepartmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(nullable = false)
    val collegeId: Long,
) {
    companion object {
        fun from(department: Department) = DepartmentEntity(
            id = department.id,
            name = department.name,
            collegeId = department.collegeId
        )
    }

    fun toDomain() = Department(
        id = id,
        name = name,
        collegeId = collegeId
    )
}
