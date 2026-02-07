from __future__ import annotations

import re

from grade_policy import MAX_GRADE, MIN_GRADE, is_valid_grade
from schemas import TargetRef, ValidationIssue


class RuleValidator:
    def __init__(self, allowed_departments: list[str]) -> None:
        self.allowed_departments = set(allowed_departments)

    def validate(
        self,
        row_no: int,
        course_code: str,
        raw_target_text: str,
        parsed_targets: list[TargetRef],
        unparsed_tokens: list[str],
    ) -> list[ValidationIssue]:
        issues: list[ValidationIssue] = []
        normalized_raw = (raw_target_text or "").strip()

        if not normalized_raw:
            issues.append(
                self._issue(
                    "TARGET_EMPTY",
                    "ERROR",
                    "수강대상이 비어 있습니다.",
                    row_no,
                    course_code,
                )
            )
            return issues

        if not parsed_targets:
            issues.append(
                self._issue(
                    "TARGET_PARSE_EMPTY",
                    "ERROR",
                    "수강대상 파싱 결과가 비어 있습니다.",
                    row_no,
                    course_code,
                    {"raw_target_text": normalized_raw},
                )
            )

        unknown_departments = sorted(
            {target.department for target in parsed_targets if target.department not in self.allowed_departments}
        )
        if unknown_departments:
            issues.append(
                self._issue(
                    "UNKNOWN_DEPARTMENT",
                    "ERROR",
                    "화이트리스트(data.yml)에 없는 학과가 포함되어 있습니다.",
                    row_no,
                    course_code,
                    {"departments": unknown_departments},
                )
            )

        invalid_parsed_grades = sorted({target.grade for target in parsed_targets if not is_valid_grade(target.grade)})
        if invalid_parsed_grades:
            issues.append(
                self._issue(
                    "GRADE_OUT_OF_RANGE",
                    "ERROR",
                    f"파싱된 학년이 {MIN_GRADE}~{MAX_GRADE} 범위를 벗어났습니다.",
                    row_no,
                    course_code,
                    {"grades": invalid_parsed_grades},
                )
            )

        raw_invalid_grades = self._find_invalid_grades_from_raw(normalized_raw)
        if raw_invalid_grades:
            issues.append(
                self._issue(
                    "RAW_GRADE_OUT_OF_RANGE",
                    "ERROR",
                    f"원본 수강대상 텍스트에 {MIN_GRADE}~{MAX_GRADE} 범위를 벗어난 학년 표기가 있습니다.",
                    row_no,
                    course_code,
                    {"grades": raw_invalid_grades},
                )
            )

        if unparsed_tokens:
            if parsed_targets:
                issues.append(
                    self._issue(
                        "UNPARSED_QUALIFIERS",
                        "WARN",
                        "비차단 성격의 추가 토큰이 존재합니다.",
                        row_no,
                        course_code,
                        {"tokens": unparsed_tokens},
                    )
                )
            elif self._is_non_blocking_unparsed_tokens(unparsed_tokens):
                issues.append(
                    self._issue(
                        "UNPARSED_QUALIFIERS",
                        "WARN",
                        "비차단 성격의 추가 토큰이 존재합니다.",
                        row_no,
                        course_code,
                        {"tokens": unparsed_tokens},
                    )
                )
            else:
                issues.append(
                    self._issue(
                        "UNPARSED_TOKENS",
                        "ERROR",
                        "파싱하지 못한 토큰이 존재합니다.",
                        row_no,
                        course_code,
                        {"tokens": unparsed_tokens},
                    )
                )

        duplicates = self._find_duplicates(parsed_targets)
        if duplicates:
            issues.append(
                self._issue(
                    "DUPLICATED_TARGET",
                    "WARN",
                    "중복 수강대상 항목이 발견되었습니다.",
                    row_no,
                    course_code,
                    {"targets": duplicates},
                )
            )

        return issues

    @staticmethod
    def _find_invalid_grades_from_raw(raw_target_text: str) -> list[int]:
        invalid: set[int] = set()

        for start, end in re.findall(r"([0-9]{1,2})\s*[-~]\s*([0-9]{1,2})\s*학년", raw_target_text):
            if start.isdigit():
                grade = int(start)
                if not is_valid_grade(grade):
                    invalid.add(grade)
            if end.isdigit():
                grade = int(end)
                if not is_valid_grade(grade):
                    invalid.add(grade)

        for match in re.findall(r"([0-9](?:\s*,\s*[0-9])+)\s*학년", raw_target_text):
            for item in re.split(r"\s*,\s*", match):
                if not item.isdigit():
                    continue
                grade = int(item)
                if not is_valid_grade(grade):
                    invalid.add(grade)

        for single in re.findall(r"([0-9]{1,2})\s*학년", raw_target_text):
            if not single.isdigit():
                continue
            grade = int(single)
            if not is_valid_grade(grade):
                invalid.add(grade)

        return sorted(invalid)

    @staticmethod
    def _find_duplicates(parsed_targets: list[TargetRef]) -> list[str]:
        seen: set[tuple[str, int]] = set()
        duplicates: list[str] = []
        for target in parsed_targets:
            key = (target.department, target.grade)
            if key in seen:
                duplicates.append(f"{target.department}{target.grade}")
            else:
                seen.add(key)
        return duplicates

    @staticmethod
    def _is_non_blocking_unparsed_tokens(tokens: list[str]) -> bool:
        if not tokens:
            return False
        qualifier_pattern = re.compile(
            r"(대상외수강제한|수강제한|타학과수강제한|순수외국인입학생|교환학생|군위탁|교직이수자|계약학과|"
            r"제외|제한|수강불가|수강신청|가능|융합|연계|전용강좌|"
            r"인문대|자연대|경영대|사회대|공대|공과대|IT대|법과대|경통대|자유전공|"
            r"내국인|시간제|A그룹|B그룹|구|포함|목|부|대)"
        )
        for token in tokens:
            if qualifier_pattern.search(token):
                continue
            return False
        return True

    @staticmethod
    def _issue(
        code: str,
        severity: str,
        message: str,
        row_no: int,
        course_code: str,
        details: dict | None = None,
    ) -> ValidationIssue:
        return ValidationIssue(
            code=code,
            severity=severity,
            message=message,
            row_no=row_no,
            course_code=course_code,
            details=details or {},
        )
