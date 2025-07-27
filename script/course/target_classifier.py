#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Target classification logic for course data parsing.
Handles complex target strings and converts them to department+grade format.
"""

import json
import os
import yaml
import re
from typing import List, Dict, Any, Optional, Tuple


def load_departments_data() -> Tuple[List[str], Dict[str, List[str]]]:
    """Load department data from data.yml file."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    data_yml_path = os.path.join(script_dir, "data.yml")
    
    try:
        with open(data_yml_path, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)
        
        all_departments = []
        college_departments = {}
        
        for college in data['ssu-data']['colleges']:
            college_name = college['name']
            departments = college['departments']
            college_departments[college_name] = departments
            all_departments.extend(departments)
        
        return all_departments, college_departments
    except Exception as e:
        print(f"Error loading data.yml: {e}")
        return [], {}


def create_department_abbreviation_map(departments: List[str]) -> Dict[str, str]:
    """Create mapping from department abbreviations to full names."""
    abbrev_map = {
        # IT대학
        'AI융합': 'AI융합학부',
        'AI융합학부': 'AI융합학부',
        '글로벌미디어': '글로벌미디어학부',
        '미디어경영': '미디어경영학과',
        '소프트': '소프트웨어학부',
        '소프트웨어': '소프트웨어학부',
        'IT융합전공': '전자정보공학부 IT융합전공',
        'IT융합': '전자정보공학부 IT융합전공',
        '전자공학전공': '전자정보공학부 전자공학전공',
        '전자공학': '전자정보공학부 전자공학전공',
        '정보보호': '정보보호학과',
        '컴퓨터': '컴퓨터학부',
        
        # 경영대학
        '경영학부': '경영학부',
        '경영': '경영학부',
        '금융': '금융학부',
        '금융학부': '금융학부',
        '벤처경영': '벤처경영학과',
        '벤처중소': '벤처중소기업학과',
        '복지경영': '복지경영학과',
        '혁신경영': '혁신경영학과',
        '회계세무': '회계세무학과',
        '회계학과': '회계학과',
        '회계': '회계학과',
        
        # 경제통상대학
        '경제': '경제학과',
        '경제학과': '경제학과',
        '국제무역': '국제무역학과',
        '글로벌통상': '글로벌통상학과',
        '금융경제': '금융경제학과',
        '통상산업': '통상산업학과',
        
        # 공과대학
        '건축학': '건축학부 건축학전공',
        '건축공학': '건축학부 건축공학전공',
        '건축학부': '건축학부 건축학부',
        '실내건축': '건축학부 실내건축전공',
        '기계': '기계공학부',
        '기계공학': '기계공학부',
        '산업정보': '산업정보시스템공학과',
        '산업정보시스템': '산업정보시스템공학과',
        '신소재': '신소재공학과',
        '신소재공학': '신소재공학과',
        '전기': '전기공학부',
        '전기공학': '전기공학부',
        '화공': '화학공학과',
        '화학공학': '화학공학과',
        
        # 법과대학
        '국제법무': '국제법무학과',
        '국제법무학과': '국제법무학과',
        '법학': '법학과',
        '법학과': '법학과',
        
        # 베어드학부대학
        '자유전공학부': '자유전공학부',
        '자유전공': '자유전공학부',
        
        # 사회과학대학
        '사회복지': '사회복지학부',
        '사회복지학부': '사회복지학부',
        '언론홍보': '언론홍보학과',
        '정보사회': '정보사회학과',
        '정외': '정치외교학과',
        '정치외교': '정치외교학과',
        '평생교육': '평생교육학과',
        '행정학부': '행정학부',
        '행정': '행정학부',
        
        # 인문대학
        '국문': '국어국문학과',
        '국어국문': '국어국문학과',
        '기독교': '기독교학과',
        '독문': '독어독문학과',
        '독어독문': '독어독문학과',
        '불문': '불어불문학과',
        '불어불문': '불어불문학과',
        '사학': '사학과',
        '스포츠': '스포츠학부',
        '스포츠학부': '스포츠학부',
        '영문': '영어영문학과',
        '영어영문': '영어영문학과',
        '문예창작전공': '예술창작학부 문예창작전공',
        '문예창작': '예술창작학부 문예창작전공',
        '영화예술전공': '예술창작학부 영화예술전공',
        '영화예술': '예술창작학부 영화예술전공',
        '일어일문': '일어일문학과',
        '일문': '일어일문학과',
        '중문': '중어중문학과',
        '중어중문': '중어중문학과',
        '철학': '철학과',
        '철학과': '철학과',
        
        # 자연과학대학
        '물리': '물리학과',
        '물리학과': '물리학과',
        '수학': '수학과',
        '수학과': '수학과',
        '의생명시스템': '의생명시스템학부',
        '의생명': '의생명시스템학부',
        '통계보험': '정보통계보험수리학과',
        '정보통계보험수리': '정보통계보험수리학과',
        '화학': '화학과',
        '화학과': '화학과',
        
        # 기타 단과대학
        '차세대반도체': '차세대반도체학과',
        '차세대반도체학과': '차세대반도체학과'
    }
    
    return abbrev_map


