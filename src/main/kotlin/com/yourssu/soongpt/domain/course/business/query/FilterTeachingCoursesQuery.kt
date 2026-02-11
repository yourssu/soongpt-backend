package com.yourssu.soongpt.domain.course.business.query

import com.yourssu.soongpt.domain.course.implement.TeachingArea
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea

data class FilterTeachingCoursesQuery(
    val schoolId: Int,
    val departmentName: String,

    /**
     * 교직 이수 관점의 대분류 (전공영역/교직영역/특성화)
     */
    val majorArea: TeachingMajorArea? = null,

    /**
     * (하위 필터, 선택) 기존 teachingArea(교직이론/소양/실습/교과교육) 기반 필터.
     * majorArea만으로도 조회 가능하지만, 기존 호환을 위해 남겨둔다.
     */
    val teachingArea: TeachingArea? = null,
)
