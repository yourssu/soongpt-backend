import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course2
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week

@JsonIgnoreProperties(ignoreUnknown = true)
data class CourseDto(
    @JsonProperty("category")
    val category: String,

    @JsonProperty("sub_category")
    val subCategory: String? = null,

    @JsonProperty("field")
    val field: String? = null,

    @JsonProperty("code")
    val code: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("professor")
    val professor: String? = null,

    @JsonProperty("department")
    val department: String,

    @JsonProperty("time_points")
    val timePoints: String,

    @JsonProperty("target")
    val target: String,

    @JsonProperty("courseTime")
    val courseTime: List<CourseTimeDto> = emptyList()
) {
    fun toDomain(): Course2 {
        return Course2(
            courseName = name,
            professorName = professor,
            category = Category.match(category),
            credit = timePoints.split("/").getOrNull(0)?.toIntOrNull() ?: 0,
            target = target,
            field = field,
            courseCode = code.toInt(),
            courseTime = courseTime.mapNotNull { it.toDomain() }
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CourseTimeDto(
    @JsonProperty("week")
    val week: String,

    @JsonProperty("startTime")
    val startTime: String,

    @JsonProperty("endTime")
    val endTime: String,

    @JsonProperty("classroom")
    val classroom: String? = null,

    @JsonProperty("courseCode")
    val courseCode: Int = 0
) {
    fun toDomain(): CourseTime? {
        return CourseTime(
            week = Week.fromName(week),
            startTime = Time.of(startTime),
            endTime = Time.of(endTime),
            classroom = classroom,
            courseId = courseCode.toLong()
        )
    }
}
