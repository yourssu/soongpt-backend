package com.yourssu.soongpt.domain.rating.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.rating.implement.Rating
import com.yourssu.soongpt.domain.rating.implement.RatingRepository
import com.yourssu.soongpt.domain.rating.storage.QRatingEntity.ratingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class RatingRepositoryImpl(
    private val ratingJpaRepository: RatingJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : RatingRepository {
    override fun save(rating: Rating): Rating {
        return ratingJpaRepository.save(RatingEntity.from(rating)).toDomain()
    }

    override fun findByCourseNameAndProfessorName(courseName: String, professorName: String): Rating? {
        return jpaQueryFactory.selectFrom(ratingEntity)
            .where(ratingEntity.courseName.like(courseName))
            .where(ratingEntity.professorName.eq(professorName))
            .fetchOne()
            ?.toDomain()
    }
}

interface RatingJpaRepository: JpaRepository<RatingEntity, Long> {
}