# Claude 검증 지시사항

## 파일 정보
- **검증 대상**: `review_ready.json`
- **항목 수**: 382
- **가이드라인**: `../REVIEW_GUIDELINES.md`

## 검증 절차

### 1단계: 파일 읽기
```
/Users/leo/Documents/development/soongpt-backend/script/26-1/target_parser/test/20260201_142034/review_ready.json를 읽어주세요.
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
{
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
}
```

### 5단계: 결과 저장

검증이 완료된 JSON을 다음 경로에 저장:
```
/Users/leo/Documents/development/soongpt-backend/script/26-1/target_parser/test/20260201_142034/claude_validated.json
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
REVIEW_GUIDELINES.md에 따라 /Users/leo/Documents/development/soongpt-backend/script/26-1/target_parser/test/20260201_142034/review_ready.json을 검증하고,
각 항목의 claude_validation 필드를 채워서
/Users/leo/Documents/development/soongpt-backend/script/26-1/target_parser/test/20260201_142034/claude_validated.json에 저장해주세요.

특히 다음을 중점적으로 확인:
1. unmapped_tokens가 의미 있는 토큰인지
2. 파싱 결과가 원본과 일치하는지
3. 100% 확신할 수 없는 경우 requires_human_review: true 설정
```

## 예상 소요 시간
- Low confidence: 368개 (우선 검토)
- Medium confidence: 14개
- 예상 시간: 약 5-10분

## 검증 완료 후
1. `claude_validated.json` 파일 확인
2. `requires_human_review: true` 항목 검토
3. 필요시 transform_targets.py 수정
