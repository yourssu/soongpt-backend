package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import org.springframework.stereotype.Component

@Component
class UserContextProvider(
    private val departmentReader: DepartmentReader
) {
    fun getContext(userId: String): UserContext {
        // TODO: 추후 userId를 기반으로 실제 사용자 정보를 조회하는 로직으로 변경 필요
        val department = departmentReader.getByName("화학공학과")
        return UserContext(
            userId = userId,
            department = department,
            grade = 3,
            schoolId = 21,
            division = "A",
        )
    }
}
