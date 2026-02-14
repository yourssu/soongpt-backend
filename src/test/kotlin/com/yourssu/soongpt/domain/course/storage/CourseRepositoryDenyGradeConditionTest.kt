package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import io.kotest.matchers.collections.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseRepositoryDenyGradeConditionTest {

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var targetRepository: TargetRepository

    @Test
    fun `deny rule for lower grade must not exclude higher grade student`() {
        val courseCode = 9_999_999_900L
        val collegeId = 10L
        val departmentId = 999L

        courseRepository.save(
            Course(
                category = Category.CHAPEL,
                code = courseCode,
                name = "비전채플(테스트)",
                professor = "TEST",
                department = "학원선교팀",
                time = "1.0",
                point = "0.5",
                personeel = 1,
                scheduleRoom = "월 10:30-11:20 (TEST 101)",
                target = "2학년 IT대 (수강제한:1학년 IT대)",
                credit = 0.5,
            )
        )

        // Allow: 2학년 IT대학
        // Deny: 1학년 IT대학
        // => 2학년 학생은 Deny(1학년)에 매칭되면 안 된다.
        targetRepository.saveAll(
            listOf(
                Target(
                    courseCode = courseCode,
                    scopeType = ScopeType.COLLEGE,
                    collegeId = collegeId,
                    grade2 = true,
                    isDenied = false,
                ),
                Target(
                    courseCode = courseCode,
                    scopeType = ScopeType.COLLEGE,
                    collegeId = collegeId,
                    grade1 = true,
                    isDenied = true,
                ),
            )
        )

        val codes = courseRepository.findCoursesWithTargetByCategory(
            category = Category.CHAPEL,
            departmentId = departmentId,
            collegeId = collegeId,
            userGrade = 2,
            maxGrade = 2,
        ).map { it.course.code }

        codes shouldContain courseCode
    }
}
