#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Course data parser for converting 2025_2학기 JSON files to CourseEntity format.
Uses target_classifier and target_validator for target field processing.
"""

import json
import glob
import os
from typing import List, Dict, Any, Optional, Tuple

# Import target processing modules
from target_classifier import classify_target
from target_validator import (
    load_valid_departments_and_colleges, 
    validate_and_clean_targets
)


def parse_category(category: str) -> str:
    """Convert category string to CourseEntity Category enum value."""
    if not category or category.strip() == "" or category == "empty":
        return "OTHER"
    
    # Handle multiple categories separated by "/"
    categories = [cat.strip() for cat in category.split("/")]
    
    # Find the first matching category in priority order
    for cat in categories:
        if "전필" in cat:
            return "MAJOR_REQUIRED"
        elif "전기" in cat:
            return "MAJOR_REQUIRED"
        elif "전선" in cat:
            return "MAJOR_ELECTIVE"
        elif cat.startswith("전공"):
            return "MAJOR_ELECTIVE"
        elif "교필" in cat:
            return "GENERAL_REQUIRED"
        elif "교선" in cat:
            return "GENERAL_ELECTIVE"
        elif "채플" in cat:
            return "CHAPEL"
    
    # If no known category found, return OTHER
    return "OTHER"


def parse_sub_category(sub_category: Optional[str]) -> Optional[str]:
    """Parse and clean sub_category field."""
    return sub_category if sub_category else None


def parse_field(field: Optional[str]) -> Optional[str]:
    """Parse and clean field."""
    return field if field else ""


def parse_code(code: str) -> int:
    """Convert code string to Long."""
    try:
        return int(code)
    except ValueError:
        # If code is not numeric, generate a hash or use default
        return hash(code) % (10**10)  # Generate a numeric code from string


def parse_name(name: str) -> str:
    """Parse and clean course name."""
    return name.strip()


def parse_professor(professor: str) -> Optional[str]:
    """Parse professor field, handling multiple professors."""
    if not professor or professor.strip() == "":
        return None
    
    # Handle multiple professors separated by newlines
    professors = professor.strip().split('\n')
    return professors[0].strip() if professors else None


def parse_department(department: str) -> str:
    """Parse department field."""
    return department.strip()


def parse_division(division: Optional[str]) -> Optional[str]:
    """Parse division field."""
    return division if division else None


def parse_time(schedule_room: str) -> str:
    """Extract time information from schedule_room field."""
    if not schedule_room or schedule_room.strip() == "":
        return ""
    
    # Extract time patterns from schedule_room
    lines = schedule_room.strip().split('\n')
    time_parts = []
    
    for line in lines:
        if line.strip():
            # Extract day and time (e.g., "화 11:00-11:50")
            parts = line.split(' ')
            if len(parts) >= 2:
                day = parts[0]
                time_range = parts[1]
                time_parts.append(f"{day} {time_range}")
    
    return ' '.join(time_parts) if time_parts else ""


def parse_time_points(time_points: str) -> Tuple[str, str]:
    """
    Parse time_points field and split into time and point strings.
    
    Args:
        time_points: String in format "time/point" (e.g., "3.0/3.0")
        
    Returns:
        Tuple of (time_str, point_str)
    """
    if not time_points or time_points.strip() == "":
        return ("", "")
    
    # Split by '/' if present
    if '/' in time_points:
        parts = time_points.split('/')
        if len(parts) >= 2:
            time_str = parts[0].strip()
            point_str = parts[1].strip()
            return (time_str, point_str)
    
    # If no '/' found, assume it's just the point value
    return ("", time_points.strip())


def parse_point(time_points: str) -> str:
    """Parse point information from time_points field."""
    _, point_str = parse_time_points(time_points)
    return point_str if point_str else "0"


def parse_personeel(personeel: str) -> int:
    """Parse personeel (enrollment) number."""
    try:
        return int(personeel) if personeel else 0
    except ValueError:
        return 0


def parse_schedule_room(schedule_room: str) -> str:
    return schedule_room if schedule_room else ""


def parse_target(target: str) -> str:
    return target.strip() if target else ""


def convert_course_item(item: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Convert a single course item from JSON to CourseEntity format. Returns None if course should be excluded."""
    # Parse category first to check for chapel courses
    category = item.get("category", "")
    
    # Handle chapel courses - extract grades from target and apply chapel logic
    parsed_category = parse_category(category)
    if parsed_category == "CHAPEL":
        original_target = item.get("target", "")
        
        # Check for special cases that should have empty target
        if ("계약학과" in original_target and "재직자전형" in original_target and 
            "7+1해외봉사자" in original_target and "파견교환학생" in original_target and
            "외국인" in original_target and "대상외수강제한" in original_target):
            target_result = ""
        else:
            # Extract grade numbers from target string
            import re
            grade_matches = re.findall(r'(\d)학년', original_target)
            if grade_matches:
                unique_grades = sorted(set(int(g) for g in grade_matches))
                
                # Chapel logic: if only 1학년, then only 1학년; if 2학년 or higher, then 2학년~5학년
                if unique_grades == [1]:
                    target_result = "전체1"
                elif any(grade >= 2 for grade in unique_grades):
                    target_result = "전체2,전체3,전체4,전체5"
                else:
                    target_result = ",".join([f"전체{grade}" for grade in unique_grades])
            else:
                # Fallback to all grades if no specific grades found
                target_result = "전체1,전체2,전체3,전체4,전체5"
    else:
        # Parse target normally for non-chapel courses
        target_result = parse_target(item.get("target", ""))
        
        # If target parsing returns empty list, exclude this course
        if not target_result:
            return None
    
    # Parse time_points to get separate time and point values
    time_from_points, point_from_points = parse_time_points(item.get("time_points", ""))
    
    return {
        "id": None,
        "category": parse_category(item.get("category", "")),
        "subCategory": parse_sub_category(item.get("sub_category")),
        "field": parse_field(item.get("field")),
        "code": parse_code(item.get("code", "0")),
        "name": parse_name(item.get("name", "")),
        "professor": parse_professor(item.get("professor", "")),
        "department": parse_department(item.get("department", "")),
        "division": parse_division(item.get("division")),
        "time": time_from_points if time_from_points else parse_time(item.get("schedule_room", "")),
        "point": point_from_points if point_from_points else "0",
        "personeel": parse_personeel(item.get("personeel", "0")),
        "scheduleRoom": parse_schedule_room(item.get("schedule_room", "")),
        "target": target_result
    }


