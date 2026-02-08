package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.timetable.implement.exception.TimetableConflictException
import org.springframework.stereotype.Component

@Component
class FinalizeTimetableValidator(
    private val timetableBitsetConverter: TimetableBitsetConverter,
    private val courseCandidateFactory: CourseCandidateFactory
) {
    fun validate(timetableId: Long, coursesToAdd: List<Course>) {
        if (coursesToAdd.isEmpty()) {
            return
        }

        // 1. 기반 시간표의 시간 정보(BitSet)를 가져옴
        val timetableBitSet = timetableBitsetConverter.convert(timetableId)

        // 2. 추가할 과목들과 시간 충돌 검사
        coursesToAdd.forEach { course ->
            val courseCandidate = courseCandidateFactory.create(course)
            if (timetableBitSet.intersects(courseCandidate.timeSlot)) {
                throw TimetableConflictException("시간이 겹치는 과목(${course.name})이 있어 시간표를 완성할 수 없습니다.")
            }
            timetableBitSet.or(courseCandidate.timeSlot) // 검사 통과한 과목은 다음 검사를 위해 BitSet에 추가
        }
    }
}
