package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component

//TODO: 리팩토링 필요
@Component
class UntakenCourseFetcher(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
) {
    fun fetchUntakenMajorCourses(
        userContext: UserContext,
        primaryTimetableCandidate: TimetableCandidate
    ): List<Course> {

        // 1. 시간표에 포함된 과목들의 상세 정보를 가져와, '전공 선택'과 그 외 과목으로 분류합니다.
        val coursesInTimetable = courseReader.findAllByCode(primaryTimetableCandidate.codes)
        val (majorElectivesInTimetable, otherCoursesInTimetable) = coursesInTimetable.partition {
            it.category == Category.MAJOR_ELECTIVE
        }

        // 2. '전공 선택'이 아닌 과목들의 ID와, 현재 시간표에 있는 '전공 선택' 과목들의 ID를 각각 Set으로 만듭니다.
        //    교체 대상은 '전공 선택' 과목들이 됩니다.
        val otherCourseIdsInTimetable = otherCoursesInTimetable.map { it.id }.toSet()
        val majorElectiveIdsInTimetable = majorElectivesInTimetable.map { it.id }.toSet()

        // 3. 교체 후보가 될 모든 '전공 선택' 과목을 조회합니다.
        // 다른 과목도 이런식으로 진행. TODO: 기획 변경사항 듣고 구체화
        val department = departmentReader.getByName(userContext.departmentName)

        // 학과, 학년, 분반으로 고정
        val allMajorCourses = courseReader.findAllBy(
            Category.MAJOR_ELECTIVE,
            department = department,
            grade = userContext.grade,
        )

        // 분반 필터링
            .filter { course ->
                userContext.division == "전체" || course.division == userContext.division
            }

        // 4. 최종 후보 목록을 필터링합니다.
        return allMajorCourses
            .filter { course ->
                // 기수강한 과목 제외 TODO: 근데 이건 불러올때 뺄예정
                // piki 한거 보고 체크
//                !takenCourseChecker.isCourseTaken(userContext.userId, course.code) &&
                // 현재 시간표의 '전공 선택'이 아닌 과목들과 중복되지 않아야 함
                !otherCourseIdsInTimetable.contains(course.id) &&
                // 현재 시간표의 '전공 선택' 과목들과도 중복되지 않아야 함 (즉, 시간표에 없는 새로운 과목)
                !majorElectiveIdsInTimetable.contains(course.id)
            }
            .sortedWith(
                compareBy<Course> {
                    when (it.category) {
                        Category.MAJOR_REQUIRED -> 1
                        Category.MAJOR_ELECTIVE -> 2
                        else -> 99 // 기타 카테고리는 후순위
                    }
                }
            )
    }
}
