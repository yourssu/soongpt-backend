#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Target validation and cleanup script.
Validates target classification results against data.yml and cleans invalid entries.
"""

import json
import os
import yaml
import re
from typing import List, Dict, Any, Set


def load_valid_departments_and_colleges() -> tuple[Set[str], Dict[str, List[str]], Set[str], Dict[str, str]]:
    """Load valid departments and colleges from data.yml."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    data_yml_path = os.path.join(script_dir, "data.yml")
    
    try:
        with open(data_yml_path, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)
        
        valid_departments = set()
        college_to_departments = {}
        valid_colleges = set()
        
        # Create college abbreviation mapping
        college_abbrev_map = {
            'IT대': 'IT대학',
            'IT대학': 'IT대학',
            '경영대': '경영대학', 
            '경영대학': '경영대학',
            '경통대': '경제통상대학',
            '경제통상대': '경제통상대학',
            '경제통상대학': '경제통상대학',
            '공과대': '공과대학',
            '공과대학': '공과대학',
            '법과대': '법과대학',
            '법과대학': '법과대학',
            '사회대': '사회과학대학',
            '사회과학대': '사회과학대학',
            '사회과학대학': '사회과학대학',
            '인문대': '인문대학',
            '인문대학': '인문대학',
            '자연대': '자연과학대학',
            '자연과학대': '자연과학대학',
            '자연과학대학': '자연과학대학'
        }
        
        for college in data['ssu-data']['colleges']:
            college_name = college['name']
            departments = college['departments']
            
            valid_colleges.add(college_name)
            college_to_departments[college_name] = departments
            valid_departments.update(departments)
        
        return valid_departments, college_to_departments, valid_colleges, college_abbrev_map
    except Exception as e:
        print(f"Error loading data.yml: {e}")
        return set(), {}, set(), {}


def extract_department_from_target(target: str) -> str:
    """Extract department name from target string (removes grade number)."""
    # Remove grade numbers (1-5) from the end
    match = re.match(r'^(.+?)([1-5])$', target)
    if match:
        dept_part = match.group(1)
        # Remove leading grade pattern like "1학년 " if present
        dept_part = re.sub(r'^\d학년\s+', '', dept_part)
        return dept_part
    
    # Remove leading grade pattern for targets without trailing numbers
    target = re.sub(r'^\d학년\s+', '', target)
    return target


def extract_grade_from_target(target: str) -> str:
    """Extract grade number from target string."""
    match = re.search(r'([1-5])$', target)
    if match:
        return match.group(1)
    return ""


def expand_college_to_departments(target: str, college_to_departments: Dict[str, List[str]]) -> List[str]:
    """Expand college name to all its departments with grade."""
    department = extract_department_from_target(target)
    grade = extract_grade_from_target(target)
    
    if department in college_to_departments:
        # Expand college to all its departments
        expanded = []
        for dept in college_to_departments[department]:
            expanded.append(f"{dept}{grade}")
        return expanded
    
    return [target]


def reparse_original_target_for_colleges(original_target: str, college_to_departments: Dict[str, List[str]], 
                                          college_abbrev_map: Dict[str, str]) -> List[str]:
    """Re-parse original target string to catch college abbreviations that classifier missed."""
    results = []
    
    # Clean the original target
    cleaned = original_target.strip()
    
    # Remove restriction patterns
    patterns_to_remove = [
        r"\(대상외수강제한\)",
        r"\(타학과수강제한\)", 
        r"\(타학과 수강 불가\)",
        r";순수외국인입학생",
        r"순수외국인입학생 제한",
        r"순수외국인입학생",
        r"수강제한.*?경영\)"
    ]
    
    for pattern in patterns_to_remove:
        cleaned = re.sub(pattern, "", cleaned)
    cleaned = cleaned.strip()
    
    # Handle newline - take first line
    if '\n' in cleaned:
        cleaned = cleaned.split('\n')[0].strip()
    
    # Look for college abbreviations in the string
    for abbrev, full_name in college_abbrev_map.items():
        if abbrev in cleaned and full_name in college_to_departments:
            # Extract grade if present
            grade_match = re.search(r'^(\d)학년', cleaned)
            if grade_match:
                grade = grade_match.group(1)
                # Expand to all departments in the college
                for dept in college_to_departments[full_name]:
                    results.append(f"{dept}{grade}")
                return results
    
    return results


