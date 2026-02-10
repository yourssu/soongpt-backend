#!/usr/bin/env python3
"""
Generate SQL INSERT statements for course_secondary_major_classification table.

Source:
  - ../course/ssu26-1.csv (이수구분(다전공))
  - ../course/recognized-courses26-1/*.xlsx (이수구분(주전공) 기반 타전공 인정과목)

Output:
  - output/26-1-course-secondary-major-inserts.sql
"""

import csv
import os
import re
import sys
from typing import Dict, List, Optional, Set, Tuple

try:
    import mysql.connector
except ImportError:
    print("Error: mysql-connector-python not installed")
    print("Install with: pip install mysql-connector-python")
    sys.exit(1)

try:
    import pandas as pd
except ImportError:
    print("Error: pandas not installed")
    print("Install with: pip install pandas openpyxl")
    sys.exit(1)

try:
    from dotenv import load_dotenv
except ImportError:
    def load_dotenv(*_args, **_kwargs):  # type: ignore
        return False


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
CSV_PATH = os.path.abspath(os.path.join(BASE_DIR, "../course/ssu26-1.csv"))
RECOGNIZED_COURSES_DIR = os.path.abspath(os.path.join(BASE_DIR, "../course/recognized-courses26-1"))
OUTPUT_DIR = os.path.join(BASE_DIR, "output", "sql")
OUTPUT_SQL_PATH = os.path.join(OUTPUT_DIR, "26-1-course-secondary-major-inserts.sql")


def parse_db_config() -> Dict[str, object]:
    env_path = os.path.join(PROJECT_ROOT, ".env")
    if os.path.exists(env_path):
        load_dotenv(env_path)

    db_url = os.getenv("DB_URL", "")
    if db_url:
        match = re.match(r"jdbc:mysql://([^:]+):(\d+)/(.+)", db_url)
        if not match:
            raise ValueError(f"Invalid DB_URL format: {db_url}")
        host, port, database = match.group(1), int(match.group(2)), match.group(3)
    else:
        host = os.getenv("DB_HOST", "localhost")
        port = int(os.getenv("DB_PORT", "3306"))
        database = os.getenv("DB_NAME", "soon")

    user = os.getenv("DB_USERNAME", os.getenv("DB_USER", "root"))
    password = os.getenv("DB_PASSWORD", "")

    return {
        "host": host,
        "port": port,
        "database": database,
        "user": user,
        "password": password,
    }


DEPARTMENT_ALIAS: Dict[str, str] = {
    "AI소프트": "AI소프트웨어학부",
    "AI융합": "AI융합학부",
    "IT융합": "전자정보공학부 IT융합전공",
    "건축공학": "건축학부 건축공학전공",
    "건축학": "건축학부 건축학전공",
    "경제": "경제학과",
    "국문": "국어국문학과",
    "국제무역": "국제무역학과",
    "국제법무": "국제법무학과",
    "기계": "기계공학부",
    "기독교": "기독교학과",
    "글로벌미디어": "글로벌미디어학부",
    "글로벌통상": "글로벌통상학과",
    "금융경제": "금융경제학과",
    "디지털미디어": "미디어경영학과",
    "독문": "독어독문학과",
    "문예창작": "예술창작학부 문예창작전공",
    "미디어경영": "미디어경영학과",
    "물리": "물리학과",
    "법학": "법학과",
    "벤처중소": "벤처중소기업학과",
    "불문": "불어불문학과",
    "사학": "사학과",
    "사회복지": "사회복지학부",
    "산업·정보": "산업정보시스템공학과",
    "소프트": "소프트웨어학부",
    "수학": "수학과",
    "스포츠": "스포츠학부",
    "신소재": "신소재공학과",
    "실내건축": "건축학부 실내건축전공",
    "언론홍보": "언론홍보학과",
    "영문": "영어영문학과",
    "영화예술": "예술창작학부 영화예술전공",
    "의생명시스템": "의생명시스템학부",
    "일어일문": "일어일문학과",
    "전기": "전기공학부",
    "전자공학": "전자정보공학부 전자공학전공",
    "정보사회": "정보사회학과",
    "정외": "정치외교학과",
    "중문": "중어중문학과",
    "차세대": "차세대반도체학과",
    "철학": "철학과",
    "컴퓨터": "컴퓨터학부",
    "컴퓨터학과": "EXCLUDE_GRADUATE_DEPT",
    "통계·보험": "정보통계보험수리학과",
    "평생교육": "평생교육학과",
    "행정학부": "행정학부",
    "화공": "화학공학과",
    "화학": "화학과",
}


PREFIX_MAP: Dict[str, Tuple[str, str]] = {
    "복필": ("DOUBLE_MAJOR", "REQUIRED"),
    "복선": ("DOUBLE_MAJOR", "ELECTIVE"),
    "부필": ("MINOR", "REQUIRED"),
    "부선": ("MINOR", "ELECTIVE"),
}

