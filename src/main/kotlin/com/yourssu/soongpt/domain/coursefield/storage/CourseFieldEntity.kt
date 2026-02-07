package com.yourssu.soongpt.domain.coursefield.storage

import com.yourssu.soongpt.domain.coursefield.implement.CourseField
import jakarta.persistence.*

@Entity
@Table(
    name = "course_field",
    indexes = [
        Index(name = "idx_course_field_code", columnList = "course_code")
    ]
)
class CourseFieldEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val courseCode: Long,

    @Column(nullable = false)
    val courseName: String,

    @Column(nullable = false)
    val field: String,
) {
    companion object {
        fun from(courseField: CourseField): CourseFieldEntity {
            return CourseFieldEntity(
                id = courseField.id,
                courseCode = courseField.courseCode,
                courseName = courseField.courseName,
                field = courseField.field,
            )
        }
    }

    fun toDomain(): CourseField {
        return CourseField(
            id = id,
            courseCode = courseCode,
            courseName = courseName,
            field = field,
        )
    }
}
