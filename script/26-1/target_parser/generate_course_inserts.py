#!/usr/bin/env python3
"""
Generate SQL INSERT statements for course table from CSV data.

Usage:
    python generate_course_inserts.py

Output:
    output/26-1-course-inserts.sql
"""

import csv
import re
import os
import sys

# Configuration
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CSV_PATH = os.path.abspath(os.path.join(BASE_DIR, "../course/ssu26-1.csv"))
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
OUTPUT_SQL_PATH = os.path.join(OUTPUT_DIR, "26-1-course-inserts.sql")

# Category mapping (based on Category.kt)
CATEGORY_MAP = {
    "전필": "MAJOR_REQUIRED",
    "전선": "MAJOR_ELECTIVE",
    "전기": "MAJOR_BASIC",
    "교필": "GENERAL_REQUIRED",
    "교선": "GENERAL_ELECTIVE",
    "채플": "CHAPEL",
}


def parse_category(major_classification: str) -> str:
    """
    Parse category from "이수구분(주전공)" field.

    Examples:
        "전공_IT경영" -> MAJOR_REQUIRED
        "전선_목회상담" -> MAJOR_ELECTIVE
        "교필" -> GENERAL_REQUIRED
        "전기-AI융합" -> MAJOR_REQUIRED
    """
    if not major_classification:
        return "OTHER"

    # Normalize
    normalized = major_classification.strip()

    # Check for exact matches first
    for key, value in CATEGORY_MAP.items():
        if normalized == key:
            return value

    # Check for patterns (prefix before _ or -)
    # Split by both _ and -
    import re
    parts = re.split(r'[_-]', normalized)
    if not parts:
        return "OTHER"

    prefix = parts[0]

    # Check if prefix matches any category
    for key, value in CATEGORY_MAP.items():
        if prefix == key:
            return value

    # Special cases
    if prefix == "전공":  # 전공_XXX -> MAJOR_ELECTIVE (전공선택)
        return "MAJOR_ELECTIVE"

    if prefix == "공기":  # 공기_XXX (석사 공통기초) -> OTHER
        return "OTHER"

    if "교직" in normalized:  # 교직 -> OTHER (or add TEACHING if needed)
        return "OTHER"

    return "OTHER"


def parse_time_point(time_point_str: str):
    """
    Parse "시간/학점(설계)" field to extract point and credit.

    Examples:
        "3.0/3.0" -> point="3.0", credit=3.0
        "4.0/3.0" -> point="4.0", credit=3.0

    Returns:
        tuple: (point_str, credit_float)
    """
    if not time_point_str:
        return time_point_str, None

    # Extract point (first number) and credit (second number after /)
    match = re.search(r'(\d+\.?\d*)/(\d+\.?\d*)', time_point_str)
    if match:
        point = match.group(1)  # First number (앞)
        credit = float(match.group(2))  # Second number (뒤)
        return point, credit

    # If it's a single number like "4.0", treat it as point, and credit is None (or same?)
    # Generally, time/point means Lecture Time (point) / Credit.
    # If only one is present, it's ambiguous.
    # However, safe default is point only.
    
    return time_point_str, None


def escape_sql_string(s: str) -> str:
    """Escape single quotes and special characters in SQL strings."""
    if s is None:
        return "NULL"
    # Escape backslash first, then single quotes, then newlines
    escaped = s.replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
    return "'" + escaped + "'"


def clean_string(s: str) -> str:
    """Clean string by removing extra whitespace."""
    if not s:
        return ""
    return ' '.join(s.split())


def parse_personeel(personeel_str: str) -> int:
    """Parse personeel (수강인원) field."""
    if not personeel_str:
        return 0

    # Remove commas and convert to int
    try:
        return int(personeel_str.replace(',', ''))
    except ValueError:
        return 0

def truncate_string(s: str, max_length: int) -> str:
    """Truncate string to max_length."""
    if not s:
        return s
    
    if len(s) > max_length:
        return s[:max_length]
    return s