MULTI_MAJOR_PATTERN = re.compile(r"^(복필|복선|부필|부선|타전공(?:인정(?:과목)?)?)\-(.+)$")
RECOGNIZED_MAJOR_PREFIXES = {"전필", "전선", "전기"}


class DepartmentMapper:
    def __init__(self, db_config: Dict[str, object]):
        self.department_name_to_id: Dict[str, int] = {}
        self._load(db_config)

    def _load(self, db_config: Dict[str, object]) -> None:
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()
        cursor.execute("SELECT id, name FROM department")
        for dept_id, dept_name in cursor:
            self.department_name_to_id[dept_name] = dept_id
        cursor.close()
        connection.close()

    def resolve_id(self, token: str) -> Optional[int]:
        normalized_name = DEPARTMENT_ALIAS.get(token, token)
        return self.department_name_to_id.get(normalized_name)

    def resolve_name(self, token: str) -> Optional[str]:
        normalized_name = DEPARTMENT_ALIAS.get(token, token)
        if normalized_name in self.department_name_to_id:
            return normalized_name
        return None


def escape_sql_string(value: str) -> str:
    escaped = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"


def parse_token(token: str) -> Optional[Tuple[str, str, str, str]]:
    match = MULTI_MAJOR_PATTERN.match(token.strip())
    if not match:
        return None

    prefix = match.group(1).strip()
    department_token = match.group(2).strip()

    if prefix.startswith("타전공"):
        return ("CROSS_MAJOR", "RECOGNIZED", "타전공인정과목", department_token)

    if prefix not in PREFIX_MAP:
        return None

    track_type, completion_type = PREFIX_MAP[prefix]
    return (track_type, completion_type, prefix, department_token)


def parse_recognized_course_classification(major_classification: str) -> Optional[Tuple[str, str]]:
    """
    Parse 이수구분(주전공) to extract prefix and department token.

    Examples:
        "전선-기독교" -> ("전선", "기독교")
        "전필-컴퓨터" -> ("전필", "컴퓨터")
        "전기-영문" -> ("전기", "영문")

    Returns:
        (prefix, department_token) or None if invalid
    """
    if not major_classification or "-" not in major_classification:
        return None

    prefix, department_token = [part.strip() for part in major_classification.split("-", 1)]
    if not prefix or not department_token:
        return None

    if prefix not in RECOGNIZED_MAJOR_PREFIXES:
        return None

    return (prefix, department_token)


def parse_course_code(raw_value: object) -> Optional[int]:
    if raw_value is None:
        return None

    if isinstance(raw_value, int):
        return raw_value

    if isinstance(raw_value, float):
        if pd.isna(raw_value):
            return None
        if raw_value.is_integer():
            return int(raw_value)
        return None

    code_str = str(raw_value).strip()
    if not code_str or code_str.lower() == "nan":
        return None

    if re.fullmatch(r"\d+\.0+", code_str):
        code_str = code_str.split(".", 1)[0]

    if code_str.isdigit():
        return int(code_str)

    return None


def generate_insert_sql(
    course_code: int,
    track_type: str,
    completion_type: str,
    department_id: int,
    raw_classification: str,
    raw_department_token: str,
) -> str:
    return (
        "INSERT INTO course_secondary_major_classification "
        "(course_code, track_type, completion_type, department_id, raw_classification, raw_department_token) VALUES ("
        f"{course_code}, "
        f"{escape_sql_string(track_type)}, "
        f"{escape_sql_string(completion_type)}, "
        f"{department_id}, "
        f"{escape_sql_string(raw_classification)}, "
        f"{escape_sql_string(raw_department_token)}"
        ");"
    )


