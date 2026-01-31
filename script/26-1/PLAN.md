# CSV 파싱 및 저장 계획 (26-1 학기)

## 개요
`ssu26-1.csv` 파일을 파싱하여 ERD_COURSE_ELIGIBILITY.md에 정의된 `course`, `course_time`, `target` 테이블에 맞게 데이터를 변환합니다.

---

## 1. CSV 컬럼 → 테이블 매핑

### CSV 헤더 구조
| 인덱스 | 컬럼명 | 예시 |
|--------|--------|------|
| 0 | 계획 | "" |
| 1 | 이수구분(주전공) | "전선_컴퓨터", "교필", "전기-AI융합/전선-소프트" |
| 2 | 이수구분(다전공) | "복선-컴퓨터" |
| 3 | 공학인증 | "" |
| 4 | 교과영역 | "['23이후]인간·언어..." |
| 5 | 과목번호 | "5043723901" |
| 6 | 과목명 | "미래IT기술 인사이트" |
| 7 | 수강유의사항 | "" |
| 8 | 강좌유형정보 | "" |
| 9 | 분반 | "" |
| 10 | 교수명 | "신용태" (줄바꿈으로 여러명 구분) |
| 11 | 개설학과 | "IT정책경영학과" |
| 12 | 시간/학점(설계) | "3.0/3.0" |
| 13 | 수강인원 | "0" |
| 14 | 여석 | "9,999" |
| 15 | 강의시간(강의실) | "토 16:20-17:10 (-신용태)\n토 17:20-18:10..." |
| 16 | 과정 | "학사과정", "석박과정" |
| 17 | 수강대상 | "전체", "전체학년 컴퓨터,소프트", "전체학년 ;순수외국인입학생 (대상외수강제한)" |

---

## 2. course 테이블 매핑

```python
course = {
    "code": row[5],                           # 과목번호
    "name": row[6].strip(),                   # 과목명
    "category": parse_category(row[1]),       # 이수구분 → Category enum
    "subCategory": row[1],                    # 이수구분(주전공) 원본
    "field": row[4],                          # 교과영역
    "professor": row[10].replace("\n", ", "), # 교수명 (줄바꿈 → 쉼표)
    "department": row[11],                    # 개설학과
    "division": row[9],                       # 분반
    "time": row[12].split("/")[0],            # 시간 (예: "3.0")
    "point": row[12],                         # 시간/학점 원본
    "personeel": int(row[13]) if row[13] else 0,  # 수강인원
    "scheduleRoom": row[15],                  # 강의시간(강의실) 원본
    "target": row[17],                        # 수강대상 원본
    "credit": parse_credit(row[12]),          # 학점 (예: 3.0)
    "area": row[4],                           # 교과영역
    "isTeachingCert": "교직" in row[7],       # 수강유의사항에 교직 포함 여부
}
```

### Category 파싱 규칙
| 이수구분 패턴 | Category |
|---------------|----------|
| `전필*`, `전기*` | MAJOR_REQUIRED |
| `전선*` | MAJOR_ELECTIVE |
| `교필*` | GENERAL_REQUIRED |
| `교선*` | GENERAL_ELECTIVE |
| `채플` | CHAPEL |
| 그 외 | OTHERS |

---

## 3. course_time 테이블 매핑

### 강의시간 파싱 규칙
입력: `"토 16:20-17:10 (조만식기념관 12214-이순녀)"`

```python
course_time = {
    "courseCode": course["code"],
    "dayOfWeek": parse_day("토"),              # "SAT"
    "startMinute": parse_time("16:20"),        # 980 (16*60 + 20)
    "endMinute": parse_time("17:10"),          # 1030 (17*60 + 10)
    "room": parse_room("(조만식기념관 12214-이순녀)")  # "조만식기념관 12214"
}
```

### 요일 매핑
| 한글 | dayOfWeek |
|------|-----------|
| 월 | MON |
| 화 | TUE |
| 수 | WED |
| 목 | THU |
| 금 | FRI |
| 토 | SAT |
| 일 | SUN |

### 시간 파싱
```python
def parse_time(time_str: str) -> int:
    """HH:MM → 분 단위 변환"""
    hour, minute = map(int, time_str.split(":"))
    return hour * 60 + minute
```

