#!/usr/bin/env python3
"""
Generate SQL INSERT statements for target table from parsed_unique_targets.json and CSV data.

Usage:
    python generate_target_inserts.py

Prerequisites:
    - MySQL database with college and department tables populated
    - Set DB credentials in .env file or environment variables

Output:
    output/26-1-target-inserts.sql
"""

import json
import csv
import yaml
import os
import sys
from typing import Dict, List, Optional, Tuple

try:
    import mysql.connector
    from mysql.connector import Error
except ImportError:
    print("Error: mysql-connector-python not installed")
    print("Install with: pip install mysql-connector-python")
    sys.exit(1)

try:
    from dotenv import load_dotenv
except ImportError:
    print("Warning: python-dotenv not installed, using environment variables only")
    print("Install with: pip install python-dotenv")
    load_dotenv = lambda: None  # no-op

# Configuration
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
DATA_YML_PATH = os.path.join(BASE_DIR, "data.yml")
CSV_PATH = os.path.join(BASE_DIR, "ssu26-1.csv")
PARSED_TARGETS_PATH = os.path.join(BASE_DIR, "parsed_unique_targets.json")
OUTPUT_DIR = os.path.join(BASE_DIR, "output")
OUTPUT_SQL_PATH = os.path.join(OUTPUT_DIR, "26-1-target-inserts.sql")

# Load .env file from project root
ENV_PATH = os.path.join(PROJECT_ROOT, ".env")
if os.path.exists(ENV_PATH):
    load_dotenv(ENV_PATH)
    print(f"Loaded environment from {ENV_PATH}\n")
else:
    print(f"Warning: .env file not found at {ENV_PATH}")
    print("Using environment variables or defaults\n")

# Parse DB_URL or use individual variables
db_url = os.getenv('DB_URL', '')
if db_url:
    # Parse JDBC URL: jdbc:mysql://host:port/database
    import re
    match = re.match(r'jdbc:mysql://([^:]+):(\d+)/(.+)', db_url)
    if match:
        db_host = match.group(1)
        db_port = int(match.group(2))
        db_name = match.group(3)
    else:
        print(f"Error: Invalid DB_URL format: {db_url}")
        sys.exit(1)
else:
    db_host = os.getenv('DB_HOST', 'localhost')
    db_port = int(os.getenv('DB_PORT', '3306'))
    db_name = os.getenv('DB_NAME', 'soon')

db_user = os.getenv('DB_USERNAME', os.getenv('DB_USER', 'root'))
db_password = os.getenv('DB_PASSWORD', '')

# DB Configuration
DB_CONFIG = {
    'host': db_host,
    'database': db_name,
    'user': db_user,
    'password': db_password,
    'port': db_port
}

print(f"Database configuration:")
print(f"  Host: {db_host}:{db_port}")
print(f"  Database: {db_name}")
print(f"  User: {db_user}")
print()

# ENUM mappings (matching Kotlin enums)
SCOPE_TYPE_MAP = {
    "university": 0,  # UNIVERSITY
    "college": 1,     # COLLEGE
    "department": 2   # DEPARTMENT
}

STUDENT_TYPE_MAP = {
    "general": 0,      # GENERAL
    "foreigner": 1,    # FOREIGNER
    "military": 2,     # MILITARY
    "teaching_cert": 3 # TEACHING_CERT
}


class CollegeDepartmentMapper:
    """Maps college/department names to their IDs by querying the database."""

    def __init__(self, db_config: dict):
        self.colleges: Dict[str, int] = {}
        self.departments: Dict[str, int] = {}
        self.dept_to_college: Dict[int, int] = {}

        # Connect to database and fetch mappings
        try:
            connection = mysql.connector.connect(**db_config)
            cursor = connection.cursor()

            # Fetch colleges
            cursor.execute("SELECT id, name FROM college")
            for (college_id, college_name) in cursor:
                self.colleges[college_name] = college_id

            # Fetch departments
            cursor.execute("SELECT id, name, college_id FROM department")
            for (dept_id, dept_name, college_id) in cursor:
                self.departments[dept_name] = dept_id
                self.dept_to_college[dept_id] = college_id

            cursor.close()
            connection.close()

            print(f"  Loaded {len(self.colleges)} colleges from DB")
            print(f"  Loaded {len(self.departments)} departments from DB")

        except Error as e:
            print(f"Error connecting to database: {e}")
            sys.exit(1)

    def get_college_id(self, college_name: str) -> Optional[int]:
        return self.colleges.get(college_name)

    def get_department_id(self, dept_name: str) -> Optional[int]:
        return self.departments.get(dept_name)

    def get_college_id_by_department(self, dept_id: int) -> Optional[int]:
        return self.dept_to_college.get(dept_id)