def main() -> None:
    db_config = parse_db_config()
    mapper = DepartmentMapper(db_config)

    inserts: List[str] = []
    dedup: Set[Tuple[int, str, str, int]] = set()
    unmapped_department_tokens: Set[str] = set()
    ignored_multi_major_tokens: Set[str] = set()
    ignored_recognized_classifications: Set[str] = set()
    parsed_multi_major_token_count = 0
    parsed_recognized_row_count = 0
    recognized_rows_total = 0
    recognized_files_loaded = 0
    recognized_file_errors: List[Tuple[str, str]] = []

    valid_course_codes: Set[int] = set()

    with open(CSV_PATH, "r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        for row in reader:
            code_str = (row.get("과목번호") or row.get("과목코드") or "").strip()
            if not code_str.isdigit():
                continue
            
            course_code = int(code_str)
            valid_course_codes.add(course_code)

            multi_major_raw = (row.get("이수구분(다전공)") or "").strip()
            if not multi_major_raw:
                continue

            # course_code is already parsed above
            tokens = [token.strip() for token in multi_major_raw.split("/") if token.strip()]

            for token in tokens:
                parsed = parse_token(token)
                if parsed is None:
                    ignored_multi_major_tokens.add(token)
                    continue

                parsed_multi_major_token_count += 1
                track_type, completion_type, raw_classification, raw_department_token = parsed

                department_id = mapper.resolve_id(raw_department_token)
                if department_id is None:
                    unmapped_department_tokens.add(raw_department_token)
                    continue

                dedup_key = (course_code, track_type, completion_type, department_id)
                if dedup_key in dedup:
                    continue
                dedup.add(dedup_key)

                inserts.append(
                    generate_insert_sql(
                        course_code=course_code,
                        track_type=track_type,
                        completion_type=completion_type,
                        department_id=department_id,
                        raw_classification=raw_classification,
                        raw_department_token=raw_department_token,
                    )
                )

    if os.path.isdir(RECOGNIZED_COURSES_DIR):
        recognized_files = [
            os.path.join(RECOGNIZED_COURSES_DIR, name)
            for name in sorted(os.listdir(RECOGNIZED_COURSES_DIR))
            if not name.startswith("~$")
            and os.path.isfile(os.path.join(RECOGNIZED_COURSES_DIR, name))
            and name.lower().endswith((".xlsx", ".xls"))
        ]

        for recognized_file in recognized_files:
            try:
                dataframe = pd.read_excel(recognized_file, engine="openpyxl")
                recognized_files_loaded += 1
            except Exception as error:  # noqa: BLE001
                recognized_file_errors.append((os.path.basename(recognized_file), str(error)))
                continue

            for _, row in dataframe.iterrows():
                recognized_rows_total += 1
                course_code = parse_course_code(row.get("과목번호", row.get("과목코드")))
                major_classification = str(row.get("이수구분(주전공)") or "").strip()
                if course_code is None or not major_classification:
                    continue

                parsed = parse_recognized_course_classification(major_classification)
                if parsed is None:
                    ignored_recognized_classifications.add(major_classification)
                    continue

                if course_code not in valid_course_codes:
                    # Skip courses that don't exist in the master CSV (prevents FK errors)
                    continue

                parsed_recognized_row_count += 1
                _, raw_department_token = parsed
                track_type = "CROSS_MAJOR"
                completion_type = "RECOGNIZED"
                raw_classification = major_classification

                department_id = mapper.resolve_id(raw_department_token)
                if department_id is None:
                    unmapped_department_tokens.add(raw_department_token)
                    continue

                dedup_key = (course_code, track_type, completion_type, department_id)
                if dedup_key in dedup:
                    continue
                dedup.add(dedup_key)

                inserts.append(
                    generate_insert_sql(
                        course_code=course_code,
                        track_type=track_type,
                        completion_type=completion_type,
                        department_id=department_id,
                        raw_classification=raw_classification,
                        raw_department_token=raw_department_token,
                    )
                )

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as file:
        file.write("-- Generated SQL INSERT statements for course_secondary_major_classification\n")
        file.write("-- Source 1: ssu26-1.csv (이수구분(다전공))\n")
        file.write("-- Source 2: recognized-courses26-1/*.xlsx (이수구분(주전공) 기반 타전공인정)\n")
        file.write(f"-- Total inserts: {len(inserts)}\n\n")
        for sql in inserts:
            file.write(sql + "\n")

    print("=== Secondary Major Insert Generator ===")
    print(f"Parsed multi-major tokens (CSV): {parsed_multi_major_token_count}")
    print(f"Loaded recognized files (XLSX): {recognized_files_loaded}")
    print(f"Recognized rows scanned (XLSX): {recognized_rows_total}")
    print(f"Parsed recognized rows (XLSX): {parsed_recognized_row_count}")
    print(f"Generated inserts: {len(inserts)}")
    print(f"Ignored multi-major tokens: {len(ignored_multi_major_tokens)}")
    print(f"Ignored recognized classifications: {len(ignored_recognized_classifications)}")
    print(f"Unmapped department tokens: {len(unmapped_department_tokens)}")
    if ignored_multi_major_tokens:
        print("\nIgnored multi-major tokens (first 20):")
        for token in sorted(ignored_multi_major_tokens)[:20]:
            print(f"  - {token}")
    if ignored_recognized_classifications:
        print("\nIgnored recognized classifications (first 20):")
        for token in sorted(ignored_recognized_classifications)[:20]:
            print(f"  - {token}")
    if unmapped_department_tokens:
        print("\nUnmapped department tokens:")
        for token in sorted(unmapped_department_tokens):
            print(f"  - {token}")
    if recognized_file_errors:
        print("\nRecognized file read errors:")
        for file_name, error in recognized_file_errors:
            print(f"  - {file_name}: {error}")
    print(f"\nOutput: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()
