#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate Custom Target Map for Course Codes
This script creates a mapping of course codes to custom target specifications.
Used for manually overriding course target requirements.
"""

import json
import os
from typing import Dict, List

def load_course_data() -> List[Dict]:
    """Load parsed course data from JSON file."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    course_file = os.path.join(script_dir, "result", "course_parser", "2025_2학기_parsed.json")
    
    if not os.path.exists(course_file):
        raise FileNotFoundError(f"Course data file not found: {course_file}")
    
    with open(course_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def create_custom_target_map() -> Dict[int, str]:
    """
    Create custom target mapping for specific course codes.
    
    Returns:
        Dictionary mapping course codes to custom target strings
    """

    vision_chapel_target = "건축학부 건축공학전공1,건축학부 건축학부1,건축학부 건축학전공1,건축학부 실내건축전공1,기계공학부1,산업정보시스템공학과1,신소재공학과1,전기공학부1,화학공학과1,물리학과1,수학과1,의생명시스템학부1,정보통계보험수리학과1,화학과1,AI융합학부1,글로벌미디어학부1,미디어경영학과1,소프트웨어학부1,전자정보공학부 IT융합전공1,전자정보공학부 전자공학전공1,정보보호학과1,컴퓨터학부1,자유전공학부1,전체2,전체3,전체4,전체5"
    custom_map = {
        # 한반도평화와통일 - 2,3,4,5학년 대상
        2150663403: "",
        
        # 수학과 전공선택 - 4학년 대상
        2150146401: "수학과4",  # 응용수학개론
        2150146601: "수학과4",  # 최적화개론

        2150146201: "수학과3",
        2150568701: "언론홍보학과2", # PR크리에이티브론

        2150101507: vision_chapel_target, # 비전채플
        2150101508: vision_chapel_target, # 비전채플
        2150101509: vision_chapel_target, # 비전채플
        2150101501: vision_chapel_target, # 비전채플
        2150101502: vision_chapel_target, # 비전채플
        2150101503: vision_chapel_target, # 비전채플
        2150101504: vision_chapel_target, # 비전채플
        2150101505: vision_chapel_target, # 비전채플
        2150101506: vision_chapel_target, # 비전채플
        # Add more custom mappings here as needed
        # Format: course_code: "department1,department2,..." or "전체1,전체2,..."
    }
    
    return custom_map

def apply_custom_targets(course_data: List[Dict], custom_map: Dict[int, str]) -> List[Dict]:
    """
    Apply custom target mappings to course data.
    
    Args:
        course_data: Original course data
        custom_map: Custom target mappings by course code
        
    Returns:
        Updated course data with custom targets applied
    """
    updated_courses = []
    
    for course in course_data:
        course_code = course.get("code")
        
        if course_code in custom_map:
            # Create updated course with custom target
            updated_course = course.copy()
            updated_course["custom_target"] = custom_map[course_code]
            updated_courses.append(updated_course)
            print(f"Applied custom target for course {course_code}: {custom_map[course_code]}")
        else:
            updated_courses.append(course)
    
    return updated_courses

def main():
    """Main function to generate custom target map."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Create output directory
    output_dir = os.path.join(script_dir, "result", "custom_target")
    os.makedirs(output_dir, exist_ok=True)
    
    # Load course data
    print("Loading course data...")
    course_data = load_course_data()
    
    # Create custom target map
    print("Creating custom target map...")
    custom_map = create_custom_target_map()
    
    # Apply custom targets
    print("Applying custom targets...")
    updated_courses = apply_custom_targets(course_data, custom_map)
    
    # Save custom target map
    map_output_file = os.path.join(output_dir, "custom_target_map.json")
    with open(map_output_file, 'w', encoding='utf-8') as f:
        json.dump(custom_map, f, ensure_ascii=False, indent=2)
    
    # Save updated course data
    course_output_file = os.path.join(output_dir, "courses_with_custom_targets.json")
    with open(course_output_file, 'w', encoding='utf-8') as f:
        json.dump(updated_courses, f, ensure_ascii=False, indent=2)
    
    print(f"Custom target map saved: {map_output_file}")
    print(f"Updated course data saved: {course_output_file}")
    print(f"Total custom mappings: {len(custom_map)}")

if __name__ == "__main__":
    main()
