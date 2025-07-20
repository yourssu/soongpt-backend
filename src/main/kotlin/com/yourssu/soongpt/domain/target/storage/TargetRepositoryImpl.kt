package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class TargetRepositoryImpl (
    val targetJpaRepository: TargetJpaRepository,
): TargetRepository {
    override fun findAllByCode(code: Long): List<Target> {
        return listOf()
    }

    override fun getByDepartmentAndGrade(
        departmentId: Long,
        grade: Int
    ): Target {
        return Target(
            id = 1L,
            departmentId = departmentId,
            courseId = 1L,
            grade = 1,
        )
    }
}

interface TargetJpaRepository : JpaRepository<TargetEntity, Long> {
}
