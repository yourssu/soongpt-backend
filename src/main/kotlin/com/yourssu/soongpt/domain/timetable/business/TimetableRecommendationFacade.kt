package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.timetable.business.dto.FullTimetableRecommendationResponse
import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.RecommendationDto
import com.yourssu.soongpt.domain.timetable.business.dto.SuggestionCandidate
import com.yourssu.soongpt.domain.timetable.business.dto.UserContext
import com.yourssu.soongpt.domain.timetable.implement.CourseCandidateProvider
import com.yourssu.soongpt.domain.timetable.implement.TimetableCombinationGenerator
import com.yourssu.soongpt.domain.timetable.implement.TimetablePersister
import com.yourssu.soongpt.domain.timetable.implement.TimetableRanker
import com.yourssu.soongpt.domain.timetable.implement.UntakenCourseFetcher
import com.yourssu.soongpt.domain.timetable.implement.dto.GroupedCourseCandidates
import com.yourssu.soongpt.domain.timetable.implement.exception.TimetableCandidateNotGeneratedException
import com.yourssu.soongpt.domain.timetable.implement.suggester.CourseSwapSuggester
import com.yourssu.soongpt.domain.timetable.implement.suggester.DivisionChangeSuggester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TimetableRecommendationFacade(
        private val courseCandidateProvider: CourseCandidateProvider,
        private val timetableCombinationGenerator: TimetableCombinationGenerator,
        private val timetableRanker: TimetableRanker,
        private val divisionChangeSuggester: DivisionChangeSuggester,
        private val courseSwapSuggester: CourseSwapSuggester,
        private val untakenCourseFetcher: UntakenCourseFetcher,
        private val timetablePersister: TimetablePersister
) {
        @Transactional
        fun recommend(command: PrimaryTimetableCommand): FullTimetableRecommendationResponse {
                // 사용자 정보 조회 (학과, 학년)
                // 해당 부분을 UserContextFetcher 같은걸로 가져오도록 수정할 예정
                // 현재는 구현상 그냥 command에서 불러오는식.
                val userContext =
                        UserContext(
                                userId = command.userId,
                                departmentName = command.departmentName,
                                grade = command.grade,
                                schoolId = 20, // TODO: 학생ID 추가 필요
                                division = "" // TODO: 분반 정보 추가 필요
                        )

                // 2. 모든 후보를 DTO로 묶기
                val groupedCourseCandidates =
                        GroupedCourseCandidates(
                                retake =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.retakeCourses
                                        ),
                                majorRequired =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.majorRequiredCourses
                                        ),
                                majorElective =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.majorElectiveCourses
                                        ),
                                otherMajor =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.otherMajorCourses
                                        ),
                                generalRequired =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.generalRequiredCourses
                                        ),
                                added =
                                        courseCandidateProvider.getCourseCandidates(
                                                command.addedCourses
                                        )
                        )

                // 3. 시간표 조합 생성
                val combinations = timetableCombinationGenerator.generate(groupedCourseCandidates)
                if (combinations.isEmpty()) {
                        throw TimetableCandidateNotGeneratedException()
                }
                val rankedCombinations = timetableRanker.rank(combinations)

                // 2. 1차 추천 시간표 선정
                val primaryTimetableCandidate = rankedCombinations.first()
                val remainingCandidates = rankedCombinations.drop(1)

                // 3. [유형 1] 분반 변경 제안 후보 생성
                val divisionChangeSuggestions: List<SuggestionCandidate> =
                        divisionChangeSuggester.suggest(
                                primaryTimetableCandidate,
                                remainingCandidates
                        )

                // new: 과목 교체 제안 후보 생성
                // 4. [유형 2] 과목 교체 제안 후보 생성
                val untakenMajorCourses =
                        untakenCourseFetcher.fetchUntakenMajorCourses(
                                userContext,
                                primaryTimetableCandidate
                        )
                val courseSwapSuggestions: List<SuggestionCandidate> =
                        courseSwapSuggester.suggest(
                                primaryTimetableCandidate,
                                command,
                                untakenMajorCourses
                        )

                // 5. 모든 제안 후보 통합 랭킹 및 최종 6개 선정
                val allSuggestions = divisionChangeSuggestions + courseSwapSuggestions
                val rankedSuggestions = timetableRanker.rankSuggestions(allSuggestions).take(6)

                // 6. 결과 DB 저장 및 최종 응답 DTO 구성
                // 여기선 후보만 뿌리고, 프론트에서 선택시 저장하는 방식도 고려해볼 수 있음 ...
                val primaryResponse = timetablePersister.persist(primaryTimetableCandidate)

                val recommendationDtos =
                        rankedSuggestions.map { suggestion ->
                                RecommendationDto(
                                        description = suggestion.description,
                                        timetable =
                                                timetablePersister.persist(
                                                        suggestion.resultingTimetableCandidate
                                                )
                                )
                        }

                return FullTimetableRecommendationResponse(
                        primaryTimetable = primaryResponse,
                        alternativeSuggestions = recommendationDtos
                )
        }
}
