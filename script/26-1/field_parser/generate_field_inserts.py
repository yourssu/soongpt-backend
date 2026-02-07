#!/usr/bin/env python3
"""
Generate SQL INSERT statements for course_field table from Excel data.

Usage:
    python generate_field_inserts.py

Output:
    output/26-1-course-field-inserts.sql
"""

import pandas as pd
import os
import sys

# Configuration
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CSV_PATH = os.path.join(BASE_DIR, "general_elective", "merged_courses.csv")
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
OUTPUT_SQL_PATH = os.path.join(OUTPUT_DIR, "26-1-course-field-inserts.sql")


def escape_sql_string(s: str) -> str:
    """Escape single quotes and special characters in SQL strings."""
    if s is None or pd.isna(s):
        return "NULL"
    s = str(s)
    # Escape backslash first, then single quotes, then newlines
    escaped = s.replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
    return "'" + escaped + "'"


def clean_string(s: str) -> str:
    """Clean string by removing extra whitespace."""
    if not s or pd.isna(s):
        return ""
    return ' '.join(str(s).split())


class CourseFieldInsertGenerator:
    """Generates SQL INSERT statements for course_field table."""

    def __init__(self):
        self.inserts = []
        self.skipped = []
        self.duplicate_codes = set()
        self.seen_codes = set()

    def generate_insert(self, row: pd.Series) -> str:
        """Generate INSERT statement for a single course field."""

        # Extract fields
        code = row.get('과목번호')
        name = clean_string(row.get('과목명', ''))
        field = row.get('교과영역', '')  # Don't clean field - preserve newlines

        # Validate required fields
        if pd.isna(code) or not str(code).isdigit():
            self.skipped.append(f"Invalid code: {code}")
            return None

        code = int(code)

        # Check for duplicates
        if code in self.seen_codes:
            self.duplicate_codes.add(code)
            return None
        self.seen_codes.add(code)

        if not name:
            self.skipped.append(f"Missing name for code {code}")
            return None

        if not field:
            self.skipped.append(f"Missing field for code {code}")
            return None

        # Build SQL
        sql = "INSERT INTO course_field ("
        sql += "course_code, course_name, field"
        sql += ") VALUES ("

        # Values
        sql += f"{code}, "  # course_code
        sql += f"{escape_sql_string(name)}, "  # course_name
        sql += f"{escape_sql_string(field)}"  # field
        sql += ");"

        return sql

    def process_csv(self, csv_path: str):
        """Process CSV file and generate all INSERT statements."""
        df = pd.read_csv(csv_path)

        print(f"Total rows in CSV: {len(df)}")
        print(f"Columns: {df.columns.tolist()}")

        for _, row in df.iterrows():
            insert = self.generate_insert(row)
            if insert:
                self.inserts.append(insert)

        print(f"\n=== CSV Processing Summary ===")
        print(f"Total course fields: {len(self.inserts)}")
        print(f"Skipped: {len(self.skipped)}")
        print(f"Duplicate codes: {len(self.duplicate_codes)}")

        if self.skipped and len(self.skipped) <= 10:
            print(f"\nSkipped reasons:")
            for reason in self.skipped[:10]:
                print(f"  - {reason}")

        if self.duplicate_codes:
            print(f"\nDuplicate course codes ({len(self.duplicate_codes)}):")
            for code in sorted(self.duplicate_codes)[:10]:
                print(f"  - {code}")


def main():
    print("=== Course Field SQL Generator ===\n")

    # Check if CSV file exists
    if not os.path.exists(CSV_PATH):
        print(f"Error: CSV file not found at {CSV_PATH}")
        sys.exit(1)

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Initialize generator
    generator = CourseFieldInsertGenerator()

    # Process CSV
    print(f"Processing CSV from {CSV_PATH}...")
    generator.process_csv(CSV_PATH)

    # Write SQL file
    print(f"\nWriting SQL to {OUTPUT_SQL_PATH}...")
    with open(OUTPUT_SQL_PATH, 'w', encoding='utf-8') as f:
        f.write("-- Generated SQL INSERT statements for course_field table\n")
        f.write("-- Generated from: merged_courses.csv\n")
        f.write(f"-- Total inserts: {len(generator.inserts)}\n\n")

        for insert in generator.inserts:
            f.write(insert + "\n")

    print(f"\n✅ Successfully generated {len(generator.inserts)} INSERT statements")
    print(f"   Output: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()