class CourseInsertGenerator:
    """Generates SQL INSERT statements for course table."""

    def __init__(self):
        self.inserts = []
        self.skipped = []
        self.duplicate_codes = set()
        self.seen_codes = set()

    def generate_insert(self, row: dict) -> str:
        """Generate INSERT statement for a single course."""

        # Extract fields
        code_str = row.get('과목번호', '').strip()
        name = clean_string(row.get('과목명', ''))
        professor = clean_string(row.get('교수명', '')) or None
        department = clean_string(row.get('개설학과', ''))
        time_point = row.get('시간/학점(설계)', '').strip()
        personeel_str = row.get('수강인원', '0').strip()
        schedule_room = row.get('강의시간(강의실)', '').strip()
        target = row.get('수강대상', '').strip()
        major_classification = row.get('이수구분(주전공)', '').strip()
        multi_major_classification = row.get('이수구분(다전공)', '').strip() or None
        field = clean_string(row.get('교과영역', '')) or None
        division = clean_string(row.get('분반', '')) or None

        # Validate required fields
        if not code_str or not code_str.isdigit():
            self.skipped.append(f"Invalid code: {code_str}")
            return None

        code = int(code_str)

        # Check for duplicates
        if code in self.seen_codes:
            self.duplicate_codes.add(code)
            return None
        self.seen_codes.add(code)

        if not name:
            self.skipped.append(f"Missing name for code {code}")
            return None

        if not department:
            self.skipped.append(f"Missing department for code {code}")
            return None

        # Parse category
        category = parse_category(major_classification)

        # Parse point and credit
        point, credit = parse_time_point(time_point)
        
        # Validate point is a number (if present)
        if point:
             try:
                 float(point)
             except ValueError:
                 self.skipped.append(f"Invalid point value: {point} for code {code}")
                 return None

        # Parse personeel
        personeel = parse_personeel(personeel_str)

        # Truncate fields to safe lengths (based on assumed DB schema limits)
        name = truncate_string(name, 100)
        professor = truncate_string(professor, 50)
        department = truncate_string(department, 50)
        division = truncate_string(division, 20)
        major_classification = truncate_string(major_classification, 255) # SubCategory can be long
        multi_major_classification = truncate_string(multi_major_classification, 2048)
        field = truncate_string(field, 50)
        target = truncate_string(target, 2048) # Target can be long
        schedule_room = truncate_string(schedule_room, 255)


        # Build SQL
        sql = "INSERT INTO course ("
        sql += "category, sub_category, multi_major_category, field, code, name, professor, department, division, "
        sql += "time, point, personeel, schedule_room, target, credit"
        sql += ") VALUES ("

        # Values
        sql += f"'{category}', "  # category (ENUM as STRING)
        sql += f"{escape_sql_string(major_classification)}, "  # sub_category
        sql += f"{escape_sql_string(multi_major_classification) if multi_major_classification else 'NULL'}, "  # multi_major_category
        field_value = escape_sql_string(field) if field else "''"
        sql += f"{field_value}, "  # field (empty string if null)
        sql += f"{code}, "  # code
        sql += f"{escape_sql_string(name)}, "  # name
        sql += f"{escape_sql_string(professor) if professor else 'NULL'}, "  # professor
        sql += f"{escape_sql_string(department)}, "  # department
        sql += f"{escape_sql_string(division) if division else 'NULL'}, "  # division
        sql += f"{escape_sql_string(point)}, "  # time (앞부분)
        credit_str = str(credit) if credit is not None else point
        sql += f"'{credit_str}', "  # point (학점)
        sql += f"{personeel}, "  # personeel
        sql += f"{escape_sql_string(schedule_room)}, "  # schedule_room
        sql += f"{escape_sql_string(target)}, "  # target
        sql += f"{credit if credit is not None else 'NULL'}"  # credit
        sql += ");"

        return sql

    def process_csv(self, csv_path: str):
        """Process CSV file and generate all INSERT statements."""
        with open(csv_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)

            for row in reader:
                insert = self.generate_insert(row)
                if insert:
                    self.inserts.append(insert)

        print(f"\n=== CSV Processing Summary ===")
        print(f"Total courses: {len(self.inserts)}")
        print(f"Skipped: {len(self.skipped)}")
        print(f"Duplicate codes: {len(self.duplicate_codes)}")

        if self.skipped and len(self.skipped) <= 20:
            print(f"\nSkipped reasons:")
            for reason in self.skipped[:20]:
                print(f"  - {reason}")
        elif len(self.skipped) > 20:    
            print(f"\nSkipped reasons (first 20):")
            for reason in self.skipped[:20]:
                print(f"  - {reason}")


        if self.duplicate_codes:
            print(f"\nDuplicate course codes ({len(self.duplicate_codes)}):")
            for code in sorted(self.duplicate_codes)[:10]:
                print(f"  - {code}")


def main():
    print("=== Course SQL Generator ===\n")

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Initialize generator
    generator = CourseInsertGenerator()

    # Process CSV
    print(f"Processing CSV from {CSV_PATH}...")
    generator.process_csv(CSV_PATH)

    # Write SQL file
    print(f"\nWriting SQL to {OUTPUT_SQL_PATH}...")
    with open(OUTPUT_SQL_PATH, 'w', encoding='utf-8') as f:
        f.write("-- Generated SQL INSERT statements for course table\n")
        f.write("-- Generated from: ssu26-1.csv\n")
        f.write(f"-- Total inserts: {len(generator.inserts)}\n\n")

        for insert in generator.inserts:
            f.write(insert + "\n")

    print(f"\n✅ Successfully generated {len(generator.inserts)} INSERT statements")
    print(f"   Output: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()