def validate_and_clean_targets(targets: List[str], valid_departments: Set[str], 
                               college_to_departments: Dict[str, List[str]], 
                               valid_colleges: Set[str], college_abbrev_map: Dict[str, str],
                               original_target: str = "") -> List[str]:
    """Validate targets and clean invalid entries."""
    cleaned_targets = []
    
    # First, try to re-parse original target for college abbreviations
    if original_target:
        college_expanded = reparse_original_target_for_colleges(original_target, college_to_departments, college_abbrev_map)
        if college_expanded:
            return college_expanded
    
    for target in targets:
        # Handle "전체" case - keep as is
        if target.startswith("전체"):
            cleaned_targets.append(target)
            continue
        
        department = extract_department_from_target(target)
        grade = extract_grade_from_target(target)
        
        # Check if it's a college abbreviation that needs mapping
        if department in college_abbrev_map:
            full_college_name = college_abbrev_map[department]
            if full_college_name in college_to_departments:
                # Expand to all departments in the college
                for dept in college_to_departments[full_college_name]:
                    cleaned_targets.append(f"{dept}{grade}")
        # Check if it's a college name that needs expansion
        elif department in valid_colleges:
            # Expand to all departments in the college
            expanded = expand_college_to_departments(target, college_to_departments)
            cleaned_targets.extend(expanded)
        elif department in valid_departments:
            # Valid department, but fix format if needed
            if grade:
                cleaned_targets.append(f"{department}{grade}")
            else:
                cleaned_targets.append(department)
        else:
            # Invalid department, replace with "기타"
            if grade:
                cleaned_targets.append(f"기타{grade}")
            else:
                cleaned_targets.append("기타")
    
    # Remove duplicates while preserving order
    seen = set()
    result = []
    for target in cleaned_targets:
        if target not in seen:
            seen.add(target)
            result.append(target)
    
    return result


def process_target_validation(input_file: str, output_file: str) -> None:
    """Process and validate target classification results."""
    print(f"Processing target validation from: {input_file}")
    
    # Load valid departments and colleges
    valid_departments, college_to_departments, valid_colleges, college_abbrev_map = load_valid_departments_and_colleges()
    
    print(f"Loaded {len(valid_departments)} valid departments")
    print(f"Loaded {len(valid_colleges)} valid colleges")
    print(f"Loaded {len(college_abbrev_map)} college abbreviations")
    
    # Load target classification data
    with open(input_file, 'r', encoding='utf-8') as f:
        classification_data = json.load(f)
    
    validated_results = {}
    total_processed = 0
    total_changes = 0
    
    # Process each classification result
    for original_target, data in classification_data.items():
        classified_targets = data["classified"]
        
        # Validate and clean the classified targets
        cleaned_targets = validate_and_clean_targets(
            classified_targets, valid_departments, college_to_departments, valid_colleges, college_abbrev_map, original_target
        )
        
        # Check if there were changes
        if cleaned_targets != classified_targets:
            total_changes += 1
        
        validated_results[original_target] = {
            "count": data["count"],
            "original": data["original"],
            "before_validation": classified_targets,
            "after_validation": cleaned_targets
        }
        
        total_processed += 1
    
    # Write validated results
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(validated_results, f, ensure_ascii=False, indent=2)
    
    print(f"Target validation completed!")
    print(f"Processed {total_processed} target strings")
    print(f"Modified {total_changes} target strings")
    print(f"Validation results saved to: {output_file}")


def main():
    """Main function to run target validation."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Input: target classification result
    input_file = os.path.join(script_dir, "result", "target_classifier", "2025_2학기_target_classification.json")
    
    # Output: validated target classification
    output_dir = os.path.join(script_dir, "result", "target_validator")
    os.makedirs(output_dir, exist_ok=True)
    output_file = os.path.join(output_dir, "2025_2학기_target_validated.json")
    
    print("Target Validator - Validating target classifications against data.yml")
    print(f"Input file: {input_file}")
    print(f"Output file: {output_file}")
    
    process_target_validation(input_file, output_file)


if __name__ == "__main__":
    main()