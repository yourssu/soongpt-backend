from __future__ import annotations

MIN_GRADE = 1
MAX_GRADE = 5
ALL_GRADES = set(range(MIN_GRADE, MAX_GRADE + 1))


def is_valid_grade(grade: int) -> bool:
    return MIN_GRADE <= grade <= MAX_GRADE
