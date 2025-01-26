package com.yourssu.soongpt.domain.department.storage

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.department.storage.exception.DepartmentNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DepartmentRepositoryImpl(
    private val departmentJpaRepository: DepartmentJpaRepository,
) : DepartmentRepository {
    override fun save(department: Department): Department {
        return departmentJpaRepository.save(DepartmentEntity.from(department))
            .toDomain()
    }

    override fun get(id: Long): Department {
        val department = departmentJpaRepository.findByIdOrNull(id)
            ?: throw DepartmentNotFoundException()
        return department.toDomain()
    }
}

interface DepartmentJpaRepository : JpaRepository<DepartmentEntity, Long> {
}