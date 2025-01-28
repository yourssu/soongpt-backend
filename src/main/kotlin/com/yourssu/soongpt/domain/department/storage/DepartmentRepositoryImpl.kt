package com.yourssu.soongpt.domain.department.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.department.storage.QDepartmentEntity.departmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class DepartmentRepositoryImpl(
    private val departmentJpaRepository: DepartmentJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : DepartmentRepository {
    override fun save(department: Department): Department {
        return departmentJpaRepository.save(DepartmentEntity.from(department))
            .toDomain()
    }

    override fun findByName(name: String): Department? {
        return jpaQueryFactory.selectFrom(departmentEntity)
            .where(departmentEntity.name.eq(name))
            .fetchOne()
            ?.toDomain()
    }
}

interface DepartmentJpaRepository : JpaRepository<DepartmentEntity, Long> {
}