package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.common.support.config.ApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class CourseManagerTest {
    @Autowired
    private lateinit var courseWriter: CourseWriter

    @Autowired
    private lateinit var coursesManager: CoursesManager

    @Test
    fun `test getCourses`() {
        // given
        val courseName = "Hello"
        val course = Course(
            courseName = courseName,
            courseCode = 123456,
            classification = Classification.MAJOR_REQUIRED,
            credit = 3,
            professorName = "John Doe",
        )
        courseWriter.save(course)
        coursesManager.initialCoursesCache()

        // when
        val courses = coursesManager.getCoursesByName(courseName)

        // then
        assert(courses.isNotEmpty()) { "Courses should not be empty" }
    }
}
