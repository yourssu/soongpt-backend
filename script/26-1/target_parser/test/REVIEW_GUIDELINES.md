# 수강대상 파싱 자동 검증 가이드라인 (Claude용)

## 목적
이 문서는 Claude가 `parsed_unique_targets.json`과 `suspicious_parsing.json`을 읽고 자동으로 검증하기 위한 기준을 정의합니다.

## Claude의 역할

Claude는 다음과 같이 각 파싱 결과를 검증합니다:

1. **파싱 결과 읽기**: `suspicious_parsing.json` 또는 `parsed_unique_targets.json` 파일 읽기
2. **검증 수행**: 각 항목을 아래 기준에 따라 분석
3. **문제 항목 식별**: 100% 확신할 수 없는 항목 찾기
4. **수정 제안**: 구체적인 수정 방법 제시
5. **결과 저장**: 검증 결과를 JSON 파일로 저장

## 검증 기준

### 1. 필수 검증 항목

각 파싱 결과에 대해 다음을 확인:

#### 1.1 원본 텍스트 완전성
```python
# 확인 사항
- 원본 텍스트의 모든 의미 있는 토큰이 파싱되었는가?
- unmapped_tokens가 있다면, 그것이 무시 가능한가?
  - 무시 가능: 쉼표(,), 세미콜론(;), 괄호 등 구분자
  - 무시 불가: 학과명, 단과대명, 특수 조건 등
```

**Claude의 판단**:
- 무시 가능한 토큰만 unmapped → ✅ 통과
- 의미 있는 토큰이 unmapped → ❌ 수정 필요
  - 제안: "DEPT_ALIAS에 'XXX': 'YYY' 추가 필요"

#### 1.2 스코프 정확성
```python
# 확인 사항
- scopeType이 원본 텍스트와 일치하는가?
  - "전체학년" → UNIVERSITY
  - "IT대학" → COLLEGE
  - "컴퓨터학부" → DEPARTMENT

- UNIVERSITY 스코프인데 "전체" 표현이 없다면?
  - 특수 플래그(foreigner, military)가 있어야 정당화됨
  - 그렇지 않으면 스코프가 잘못되었을 가능성
```

**Claude의 판단**:
```
원본: "1학년 컴퓨터학부"
파싱: scopeType: UNIVERSITY
→ ❌ DEPARTMENT여야 함
제안: scopeType을 DEPARTMENT로, departmentName을 "컴퓨터학부"로 수정
```

#### 1.3 학년 범위 정확성
```python
# 확인 사항
- "1학년" → minGrade=1, maxGrade=1
- "1~3학년" → minGrade=1, maxGrade=3
- "전체학년" → minGrade=1, maxGrade=5
- 학년 명시 없음 → minGrade=1, maxGrade=5 (기본값)
```

**Claude의 판단**:
```
원본: "2학년 컴퓨터학부"
파싱: minGrade=1, maxGrade=5
→ ❌ 학년 범위 불일치
제안: minGrade=2, maxGrade=2로 수정
```

#### 1.4 제외/포함 로직
```python
# 확인 사항
- "(제외)" 패턴이 있으면 isExcluded=true
- "대상외수강제한" → isStrictRestriction=true + 문맥에 따라 isExcluded 결정
- 제외 대상만 있고 허용 대상이 없는가?
  - "전체 (중문 제외)" → UNIVERSITY(allowed) + 중문(excluded) 2개여야 함
```

**Claude의 판단**:
```
원본: "전체학년 (중문 제외)"
파싱: [
  { scopeType: DEPARTMENT, departmentName: 중어중문학과, isExcluded: true }
]
→ ❌ 기본 허용 대상 누락
제안: UNIVERSITY scope (isExcluded=false) 추가 필요
```

#### 1.5 특수 플래그
```python
# 확인 사항
- "순수외국인", "외국인", "외국국적" → isForeignerOnly=true
- "군위탁" → isMilitaryOnly=true
- "교직이수자", "교직이수" → isTeachingCertificateStudent=true
- "대상외수강제한", "타학과수강제한" → isStrictRestriction=true
```

### 2. 이슈 타입별 검증 로직

#### 2.1 unmapped_tokens
```python
def validate_unmapped_tokens(item):
    """
    unmapped_tokens가 있으면:
    1. 각 토큰이 구분자인지 확인 (쉼표, 괄호 등)
    2. 의미 있는 토큰이면 어떤 종류인지 판단:
       - 학과명? → DEPT_ALIAS 추가 제안
       - 단과대명? → COLLEGE_ALIAS 추가 제안
       - 특수 조건? (교환학생, 계약학과 등) → 필터링 또는 특수 처리 제안

    반환:
    - is_valid: bool (무시 가능한 토큰만 있으면 True)
    - suggestions: List[str] (수정 제안)
    """
```

