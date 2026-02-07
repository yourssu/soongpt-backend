from __future__ import annotations

import csv
import re
from dataclasses import dataclass

from grade_policy import ALL_GRADES, MAX_GRADE, MIN_GRADE, is_valid_grade
from schemas import TargetRef

TARGET_COLUMN_CANDIDATES = ("수강대상", "수강 대상", "target", "Target")
COURSE_CODE_COLUMN_CANDIDATES = ("과목코드", "과목번호", "course_code", "code", "Code")

_DEFAULT_ALL_GRADES = set(ALL_GRADES)
_WHOLE_MARKER_PATTERN = re.compile(r"(전체학년|전학년|전체)")
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
    "제외",
    "제한",
}
_NON_BLOCKING_QUALIFIER_PATTERN = re.compile(
    r"(대상외수강제한|수강제한|타학과수강제한|순수외국인입학생|교환학생|군위탁|교직이수자|계약학과|제외|제한)"
)
_SOFT_TOKEN_PATTERN = re.compile(
    r"(융합|연계|트랙|전용강좌|수강불가|수강신청|가능|제한|제외|교환학생|군위탁|"
    r"인문대|자연대|경영대|사회대|공대|공과대|IT대|법과대|경통대|자유전공|"
    r"내국인|시간제|A그룹|B그룹|구|포함|목|부|대|문|학점교류생|국내대학|실습학교|확정된|학생만|"
    r"인문사회계열만|인문사회계열|자연과학계열|조직미지정|선취업후진학학과|장기해외봉사|현장실습|축구단|"
    r"장애학생|승인자에|한함|등)$"
)

