#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import yaml
import re
from collections import defaultdict

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

def analyze_classifications():
    colleges, departments = load_valid_data()
    classification_data = load_classification_data()
    
    # 문제점 분류
    problems = {
        'original_text_included': [],  # 원본 텍스트가 그대로 포함된 경우
        'invalid_department': [],      # data.yml에 없는 부서명
        'special_program': [],         # 계약학과, 융합전공 등 특수 프로그램명
        'unclassified': []            # 분류되지 않은 경우
    }
    
    for original, info in classification_data.items():
        count = info['count']
        classified = info['classified']
        
        # 분류되지 않은 경우
        if not classified:
            problems['unclassified'].append({
                'original': original,
                'count': count,
                'issue': '분류되지 않음'
            })
            continue
        
        for classification in classified:
            # 1. 원본 텍스트가 그대로 분류에 포함된 경우 체크
            if original in classification or classification in original:
                # 단순히 학과명이 포함된 정상적인 경우는 제외
                if not any(dept in classification for dept in departments):
                    problems['original_text_included'].append({
                        'original': original,
                        'classification': classification,
                        'count': count
                    })
            
            # 2. 계약학과, 융합전공 등 특수 프로그램명이 포함된 경우
            special_keywords = ['계약학과', '융합전공', '순수외국인', '대상외수강제한', '융합학부']
            if any(keyword in classification for keyword in special_keywords):
                problems['special_program'].append({
                    'original': original,
                    'classification': classification,
                    'count': count,
                    'special_keyword': [kw for kw in special_keywords if kw in classification]
                })
            
            # 3. data.yml에 없는 부서명이 포함된 경우 체크
            # 학년 숫자를 제거한 후 체크
            clean_classification = re.sub(r'\d+$', '', classification)
            
            # 유효한 학과/전공인지 체크
            is_valid_dept = False
            for dept in departments:
                if dept in clean_classification or clean_classification in dept:
                    is_valid_dept = True
                    break
            
            # "전체" 키워드는 유효한 것으로 간주
            if "전체" in clean_classification:
                is_valid_dept = True
                
            if not is_valid_dept and clean_classification not in ['전체1', '전체2', '전체3', '전체4', '전체5']:
                problems['invalid_department'].append({
                    'original': original,
                    'classification': classification,
                    'clean_classification': clean_classification,
                    'count': count
                })
    
    return problems

def print_analysis_results(problems):
    """분석 결과를 출력"""
    print("=" * 80)
    print("타겟 분류 파일 문제점 분석 결과")
    print("=" * 80)
    
    # 1. 원본 텍스트가 그대로 분류에 포함된 경우
    print(f"\n1. 원본 텍스트가 그대로 분류에 포함된 경우 ({len(problems['original_text_included'])}건)")
    print("-" * 50)
    for item in problems['original_text_included'][:10]:  # 상위 10개만 표시
        print(f"원본: {item['original']}")
        print(f"분류: {item['classification']}")
        print(f"개수: {item['count']}")
        print()
    if len(problems['original_text_included']) > 10:
        print(f"... 총 {len(problems['original_text_included'])}건 중 10건만 표시")
    
    # 2. data.yml에 없는 부서명이 포함된 경우
    print(f"\n2. data.yml에 없는 부서명이 포함된 경우 ({len(problems['invalid_department'])}건)")
    print("-" * 50)
    # 빈도별로 정렬
    invalid_dept_summary = defaultdict(int)
    for item in problems['invalid_department']:
        invalid_dept_summary[item['clean_classification']] += item['count']
    
    for clean_class, total_count in sorted(invalid_dept_summary.items(), key=lambda x: x[1], reverse=True)[:15]:
        example = next(item for item in problems['invalid_department'] if item['clean_classification'] == clean_class)
        print(f"잘못된 분류: {clean_class}")
        print(f"원본 예시: {example['original']}")
        print(f"총 개수: {total_count}")
        print()
    
    # 3. 계약학과, 융합전공 등 특수 프로그램명이 그대로 포함된 경우
    print(f"\n3. 특수 프로그램명이 그대로 포함된 경우 ({len(problems['special_program'])}건)")
    print("-" * 50)
    special_summary = defaultdict(int)
    for item in problems['special_program']:
        special_summary[item['classification']] += item['count']
    
    for classification, total_count in sorted(special_summary.items(), key=lambda x: x[1], reverse=True)[:10]:
        example = next(item for item in problems['special_program'] if item['classification'] == classification)
        print(f"분류: {classification}")
        print(f"원본 예시: {example['original']}")
        print(f"특수 키워드: {', '.join(example['special_keyword'])}")
        print(f"총 개수: {total_count}")
        print()
    
    # 4. 분류되지 않은 경우
    print(f"\n4. 분류되지 않은 경우 ({len(problems['unclassified'])}건)")
    print("-" * 50)
    for item in sorted(problems['unclassified'], key=lambda x: x['count'], reverse=True)[:10]:
        print(f"원본: {item['original']}")
        print(f"개수: {item['count']}")
        print()
    
    # 요약 통계
    print("\n" + "=" * 80)
    print("요약 통계")
    print("=" * 80)
    total_issues = sum(len(problems[key]) for key in problems)
    print(f"총 문제 건수: {total_issues}")
    print(f"  - 원본 텍스트 포함: {len(problems['original_text_included'])}")
    print(f"  - 유효하지 않은 부서명: {len(problems['invalid_department'])}")
    print(f"  - 특수 프로그램명 포함: {len(problems['special_program'])}")
    print(f"  - 분류되지 않음: {len(problems['unclassified'])}")

if __name__ == "__main__":
    problems = analyze_classifications()
    print_analysis_results(problems)