**예시**:
```
원본: "전체학년 교환학생(대상외수강제한)"
Unmapped: ['교환학생']
parsed_targets: []

Claude 판단:
❌ CRITICAL: "교환학생"이 파싱 안됨
분석: "교환학생"은 특수 학생 유형으로, 파싱 대상이 아닐 수 있음
제안:
  1. transform_targets.py의 필터 키워드에 "교환학생" 추가
  2. 또는 특수 플래그 추가 (isExchangeStudent)
  3. 이 경우 전체 파싱 결과가 비어있어 UNIVERSITY scope 추가 필요
```

#### 2.2 no_parsing
```python
def validate_no_parsing(item):
    """
    parsed_targets가 비어있으면:
    1. 원본 텍스트 분석
    2. 융합전공인가? → data.yml 추가 제안
    3. 특수 케이스인가? → 전처리 로직 제안

    반환:
    - is_critical: bool (반드시 수정 필요)
    - root_cause: str (원인 분석)
    - suggestions: List[str]
    """
```

**예시**:
```
원본: "전체학년 ICT유통물류융합"
parsed_targets: []

Claude 판단:
❌ CRITICAL: 파싱 완전 실패
분석: "ICT유통물류융합"은 융합전공명으로 추정
제안:
  1. data.yml에 융합전공 섹션 추가:
     - name: ICT유통물류융합
  2. 또는 DEPT_ALIAS에 추가:
     "ICT유통물류": "ICT유통물류융합학과"
  3. 파싱 후 DEPARTMENT scope로 변환되어야 함
```

#### 2.3 multiple_departments
```python
def validate_multiple_departments(item):
    """
    4개 이상의 DEPARTMENT가 파싱되었으면:
    1. 원본 텍스트 확인
    2. 실제로 여러 학과를 나열한 것인가?
       - "1학년 불문, 중문, 일문, 철학" → ✅ 정상
    3. 부분 매칭으로 과도하게 매칭되었는가?
       - "건축" → 건축학, 실내건축, 건축공학 → ⚠️ 검토 필요

    반환:
    - is_valid: bool
    - warning: Optional[str]
    """
```

**예시**:
```
원본: "1학년 건축"
parsed_targets: [
  { departmentName: 건축학부 건축학전공 },
  { departmentName: 건축학부 실내건축전공 },
  { departmentName: 건축학부 건축공학전공 }
]

Claude 판단:
⚠️  WARNING: 과도한 매칭 가능성
분석: "건축"이라는 단일 토큰이 3개 학과로 확장됨
원인: DEPT_ALIAS의 "건축": [list] 매핑
제안:
  1. 원본 데이터 확인 필요 - "건축" 단독 사용 시 의도가 무엇인지?
  2. 만약 건축학부 전체를 의미한다면 정상
  3. 특정 전공을 의미한다면 DEPT_ALIAS 수정 필요
```

#### 2.4 strict_without_specifics
```python
def validate_strict_restriction(item):
    """
    isStrictRestriction=true인데:
    1. scopeType이 UNIVERSITY만 있으면 너무 광범위
    2. 구체적인 COLLEGE나 DEPARTMENT가 있어야 정상

    반환:
    - is_too_broad: bool
    - recommendation: str
    """
```

**예시**:
```
원본: "전체 (대상외수강제한)"
parsed_targets: [
  { scopeType: UNIVERSITY, isStrictRestriction: true }
]

Claude 판단:
⚠️  WARNING: 제한 범위가 너무 광범위
분석: "대상외수강제한"이 전체 학년에 적용됨
의문: 정말로 모든 학생이 수강 불가능한가?
제안:
  1. 원본 데이터 재확인 필요
  2. 특정 학과/단과대가 누락되었을 가능성
```

#### 2.5 exclusion_without_base
```python
def validate_exclusion_logic(item):
    """
    제외 로직 검증:
    1. isExcluded=true인 항목이 있는가?
    2. isExcluded=false인 항목도 있는가?
    3. 제외만 있고 허용이 없으면 블랙리스트 로직 오류

    반환:
    - has_base: bool (기본 허용 대상 존재 여부)
    - needs_university_base: bool
    """
```

