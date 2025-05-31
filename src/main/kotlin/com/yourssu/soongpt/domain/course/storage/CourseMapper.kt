
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.yourssu.soongpt.domain.course.implement.Course2

private val objectMapper = jacksonObjectMapper()

object CourseMapper {
    fun toCourseDomain(jsonString: String): List<Course2> {
        return parseFromJson(jsonString).map { it.toDomain() }
    }

    private fun parseFromJson(jsonString: String): List<CourseDto> {
        return objectMapper.readValue<List<CourseDto>>(jsonString)
    }
}
