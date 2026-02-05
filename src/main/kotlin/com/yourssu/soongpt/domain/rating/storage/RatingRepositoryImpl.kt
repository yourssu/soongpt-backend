package com.yourssu.soongpt.domain.rating.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.rating.implement.Rating
import com.yourssu.soongpt.domain.rating.implement.RatingRepository
import com.yourssu.soongpt.domain.rating.storage.QRatingEntity.ratingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
class RatingRepositoryImpl (
    private val ratingJpaRepository: RatingJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : RatingRepository {
    override fun save(rating: Rating): Rating {
        return ratingJpaRepository.save(RatingEntity.from(rating)).toDomain()
    }

    override fun findByCode(code: Long): Rating? {
        return jpaQueryFactory.selectFrom(ratingEntity)
            .where(ratingEntity.code.eq(code))
            .fetchFirst()
            ?.toDomain()
    }

    override fun findAll(): List<Rating> {
        return jpaQueryFactory.selectFrom(ratingEntity)
            .fetch()
            .map { it.toDomain() }
    }

    override fun findAllByCourseCodes(courseCodes: List<Long>): List<Rating> {
        return jpaQueryFactory.selectFrom(ratingEntity)
            .where(ratingEntity.code.`in`(courseCodes))
            .fetch()
            .map { it.toDomain() }
    }
}

interface RatingJpaRepository : JpaRepository<RatingEntity, Long> {
}
