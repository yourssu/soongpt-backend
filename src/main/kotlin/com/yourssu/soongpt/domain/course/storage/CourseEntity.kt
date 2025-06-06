package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.course.implement.Course
import jakarta.persistence.*

@Entity
@Table(name = "course")
class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val courseName: String,

    @Column(nullable = true, unique = true)
    val courseCode: Int?,

    @Column(nullable = true)
    val professorName: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val classification: Classification,

    @Column(nullable = false)
    val credit: Int,
) {
    companion object {
        fun from(course: Course) = CourseEntity(
            id = course.id,
            courseName = course.courseName,
            courseCode = course.courseCode,
            professorName = course.professorName,
            classification = course.classification,
            credit = course.credit,
        )
    }

    fun toDomain() = Course(
        id = id,
        courseName = courseName,
        professorName = professorName,
        classification = classification,
        credit = credit,
        courseCode = courseCode,
    )
}
