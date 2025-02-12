package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.timetable.implement.strategy.FreeDayTagStrategy
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoEveningClassesStrategy
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoLongBreaksStrategy.Companion.BREAKS_MINUTE
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoLongBreaksStrategy.Companion.BREAKS_SCORE
import com.yourssu.soongpt.domain.timetable.implement.strategy.NoMorningClassesStrategy.Companion.MORNING_CLASSES_SCORE

class TimetableCandidate(
    val courses: Courses,
    private val coursesTimes: CourseTimes,
    val tag: Tag,
    val score: Int = 0,
) {
    companion object {
        fun fromAllTags(courses: Courses, coursesTimes: CourseTimes): List<TimetableCandidate> {
            return Tag.entries.map { tag ->
                TimetableCandidate(courses, coursesTimes, tag)
            }
        }

        fun empty(): List<TimetableCandidate> {
            return Tag.entries.map { tag ->
                TimetableCandidate(Courses(emptyList()), CourseTimes(emptyList()), tag)
            }
        }
    }

    fun isCorrect(): Boolean {
        return tag.strategy.isCorrect(courses, coursesTimes)
    }

    fun isCorrectCreditRule(): Boolean {
        return courses.totalCredit() < 23
    }

    fun hasOverlappingCourseTimes(): Boolean {
        return coursesTimes.hasOverlappingCourseTimes()
    }

    fun generateNewTimetableCandidate(
        courses: Courses,
        courseTimes: CourseTimes,
        score: Int,
    ): TimetableCandidate {
        return TimetableCandidate(
            courses = this.courses.extend(courses),
            coursesTimes = this.coursesTimes.extend(courseTimes),
            tag = tag,
            score = this.score + score,
        )
    }

    fun calculateFinalScore(): Int {
        return score + calculateMorningScore() + calculateBreaksScore() + calculateEveningClassesScore() +
                calculateOneClassPerDayScore() + calculateFreeDayScore()
    }

    private fun calculateMorningScore(): Int {
        return coursesTimes.countMorningClasses() * MORNING_CLASSES_SCORE
    }

    private fun calculateBreaksScore(): Int {
        return coursesTimes.countBreaks(BREAKS_MINUTE) * BREAKS_SCORE
    }

    private fun calculateEveningClassesScore(): Int {
        return coursesTimes.countEveningClasses() * NoEveningClassesStrategy.EVENING_CLASSES_SCORE
    }

    private fun calculateOneClassPerDayScore(): Int {
        return coursesTimes.countOneClassPerDay() * FreeDayTagStrategy.ONE_CLASS_PER_DAY_SCORE
    }

    private fun calculateFreeDayScore(): Int {
        return coursesTimes.countFreeDayScore()
    }

    fun coursesHashed(): String {
        return courses.getAllIds().sorted()
            .joinToString()
    }

    fun copy(tag: Tag = this.tag, score: Int = this.score): TimetableCandidate {
        return TimetableCandidate(courses, coursesTimes, tag, score)
    }

    fun totalCredit(): Int {
        return courses.totalCredit()
    }
}