### 강의실 파싱
```python
def parse_room(room_str: str) -> str:
    """(건물명 호수-교수명) → 건물명 호수"""
    # 정규식: \((.+?)-[^)]*\) 또는 \((.+)\)
    match = re.search(r'\(([^-]+)', room_str)
    return match.group(1).strip() if match else ""
```

### 멀티라인 처리
강의시간이 줄바꿈으로 구분된 경우 각각 별도 레코드 생성:
```
월 13:00-13:50 (조만식기념관 12214-이순녀)
월 14:00-14:50 (조만식기념관 12214-이순녀)
```
→ 2개의 course_time 레코드

---

## 4. target 테이블 매핑

### 수강대상 파싱 방식 비교

수강대상 필드는 다양한 패턴이 존재하여 파싱이 복잡합니다.

#### 실제 데이터 예시
```
"전체"
"전체학년 컴퓨터,소프트"
"전체학년 ;순수외국인입학생 (대상외수강제한)"
"1학년 자연대, 공과대  (수강제한:1학년 인문,법,사회,경통,경영,자유전공)"
"1학년 외국국적학생(1학년), IT대, AI대(정보보호학과(계약) 포함)"
"1학년 인문대(기독,국문,독문,불문,중문) (대상외수강제한)"
"2학년 경통대, IT대, AI대학(정보보호학과(계약) 포함)  (수강제한:1학년 인문,법,사회,경통,경영,자유전공)"
```

---

#### 방식 1: 정규식 기반 (Rule-based)

**장점**
- 실행 속도 빠름
- 비용 없음
- 결정론적 결과 (동일 입력 → 동일 출력)
- 오프라인 실행 가능

**단점**
- 모든 패턴을 사전에 정의해야 함
- 새로운 패턴 발견 시 코드 수정 필요
- 복잡한 중첩 패턴 처리 어려움

**구현 예시**
```python
import re

def parse_target_regex(target_str: str) -> list[dict]:
    results = []

    # 외국인 전용
    is_foreigner = "순수외국인" in target_str or "외국국적" in target_str

    # 대상 외 수강제한
    is_excluded = "대상외수강제한" in target_str

    # 학년 파싱
    grade_match = re.search(r"(\d)~?(\d)?학년|전체학년|전체", target_str)
    if grade_match:
        if "전체" in grade_match.group(0):
            min_grade, max_grade = 1, 5
        else:
            min_grade = int(grade_match.group(1))
            max_grade = int(grade_match.group(2) or grade_match.group(1))

    # 학과/단과대 파싱 (복잡한 정규식 필요)
    # ...

    return results
```

**적합한 경우**
- 패턴이 명확하고 제한적
- 대량 데이터 처리 필요
- 비용 최소화 필요

---

#### 방식 2: AI 기반 (LLM 활용)

**장점**
- 복잡한 자연어 패턴 처리 가능
- 새로운 패턴에 유연하게 대응
- 문맥 이해 기반 파싱

**단점**
- API 비용 발생
- 처리 속도 느림 (네트워크 지연)
- 비결정론적 결과 가능
- 할루시네이션 위험

**구현 예시**
```python
import openai

def parse_target_ai(target_str: str, dept_list: list[str]) -> list[dict]:
    prompt = f"""
다음 수강대상 문자열을 파싱하여 JSON으로 반환하세요.

수강대상: "{target_str}"

사용 가능한 학과 목록: {dept_list}

반환 형식:
{{
  "targets": [
    {{
      "scopeType": "UNIVERSITY" | "COLLEGE" | "DEPARTMENT",
      "collegeName": "단과대명 또는 null",
      "departmentName": "학과명 또는 null",
      "minGrade": 1-5,
      "maxGrade": 1-5,
      "isExcluded": boolean,
      "isForeignerOnly": boolean
    }}
  ]
}}
"""
    response = openai.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
        response_format={"type": "json_object"}
    )
    return json.loads(response.choices[0].message.content)
```

**적합한 경우**
- 패턴이 매우 다양하고 예측 불가
- 일회성 데이터 마이그레이션
- 높은 정확도 필요

---

#### 방식 3: 하이브리드 (권장)

**전략**: 정규식으로 처리 가능한 케이스는 정규식, 실패 시 AI로 fallback

**장점**
- 비용 효율성 (대부분 정규식으로 처리)
- 높은 커버리지 (AI가 예외 케이스 처리)
- 결과 검증 가능

