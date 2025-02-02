package com.yourssu.soongpt.common.business.initialization

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.domain.college.storage.QCollegeEntity.collegeEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class CollegesAndDepartmentsInitializerTest {
    @Autowired
    private lateinit var collegesAndDepartmentsInitializer: CollegesAndDepartmentsInitializer

    @Autowired
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class 단과대_및_학과_초기설정_클래스는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 단과대_테이블이_비어있는_경우 {
            @Test
            @DisplayName("data.yml 파일의 단과대 및 학과 정보로 초기설정을 한다.")
            fun success() {
                collegesAndDepartmentsInitializer.run()

                val collegeCount: Boolean = jpaQueryFactory.select(collegeEntity.count())
                    .from(collegeEntity)
                    .fetchOne()!! > 0

                assertThat(collegeCount).isTrue
            }
        }
    }
}