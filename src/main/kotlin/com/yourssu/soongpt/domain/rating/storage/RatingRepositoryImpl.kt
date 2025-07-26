package com.yourssu.soongpt.domain.rating.storage

import com.yourssu.soongpt.domain.department.storage.DepartmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
class RatingRepositoryImpl (
    private val ratingJpaRepository: RatingJpaRepository,
) : RatingRepository {
    override fun saveAll(ratings: List<Rating>): List<Rating> {
        return ratingJpaRepository.saveAll(ratings.map { RatingEntity.from(it) })
            .map { it.toDomain() }.toList()
    }

    override fun getByCode(code: Long): Rating {
        return ratingJpaRepository.findByCode(code)?.toDomain()
            ?: throw RatingNotFoundException()
    }
)

interface DepartmentJpaRepository : JpaRepository<DepartmentEntity, Long> {
}
