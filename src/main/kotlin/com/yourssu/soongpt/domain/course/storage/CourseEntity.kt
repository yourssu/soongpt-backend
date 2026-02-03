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

    @Column(nullable = true)
    val field: String? = null,

    @Column(nullable = false, unique = true)
    val code: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    val professor: String? = null,

    @Column(nullable = false)
    val department: String,

    @Column(nullable = true)
    val division: String? = null,

    @Column(nullable = false)
    val time: String,

    @Column(nullable = false)
    val point: String,

    @Column(nullable = false)
    val personeel: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val scheduleRoom: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val target: String,

    @Column(nullable = true)
    val credit: Double? = null,
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
                division = course.division,
                time = course.time,
                point = course.point,
                personeel = course.personeel,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
                credit = course.credit,
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
            division = division,
            time = time,
            point = point,
            personeel = personeel,
            scheduleRoom = scheduleRoom,
            target = target,
            credit = credit,
        )
    }
}
