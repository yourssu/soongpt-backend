package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import org.springframework.stereotype.Component

@Component
class CourseCandidateProvider(private val courseReader: CourseReader) {
    fun getCourseCandidates(selectedCourses: List<SelectedCourseCommand>): List<List<Course>> {
        return selectedCourses.map {
            // 해당 부분에서 이미 들었던걸 거르는 로직 필요, 이건 피키가 처리할 예정.
            // 여기서 추가 파라미터로 뭐가 들어가는지는 생각해봐야함
            val courses =
                    if (it.selectedCourseIds.isEmpty()) {
                        // 분반 선택 안할때 (전체 분반 후보)를 가져와서 해야함
                        // TODO: piki가 만들어준걸 받아온다 가정.
                        emptyList()
                    } else {
                        courseReader.findAllByCode(it.selectedCourseIds)
                    }
            courses.ifEmpty { emptyList() }
        }
    }
}
