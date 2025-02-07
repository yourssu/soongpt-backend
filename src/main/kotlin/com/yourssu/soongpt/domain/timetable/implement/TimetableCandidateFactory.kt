package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import org.springframework.stereotype.Component

private const val MORNING_CLASSES_SCORE = 10

@Component
class TimetableCandidateFactory(
    private val courseTimeReader: CourseTimeReader,
) {
    fun createTimetableCandidatesWithRule(coursesCandidates: List<Courses>): TimetableCandidates {
        return TimetableCandidates(coursesCandidates.flatMap {
            TimetableCandidate.fromAllTags(
                courses = it,
                coursesTimes = CourseTimes(courseTimeReader.findAllByCourseIds(it.getAllIds())),
            )
        }).filterRules()
    }

    fun extendWithRatings(
        timetableCandidates: TimetableCandidates,
        addCourses: List<Pair<Courses, Double>>,
        morningClassesScore: Int = MORNING_CLASSES_SCORE,
    ): TimetableCandidates {
        return timetableCandidates.extendCourses(addCourses.map { (courses, score) ->
            val courseTimes = courseTimeReader.findAllByCourseIds(courses.getAllIds())
            Triple(courses, CourseTimes(courseTimes), score.toInt() - countMorningClasses(CourseTimes(courseTimes), morningClassesScore))
        }).filterRules()
    }

    private fun countMorningClasses(courseTimes: CourseTimes, score: Int): Int {
        return courseTimes.countMorningClasses() * score
    }

    fun pickTopNEachTag(timetableCandidates: TimetableCandidates, n: Int): TimetableCandidates {
        return TimetableCandidates(timetableCandidates.values.groupBy { it.tag }
            .map { it.value.sortedByDescending { it.score }.take(n) }.flatten())
    }
}