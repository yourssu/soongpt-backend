#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate Course SQL Insert Script
This script reads parsed course data and generates SQL INSERT statements for CourseEntity.
"""

import json
import os
from typing import List, Dict, Any

def load_course_data() -> List[Dict]:
    """Load parsed course data from JSON file."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    course_file = os.path.join(script_dir, "result", "course_parser", "2025_2학기_parsed.json")
    
    if not os.path.exists(course_file):
        raise FileNotFoundError(f"Course data file not found: {course_file}")
    
    with open(course_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def escape_sql_string(value: str) -> str:
    """Escape single quotes and other special characters in SQL strings."""
    if value is None:
        return "NULL"
    # Replace single quotes with two single quotes for SQL escaping
    return value.replace("'", "''")

def generate_course_inserts(course_data: List[Dict]) -> List[str]:
    """
    Generate SQL INSERT statements for CourseEntity.
    
    Args:
        course_data: Parsed course data
        
    Returns:
        List of SQL INSERT statements
    """
    insert_statements = []
    
    # Header comment
    insert_statements.append("-- SQL INSERT statements for CourseEntity")
    insert_statements.append("-- Generated from parsed course data")
    insert_statements.append("")
    
    for course in course_data:
        # Extract and escape fields
        category = course.get("category", "OTHER")
        sub_category = course.get("subCategory")
        field = course.get("field")
        code = course.get("code", 0)
        name = escape_sql_string(course.get("name", ""))
        professor = course.get("professor")
        department = escape_sql_string(course.get("department", ""))
        division = course.get("division")
        time = escape_sql_string(course.get("time", ""))
        point = escape_sql_string(course.get("point", "0"))
        personeel = course.get("personeel", 0)
        schedule_room = escape_sql_string(course.get("scheduleRoom", ""))
        target = escape_sql_string(course.get("target", ""))
        
        # Handle NULL values
        sub_category_sql = f"'{escape_sql_string(sub_category)}'" if sub_category else "NULL"
        field_sql = f"'{escape_sql_string(field)}'" if field else "NULL"
        professor_sql = f"'{escape_sql_string(professor)}'" if professor else "NULL"
        division_sql = f"'{escape_sql_string(division)}'" if division else "NULL"
        
        # Generate INSERT statement
        sql = f"""INSERT INTO course (category, sub_category, field, code, name, professor, department, division, time, point, personeel, schedule_room, target) 
VALUES ('{category}', {sub_category_sql}, {field_sql}, {code}, '{name}', {professor_sql}, '{department}', {division_sql}, '{time}', '{point}', {personeel}, '{schedule_room}', '{target}');"""
        
        insert_statements.append(sql)
        insert_statements.append("")
    
    return insert_statements

def main():
    """Main function to generate course SQL insert script."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Create output directory
    output_dir = os.path.join(script_dir, "result", "generate_sql_insert")
    os.makedirs(output_dir, exist_ok=True)
    
    # Load data
    print("Loading course data...")
    course_data = load_course_data()
    
    print("Generating SQL INSERT statements...")
    insert_statements = generate_course_inserts(course_data)
    
    # Write SQL file
    output_file = os.path.join(output_dir, "course_insert.sql")
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(insert_statements))
    
    print(f"SQL INSERT statements generated: {output_file}")
    print(f"Total statements: {len([s for s in insert_statements if s.startswith('INSERT')])}")

if __name__ == "__main__":
    main()