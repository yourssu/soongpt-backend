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

    override fun getByDepartmentIdAndGrade(departmentId: Long, grade: Int): DepartmentGrade {
        val departmentGrade = departmentGradeJpaRepository.findByDepartmentIdAndGrade(departmentId, grade)
            ?: throw DepartmentGradeNotFoundException()
        return departmentGrade.toDomain()
    }

    override fun getAll(): List<DepartmentGrade> {
        return departmentGradeJpaRepository.findAll().map { it.toDomain() }
    }

    override fun getByDepartmentId(departmentId: Long): List<DepartmentGrade> {
        return departmentGradeJpaRepository.findByDepartmentId(departmentId).map { it.toDomain() }
    }

    override fun getByGrade(grade: Int): List<DepartmentGrade> {
        return departmentGradeJpaRepository.findByGrade(grade).map { it.toDomain() }
    }
}

interface DepartmentGradeJpaRepository : JpaRepository<DepartmentGradeEntity, Long> {
    fun findByDepartmentIdAndGrade(departmentId: Long, grade: Int): DepartmentGradeEntity?
    fun findByDepartmentId(departmentId: Long): List<DepartmentGradeEntity>
    fun findByGrade(grade: Int): List<DepartmentGradeEntity>
}