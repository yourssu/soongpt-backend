#!/usr/bin/env python3
import json
import re
from typing import Dict, List

def extract_grade_from_key(key: str) -> List[int]:
    """키에서 학년 정보를 추출합니다."""
    grades = []
    
    # "1학년", "2학년" 등의 패턴을 찾습니다
    grade_matches = re.findall(r'(\d+)학년', key)
    for match in grade_matches:
        grades.append(int(match))
    
    # "전체학년"인 경우 1-5학년으로 처리
    if '전체학년' in key or key == '전체':
        grades = [1, 2, 3, 4, 5]
    
    # 중복 제거하고 정렬
    return sorted(list(set(grades)))

def has_grade_suffix(department: str) -> bool:
    """학과명 뒤에 이미 학년이 붙어있는지 확인합니다."""
    return bool(re.search(r'\d+$', department))

def add_grades_to_department(department: str, grades: List[int]) -> str:
    """학과명에 학년을 추가합니다 (학년이 없는 경우에만)."""
    if has_grade_suffix(department):
        return department
    
    if not grades:
        return department
    
    # 학년별로 학과명 생성
    dept_with_grades = []
    for grade in grades:
        dept_with_grades.append(f"{department}{grade}")
    
    return ','.join(dept_with_grades)

def process_target_map(file_path: str) -> None:
    """target_map.json 파일을 처리합니다."""
    
    # JSON 파일 읽기
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # 각 항목 처리
    for key, value in data.items():
        if not value or value.strip() == "":
            continue
            
        # 키에서 학년 정보 추출
        grades = extract_grade_from_key(key)
        
        if not grades:
            continue
        
        # 쉼표로 구분된 학과들 처리
        departments = [dept.strip() for dept in value.split(',') if dept.strip()]
        updated_departments = []
        
        for dept in departments:
            updated_dept = add_grades_to_department(dept, grades)
            updated_departments.append(updated_dept)
        
        # 결과 업데이트
        data[key] = ','.join(updated_departments)
    
    # JSON 파일 저장 (원본 덮어쓰기)
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    
    print(f"처리 완료: {file_path}")

if __name__ == "__main__":
    file_path = "/25-2/course/result/target_map/target_map.json"
    process_target_map(file_path)