_DEFAULT_ALIAS_MAP = {
    "기계": "기계공학부",
    "화공": "화학공학과",
    "전기": "전기공학부",
    "신소재": "신소재공학과",
    "산업정보": "산업정보시스템공학과",
    "IT융합전공": "전자정보공학부 IT융합전공",
    "IT융합": "전자정보공학부 IT융합전공",
    "전자공학전공": "전자정보공학부 전자공학전공",
    "전자공학": "전자정보공학부 전자공학전공",
    "AI융합": "AI융합학부",
    "AI소프트": "소프트웨어학부",
    "소프트": "소프트웨어학부",
    "컴퓨터": "컴퓨터학부",
    "글로벌미디어": "글로벌미디어학부",
    "디지털미디어": "미디어경영학과",
    "미디어경영": "미디어경영학과",
    "의생명": "의생명시스템학부",
    "의생명시스템": "의생명시스템학부",
    "물리": "물리학과",
    "화학": "화학과",
    "수학": "수학과",
    "통계보험": "정보통계보험수리학과",
    "경영": "경영학부",
    "벤처중소": "벤처중소기업학과",
    "경제": "경제학과",
    "국제무역": "국제무역학과",
    "글로벌통상": "글로벌통상학과",
    "금융경제": "금융경제학과",
    "금융학부": "금융학부",
    "법학": "법학과",
    "국제법무": "국제법무학과",
    "일문": "일어일문학과",
    "사회복지": "사회복지학부",
    "언론홍보": "언론홍보학과",
    "정보사회": "정보사회학과",
    "정외": "정치외교학과",
    "평생교육": "평생교육학과",
    "행정": "행정학부",
    "국문": "국어국문학과",
    "기독교": "기독교학과",
    "독문": "독어독문학과",
    "불문": "불어불문학과",
    "사학": "사학과",
    "영문": "영어영문학과",
    "일어일문": "일어일문학과",
    "중문": "중어중문학과",
    "철학": "철학과",
    "스포츠": "스포츠학부",
    "문예창작전공": "예술창작학부 문예창작전공",
    "문예창작": "예술창작학부 문예창작전공",
    "영화예술전공": "예술창작학부 영화예술전공",
    "영화예술": "예술창작학부 영화예술전공",
    "자유전공": "자유전공학부",
    "실내건축": "건축학부 실내건축전공",
    "건축공학": "건축학부 건축공학전공",
    "건축학": "건축학부 건축학전공",
    "화학공학": "화학공학과",
    "전기공학": "전기공학부",
    "전자정보공학부-IT융합": "전자정보공학부 IT융합전공",
    "전자정보공학부-전자공학": "전자정보공학부 전자공학전공",
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
    candidate_map: dict[str, set[str]] = {}
    allowed_set = set(allowed_departments)

    def add(alias: str, canonical: str) -> None:
        alias_name = alias.strip()
        canonical_name = canonical.strip()
        if not alias_name or not canonical_name:
            return
        if canonical_name not in allowed_set:
            return
        candidate_map.setdefault(alias_name, set()).add(canonical_name)

    for department in allowed_departments:
        add(department, department)

    for alias, canonical in _DEFAULT_ALIAS_MAP.items():
        add(alias, canonical)

    for alias, canonical in aliases.items():
        add(alias, canonical)

    for department in allowed_departments:
        normalized = _normalize_department_name(department)
        add(normalized, department)
        for token in normalized.split(" "):
            if len(token) >= 2:
                add(token, department)

    lookup: list[tuple[str, str]] = []
    for alias, canonicals in candidate_map.items():
        if len(canonicals) != 1:
            continue
        canonical = next(iter(canonicals))
        lookup.append((alias, canonical))

    return sorted(lookup, key=lambda item: len(item[0]), reverse=True)


def parse_target_text(
    raw_target_text: str,
    alias_lookup: list[tuple[str, str]],
    allowed_departments: list[str] | None = None,
) -> tuple[list[TargetRef], list[str]]:
    normalized = _normalize_text(raw_target_text)
    if not normalized:
        return [], ["EMPTY_TARGET_TEXT"]

    cleaned = _strip_non_blocking_qualifiers(normalized)
    departments, removed_dept_text = _extract_departments(cleaned, alias_lookup)
    grades = _extract_grade_set(normalized)
    if grades is None:
        grades = set(_DEFAULT_ALL_GRADES)

    contains_whole = bool(_WHOLE_MARKER_PATTERN.search(normalized))
    if contains_whole and not departments and allowed_departments:
        departments = list(dict.fromkeys(allowed_departments))

    excluded_departments = _extract_excluded_departments(normalized, alias_lookup)
    tentative_unparsed_tokens = _extract_unparsed_tokens(removed_dept_text)
    if (
        not departments
        and allowed_departments
        and _should_expand_to_all_departments(
            raw_text=normalized,
            grades=grades,
            contains_whole=contains_whole,
            excluded_departments=excluded_departments,
            unparsed_tokens=tentative_unparsed_tokens,
        )
    ):
        departments = [department for department in allowed_departments if department not in excluded_departments]

    if excluded_departments and departments:
        departments = [department for department in departments if department not in excluded_departments]
    if not departments and allowed_departments and excluded_departments:
        departments = [department for department in allowed_departments if department not in excluded_departments]

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


def _normalize_department_name(department_name: str) -> str:
    normalized = department_name
    normalized = normalized.replace("학부", "")
    normalized = normalized.replace("학과", "")
    normalized = normalized.replace("전공", "")
    normalized = re.sub(r"\s+", " ", normalized).strip()
    return normalized


def _normalize_text(text: str) -> str:
    normalized = text or ""
    normalized = normalized.replace("\n", " ").replace("\t", " ")
    normalized = re.sub(r"\s+", " ", normalized)
    return normalized.strip()


def _strip_non_blocking_qualifiers(text: str) -> str:
    cleaned = text
    cleaned = re.sub(r"\([^)]*(대상외수강제한|수강제한|교환학생|순수외국인입학생|군위탁|계약학과|교직이수자|제외)[^)]*\)", " ", cleaned)
    cleaned = re.sub(r"(대상외수강제한|수강제한|타학과수강제한|순수외국인입학생|교환학생|군위탁|교직이수자|계약학과)", " ", cleaned)
    cleaned = re.sub(r"\s+", " ", cleaned).strip()
    return cleaned


def _extract_excluded_departments(text: str, alias_lookup: list[tuple[str, str]]) -> set[str]:
    excluded: set[str] = set()
    for alias, canonical in alias_lookup:
        pattern = rf"{_alias_pattern(alias)}\s*제외"
        if re.search(pattern, text):
            excluded.add(canonical)
    return excluded


def _extract_departments(text: str, alias_lookup: list[tuple[str, str]]) -> tuple[list[str], str]:
    departments: list[str] = []
    removed = text
    seen_departments: set[str] = set()

    for alias, canonical in alias_lookup:
        if not alias:
            continue
        pattern = _alias_pattern(alias)
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
    has_explicit_grade_expression = False

    for start, end in re.findall(r"([0-9]{1,2})\s*[-~]\s*([0-9]{1,2})\s*학년", text):
        has_explicit_grade_expression = True
        try:
            low = int(start)
            high = int(end)
        except ValueError:
            continue
        if low > high:
            low, high = high, low
        for grade in range(low, high + 1):
            if is_valid_grade(grade):
                grades.add(grade)

    for match in re.findall(r"([0-9](?:\s*,\s*[0-9])+)\s*학년", text):
        has_explicit_grade_expression = True
        for item in re.split(r"\s*,\s*", match):
            if item.isdigit():
                grade = int(item)
                if is_valid_grade(grade):
                    grades.add(grade)

    for start in re.findall(r"([0-9]{1,2})\s*학년\s*이상", text):
        has_explicit_grade_expression = True
        if start.isdigit():
            low = int(start)
            for grade in range(low, MAX_GRADE + 1):
                if is_valid_grade(grade):
                    grades.add(grade)

    for end in re.findall(r"([0-9]{1,2})\s*학년\s*이하", text):
        has_explicit_grade_expression = True
        if end.isdigit():
            high = int(end)
            for grade in range(MIN_GRADE, high + 1):
                if is_valid_grade(grade):
                    grades.add(grade)

    for single in re.findall(r"([0-9]{1,2})\s*학년", text):
        has_explicit_grade_expression = True
        if single.isdigit():
            grade = int(single)
            if is_valid_grade(grade):
                grades.add(grade)

    if grades:
        return grades
    if has_explicit_grade_expression:
        return set()
    return None


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

    cleaned = re.sub(_NON_BLOCKING_QUALIFIER_PATTERN, " ", cleaned)
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


def _alias_pattern(alias: str) -> str:
    escaped = re.escape(alias)
    if len(alias) <= 4:
        return rf"(?<![가-힣A-Za-z0-9]){escaped}(?![가-힣A-Za-z0-9])"
    return escaped


def _should_expand_to_all_departments(
    raw_text: str,
    grades: set[int] | None,
    contains_whole: bool,
    excluded_departments: set[str],
    unparsed_tokens: list[str],
) -> bool:
    if contains_whole:
        return True
    if excluded_departments:
        return True
    if grades:
        if not unparsed_tokens:
            return True
        return all(_is_soft_unparsed_token(token) for token in unparsed_tokens)
    if not raw_text:
        return False
    if any(keyword in raw_text for keyword in ("수강제한", "수강불가", "제외", "교환학생", "군위탁", "교직이수자")):
        return True
    return False


def _is_soft_unparsed_token(token: str) -> bool:
    normalized = token.strip()
    if not normalized:
        return True
    if _SOFT_TOKEN_PATTERN.search(normalized):
        return True
    if normalized.endswith("융합학과"):
        return True
    if normalized.endswith("융합"):
        return True
    if normalized.endswith("연계"):
        return True
    return False
