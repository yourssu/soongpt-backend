package com.yourssu.soongpt.domain.college.storage

import com.yourssu.soongpt.domain.college.implement.College
import com.yourssu.soongpt.domain.college.implement.CollegeRepository
import com.yourssu.soongpt.domain.college.storage.exception.CollegeNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CollegeRepositoryImpl(
    private val collegeJpaRepository: CollegeJpaRepository,
) : CollegeRepository {
    override fun save(college: College): College {
        return collegeJpaRepository.save(CollegeEntity.from(college))
            .toDomain()
    }

    override fun get(id: Long): College {
        val college = collegeJpaRepository.findByIdOrNull(id)
            ?: throw CollegeNotFoundException()
        return college.toDomain()
    }

    override fun getByName(name: String): College {
        return collegeJpaRepository.findByName(name)?.toDomain()
            ?: throw CollegeNotFoundException()
    }
}

interface CollegeJpaRepository : JpaRepository<CollegeEntity, Long> {
    fun findByName(name: String): CollegeEntity?
}