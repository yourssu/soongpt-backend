#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate Target SQL Insert Script
This script reads target classification results and generates SQL INSERT statements for TargetEntity.
courseId is determined by joining with CourseEntity based on the course code.
"""

import json
import os
import re
from typing import Dict, List, Set, Optional

def load_target_classification() -> Dict:
    """Load target classification results from JSON file."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    classification_file = os.path.join(script_dir, "result", "target_classifier", "2025_2학기_target_classification.json")
    
    if not os.path.exists(classification_file):
        raise FileNotFoundError(f"Target classification file not found: {classification_file}")
    
    with open(classification_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def load_course_data() -> List[Dict]:
    """Load parsed course data from JSON file."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    course_file = os.path.join(script_dir, "result", "course_parser", "2025_2학기_parsed.json")
    
    if not os.path.exists(course_file):
        raise FileNotFoundError(f"Course data file not found: {course_file}")
    
    with open(course_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def parse_department_grade(target_str: str) -> List[tuple]:
    """
    Parse department+grade string to extract department name and grade.
    
    Args:
        target_str: String like "컴퓨터학부1", "전체1", "스포츠학부2"
        
    Returns:
        List of (department_name, grade) tuples
    """
    results = []
    
    # Handle special cases
    if target_str.startswith("전체"):
        # Extract grade from "전체1", "전체2", etc.
        grade_match = re.search(r'전체(\d+)', target_str)
        if grade_match:
            grade = int(grade_match.group(1))
            # "전체" means all departments, but we'll mark it as null for now
            # This will need to be handled differently in the actual database
            results.append((None, grade))
    else:
        # Extract department and grade from strings like "컴퓨터학부1", "스포츠학부2"
        match = re.search(r'^(.+?)(\d+)$', target_str)
        if match:
            department = match.group(1)
            grade = int(match.group(2))
            results.append((department, grade))
    
    return results

def generate_department_mapping() -> Dict[str, int]:
    """
    Generate mapping from department names to department IDs.
    This is a placeholder - in real implementation, this should query the database.
    """
    # This mapping should be generated from actual database queries
    # For now, we'll create placeholder mappings
    department_mapping = {
        "컴퓨터학부": 1,
        "전자정보공학부": 2,
        "스포츠학부": 3,
        "경영학부": 4,
        "기계공학부": 5,
        "건축학부": 6,
        "화학공학과": 7,
        "수학과": 8,
        "물리학과": 9,
        "화학과": 10,
        "생명과학과": 11,
        "정보통계보험수리학과": 12,
        "영어영문학과": 13,
        "독어독문학과": 14,
        "불어불문학과": 15,
        "중어중문학과": 16,
        "일어일문학과": 17,
        "철학과": 18,
        "사학과": 19,
        "법학과": 20,
        "행정학과": 21,
        "정치외교학과": 22,
        "사회복지학부": 23,
        "언론홍보학과": 24,
        "평생교육학과": 25,
        "경제학과": 26,
        "금융학부": 27,
        "회계학과": 28,
        "벤처중소기업학과": 29,
        "국제통상학과": 30,
        # Add more mappings as needed
    }
    return department_mapping

def generate_target_inserts(classification_data: Dict, course_data: List[Dict], department_mapping: Dict[str, int]) -> List[str]:
    """
    Generate SQL INSERT statements for TargetEntity.
    
    Args:
        classification_data: Target classification results
        course_data: Parsed course data with code and target fields
        department_mapping: Mapping from department names to IDs
        
    Returns:
        List of SQL INSERT statements
    """
    insert_statements = []
    target_id = 1
    
    # Header comment
    insert_statements.append("-- SQL INSERT statements for TargetEntity")
    insert_statements.append("-- Generated from target classification results")
    insert_statements.append("")
    
    # Create a mapping of target -> list of course codes
    target_to_codes = {}
    for course in course_data:
        target = course.get("target", "")
        code = course.get("code")
        if target and code:
            if target not in target_to_codes:
                target_to_codes[target] = []
            target_to_codes[target].append(code)
    
    for original_target, data in classification_data.items():
        classified_targets = data.get("classified", [])
        
        if not classified_targets:
            continue
        
        # Get all course codes for this target
        course_codes = target_to_codes.get(original_target, [])
        if not course_codes:
            continue
            
        for classified_target in classified_targets:
            department_grades = parse_department_grade(classified_target)
            
            for department_name, grade in department_grades:
                if department_name is None:
                    # Handle "전체" case - this means all departments for the given grade
                    # We'll generate entries for all departments
                    for dept_name, dept_id in department_mapping.items():
                        for code in course_codes:
                            sql = f"""INSERT INTO target (id, department_id, course_id, grade) 
SELECT {target_id}, {dept_id}, c.id, {grade}
FROM course c 
WHERE c.code = {code};"""
                            insert_statements.append(sql)
                            insert_statements.append("")
                            target_id += 1
                else:
                    # Handle specific department
                    dept_id = department_mapping.get(department_name)
                    if dept_id:
                        for code in course_codes:
                            sql = f"""INSERT INTO target (id, department_id, course_id, grade) 
SELECT {target_id}, {dept_id}, c.id, {grade}
FROM course c 
WHERE c.code = {code};"""
                            insert_statements.append(sql)
                            insert_statements.append("")
                            target_id += 1
                    else:
                        # Unknown department - add as comment
                        insert_statements.append(f"-- UNKNOWN DEPARTMENT: {department_name} for target: {original_target}")
                        insert_statements.append("")
    
    return insert_statements

def main():
    """Main function to generate target SQL insert script."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Create output directory
    output_dir = os.path.join(script_dir, "result", "generate_sql_insert")
    os.makedirs(output_dir, exist_ok=True)
    
    # Load data
    print("Loading target classification data...")
    classification_data = load_target_classification()
    
    print("Loading course data...")
    course_data = load_course_data()
    
    print("Generating department mapping...")
    department_mapping = generate_department_mapping()
    
    print("Generating SQL INSERT statements...")
    insert_statements = generate_target_inserts(classification_data, course_data, department_mapping)
    
    # Write SQL file
    output_file = os.path.join(output_dir, "target_insert.sql")
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(insert_statements))
    
    print(f"SQL INSERT statements generated: {output_file}")
    print(f"Total statements: {len([s for s in insert_statements if s.startswith('INSERT')])}")

if __name__ == "__main__":
    main()