class TargetSQLGenerator:
    """Generates SQL INSERT statements for target table."""

    def __init__(self, mapper: CollegeDepartmentMapper):
        self.mapper = mapper
        self.parsed_targets: Dict[str, dict] = {}
        self.missing_departments = set()
        self.missing_colleges = set()

    def load_parsed_targets(self, path: str):
        """Load parsed_unique_targets.json into a lookup dict."""
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        for entry in data:
            original_text = entry['original_text']
            self.parsed_targets[original_text] = entry

    def parse_resource(self, resource: str) -> Tuple[int, Optional[int], Optional[int]]:
        """
        Parse Resource field to extract scope_type, college_id, department_id.

        Returns:
            (scope_type, college_id, department_id)
        """
        if resource == "university":
            return (SCOPE_TYPE_MAP["university"], None, None)

        if resource.startswith("college/"):
            college_name = resource.split("/", 1)[1]
            college_id = self.mapper.get_college_id(college_name)
            if college_id is None:
                self.missing_colleges.add(college_name)
                return None
            return (SCOPE_TYPE_MAP["college"], college_id, None)

        if resource.startswith("department/"):
            dept_name = resource.split("/", 1)[1]
            dept_id = self.mapper.get_department_id(dept_name)
            if dept_id is None:
                self.missing_departments.add(dept_name)
                return None
            college_id = self.mapper.get_college_id_by_department(dept_id)
            return (SCOPE_TYPE_MAP["department"], None, dept_id)

        raise ValueError(f"Unknown resource format: {resource}")

    def parse_grades(self, grade_condition: dict) -> Tuple[bool, bool, bool, bool, bool]:
        """
        Convert Grade condition (min/max) to grade1~grade5 booleans.

        Returns:
            (grade1, grade2, grade3, grade4, grade5)
        """
        min_grade = grade_condition.get('min', 1)
        max_grade = grade_condition.get('max', 5)

        return (
            min_grade <= 1 <= max_grade,
            min_grade <= 2 <= max_grade,
            min_grade <= 3 <= max_grade,
            min_grade <= 4 <= max_grade,
            min_grade <= 5 <= max_grade,
        )

    def generate_insert(self, course_code: int, parsed_target: dict, category: str = "", offering_dept: str = "") -> List[str]:
        """
        Generate INSERT statements for a single parsed_target.
        
        Args:
            course_code: Course ID
            parsed_target: Parsed target rule dict
            category: Course category (e.g. "전선", "교필") for scope override logic
            offering_dept: Offering department name for scope override logic
        """
        inserts = []

        effect = parsed_target.get('Effect', 'Allow')
        is_denied = 'true' if effect == 'Deny' else 'false'

        resource = parsed_target['Resource']
        scope_result = self.parse_resource(resource)
        if scope_result is None:
            return []  # Skip if college/department not found

        scope_type, college_id, department_id = scope_result

        # === Scope Override Logic ===
        # If Target is "University" (All) AND Course is Major (전필/전선/전기/전공)
        # -> Change Scope to DEPARTMENT (Offering Dept)
        if scope_type == SCOPE_TYPE_MAP["university"]:
            # Check for major keywords: 전필, 전선, 전기, 전공 (covers '전공_...')
            is_major = any(major_type in category for major_type in ["전필", "전선", "전기", "전공"])
            if is_major and offering_dept:
                # Override to Department Scope
                
                # Manual Mapping for specific majors -> Dept/School if needed
                DEPT_MAPPING = {
                    # Common Aliases
                    "전자공학과": "전자정보공학부 전자공학전공", 
                    "문예창작학과": "예술창작학부 문예창작전공",
                    "영화예술전공": "예술창작학부 영화예술전공",
                    "행정학과": "행정학부",
                    "사회복지학과": "사회복지학부",
                    "건축학과": "건축학부 건축학전공",
                    "건축학부": "건축학부 건축학부", 
                    "실내건축전공": "건축학부 실내건축전공",
                    "산업·정보시스템공학과": "산업정보시스템공학과", 
                    "기계공학과": "기계공학부",
                    "스포츠학부": "스포츠학부",
                    "법학과": "법학과",
                    # Specific Majors
                    "상담복지전공": "사회복지학부",
                    "사회복지실천전공": "사회복지학부",
                    "사회복지전공": "사회복지학부",
                    "NGO·기업사회공헌전공": "사회복지학부",
                    "첨단융합안전공학과(계약학과)": "안전융합대학원", 
                    "산업정보시스템공학과": "산업·정보시스템공학과"
                }
                
                target_dept_name = DEPT_MAPPING.get(offering_dept, offering_dept)
                dept_id = self.mapper.get_department_id(target_dept_name)
                
                if dept_id:
                    scope_type = SCOPE_TYPE_MAP["department"]
                    department_id = dept_id
                    college_id = None # Ensure college_id is NULL for dept scope
                else:
                    print(f"  [Warning] Course {course_code} ({category}): Scope Override Failed. Could not find ID for offering dept '{offering_dept}' (Mapped: '{target_dept_name}'). Skipping target generation.")
                    return [] # SKIP generating this target

        condition = parsed_target.get('Condition', {})
        grade_condition = condition.get('Grade', {'min': 1, 'max': 5})
        g1, g2, g3, g4, g5 = self.parse_grades(grade_condition)

        student_types = condition.get('StudentType', ['general'])
        is_strict = 'true' if parsed_target.get('Strict', False) else 'false'

        # Generate one INSERT per student type
        for student_type_str in student_types:
            student_type = STUDENT_TYPE_MAP.get(student_type_str, 0)

            # Build SQL (Note: DB columns are grade1, grade2, etc. without underscore)
            sql = f"INSERT INTO target (course_code, scope_type, college_id, department_id, grade1, grade2, grade3, grade4, grade5, is_denied, is_strict, student_type) VALUES ("
            sql += f"{course_code}, "
            sql += f"{scope_type}, "
            sql += f"{college_id if college_id is not None else 'NULL'}, "
            sql += f"{department_id if department_id is not None else 'NULL'}, "
            sql += f"{1 if g1 else 0}, "  # Convert boolean to bit (1/0)
            sql += f"{1 if g2 else 0}, "
            sql += f"{1 if g3 else 0}, "
            sql += f"{1 if g4 else 0}, "
            sql += f"{1 if g5 else 0}, "
            sql += f"{1 if is_denied == 'true' else 0}, "  # Convert string to bit
            sql += f"{1 if is_strict == 'true' else 0}, "   # Convert string to bit
            sql += f"{student_type}"
            sql += ");"

            inserts.append(sql)

        return inserts

    def process_csv(self, csv_path: str) -> List[str]:
        """
        Process CSV file and generate all INSERT statements.
        """
        all_inserts = []
        matched_count = 0
        unmatched_count = 0
        unmatched_targets = set()

        with open(csv_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)

            for row in reader:
                course_code_str = row.get('과목번호', '').strip()
                target_text = row.get('수강대상', '').strip()
                category = row.get('이수구분(주전공)', '').strip()
                offering_dept = row.get('개설학과', '').strip()

                # Skip empty or invalid course codes
                if not course_code_str or not course_code_str.isdigit():
                    continue

                course_code = int(course_code_str)

                # Skip if no target text
                if not target_text:
                    continue

                # Lookup in parsed_targets
                if target_text not in self.parsed_targets:
                    unmatched_count += 1
                    unmatched_targets.add(target_text)
                    continue

                matched_count += 1
                parsed_entry = self.parsed_targets[target_text]

                # Generate INSERT for each parsed_target
                for parsed_target in parsed_entry['parsed_targets']:
                    inserts = self.generate_insert(course_code, parsed_target, category, offering_dept)
                    all_inserts.extend(inserts)

        print(f"\n=== CSV Processing Summary ===")
        print(f"Matched targets: {matched_count}")
        print(f"Unmatched targets: {unmatched_count}")

        if unmatched_targets:
            print(f"\nFirst 10 unmatched targets:")
            for i, target in enumerate(sorted(unmatched_targets)[:10]):
                print(f"  - {target}")

        if self.missing_departments:
            print(f"\nMissing departments ({len(self.missing_departments)}):")
            for dept in sorted(self.missing_departments)[:10]:
                print(f"  - {dept}")

        if self.missing_colleges:
            print(f"\nMissing colleges ({len(self.missing_colleges)}):")
            for college in sorted(self.missing_colleges):
                print(f"  - {college}")

        return all_inserts