**구현 예시**
```python
def parse_target_hybrid(target_str: str, dept_list: list[str]) -> list[dict]:
    # 1단계: 정규식으로 시도
    try:
        result = parse_target_regex(target_str)
        if validate_result(result, dept_list):
            return result
    except ParseError:
        pass

    # 2단계: AI fallback
    return parse_target_ai(target_str, dept_list)

def validate_result(result: list[dict], dept_list: list[str]) -> bool:
    """파싱 결과 검증"""
    for target in result:
        if target["scopeType"] == "DEPARTMENT":
            if target["departmentName"] not in dept_list:
                return False  # 매핑 실패
    return True
```

**워크플로우**
```
┌─────────────────┐
│  수강대상 입력   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  정규식 파싱     │
└────────┬────────┘
         │
    ┌────┴────┐
    │ 성공?   │
    └────┬────┘
     Yes │    No
         │     │
         ▼     ▼
┌─────────┐  ┌─────────────┐
│ 검증    │  │ AI 파싱     │
└────┬────┘  └──────┬──────┘
     │              │
     ▼              ▼
┌─────────────────────────┐
│       결과 저장          │
└─────────────────────────┘
```

---

#### 방식 비교 요약

| 기준 | 정규식 | AI | 하이브리드 |
|------|--------|-----|-----------|
| **처리 속도** | ⭐⭐⭐ 빠름 | ⭐ 느림 | ⭐⭐ 중간 |
| **비용** | ⭐⭐⭐ 무료 | ⭐ 유료 | ⭐⭐ 저렴 |
| **유연성** | ⭐ 낮음 | ⭐⭐⭐ 높음 | ⭐⭐⭐ 높음 |
| **정확도** | ⭐⭐ 패턴 의존 | ⭐⭐ 할루시네이션 위험 | ⭐⭐⭐ 높음 |
| **유지보수** | ⭐ 패턴 추가 필요 | ⭐⭐ 프롬프트 관리 | ⭐⭐ 중간 |
| **결정론성** | ⭐⭐⭐ 보장 | ⭐ 미보장 | ⭐⭐ 정규식 우선 |

---

#### 권장 방식: 하이브리드

1. **1차 파싱 (정규식)**: 명확한 패턴 80% 이상 처리
   - `전체`, `전체학년`
   - `N학년 학과1,학과2,...`
   - `순수외국인`, `대상외수강제한` 플래그

2. **2차 파싱 (AI)**: 복잡한 패턴 처리
   - 괄호 중첩: `인문대(기독,국문,독문,불문,중문)`
   - 복합 조건: `(수강제한:1학년 인문,법,사회)`
   - 약어/비표준 표기

3. **검증 단계**: 학과명 매핑 확인
   - `data.yml`의 학과 목록과 대조
   - 매핑 실패 시 로그 출력

---

### 수강대상(target) 파싱 결과 리뷰 방법

---

#### 1. 자동 검증

| 검증 항목 | 체크 내용 |
|-----------|-----------|
| **스키마** | 필수 필드 존재, scopeType 유효성, 학년 범위 1~5 |
| **참조 무결성** | collegeId/departmentId가 data.yml에 존재 |
| **역검증** | 원본에 "외국인" → isForeignerOnly=true, "대상외수강제한" → isExcluded=true |

```python
def validate_target(target: dict, original: str) -> list[str]:
    errors = []

    # scopeType 일관성
    if target["scopeType"] == "DEPARTMENT" and not target.get("departmentId"):
        errors.append("DEPARTMENT requires departmentId")

    # 학년 범위
    if not (1 <= target["minGrade"] <= target["maxGrade"] <= 5):
        errors.append(f"Invalid grade: {target['minGrade']}-{target['maxGrade']}")

    # 역검증
    if "외국인" in original and not target.get("isForeignerOnly"):
        errors.append("Missing isForeignerOnly flag")
    if "대상외수강제한" in original and not target.get("isExcluded"):
        errors.append("Missing isExcluded flag")

    return errors
```

---

#### 2. 통계 리포트

```
=== target 파싱 통계 ===
총 target 수: 3,456

[Scope 분포]
- UNIVERSITY: 456 (13.2%)
- COLLEGE: 234 (6.8%)
- DEPARTMENT: 2,766 (80.0%)

[플래그]
- isExcluded: 89건
- isForeignerOnly: 45건

[이상치]
⚠️ 과목 #234: target 25개 (평균 3배 초과)
⚠️ 과목 #567: target 0개 (파싱 실패)
```