def classify_target(target: str) -> List[str]:
    """Parse target field and return list of department+grade combinations."""
    if not target or target.strip() == "":
        return ["전체1", "전체2", "전체3", "전체4", "전체5"]
    
    # Check for foreign student restrictions or exchange student restrictions - exclude these courses entirely
    if ("순수외국인입학생" in target or 
        "외국인입학생" in target or 
        "순수외국인" in target or
        "교환학생" in target):
        return []  # Return empty list to exclude the course
    
    # Load department data
    all_departments, college_departments = load_departments_data()
    abbrev_map = create_department_abbreviation_map(all_departments)
    
    # Clean target string
    cleaned = target.strip()
    
    # Remove common restriction patterns
    patterns_to_remove = [
        r"\(대상외수강제한\)",
        r"\(타학과수강제한\)", 
        r"\(타학과 수강 불가\)",
        r";순수외국인입학생",
        r"순수외국인입학생 제한",
        r"순수외국인입학생",
        r"내국인 전용",
        r"교직이수자",
        r";교직이수자",
        r"국내대학 학점교류생 수강제한",
        r"국내 대학 학점교류생 수강 제한",
        r"교환학생 수강불가",
        r"수강제한.*?경영\)",
        r"\(.*?수강제한.*?\)"
    ]
    
    for pattern in patterns_to_remove:
        cleaned = re.sub(pattern, "", cleaned)
    cleaned = cleaned.strip()
    
    results = []
    
    # Handle newline separated grade conditions - parse all grades
    if '\n' in cleaned and any(re.match(r'^\d학년', line.strip()) for line in cleaned.split('\n')):
        # Multiple grade lines detected - parse each one
        lines = cleaned.split('\n')
        for line in lines:
            line = line.strip()
            if not line or '(' in line:  # Skip empty lines or restriction patterns
                continue
            
            grade_match = re.search(r'^(\d)학년', line)
            if grade_match:
                grade = int(grade_match.group(1))
                grade = min(grade, 5)  # Cap at 5
                
                # Extract department part after grade
                dept_part = line[len(grade_match.group(0)):].strip()
                
                if not dept_part or dept_part == "전체":
                    results.append(f"전체{grade}")
        
        if results:
            return results
    
    # Handle single newline case - take first line only for non-grade patterns
    if '\n' in cleaned:
        cleaned = cleaned.split('\n')[0].strip()
    
    # Check for "전체" patterns first  
    if (cleaned == "전체" or 
        cleaned.startswith("전체학년 전체") or
        cleaned.startswith("전체(") or
        cleaned == "전체학년"):
        return ["전체1", "전체2", "전체3", "전체4", "전체5"]
    
    # Handle "전체학년 [departments]" pattern first
    if cleaned.startswith("전체학년 "):
        dept_part = cleaned[4:].strip()  # Remove "전체학년 "
        
        # Parse departments
        dept_candidates = re.split(r'[,;]\s*', dept_part)
        
        for dept_candidate in dept_candidates:
            dept_candidate = dept_candidate.strip()
            if not dept_candidate:
                continue
            
            # Try to match with abbreviation map
            if dept_candidate in abbrev_map:
                dept_name = abbrev_map[dept_candidate]
                for grade in [1, 2, 3, 4, 5]:
                    results.append(f"{dept_name}{grade}")
            elif dept_candidate in all_departments:
                for grade in [1, 2, 3, 4, 5]:
                    results.append(f"{dept_candidate}{grade}")
        
        return results if results else [cleaned]
    
    # Handle college-wide patterns (e.g., "IT대학 전체")
    if "IT대학" in cleaned or "IT대" in cleaned:
        it_departments = college_departments.get('IT대학', [])
        for dept in it_departments:
            for grade in [1, 2, 3, 4, 5]:
                results.append(f"{dept}{grade}")
        return results
    
    # Parse grade information
    grade_match = re.search(r'^(\d)학년', cleaned)
    if grade_match:
        grade = int(grade_match.group(1))
        grade = min(grade, 5)  # Cap at 5
        
        # Extract department part after grade
        dept_part = cleaned[len(grade_match.group(0)):].strip()
        
        if not dept_part or dept_part == "전체":
            return [f"전체{grade}"]
        
        # Parse departments from the remaining string
        # Split by common separators
        dept_candidates = re.split(r'[,;]\s*', dept_part)
        
        for dept_candidate in dept_candidates:
            dept_candidate = dept_candidate.strip()
            if not dept_candidate:
                continue
            
            # Try to match with abbreviation map
            if dept_candidate in abbrev_map:
                results.append(f"{abbrev_map[dept_candidate]}{grade}")
            elif dept_candidate in all_departments:
                results.append(f"{dept_candidate}{grade}")
            else:
                # If not found, return original string
                results.append(cleaned)
                break
    
    # If no results found, return original string or default
    if not results:
        if any(dept in cleaned for dept in abbrev_map.keys()):
            # Try to extract department without grade
            for abbrev, full_name in abbrev_map.items():
                if abbrev in cleaned:
                    for grade in [1, 2, 3, 4, 5]:
                        results.append(f"{full_name}{grade}")
                    break
        else:
            # Return original string if no match found
            return [cleaned] if cleaned else ["전체1", "전체2", "전체3", "전체4", "전체5"]
    
    return results if results else ["전체1", "전체2", "전체3", "전체4", "전체5"]


