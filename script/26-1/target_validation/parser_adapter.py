from __future__ import annotations

import csv
import re
from dataclasses import dataclass

from schemas import TargetRef

TARGET_COLUMN_CANDIDATES = ("수강대상", "수강 대상", "target", "Target")
COURSE_CODE_COLUMN_CANDIDATES = ("과목코드", "course_code", "code", "Code")

_DEFAULT_ALL_GRADES = {1, 2, 3, 4, 5}
_GENERIC_NOISE = {
    "수강대상",
    "수강",
    "대상",
    "가능",
    "재학생",
    "학생",
    "학년",
    "전체",
    "전학년",
    "전체학년",
    "이상",
    "이하",
    "및",
    "또는",
    "공통",
    "재수강",
}


@dataclass
class InputRow:
    row_no: int
    course_code: str
    raw_target_text: str
    raw_record: dict[str, str]


def detect_column(headers: list[str], explicit: str | None, candidates: tuple[str, ...]) -> str:
    if explicit:
        if explicit not in headers:
            raise ValueError(f"column not found: {explicit}")
        return explicit

    for candidate in candidates:
        if candidate in headers:
            return candidate

    raise ValueError(f"could not detect column from candidates={candidates}, headers={headers}")


def read_input_rows(
    csv_path: str,
    target_column: str | None = None,
    course_code_column: str | None = None,
) -> tuple[list[InputRow], str, str]:
    with open(csv_path, encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        if not reader.fieldnames:
            raise ValueError("CSV header is empty.")
        headers = [header.strip() for header in reader.fieldnames]
        target_col = detect_column(headers, target_column, TARGET_COLUMN_CANDIDATES)
        code_col = detect_column(headers, course_code_column, COURSE_CODE_COLUMN_CANDIDATES)

        rows: list[InputRow] = []
        for index, raw_row in enumerate(reader, start=2):
            record = {str(key).strip(): (value or "").strip() for key, value in raw_row.items()}
            rows.append(
                InputRow(
                    row_no=index,
                    course_code=record.get(code_col, f"ROW-{index}"),
                    raw_target_text=record.get(target_col, ""),
                    raw_record=record,
                )
            )
    return rows, target_col, code_col


def build_alias_lookup(allowed_departments: list[str], aliases: dict[str, str]) -> list[tuple[str, str]]:
    lookup: dict[str, str] = {}
    for department in allowed_departments:
        lookup[department] = department

    allowed_set = set(allowed_departments)
    for alias, canonical in aliases.items():
        if canonical in allowed_set:
            lookup[alias] = canonical

    return sorted(lookup.items(), key=lambda item: len(item[0]), reverse=True)


def parse_target_text(
    raw_target_text: str,
    alias_lookup: list[tuple[str, str]],
) -> tuple[list[TargetRef], list[str]]:
    normalized = _normalize_text(raw_target_text)
    if not normalized:
        return [], ["EMPTY_TARGET_TEXT"]

    departments, removed_dept_text = _extract_departments(normalized, alias_lookup)
    grades = _extract_grade_set(normalized)
    if grades is None:
        grades = set(_DEFAULT_ALL_GRADES)

    parsed_targets: list[TargetRef] = []
    seen: set[tuple[str, int]] = set()
    for department in departments:
        for grade in sorted(grades):
            key = (department, grade)
            if key in seen:
                continue
            seen.add(key)
            parsed_targets.append(TargetRef(department=department, grade=grade))

    unparsed_tokens = _extract_unparsed_tokens(removed_dept_text)
    if not departments:
        if normalized not in unparsed_tokens:
            unparsed_tokens.insert(0, normalized)
        return [], _deduplicate(unparsed_tokens)

    return parsed_targets, _deduplicate(unparsed_tokens)


def _normalize_text(text: str) -> str:
    normalized = text or ""
    normalized = normalized.replace("\n", " ").replace("\t", " ")
    normalized = re.sub(r"\s+", " ", normalized)
    return normalized.strip()


def _extract_departments(text: str, alias_lookup: list[tuple[str, str]]) -> tuple[list[str], str]:
    departments: list[str] = []
    removed = text
    seen_departments: set[str] = set()

    for alias, canonical in alias_lookup:
        if not alias:
            continue
        pattern = re.escape(alias)
        if not re.search(pattern, removed, flags=re.IGNORECASE):
            continue

        if canonical not in seen_departments:
            seen_departments.add(canonical)
            departments.append(canonical)
        removed = re.sub(pattern, " ", removed, flags=re.IGNORECASE)

    return departments, _normalize_text(removed)


def _extract_grade_set(text: str) -> set[int] | None:
    if re.search(r"(전체|전)\s*학년", text):
        return set(_DEFAULT_ALL_GRADES)

    grades: set[int] = set()

    for start, end in re.findall(r"([0-9]{1,2})\s*[-~]\s*([0-9]{1,2})\s*학년", text):
        try:
            low = int(start)
            high = int(end)
        except ValueError:
            continue
        if low > high:
            low, high = high, low
        for grade in range(low, high + 1):
            grades.add(grade)

    for match in re.findall(r"([0-9](?:\s*,\s*[0-9])+)\s*학년", text):
        for item in re.split(r"\s*,\s*", match):
            if item.isdigit():
                grades.add(int(item))

    for start in re.findall(r"([0-9]{1,2})\s*학년\s*이상", text):
        if start.isdigit():
            low = int(start)
            for grade in range(low, 6):
                grades.add(grade)

    for end in re.findall(r"([0-9]{1,2})\s*학년\s*이하", text):
        if end.isdigit():
            high = int(end)
            for grade in range(1, high + 1):
                grades.add(grade)

    for single in re.findall(r"([0-9]{1,2})\s*학년", text):
        if single.isdigit():
            grades.add(int(single))

    return grades or None


def _extract_unparsed_tokens(text: str) -> list[str]:
    cleaned = text
    patterns = [
        r"([0-9]{1,2})\s*[-~]\s*([0-9]{1,2})\s*학년",
        r"([0-9](?:\s*,\s*[0-9])+)\s*학년",
        r"([0-9]{1,2})\s*학년\s*이상",
        r"([0-9]{1,2})\s*학년\s*이하",
        r"([0-9]{1,2})\s*학년",
        r"(전체|전)\s*학년",
    ]
    for pattern in patterns:
        cleaned = re.sub(pattern, " ", cleaned)

    cleaned = re.sub(r"[/|;]", " ", cleaned)
    cleaned = re.sub(r"[()\[\]{}]", " ", cleaned)
    cleaned = re.sub(r"[,:.]", " ", cleaned)
    cleaned = re.sub(r"\s+", " ", cleaned).strip()

    if not cleaned:
        return []

    tokens = [token.strip() for token in cleaned.split(" ") if token.strip()]
    result: list[str] = []
    for token in tokens:
        if token in _GENERIC_NOISE:
            continue
        if token.isdigit():
            continue
        result.append(token)

    return result


def _deduplicate(values: list[str]) -> list[str]:
    unique: list[str] = []
    seen: set[str] = set()
    for value in values:
        if value in seen:
            continue
        seen.add(value)
        unique.append(value)
    return unique
