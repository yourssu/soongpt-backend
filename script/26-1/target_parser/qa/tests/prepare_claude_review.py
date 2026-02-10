#!/usr/bin/env python3
"""
Claude 검증을 위한 suspicious items 준비 스크립트

suspicious_parsing.json을 읽고 Claude가 검증하기 쉬운 형태로 변환합니다.
변환된 파일은 Claude에게 직접 전달되어 검증을 받습니다.
"""

import json
import os
from datetime import datetime

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# Find the most recent suspicious parsing file
suspicious_files = [f for f in os.listdir(BASE_DIR) if f.startswith("suspicious_parsing_")]
if not suspicious_files:
    print("❌ No suspicious parsing files found!")
    exit(1)

latest_file = sorted(suspicious_files)[-1]
suspicious_path = os.path.join(BASE_DIR, latest_file)

# Extract timestamp
timestamp = latest_file.replace("suspicious_parsing_", "").replace(".json", "")
timestamp_dir = os.path.join(BASE_DIR, timestamp)

print(f"📖 Reading: {latest_file}")

# Load data
with open(suspicious_path, 'r', encoding='utf-8') as f:
    data = json.load(f)

metadata = data["metadata"]
suspicious_items = data["suspicious_items"]

# Prepare for Claude review
review_ready_items = []

for idx, item in enumerate(suspicious_items):
    review_ready_items.append({
        "id": idx,
        "original_text": item["original_text"],
        "count": item["count"],
        "parsed_targets": item["parsed_targets"],
        "confidence": item["confidence"],
        "issues": item["issues"],
        # Claude will fill these fields
        "claude_validation": {
            "status": None,  # "valid" | "warning" | "critical"
            "claude_confidence": None,  # "high" | "medium" | "low"
            "requires_human_review": None,
            "analysis": None,
            "suggestions": [],
            "expected_targets": None
        }
    })

# Save review-ready file
review_ready_path = os.path.join(timestamp_dir, "review_ready.json")

with open(review_ready_path, 'w', encoding='utf-8') as f:
    json.dump({
        "metadata": {
            **metadata,
            "prepared_for_claude": datetime.now().strftime("%Y%m%d_%H%M%S"),
            "review_status": "pending"
        },
        "items": review_ready_items
    }, f, ensure_ascii=False, indent=2)

print(f"✅ Prepared {len(review_ready_items)} items for Claude review")
print(f"📄 Saved to: {review_ready_path}")

# Create instruction file for Claude
instruction_path = os.path.join(timestamp_dir, "CLAUDE_REVIEW_INSTRUCTION.md")

instruction_content = f"""# Claude 검증 지시사항

## 파일 정보
- **검증 대상**: `review_ready.json`
- **항목 수**: {len(review_ready_items)}
- **가이드라인**: `../REVIEW_GUIDELINES.md`

## 검증 절차

### 1단계: 파일 읽기
```
{review_ready_path}를 읽어주세요.
```

### 2단계: REVIEW_GUIDELINES.md 참고
```
../REVIEW_GUIDELINES.md의 검증 기준을 따라주세요.
```

### 3단계: 각 항목 검증

각 항목(`items` 배열의 각 요소)에 대해:

1. **원본 텍스트 분석**: `original_text` 확인
2. **파싱 결과 검토**: `parsed_targets` 확인
3. **기존 이슈 검토**: `issues` 확인
4. **추가 문제 발견**: REVIEW_GUIDELINES.md 기준으로 검증

### 4단계: claude_validation 필드 채우기

각 항목의 `claude_validation` 필드를 다음과 같이 채워주세요:

```json
{{
  "status": "valid" | "warning" | "critical",
  "claude_confidence": "high" | "medium" | "low",
  "requires_human_review": true | false,
  "analysis": "상세 분석 내용 (한글)",
  "suggestions": [
    "구체적인 수정 제안 1",
    "구체적인 수정 제안 2"
  ],
  "expected_targets": [
    // Claude가 제안하는 올바른 파싱 결과 (있는 경우)
  ]
}}
```

### 5단계: 결과 저장

검증이 완료된 JSON을 다음 경로에 저장:
```
{timestamp_dir}/claude_validated.json
```

## 검증 기준 요약

### Valid (정상)
- 모든 토큰이 올바르게 파싱됨
- 스코프, 학년, 플래그 모두 정확
- unmapped_tokens는 구분자만 존재

### Warning (경고)
- 경미한 문제 (예: 쉼표 unmapped)
- 과도한 매칭 가능성 (multiple_departments)
- 원본 확인 권장

### Critical (심각)
- 의미 있는 토큰 파싱 실패
- 파싱 결과 완전 실패
- 제외/포함 로직 오류
- 반드시 수정 필요

## Claude에게 요청할 메시지

```
REVIEW_GUIDELINES.md에 따라 {review_ready_path}을 검증하고,
각 항목의 claude_validation 필드를 채워서
{timestamp_dir}/claude_validated.json에 저장해주세요.

특히 다음을 중점적으로 확인:
1. unmapped_tokens가 의미 있는 토큰인지
2. 파싱 결과가 원본과 일치하는지
3. 100% 확신할 수 없는 경우 requires_human_review: true 설정
```

## 예상 소요 시간
- Low confidence: {metadata['confidence_breakdown']['low']}개 (우선 검토)
- Medium confidence: {metadata['confidence_breakdown']['medium']}개
- 예상 시간: 약 5-10분

## 검증 완료 후
1. `claude_validated.json` 파일 확인
2. `requires_human_review: true` 항목 검토
3. 필요시 transform_targets.py 수정
"""

with open(instruction_path, 'w', encoding='utf-8') as f:
    f.write(instruction_content)

print(f"📋 Instructions saved to: {instruction_path}")

print(f"\n{'='*80}")
print("🤖 Claude 검증 준비 완료!")
print(f"{'='*80}")
print(f"\n다음 단계:")
print(f"1. Claude에게 다음 파일을 전달:")
print(f"   - 검증 대상: {review_ready_path}")
print(f"   - 지시사항: {instruction_path}")
print(f"\n2. Claude에게 요청:")
print(f'   "REVIEW_GUIDELINES.md에 따라 review_ready.json을 검증하고')
print(f'    claude_validated.json으로 저장해주세요"')
print(f"\n3. 검증 결과 확인:")
print(f"   {timestamp_dir}/claude_validated.json")
print(f"\n{'='*80}\n")