def process_target_classification(input_file: str, output_file: str) -> None:
    """Process target classification and create before/after comparison."""
    print(f"Processing target classification from: {input_file}")
    
    # Load the target data
    with open(input_file, 'r', encoding='utf-8') as f:
        target_data = json.load(f)
    
    comparison_results = {}
    
    # Process each target string
    for original_target, count in target_data.items():
        classified_targets = classify_target(original_target)
        comparison_results[original_target] = {
            "count": count,
            "original": original_target,
            "classified": classified_targets
        }
    
    # Write comparison results
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(comparison_results, f, ensure_ascii=False, indent=2)
    
    print(f"Target classification comparison saved to: {output_file}")
    print(f"Processed {len(comparison_results)} unique target strings")


def main():
    """Main function to run target classification."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Input: target grouper result
    input_file = os.path.join(script_dir, "result", "course_grouper", "2025_2학기_*_target.json")
    
    # Output: target classification comparison
    output_dir = os.path.join(script_dir, "result", "target_classifier")
    os.makedirs(output_dir, exist_ok=True)
    output_file = os.path.join(output_dir, "2025_2학기_target_classification.json")
    
    print("Target Classifier - Converting target strings to department+grade format")
    print(f"Input file: {input_file}")
    print(f"Output file: {output_file}")
    
    process_target_classification(input_file, output_file)


if __name__ == "__main__":
    main()