**예시**:
```
원본: "전체학년 (IT대학 제외)"
parsed_targets: [
  { scopeType: COLLEGE, collegeName: IT대학, isExcluded: true }
]

Claude 판단:
❌ CRITICAL: 기본 허용 대상 누락
분석: "전체학년"에서 "IT대학 제외"는 블랙리스트 패턴
기대 결과:
  1. UNIVERSITY scope (isExcluded=false) - 기본 허용
  2. COLLEGE IT대학 (isExcluded=true) - 예외
현재 결과: 예외만 있고 기본 허용 없음
제안: transform_targets.py의 exclusion 로직 개선 필요
```

### 3. 검증 출력 형식

Claude는 검증 결과를 다음 형식으로 출력:

```json
{
  "validation_timestamp": "20260201_143000",
  "total_items": 621,
  "validated_items": 621,
  "summary": {
    "valid": 239,
    "warnings": 14,
    "critical_issues": 368
  },
  "items": [
    {
      "original_text": "...",
      "status": "critical" | "warning" | "valid",
      "issues": [
        {
          "type": "unmapped_tokens",
          "severity": "critical",
          "description": "의미 있는 토큰이 파싱되지 않음",
          "tokens": ["교환학생"],
          "analysis": "\"교환학생\"은 특수 학생 유형으로...",
          "suggestions": [
            "transform_targets.py 필터에 추가",
            "특수 플래그 isExchangeStudent 도입 검토"
          ]
        }
      ],
      "claude_confidence": "low" | "medium" | "high",
      "requires_human_review": true,
      "parsed_targets": [...],
      "expected_targets": [...] // Claude가 제안하는 올바른 파싱 결과
    }
  ]
}
```

### 4. 검증 실행 방법

사용자가 Claude에게 검증을 요청하는 방법:

```
"REVIEW_GUIDELINES.md에 따라 suspicious_parsing.json을 검증해줘"
```

또는

```
"test/{timestamp}/suspicious_for_review.json의 low confidence 항목들을 검증하고,
100% 확신하지 못하는 항목을 찾아서 수정 제안해줘"
```

Claude는:
1. 해당 파일 읽기
2. 각 항목에 대해 위의 검증 기준 적용
3. 문제 항목 식별 및 분석
4. 수정 제안 생성
5. 검증 결과 JSON 파일 생성

### 5. 자동화된 검증 스크립트

향후 Claude와의 상호작용을 스크립트화하려면:

```python
# claude_validate.py (구상)
# Claude API를 사용하여 자동 검증

import anthropic
import json

def validate_with_claude(items, guidelines_path):
    """
    Claude에게 항목들을 검증 요청
    """
    client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"])

    # REVIEW_GUIDELINES.md 읽기
    with open(guidelines_path) as f:
        guidelines = f.read()

    # Claude에게 검증 요청
    prompt = f"""
    다음 가이드라인에 따라 파싱 결과를 검증해주세요:

    {guidelines}

    검증할 항목:
    {json.dumps(items, ensure_ascii=False, indent=2)}

    각 항목에 대해:
    1. 문제점 분석
    2. 심각도 판단 (critical/warning/valid)
    3. 구체적인 수정 제안

    JSON 형식으로 결과를 반환해주세요.
    """

    response = client.messages.create(
        model="claude-sonnet-4-5-20250929",
        max_tokens=8000,
        messages=[{"role": "user", "content": prompt}]
    )

    return json.loads(response.content[0].text)
```

## 주요 차이점 (기존 가이드라인과)

### 기존 (수동 검토)
- 사람이 flashcard로 항목 검토
- E 키로 오류 표시
- 수동으로 판단

### 현재 (Claude 자동 검증)
- Claude가 가이드라인 읽고 자동 검증
- 각 항목에 대해 구체적인 분석
- 수정 제안 자동 생성
- 100% 확신하지 못하는 경우 명시

## 체크리스트 (Claude용)

각 항목 검증 시 Claude가 확인해야 할 사항:

- [ ] 원본 텍스트의 모든 의미 있는 토큰이 파싱되었는가?
- [ ] scopeType이 원본과 일치하는가?
- [ ] 학년 범위가 정확한가?
- [ ] 제외/포함 로직이 올바른가?
- [ ] 특수 플래그가 정확한가?
- [ ] 제외만 있고 허용이 없는 경우, 기본 대상이 추가되었는가?
- [ ] 다중 학과 파싱이 의도된 것인가, 아니면 과도한 매칭인가?

## Claude의 최종 판단 기준

```python
if 모든 검증 통과:
    status = "valid"
    claude_confidence = "high"

elif 경미한 문제만 있음 (예: 쉼표 unmapped):
    status = "warning"
    claude_confidence = "medium"
    requires_human_review = False

else:
    status = "critical"
    claude_confidence = "low"
    requires_human_review = True
    # 구체적인 수정 제안 필수
```
