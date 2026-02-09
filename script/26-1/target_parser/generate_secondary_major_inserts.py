#!/usr/bin/env python3
"""
Generate SQL INSERT statements for course_secondary_major_classification table.

Source:
  - ../course/ssu26-1.csv (이수구분(다전공))

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
    from dotenv import load_dotenv
except ImportError:
    def load_dotenv(*_args, **_kwargs):  # type: ignore
        return False


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
CSV_PATH = os.path.abspath(os.path.join(BASE_DIR, "../course/ssu26-1.csv"))
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
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
    ignored_tokens: Set[str] = set()
    parsed_token_count = 0

    with open(CSV_PATH, "r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        for row in reader:
            code_str = (row.get("과목번호") or "").strip()
            multi_major_raw = (row.get("이수구분(다전공)") or "").strip()
            if not code_str.isdigit() or not multi_major_raw:
                continue

            course_code = int(code_str)
            tokens = [token.strip() for token in multi_major_raw.split("/") if token.strip()]

            for token in tokens:
                parsed = parse_token(token)
                if parsed is None:
                    ignored_tokens.add(token)
                    continue

                parsed_token_count += 1
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

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as file:
        file.write("-- Generated SQL INSERT statements for course_secondary_major_classification\n")
        file.write("-- Source: ssu26-1.csv (이수구분(다전공))\n")
        file.write(f"-- Total inserts: {len(inserts)}\n\n")
        for sql in inserts:
            file.write(sql + "\n")

    print("=== Secondary Major Insert Generator ===")
    print(f"Parsed tokens: {parsed_token_count}")
    print(f"Generated inserts: {len(inserts)}")
    print(f"Ignored tokens: {len(ignored_tokens)}")
    print(f"Unmapped department tokens: {len(unmapped_department_tokens)}")
    if ignored_tokens:
        print("\nIgnored tokens (first 20):")
        for token in sorted(ignored_tokens)[:20]:
            print(f"  - {token}")
    if unmapped_department_tokens:
        print("\nUnmapped department tokens:")
        for token in sorted(unmapped_department_tokens):
            print(f"  - {token}")
    print(f"\nOutput: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()
