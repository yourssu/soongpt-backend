package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.ArrayList

private const val MAXIMUM_TIMETABLE_CANDIDATES = 50

@Component
class TimetableCombinationGenerator {
    fun generate(
        courseCandidateGroups: List<List<CourseCandidate>>,
        baseTimetable: TimetableCandidate? = null,
        maxCandidates: Int? = MAXIMUM_TIMETABLE_CANDIDATES,
    ): List<TimetableCandidate> {
        val results = mutableListOf<TimetableCandidate>()
        val builder = if (baseTimetable != null) {
            TimetableCandidateBuilder(
                initialCodes = baseTimetable.codes,
                initialTimeSlot = baseTimetable.timeSlot
            )
        } else {
            TimetableCandidateBuilder()
        }

        // 비어있는 과목 그룹(선택한 분반이 모두 기수강 과목인 경우 등)은 조합에서 제외
        val validCourseCandidateGroups = courseCandidateGroups.filter { it.isNotEmpty() }
        if (validCourseCandidateGroups.isEmpty()) return emptyList()

        fun findCombinations(depth: Int) {
            if (maxCandidates != null && results.size >= maxCandidates) {
                return
            }

            if (depth == validCourseCandidateGroups.size) {
                results.add(builder.build())
                return
            }

            // 각 과목 그룹(한 과목의 모든 분반 후보)을 순회
            for (candidate in validCourseCandidateGroups[depth]) {
                if (builder.add(candidate)) {
                    findCombinations(depth + 1)
                    builder.remove(candidate)
                }
            }
        }

        findCombinations(0)
        return results
    }
}
