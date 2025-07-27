#!/usr/bin/env python3
import json
from pathlib import Path

def extract_field_lists(input_file):
    """
    JSON 파일에서 각 필드의 값별 개수를 추출하여 별도의 dict JSON 파일들로 저장
    """
    
    # 입력 파일 읽기
    with open(input_file, 'r', encoding='utf-8') as f:
        courses = json.load(f)
    
    # 각 필드별로 값 개수 수집
    field_counts = {}
    
    # 첫 번째 항목에서 필드 이름들 가져오기
    if courses:
        for field_name in courses[0].keys():
            field_counts[field_name] = {}
    
    # 모든 항목에서 각 필드의 값들 개수 집계
    for course in courses:
        for field_name, value in course.items():
            # null이나 빈 문자열도 포함
            if value is None:
                key = "null"
            elif value == "":
                key = "empty"
            else:
                key = str(value)
            
            if key in field_counts[field_name]:
                field_counts[field_name][key] += 1
            else:
                field_counts[field_name][key] = 1
    
    # 각 필드별로 파일 생성
    input_path = Path(input_file)
    base_name = input_path.stem
    output_dir = input_path.parent / "result" / "course_grouper"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    created_files = []
    
    for field_name, counts in field_counts.items():
        if counts:  # 값이 있는 필드만 처리
            output_file = output_dir / f"{base_name}_{field_name}.json"
            
            # 개수 순으로 정렬 (많은 순)
            sorted_counts = dict(sorted(counts.items(), key=lambda x: x[1], reverse=True))
            
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(sorted_counts, f, ensure_ascii=False, indent=2)
            
            created_files.append(str(output_file))
            print(f"Created {output_file} with {len(sorted_counts)} unique values")
    
    return created_files

def main():
    # 원본 2025_2학기_*.json 파일만 찾기
    current_dir = Path('.')
    course_files = []
    
    # 원본 파일 찾기 (파일명에 asterisk가 포함된 파일)
    original_file = current_dir / "2025_2학기_*.json"
    if original_file.exists():
        course_files.append(original_file)
    
    if not course_files:
        print("No matching files found (course/2025_2학기_*.json)")
        return
    
    for file_path in course_files:
        print(f"\nProcessing {file_path}...")
        try:
            created_files = extract_field_lists(file_path)
            print(f"Successfully created {len(created_files)} field list files")
        except Exception as e:
            print(f"Error processing {file_path}: {e}")

if __name__ == "__main__":
    main()