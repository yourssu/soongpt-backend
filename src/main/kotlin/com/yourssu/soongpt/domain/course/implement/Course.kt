package com.yourssu.soongpt.domain.course.implement

class Course(
    val id: Long? = null,
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Long,
    val name: String,
    val professor: String? = null,
    val department: String,
    val division: String? = null,
    val time: String,
    val point: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
) {
    fun copy(
        id: Long? = this.id,
        category: Category = this.category,
        subCategory: String? = this.subCategory,
        field: String? = this.field,
        code: Long = this.code,
        name: String = this.name,
        professor: String? = this.professor,
        department: String = this.department,
        division: String? = this.division,
        time: String = this.time,
        point: String = this.point,
        personeel: Int = this.personeel,
        scheduleRoom: String = this.scheduleRoom,
        target: String = this.target
    ): Course {
        return Course(
            id,
            category,
            subCategory,
            field,
            code,
            name,
            professor,
            department,
            division,
            time,
            point,
            personeel,
            scheduleRoom,
            target
        )
    }
}
