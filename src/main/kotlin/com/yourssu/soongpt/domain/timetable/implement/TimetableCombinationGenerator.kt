package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.implement.dto.GroupedCourseCandidates
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component

private const val MAXIMUM_TIMETABLE_CANDIDATES = 2000

@Component
class TimetableCombinationGenerator(private val courseCandidateFactory: CourseCandidateFactory) {
    fun generate(groupedCandidates: GroupedCourseCandidates): List<TimetableCandidate> {
        val courseGroups = groupedCandidates.getAllOrdered()
        val courseCandidateGroups =
                courseGroups
                        .filter { it.isNotEmpty() } // 분반이 없는 과목 그룹은 조합에서 제외
                        .map { group ->
                            group.map { course -> courseCandidateFactory.create(course) }
                        }

        val results = mutableListOf<TimetableCandidate>()
        val builder = TimetableCandidateBuilder()

        fun findCombinations(depth: Int) {
            if (results.size >= MAXIMUM_TIMETABLE_CANDIDATES) {
                return
            }

            if (depth == courseCandidateGroups.size) {
                results.add(builder.build())
                return
            }

            for (candidate in courseCandidateGroups[depth]) {
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
