package com.yourssu.soongpt.domain.departmentGrade.storage

import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeRepository
import com.yourssu.soongpt.domain.departmentGrade.storage.exception.DepartmentGradeNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DepartmentGradeRepositoryImpl(
    private val departmentGradeJpaRepository: DepartmentGradeJpaRepository,
): DepartmentGradeRepository {
    override fun save(departmentGrade: DepartmentGrade): DepartmentGrade {
        return departmentGradeJpaRepository.save(DepartmentGradeEntity.from(departmentGrade))
            .toDomain()
    }

    override fun saveAll(departmentGrades: List<DepartmentGrade>): List<DepartmentGrade> {
        return departmentGradeJpaRepository.saveAll(departmentGrades.map { DepartmentGradeEntity.from(it) })
            .map { it.toDomain() }
    }

    override fun get(id: Long): DepartmentGrade {
        val departmentGrade = departmentGradeJpaRepository.findByIdOrNull(id)
            ?: throw DepartmentGradeNotFoundException()
        return departmentGrade.toDomain()
    }
}

interface DepartmentGradeJpaRepository : JpaRepository<DepartmentGradeEntity, Long> {
}