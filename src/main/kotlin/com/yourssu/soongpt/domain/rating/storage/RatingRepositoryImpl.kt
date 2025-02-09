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
            .where(ratingEntity.courseName.eq(courseName))
            .where(ratingEntity.professorName.eq(professorName))
            .fetchFirst()
            ?.toDomain()
    }

    override fun findAllByCourseNameAndProfessorName(pairs: List<Triple<Long, String, String>>): Map<Long, Double> {
        val results = mutableMapOf<Long, Double>()

        val predicates = pairs.map {
            ratingEntity.courseName.eq(it.second).and(ratingEntity.professorName.eq(it.third))
        }

        val ratings = jpaQueryFactory.selectFrom(ratingEntity)
            .where(predicates.reduce { acc, predicate -> acc.or(predicate) })
            .fetch()
            .map { it.toDomain() }

        for (pair in pairs) {
            val rating =
                ratings.find { it.courseName == pair.second && it.professorName == pair.third }
            results[pair.first] = rating?.point ?: Rating.INIT
        }
        return results
    }


}

interface RatingJpaRepository : JpaRepository<RatingEntity, Long> {
}