---

#### 3. 샘플 리뷰 시트 (CSV)

50~100건 층화 샘플링 후 수동 검토:

| 과목코드 | 원본_수강대상 | scopeType | 대상명 | 학년 | isExcluded | isForeignerOnly | 정확(O/X) | 오류 |
|----------|---------------|-----------|--------|------|------------|-----------------|-----------|------|
| 2150101507 | 1학년 자연대, 공과대 | COLLEGE | 자연과학대학 | 1-1 | F | F | O | |
| 2150078502 | 1학년 인문대(기독,국문,...) | DEPARTMENT | 기독교학과 | 1-1 | T | F | X | 학과 누락 |

**샘플링 기준**: scopeType별, 플래그별, AI 파싱 케이스, 다중 target 케이스

---

#### 4. AI 교차 검증 (선택)

정확도 95% 미달 시 다른 모델로 재파싱하여 불일치 케이스 확인:

```python
def cross_validate(original: str, parsed: list[dict]) -> bool:
    reparsed = parse_target_ai(original, model="gpt-4o")
    return compare_targets(parsed, reparsed)
```

---

#### 5. 품질 기준

| 지표 | 목표 |
|------|------|
| 스키마 검증 통과 | 100% |
| 참조 무결성 | 100% |
| 샘플 정확도 | ≥ 95% |
| 이상치 비율 | ≤ 2% |

---

#### 6. 산출물

| 파일 | 설명 |
|------|------|
| `target_validation.json` | 자동 검증 오류 |
| `target_stats.txt` | 통계 리포트 |
| `target_review.csv` | 샘플 리뷰 시트 |

---

### 수강대상 파싱 규칙

#### Case 1: 전체 (전교생)
입력: `"전체"` 또는 `"전체학년"`
```python
target = {
    "courseCode": course["code"],
    "scopeType": "UNIVERSITY",
    "collegeId": None,
    "departmentId": None,
    "minGrade": 1,
    "maxGrade": 5,
    "isExcluded": False,
    "isForeignerOnly": False,
}
```

#### Case 2: 특정 학년 + 학과
입력: `"전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합"`
```python
# 학과별로 각각 target 레코드 생성
for dept in ["기계", "화공", "전기", ...]:
    target = {
        "courseCode": course["code"],
        "scopeType": "DEPARTMENT",
        "departmentId": find_department_id(dept),
        "minGrade": 1,
        "maxGrade": 5,
        "isExcluded": False,
        "isForeignerOnly": False,
    }
```

#### Case 3: 외국인 전용
입력: `"전체학년 ;순수외국인입학생 (대상외수강제한)"`
```python
target = {
    "courseCode": course["code"],
    "scopeType": "UNIVERSITY",
    "minGrade": 1,
    "maxGrade": 5,
    "isExcluded": False,  # 외국인 입장에서는 제한 아님
    "isForeignerOnly": True,
}
```

#### Case 4: 특정 학년
입력: `"2학년 컴퓨터,소프트"`
```python
target = {
    "courseCode": course["code"],
    "scopeType": "DEPARTMENT",
    "departmentId": find_department_id("컴퓨터"),
    "minGrade": 2,
    "maxGrade": 2,
    "isExcluded": False,
    "isForeignerOnly": False,
}
```

#### Case 5: 학년 범위
입력: `"1~3학년 컴퓨터"`
```python
target = {
    "courseCode": course["code"],
    "scopeType": "DEPARTMENT",
    "departmentId": find_department_id("컴퓨터"),
    "minGrade": 1,
    "maxGrade": 3,
    "isExcluded": False,
    "isForeignerOnly": False,
}
```

### 수강대상 파싱 정규식
```python
# 학년 파싱
grade_pattern = r"(\d)~?(\d)?학년"  # 1학년, 1~3학년, 전체학년

# 학과 리스트 파싱
dept_pattern = r"학년\s*(.+?)(?:\s*;|$)"  # 학년 뒤의 학과 목록

# 외국인 전용
foreigner_pattern = r"순수외국인"

# 대상 외 수강제한
excluded_pattern = r"대상외수강제한"
```

