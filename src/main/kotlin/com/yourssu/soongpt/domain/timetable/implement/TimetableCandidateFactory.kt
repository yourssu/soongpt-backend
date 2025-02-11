package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.timetable.implement.exception.TimetableCreatedBadRequestException
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoMorningClassesStrategy.Companion.MORNING_CLASSES_SCORE
import org.springframework.stereotype.Component


@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
) {
    companion object {
        private const val MAXIMUM_TAG_LIMIT = 3
        private const val MAXIMUM_PER_TAG = 3
        private const val TOTAL = 5
    }

    fun createTimetableCandidatesWithRule(coursesCandidates: List<Courses>): TimetableCandidates {
        if (validateEmptyCase(coursesCandidates)) {
            return TimetableCandidates(TimetableCandidate.empty())
        }
        val timetableCandidates= TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        }).filterRules()
        validateNoneTimetableCases(timetableCandidates)
        return timetableCandidates
    }

    private fun validateNoneTimetableCases(timetableCandidates: TimetableCandidates) {
        if (timetableCandidates.values.isEmpty()) {
            throw TimetableCreatedBadRequestException()
        }
    }

    private fun validateEmptyCase(coursesCandidates: List<Courses>): Boolean {
        return coursesCandidates.isEmpty()
    }

    fun extendWithRatings(
        timetableCandidates: TimetableCandidates,
        addCourses: List<Pair<Courses, Double>>,
        morningClassesScore: Int = MORNING_CLASSES_SCORE,
    ): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map { (courses, score) ->
            val courseTimes = courseTimeReader.findAllByCourseIds(courses.getAllIds())
            Triple(courses, CourseTimes(courseTimes), score.toInt() + countMorningClasses(CourseTimes(courseTimes), morningClassesScore))
        }).filterRules()
    }

    private fun countMorningClasses(courseTimes: CourseTimes, score: Int): Int {
        return courseTimes.countMorningClasses() * score
    }

    fun pickTopNEachTag(timetableCandidates: TimetableCandidates, n: Int): TimetableCandidates {
        return TimetableCandidates(timetableCandidates.values.groupBy { it.tag }
            .map { timetables -> timetables.value.sortedByDescending { it.score }.take(n) }.flatten())
    }

    fun pickFinalTimetables(step: TimetableCandidates): TimetableCandidates {
        return step.pickTopNOfFinalScores(
            maximumTagLimit = MAXIMUM_TAG_LIMIT,
            maximumPerTag = MAXIMUM_PER_TAG,
            total = TOTAL
        )
    }
}