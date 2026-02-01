#!/usr/bin/env python3
import json

# Test cases to extract
test_cases = [
    "2학년 전체\n3학년 전체\n4학년 전체\n5학년 전체 (대상외수강제한)",
    "스포츠학부 제외",
    "전체학년 교환학생(대상외수강제한)",
    "전체학년 경영학부 군위탁 (대상외수강제한)",
    "순수외국인입학생 제한",
    "전체(일어일문 제외)",
    "1학년 글로벌미디어 ,AI소프트웨어",
    "전체학년 디지털미디어(미디어경영) (대상외수강제한)",
    "1학년 기계 ,산업정보 ,건축학부",
    "2학년 화공 ,건축학부",
    "2학년 소프트 ,AI융합학부 ,AI소프트웨어",
    "1학년 산업정보 ,건축학부",
    "1학년 글로벌미디어 ,AI소프트",
    "전체(중문 제외),(중국국적학생 제외)",
    "전체학년 전체(영어영문학과제외)",
    "전체(글로벌통상제외)",
    "전체(불문제외)",
    "2학년 ;순수외국인입학생 ,언론홍보\n3학년 ;순수외국인입학생 ,언론홍보\n4학년 ;순수외국인입학생 ,언론홍보",
    "3학년 전체;교직이수자\n4학년 전체;교직이수자",
    "전체(화학과 제외)",
    "전체(독문 제외)",
    "4학년 전체\n5학년 전체 (대상외수강제한)",
    "전체학년 이공계 전체(공대, IT대) 및 자유전공학부 (대상외수강제한)",
    "전체(법대제외)",
    "전체 (영화예술전공 수강제한)",
    "IT융합전공, 컴퓨터, 소프트, AI융합, 글로벌미디어, 정보보호학과 제한 (대상외수강제한)",
    "1학년 언론홍보, 내국인 전용강좌",
    "1학년 IT융합전공(전자공학수강제한) (대상외수강제한)",
    "1학년 전자공학(대상외수강제한)",
    "2학년 언론홍보, 통일외교및개발협력융합;순수외국인입학생\n3학년 언론홍보, 통일외교및개발협력융합;순수외국인입학생\n4학년 언론홍보, 통일외교및개발협력융합;순수외국인입학생",
    "3학년 전체\n4학년 전체\n5학년 전체 (대상외수강제한)",
    "3학년 ;교직이수자 ,물리 ,화학\n4학년 ;교직이수자 ,물리 ,화학",
    "3학년 ;교직이수자 ,수학\n4학년 ;교직이수자 ,수학",
    "3학년 ;교직이수자 ,철학\n4학년 ;교직이수자 ,철학",
    "전체(IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 정보보호학과, 학점교류생 제한) (대상외수강제한)",
    "전체(건축학부 제외)"
]

# Load parsed targets
with open('parsed_unique_targets.json', 'r', encoding='utf-8') as f:
    all_targets = json.load(f)

# Extract matching test cases
results = []
for test_case in test_cases:
    for item in all_targets:
        if item['original_text'] == test_case:
            results.append(item)
            break

# Write to new file
with open('test_cases_corrected.json', 'w', encoding='utf-8') as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

print(f"Extracted {len(results)} test cases out of {len(test_cases)} total")
print(f"Results saved to test_cases_corrected.json")
