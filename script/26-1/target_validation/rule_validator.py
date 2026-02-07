from __future__ import annotations

import re

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

        invalid_parsed_grades = sorted({target.grade for target in parsed_targets if target.grade < 1 or target.grade > 5})
        if invalid_parsed_grades:
            issues.append(
                self._issue(
                    "GRADE_OUT_OF_RANGE",
                    "ERROR",
                    "파싱된 학년이 1~5 범위를 벗어났습니다.",
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
                    "원본 수강대상 텍스트에 1~5 범위를 벗어난 학년 표기가 있습니다.",
                    row_no,
                    course_code,
                    {"grades": raw_invalid_grades},
                )
            )

        if unparsed_tokens:
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
        for match in re.findall(r"([0-9]{1,2})\s*학년", raw_target_text):
            if not match.isdigit():
                continue
            grade = int(match)
            if grade < 1 or grade > 5:
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