---

## 5. 학과명 매핑 (data.yml 기반)

### 약어 → 정식 학과명 매핑
```python
DEPT_ALIAS = {
    # IT대학
    "컴퓨터": "컴퓨터학부",
    "소프트": "소프트웨어학부",
    "AI융합": "AI융합학부",
    "글미": "글로벌미디어학부",
    "정통전": "전자정보공학부 IT융합전공",  # 또는 전자공학전공
    "전자정보공학부-IT융합": "전자정보공학부 IT융합전공",
    "전자정보공학부-전자공학": "전자정보공학부 전자공학전공",

    # 공과대학
    "기계": "기계공학부",
    "화공": "화학공학과",
    "전기": "전기공학부",
    "건축학부": "건축학부 건축학부",
    "신소재": "신소재공학과",
    "산업정보": "산업정보시스템공학과",

    # 자연과학대학
    "물리": "물리학과",
    "화학": "화학과",
    "수학": "수학과",
    "의생명": "의생명시스템학부",
    "의생명시스템": "의생명시스템학부",

    # AI대학
    "정보보호": "정보보호학과",
    "AI소프트웨어": "AI소프트웨어학과",

    # 기타
    ...
}
```

---

## 6. 출력 형식

### JSON 출력 구조
```json
{
  "courses": [
    {
      "code": 5043723901,
      "name": "미래IT기술 인사이트",
      "category": "MAJOR_ELECTIVE",
      "subCategory": "전공_IT경영",
      "field": "",
      "professor": "신용태",
      "department": "IT정책경영학과",
      "division": "",
      "time": "3.0",
      "point": "3.0/3.0",
      "personeel": 0,
      "scheduleRoom": "토 16:20-17:10...",
      "target": "전체",
      "credit": 3.0,
      "area": "",
      "isTeachingCert": false,
      "courseTimes": [
        {
          "dayOfWeek": "SAT",
          "startMinute": 980,
          "endMinute": 1030,
          "room": ""
        }
      ],
      "targets": [
        {
          "scopeType": "UNIVERSITY",
          "collegeId": null,
          "departmentId": null,
          "minGrade": 1,
          "maxGrade": 5,
          "isExcluded": false,
          "isForeignerOnly": false
        }
      ]
    }
  ]
}
```

---

## 7. 구현 단계

### Step 1: CSV 파싱
- pandas 또는 csv 모듈로 멀티라인 필드 처리
- 인코딩: UTF-8

### Step 2: 데이터 변환
1. 각 행에 대해 `course` 객체 생성
2. `강의시간(강의실)` 파싱하여 `course_time` 리스트 생성
3. `수강대상` 파싱하여 `target` 리스트 생성

### Step 3: 학과 매핑
- `data.yml`의 학과 목록과 매핑
- 매핑 실패 시 로그 출력 및 수동 확인 필요

### Step 4: 검증
- 필수 필드 누락 체크
- 과목코드 중복 체크
- 학과 매핑 실패 목록 출력

### Step 5: JSON 출력
- `courses_26_1.json` 파일로 저장
- `src/main/resources/data/2026_1/` 디렉토리에 배치

---

## 8. 예외 처리

### 파싱 실패 케이스
| 케이스 | 처리 방법 |
|--------|-----------|
| 강의시간 없음 | course_time 빈 배열 |
| 수강대상 "전체" | UNIVERSITY scope |
| 학과 매핑 실패 | 로그 출력, 수동 매핑 필요 |
| 석박과정 | 과정 필드로 필터링 가능 |

### 특수 케이스
1. **교수명 여러 명**: 줄바꿈 → 쉼표로 변환
2. **강의시간 여러 개**: 각각 별도 course_time 레코드
3. **학과 복수 표기**: 슬래시(`/`) 또는 쉼표(`,`)로 구분
4. **학년 범위**: `~` 또는 숫자로 min/max 구분

---

## 9. 실행 방법

```bash
cd script/26-1
python main.py
```

### 의존성
```bash
pip install pyyaml pandas
```

---

## 10. 산출물

| 파일 | 설명 |
|------|------|
| `courses_26_1.json` | 변환된 과목 데이터 |
| `mapping_errors.log` | 학과 매핑 실패 목록 |
| `statistics.txt` | 파싱 통계 (총 과목 수, 학과별 분포 등) |
