package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import jakarta.persistence.*

@Entity
@Table(name = "course")
class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,

    @Column(nullable = true)
    val subCategory: String? = null,

    @Column(nullable = false)
    val field: String? = null,

    @Column(nullable = true, unique = true)
    val code: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    val professor: String? = null,

    @Column(nullable = false)
    val department: String,

    @Column(nullable = false)
    val timePoints: String,

    @Column(nullable = false)
    val personeel: Int,

    @Column(nullable = false)
    val scheduleRoom: String,

    @Column(nullable = false)
    val target: String,
) {
    companion object {
        fun from(course: Course): CourseEntity {

            return CourseEntity(
                id = course.id,
                category = course.category,
                subCategory = course.subCategory,
                field = course.field,
                code = course.code,
                name = course.name,
                professor = course.professor,
                department = course.department,
                timePoints = course.timePoints,
                personeel = 0,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
            )
        }
    }


    fun toDomain(): Course {
        return Course(
            id = id,
            category = category,
            subCategory = subCategory,
            field = field,
            code = code,
            name = name,
            professor = professor,
            department = department,
            timePoints = timePoints,
            personeel = personeel,
            scheduleRoom = scheduleRoom,
            target = target
        )
    }
}
