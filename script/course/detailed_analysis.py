#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import yaml
import re
from collections import defaultdict, Counter

def load_valid_data():
    """data.yml에서 유효한 colleges와 departments 목록을 로드"""
    with open('data.yml', 'r', encoding='utf-8') as f:
        data = yaml.safe_load(f)
    
    colleges = set()
    departments = set()
    
    for college_info in data['ssu-data']['colleges']:
        college_name = college_info['name']
        colleges.add(college_name)
        for dept in college_info['departments']:
            departments.add(dept)
    
    return colleges, departments

def load_classification_data():
    """타겟 분류 결과를 로드"""
    with open('result/target_classifier/2025_2학기_target_classification.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data

def extract_fusion_majors():
    """융합전공들을 추출"""
    classification_data = load_classification_data()
    
    fusion_majors = set()
    special_programs = set()
    invalid_departments = set()
    
    for original, info in classification_data.items():
        classified = info['classified']
        
        for classification in classified:
            # 융합전공 패턴 찾기
            fusion_patterns = [
                r'(\w+융합\w*)',  # XX융합XX 패턴
                r'(\w+·\w+)',     # XX·XX 패턴 
                r'(ICT\w+)',      # ICT로 시작하는 패턴
                r'(AI\w+)',       # AI로 시작하는 패턴
                r'(스마트\w+)',    # 스마트로 시작하는 패턴
                r'(뉴미디어\w+)',  # 뉴미디어로 시작하는 패턴
                r'(동아시아\w+)',  # 동아시아로 시작하는 패턴
            ]
            
            for pattern in fusion_patterns:
                matches = re.findall(pattern, original)
                for match in matches:
                    if '융합' in match or '·' in match or match.startswith(('ICT', 'AI', '스마트', '뉴미디어', '동아시아')):
                        fusion_majors.add(match)
            
            # 계약학과, 특수 프로그램
            if '계약학과' in classification or '순수외국인' in classification:
                special_programs.add(classification)
            
            # 유효하지 않은 학과명들
            if any(keyword in classification for keyword in ['융합', '·', 'ICT', 'AI', '스마트', '뉴미디어', '동아시아']) and \
               classification not in ['AI융합학부1', 'AI융합학부2', 'AI융합학부3', 'AI융합학부4', 'AI융합학부5']:
                invalid_departments.add(classification)
    
    return fusion_majors, special_programs, invalid_departments

def analyze_detailed_problems():
    """상세한 문제 분석"""
    colleges, departments = load_valid_data()
    classification_data = load_classification_data()
    
    # 문제별 상세 분석
    problems = {
        'fusion_majors_not_in_system': [],  # 시스템에 없는 융합전공들
        'contract_departments': [],         # 계약학과들
        'foreign_student_restrictions': [], # 순수외국인 제한
        'complex_original_texts': [],       # 복잡한 원본 텍스트들
        'invalid_classifications': []       # 완전히 잘못된 분류들
    }
    
    fusion_keywords = ['융합', '·', 'ICT', 'AI', '스마트', '뉴미디어', '동아시아', '순환경제']
    
    for original, info in classification_data.items():
        count = info['count']
        classified = info['classified']
        
        # 순수외국인 관련
        if '순수외국인' in original:
            problems['foreign_student_restrictions'].append({
                'original': original,
                'classified': classified,
                'count': count
            })
        
        # 계약학과 관련
        if '계약학과' in original:
            problems['contract_departments'].append({
                'original': original,
                'classified': classified,
                'count': count
            })
        
        # 융합전공 관련
        if any(keyword in original for keyword in fusion_keywords):
            # 융합전공명들 추출
            fusion_found = []
            for keyword in fusion_keywords:
                if keyword in original:
                    # 해당 키워드 주변 텍스트 추출
                    pattern = rf'\w*{keyword}\w*'
                    matches = re.findall(pattern, original)
                    fusion_found.extend(matches)
            
            problems['fusion_majors_not_in_system'].append({
                'original': original,
                'classified': classified,
                'count': count,
                'fusion_keywords_found': list(set(fusion_found))
            })
        
        # 복잡한 원본 텍스트 (여러 학과가 포함된 경우)
        if ',' in original and len(original) > 50:
            problems['complex_original_texts'].append({
                'original': original,
                'classified': classified,
                'count': count
            })
        
        # 완전히 잘못된 분류 (원본 텍스트가 그대로 분류에 들어간 경우)
        for classification in classified:
            if original == classification or (len(original) > 20 and original in classification):
                problems['invalid_classifications'].append({
                    'original': original,
                    'classification': classification,
                    'count': count
                })
    
    return problems

def print_detailed_results(problems):
    """상세 분석 결과 출력"""
    print("=" * 100)
    print("타겟 분류 파일 상세 문제점 분석")
    print("=" * 100)
    
    # 1. 시스템에 없는 융합전공들
    print(f"\n1. 시스템에 없는 융합전공들 ({len(problems['fusion_majors_not_in_system'])}건)")
    print("-" * 80)
    fusion_counter = Counter()
    for item in problems['fusion_majors_not_in_system']:
        for fusion in item['fusion_keywords_found']:
            fusion_counter[fusion] += item['count']
    
    print("발견된 융합전공/특수프로그램 (빈도순):")
    for fusion, count in fusion_counter.most_common(20):
        print(f"  - {fusion}: {count}회")
    
    # 2. 계약학과들
    print(f"\n2. 계약학과 관련 ({len(problems['contract_departments'])}건)")
    print("-" * 80)
    for item in problems['contract_departments']:
        print(f"원본: {item['original']}")
        print(f"분류: {item['classified']}")
        print(f"개수: {item['count']}")
        print()
    
    # 3. 순수외국인 제한
    print(f"\n3. 순수외국인 제한 관련 ({len(problems['foreign_student_restrictions'])}건)")
    print("-" * 80)
    for item in sorted(problems['foreign_student_restrictions'], key=lambda x: x['count'], reverse=True)[:10]:
        print(f"원본: {item['original']}")
        print(f"분류: {item['classified']}")
        print(f"개수: {item['count']}")
        print()
    
    # 4. 복잡한 원본 텍스트들
    print(f"\n4. 복잡한 원본 텍스트들 ({len(problems['complex_original_texts'])}건)")
    print("-" * 80)
    for item in sorted(problems['complex_original_texts'], key=lambda x: x['count'], reverse=True)[:10]:
        print(f"원본: {item['original'][:100]}{'...' if len(item['original']) > 100 else ''}")
        print(f"분류 개수: {len(item['classified'])}")
        print(f"수강생 수: {item['count']}")
        print()
    
    # 5. 완전히 잘못된 분류들
    print(f"\n5. 완전히 잘못된 분류들 ({len(problems['invalid_classifications'])}건)")
    print("-" * 80)
    for item in sorted(problems['invalid_classifications'], key=lambda x: x['count'], reverse=True)[:10]:
        print(f"원본: {item['original']}")
        print(f"잘못된 분류: {item['classification']}")
        print(f"개수: {item['count']}")
        print()

def generate_recommendations():
    """개선 권장사항 생성"""
    print("\n" + "=" * 100)
    print("개선 권장사항")
    print("=" * 100)
    
    print("\n1. 융합전공 처리 개선")
    print("   - data.yml에 융합전공들을 추가하거나")
    print("   - 융합전공 매핑 테이블을 별도로 생성")
    print("   - 융합전공은 기존 학과와 연결하여 분류")
    
    print("\n2. 특수 프로그램 처리")
    print("   - 계약학과, 순수외국인 등은 별도 카테고리로 분류")
    print("   - '대상외수강제한' 플래그를 별도로 처리")
    
    print("\n3. 원본 텍스트 전처리")
    print("   - 복잡한 텍스트는 파싱하여 개별 학과로 분리")
    print("   - 불필요한 수식어 제거 (예: 수강제한, 대상외 등)")
    
    print("\n4. 분류 알고리즘 개선")
    print("   - 원본 텍스트가 그대로 분류되지 않도록 검증")
    print("   - 유효한 학과명만 분류 결과로 반환")

if __name__ == "__main__":
    problems = analyze_detailed_problems()
    print_detailed_results(problems)
    generate_recommendations()