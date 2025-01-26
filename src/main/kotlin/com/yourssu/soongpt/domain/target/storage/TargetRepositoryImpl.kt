package com.yourssu.soongpt.domain.target.storage

import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.exception.TargetNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class TargetRepositoryImpl(
    private val targetJpaRepository: TargetJpaRepository,
): TargetRepository {
    override fun save(target: Target): Target {
        return targetJpaRepository.save(TargetEntity.from(target))
            .toDomain()
    }

    override fun get(id: Long): Target {
        val target = targetJpaRepository.findByIdOrNull(id)
            ?: throw TargetNotFoundException()
        return target.toDomain()
    }
}

interface TargetJpaRepository: JpaRepository<TargetEntity, Long> {
}