def main():
    print("=== Target SQL Generator ===\n")

    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Initialize mapper from database
    print(f"Fetching college/department IDs from database...")
    mapper = CollegeDepartmentMapper(DB_CONFIG)

    # Initialize generator
    generator = TargetSQLGenerator(mapper)

    # Load parsed targets
    print(f"\nLoading parsed targets from {PARSED_TARGETS_PATH}...")
    generator.load_parsed_targets(PARSED_TARGETS_PATH)
    print(f"  Loaded {len(generator.parsed_targets)} unique target patterns")

    # Process CSV
    print(f"\nProcessing CSV from {CSV_PATH}...")
    inserts = generator.process_csv(CSV_PATH)

    # Write SQL file
    print(f"\nWriting SQL to {OUTPUT_SQL_PATH}...")
    with open(OUTPUT_SQL_PATH, 'w', encoding='utf-8') as f:
        f.write("-- Generated SQL INSERT statements for target table\n")
        f.write("-- Generated from: ssu26-1.csv + parsed_unique_targets.json\n")
        f.write(f"-- Total inserts: {len(inserts)}\n\n")

        for insert in inserts:
            f.write(insert + "\n")

    print(f"\n✅ Successfully generated {len(inserts)} INSERT statements")
    print(f"   Output: {OUTPUT_SQL_PATH}")


if __name__ == "__main__":
    main()