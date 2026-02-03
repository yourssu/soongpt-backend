#!/usr/bin/env python3
"""
Query Course Tool

Usage:
    python query_courses.py --code <course_code>
    python query_courses.py --dept <department_name> --grade <grade> --category <category>

Example:
    python query_courses.py --code 2150175404
    python query_courses.py --dept "컴퓨터학부" --grade 1 --category "전필"
"""

import mysql.connector
import os
import sys
import argparse
from dotenv import load_dotenv
import re
from typing import Dict, List, Optional, Tuple

# Load environment
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
ENV_PATH = os.path.join(PROJECT_ROOT, ".env")

if os.path.exists(ENV_PATH):
    load_dotenv(ENV_PATH)

# DB Configuration
db_url = os.getenv('DB_URL', '')
if db_url:
    match = re.match(r'jdbc:mysql://([^:]+):(\d+)/(.+)', db_url)
    if match:
        db_host = match.group(1)
        db_port = int(match.group(2))
        db_name = match.group(3)
    else:
        print(f"Error: Invalid DB_URL format")
        sys.exit(1)
else:
    db_host = os.getenv('DB_HOST', 'localhost')
    db_port = int(os.getenv('DB_PORT', '3306'))
    db_name = os.getenv('DB_NAME', 'soon')

db_user = os.getenv('DB_USERNAME', 'yourssu')
db_password = os.getenv('DB_PASSWORD', '')

DB_CONFIG = {
    'host': db_host,
    'database': db_name,
    'user': db_user,
    'password': db_password,
    'port': db_port
}

# Mapping Constants
CATEGORY_MAP = {
    "전필": "MAJOR_REQUIRED",
    "전선": "MAJOR_ELECTIVE",
    "전기": "MAJOR_BASIC",
    "교필": "GENERAL_REQUIRED",
    "교선": "GENERAL_ELECTIVE",
    "채플": "CHAPEL",
    "기타": "OTHER"
}

REVERSE_CATEGORY_MAP = {v: k for k, v in CATEGORY_MAP.items()}

def get_db_connection():
    try:
        return mysql.connector.connect(**DB_CONFIG)
    except mysql.connector.Error as e:
        print(f"Error connecting to database: {e}")
        sys.exit(1)

def get_dept_info(cursor, dept_name: str) -> Tuple[Optional[int], Optional[int]]:
    """Returns (department_id, college_id) for a given department name."""
    query = "SELECT id, college_id FROM department WHERE name = %s"
    cursor.execute(query, (dept_name,))
    row = cursor.fetchone()
    if row:
        return row['id'], row['college_id']
    return None, None

def print_courses(courses):
    if not courses:
        print("No courses found.")
        return

    print(f"\nFound {len(courses)} courses:")
    print("-" * 120)
    print(f"{'Code':<12} | {'Category':<10} | {'Name':<35} | {'Professor':<10} | {'Dept':<15} | {'Credit':<5} | {'Target (Original)'}")
    print("-" * 150)
    
    for course in courses:
        code = course['code']
        category_enum = course['category']
        category_kor = REVERSE_CATEGORY_MAP.get(category_enum, category_enum)
        name = course['name'][:33] + ".." if len(course['name']) > 35 else course['name']
        professor = (course['professor'] or "")[:10]
        dept = course['department'][:15]
        credit = course['credit']
        target_raw = course['target']

        print(f"{code:<12} | {category_kor:<10} | {name:<35} | {professor:<10} | {dept:<15} | {credit:<5} | {target_raw}")
    print("-" * 150)

def query_by_code(code: str):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    query = "SELECT * FROM course WHERE code = %s"
    cursor.execute(query, (code,))
    courses = cursor.fetchall()
    
    print_courses(courses)
    
    # Show detailed target info for this course
    if courses:
        print("\n[Target Information]")
        target_query = """
        SELECT t.*, c.name as college_name, d.name as dept_name
        FROM target t
        LEFT JOIN college c ON t.college_id = c.id
        LEFT JOIN department d ON t.department_id = d.id
        WHERE t.course_code = %s
        """
        cursor.execute(target_query, (code,))
        targets = cursor.fetchall()
        for t in targets:
            scope = "University" if t['scope_type'] == 0 else \
                    f"College ({t['college_name']})" if t['scope_type'] == 1 else \
                    f"Dept ({t['dept_name']})"
            
            grades = []
            if t['grade1']: grades.append("1")
            if t['grade2']: grades.append("2")
            if t['grade3']: grades.append("3")
            if t['grade4']: grades.append("4")
            if t['grade5']: grades.append("5")
            
            effect = "DENY" if t['is_denied'] else "ALLOW"
            
            print(f"  - Scope: {scope:<25} | Grades: {','.join(grades):<10} | Effect: {effect}")

    cursor.close()
    conn.close()

def query_by_criteria(dept_name: str, grade: int, category: str):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    # 1. Resolve Department Info
    dept_id, college_id = get_dept_info(cursor, dept_name)
    if not dept_id:
        print(f"Error: Department '{dept_name}' not found.")
        return
    
    print(f"Criteria: Dept='{dept_name}' (ID: {dept_id}, CollegeID: {college_id}), Grade={grade}, Category='{category}'")

    # 2. Resolve Category Enum
    category_enum = CATEGORY_MAP.get(category)
    if not category_enum:
        print(f"Error: Invalid category '{category}'. Valid values: {list(CATEGORY_MAP.keys())}")
        return

    # 3. Build Query
    # Logic:
    # - Match Course Category
    # - Join Target
    # - Target Condition:
    #   - Student Type = 0 (General) - Assuming general student for manual query
    #   - Grade bit is set (e.g. grade1=1 if grade=1)
    #   - Scope check:
    #       - scope_type=0 (Uni) -> Always match
    #       - scope_type=1 (College) -> target.college_id == valid_college_id
    #       - scope_type=2 (Dept) -> target.department_id == valid_dept_id
    #   - Exclusion: is_denied=0 (Allow list only? Or handle exclusions? Usually 'Allow' rows define who CAN take it)
    #     Strictly speaking, finding "Available" courses is complex if there are DENY rules. 
    #     For simplicity in this tool: Find rows that explicitly ALLOW matching this student.
    
    grade_col = f"grade{grade}"
    
    query = f"""
    SELECT DISTINCT c.*
    FROM course c
    JOIN target t ON c.code = t.course_code
    WHERE c.category = %s
      AND t.student_type = 0
      AND t.is_denied = 0
      AND t.{grade_col} = 1
      AND (
          t.scope_type = 0
          OR (t.scope_type = 1 AND t.college_id = %s)
          OR (t.scope_type = 2 AND t.department_id = %s)
      )
    ORDER BY c.name
    """
    
    cursor.execute(query, (category_enum, college_id, dept_id))
    courses = cursor.fetchall()
    
    print_courses(courses)

    cursor.close()
    conn.close()

def main():
    parser = argparse.ArgumentParser(description="Query Course Tool")
    parser.add_argument("--code", type=str, help="Course Code to search")
    parser.add_argument("--dept", type=str, help="Department Name")
    parser.add_argument("--grade", type=int, help="Grade (1-5)")
    parser.add_argument("--category", type=str, help="Category (전필, 전선, 전기, 교필, 교선, 채플, 기타)")

    args = parser.parse_args()

    if args.code:
        query_by_code(args.code)
    elif args.dept and args.grade and args.category:
        query_by_criteria(args.dept, args.grade, args.category)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
