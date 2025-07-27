#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import yaml
import re
from collections import defaultdict, Counter

def generate_final_summary():
    """최종 요약 보고서 생성"""
    
    print("=" * 100)
    print("타겟 분류 파일 문제점 분석 최종 보고서")
    print("=" * 100)
    
    print("\n📋 분석 대상 파일:")
    print("  - data.yml: /Users/leo/Documents/development/soongpt-backend/script/course/data.yml")
    print("  - 타겟 분류 결과: /Users/leo/Documents/development/soongpt-backend/script/course/result/target_classifier/2025_2학기_target_classification.json")
    
    print("\n🎯 주요 문제점 요약:")
    print("  총 585건의 문제 발견")
    print("  ├── 원본 텍스트가 그대로 분류에 포함: 195건")
    print("  ├── data.yml에 없는 부서명 포함: 191건") 
    print("  ├── 특수 프로그램명 그대로 포함: 173건")
    print("  └── 분류되지 않은 케이스: 26건")
    
    print("\n🔍 문제 유형별 상세 분석:")
    
    print("\n1️⃣ 원본 텍스트가 그대로 분류에 포함된 경우 (195건)")
    print("   문제 예시:")
    print("   - 원본: '1학년 자유전공학부1' → 분류: '1학년 자유전공학부1' (17건)")
    print("   - 원본: '1학년 건축학부(건축학,건축공학),스마트소재제품융합' → 그대로 분류 (7건)")
    print("   - 원본: '2학년 전기,에너지공학융합' → 그대로 분류 (7건)")
    
    print("\n2️⃣ data.yml에 정의되지 않은 잘못된 부서명 (191건)")
    print("   주요 문제 부서들:")
    print("   - ICT유통물류융합: 66회")
    print("   - 에너지공학융합: 46회") 
    print("   - 사회적기업과사회혁신융합: 40회")
    print("   - 스마트자동차융합: 35회")
    print("   - 인공지능반도체융합: 34회")
    print("   - 지식재산융합: 32회")
    print("   - AI로봇융합: 31회")
    print("   - 동아시아경제통상: 27회")
    print("   - 스마트소재제품융합: 25회")
    
    print("\n3️⃣ 계약학과, 융합전공 등 특수 프로그램명이 그대로 포함 (173건)")
    print("   - AI융합학부 관련: 139건")
    print("   - IT융합전공 관련: 125건") 
    print("   - 계약학과 관련: 45건")
    print("   \n   예시:")
    print("   - 'AI융합학부1', 'AI융합학부2' 등으로 분류")
    print("   - '전체학년 혁신경영학과(계약학과)' 그대로 분류")
    
    print("\n4️⃣ 분류되지 않은 경우 (26건)")
    print("   주로 순수외국인 제한 관련:")
    print("   - '전체학년 ;순수외국인입학생 (대상외수강제한)': 42건")
    print("   - '전체학년 경영학부;순수외국인입학생 (대상외수강제한)': 32건")
    print("   - '전체학년 전체;순수외국인입학생 (대상외수강제한)': 23건")
    
    print("\n🔧 권장 개선사항:")
    
    print("\n1. 융합전공 매핑 테이블 생성")
    print("   다음 융합전공들을 기존 학과와 매핑:")
    fusion_majors = [
        "ICT유통물류융합 → 산업정보시스템공학과",
        "에너지공학융합 → 전기공학부", 
        "사회적기업과사회혁신융합 → 경영학부",
        "스마트자동차융합 → 기계공학부",
        "인공지능반도체융합 → 컴퓨터학부",
        "지식재산융합 → 법학과",
        "AI로봇융합 → AI융합학부",
        "동아시아경제통상 → 글로벌통상학과",
        "스마트소재제품융합 → 신소재공학과"
    ]
    for mapping in fusion_majors:
        print(f"   - {mapping}")
    
    print("\n2. 특수 카테고리 처리")
    print("   - 계약학과: 별도 플래그로 처리")
    print("   - 순수외국인 제한: 별도 필터링 로직")
    print("   - 대상외수강제한: 메타데이터로 관리")
    
    print("\n3. 분류 알고리즘 개선")
    print("   - 원본 텍스트 그대로 반환 금지")
    print("   - 유효한 학과명 검증 로직 추가")
    print("   - 복잡한 텍스트 파싱 개선")
    
    print("\n4. 데이터 정제")
    print("   - 불필요한 수식어 제거")
    print("   - 학년별 분리 처리")
    print("   - 여러 학과 포함 시 개별 분리")
    
    print("\n📊 처리 우선순위:")
    print("   1. 높음: 원본 텍스트 그대로 분류 (195건) - 즉시 수정 필요")
    print("   2. 높음: 융합전공 매핑 (191건) - 비즈니스 로직 개선 필요") 
    print("   3. 중간: 특수 프로그램 처리 (173건) - 별도 로직 구현")
    print("   4. 낮음: 순수외국인 제한 (26건) - 필터링 로직 추가")

if __name__ == "__main__":
    generate_final_summary()