def process_single_file(json_file: str, output_file: str) -> None:
    """Process a single JSON file and create output file."""
    all_courses = []
    seen_courses = set()  # Track unique courses by (code, name, department)
    
    if not os.path.exists(json_file):
        print(f"File not found: {json_file}")
        return
    
    print(f"Processing: {json_file}")
    
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        if isinstance(data, list):
            for item in data:
                converted_item = convert_course_item(item)
                if converted_item is not None:  # Only add non-excluded courses
                    # Create unique key for deduplication (only code)
                    unique_key = converted_item["code"]
                    
                    # Only add if not seen before
                    if unique_key not in seen_courses:
                        seen_courses.add(unique_key)
                        all_courses.append(converted_item)
        else:
            print(f"Warning: {json_file} does not contain a list")
            
    except Exception as e:
        print(f"Error processing {json_file}: {e}")
        return
    
    # Write output file
    print(f"Writing {len(all_courses)} unique courses to {output_file}")
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(all_courses, f, ensure_ascii=False, indent=2)
    
    print("Conversion completed successfully!")


def process_json_files(input_pattern: str, output_file: str) -> None:
    """Process all JSON files matching the pattern and create output file."""
    all_courses = []
    seen_courses = set()  # Track unique courses by (code, name, department)
    
    # Find all files matching the pattern
    json_files = glob.glob(input_pattern)
    
    if not json_files:
        print(f"No files found matching pattern: {input_pattern}")
        return
    
    print(f"Found {len(json_files)} files to process")
    
    for json_file in json_files:
        print(f"Processing: {json_file}")
        
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            if isinstance(data, list):
                for item in data:
                    converted_item = convert_course_item(item)
                    if converted_item is not None:  # Only add non-excluded courses
                        # Create unique key for deduplication (only code)
                        unique_key = converted_item["code"]
                        
                        # Only add if not seen before
                        if unique_key not in seen_courses:
                            seen_courses.add(unique_key)
                            all_courses.append(converted_item)
            else:
                print(f"Warning: {json_file} does not contain a list")
                
        except Exception as e:
            print(f"Error processing {json_file}: {e}")
            continue
    
    # Write output file
    print(f"Writing {len(all_courses)} unique courses to {output_file}")
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(all_courses, f, ensure_ascii=False, indent=2)
    
    print("Conversion completed successfully!")


def main():
    """Main function to run the course parser."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Handle the specific filename with literal asterisk
    json_file = os.path.join(script_dir, "2025_2학기_*.json")
    # Create result/course_parser directory and save result there
    output_dir = os.path.join(script_dir, "result", "course_parser")
    os.makedirs(output_dir, exist_ok=True)
    output_file = os.path.join(output_dir, "2025_2학기_parsed.json")
    
    print("Course Parser - Converting 2025_2학기 JSON files to CourseEntity format")
    print(f"Input file: {json_file}")
    print(f"Output file: {output_file}")
    
    # Process the single file directly
    process_single_file(json_file, output_file)


if __name__ == "__main__":
    main()
