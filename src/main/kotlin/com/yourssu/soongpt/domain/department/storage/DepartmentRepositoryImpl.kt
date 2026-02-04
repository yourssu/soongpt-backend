package com.yourssu.soongpt.domain.department.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.department.storage.QDepartmentEntity.departmentEntity
import com.yourssu.soongpt.domain.department.storage.exception.DepartmentNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class DepartmentRepositoryImpl(
    private val departmentJpaRepository: DepartmentJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : DepartmentRepository {

    override fun saveAll(departments: List<Department>): List<Department> {
        return departmentJpaRepository.saveAll(departments.map { DepartmentEntity.from(it) })
            .map { it.toDomain() }.toList()
    }

    override fun getByName(name: String): Department {
        return jpaQueryFactory.selectFrom(departmentEntity)
            .where(departmentEntity.name.eq(name))
            .fetchOne()
            ?.toDomain()
            ?: throw DepartmentNotFoundException()
    }

    override fun get(id: Long): Department {
        return departmentJpaRepository.findById(id)
            .orElseThrow { DepartmentNotFoundException() }
            .toDomain()
    }
}

interface DepartmentJpaRepository : JpaRepository<DepartmentEntity, Long> {
}
