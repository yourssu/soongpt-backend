# 전공 과목 추천 조회 (GET /api/courses/recommend)

## 개요

- **목적**: SSO로 동기화된 학적 정보(주전공·학년·졸업사정표·수강이력)를 기반으로, 이번 학기에 수강해야 할 **전공기초 / 전공필수 / 전공선택 / 재수강** 과목을 카테고리별로 추천한다.
- **우선순위**
  - **1순위 성능·정합성**: rusaint 스냅샷 + DB(Target JOIN) 기반으로 정확한 미이수/대상외 필터링
  - **2순위 프론트 편의**: 프론트는 카테고리 단위로 공통 UI만 구현하면 되도록 일관된 스키마 제공
  - **3순위 레거시 유지**: 기존 과목 정보 구조(`category`, `subCategory`, `time`, `point`, `scheduleRoom`, `target` 등)를 가능한 한 `section` 단위에 보존
- **데이터 소스**
  - **SyncSessionStore.usaintData**
    - `graduationSummary.majorFoundation/majorRequired/majorElective` → 카테고리별 `required`, `completed`, `satisfied`
    - `takenCourses[].subjectCodes` → **중복 제거한 subjectCodes**로 미이수 과목 필터링
    - `basicInfo.department`, `basicInfo.grade` → 주전공 학과/학년
    - `lowGradeSubjectCodes` → C+ 이하 성적 과목코드 (재수강 추천용)
  - **DB**
    - 이번 학기 개설 과목 (Course)
    - Target(수강 대상/대상외수강제한) 정보

---

## Request

### Headers / Cookie

| Name             | Type   | Required | Description                             |
| ---------------- | ------ | -------- | --------------------------------------- |
| `soongpt_auth` | cookie | Yes      | SSO 콜백에서 발급된 JWT 쿠키 (HttpOnly) |

### Query Parameters

| Name         | Type   | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ------------ | ------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `category` | string | No       | 추천할 카테고리 필터. 지정하지 않으면 `전공기초 → 전공필수 → 전공선택`을 **모두** 조회한다 (**RETAKE는 기본 미포함**). 콤마 구분으로 여러 개 지정 가능. 가능한 값:``- `MAJOR_BASIC` (전공기초)``- `MAJOR_REQUIRED` (전공필수)``- `MAJOR_ELECTIVE` (전공선택)``- `RETAKE` (재수강) — C+ 이하 성적의 이번 학기 개설 과목``예: `?category=MAJOR_REQUIRED`, `?category=RETAKE`, `?category=MAJOR_REQUIRED,RETAKE` |

### 예시

```http
GET /api/courses/recommend
Cookie: soongpt_auth={JWT}
```

```http
GET /api/courses/recommend?category=MAJOR_REQUIRED,MAJOR_ELECTIVE
Cookie: soongpt_auth={JWT}
```

```http
GET /api/courses/recommend?category=RETAKE
Cookie: soongpt_auth={JWT}
```

```http
GET /api/courses/recommend?category=RETAKE,MAJOR_REQUIRED
Cookie: soongpt_auth={JWT}
```

---

## Response

### 공통 구조

- 응답 본문: `result` = `{ categories: CategoryGroup[] }`
- 각 `CategoryGroup`은 하나의 전공 카테고리(전공기초·전공필수·전공선택)에 대한 추천 결과.
- 기본 카테고리 순서: **전공기초 → 전공필수 → 전공선택**
  (단, `category` 쿼리로 필터링하면 해당 카테고리들만 순서대로 포함)

```json
{
  "timestamp": "...",
  "result": {
    "categories": [ /* CategoryGroup[] */ ]
  }
}
```

---

### CategoryGroup

| Name            | Type                | Nullable | Description                                                                                                                                                                                |
| --------------- | ------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `category`    | string              | No       | 카테고리 키.`MAJOR_BASIC`, `MAJOR_REQUIRED`, `MAJOR_ELECTIVE`, `RETAKE` 중 하나.                                                                                                   |
| `progress`    | Progress            | Yes      | 졸업사정표 기준 학점 이수 현황.`RETAKE`에서는 `null`.                                                                                                                                  |
| `courses`     | RecommendedCourse[] | No       | 추천 과목 목록 (미이수 + 이번 학기 개설).                                                                                                                                                  |
| `gradeGroups` | GradeGroup[]        | Yes      | 학년별 그룹핑 (**전공선택에만** 사용, 전공기초/전공필수/RETAKE는 `null`). 프론트에서 `courses`의 `targetGrades`로도 그룹핑 가능하지만, 백엔드에서 미리 그룹핑해주는 편의 필드. |
| `message`     | string              | Yes      | 안내 문구 (이수 완료 또는 추천 과목이 없을 때).                                                                                                                                            |

#### Progress

`graduationSummary`의 각 필드(예: `majorFoundation`, `majorRequired`, `majorElective`)를 그대로 투영한다.

```json
"progress": {
  "required": 15.0,
  "completed": 9.0,
  "satisfied": false
}
```

| Name          | Type    | Nullable | Description                                              |
| ------------- | ------- | -------- | -------------------------------------------------------- |
| `required`  | number  | Yes      | 졸업 요건 학점 (`graduationSummary.*.required`)        |
| `completed` | number  | Yes      | 지금까지 이수한 학점 (`graduationSummary.*.completed`) |
| `satisfied` | boolean | No       | 요건 충족 여부 (`graduationSummary.*.satisfied`)       |

---

### RecommendedCourse

**분반(baseCode) 기준으로 그룹핑된 추천 과목 단위**이다. 썸네일 카드 표시용 정보를 포함한다.

```json
{
  "baseCourseCode": 2150545500,
  "courseName": "컴퓨터구조",
  "credits": 3.0,
  "targetGrades": [2],
  "timing": "LATE",
  "professors": ["홍길동", "김철수"],
  "department": "소프트웨어학부",
  "sections": [ /* Section[] */ ]
}
```

| Name               | Type      | Nullable | Description                                                                                                                                                            |
| ------------------ | --------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `baseCourseCode` | long      | No       | 과목 기본 코드 (분반 공통, 하위 2자리 제거).                                                                                                                           |
| `courseName`     | string    | No       | 과목명.                                                                                                                                                                |
| `credits`        | number    | Yes      | 학점.                                                                                                                                                                  |
| `targetGrades`   | int[]     | No       | 대상 학년 목록 (예:`[2]`, `[3,4]`). Target의 `grade1~5`에서 추출.                                                                                                |
| `timing`         | string    | Yes      | `"ON_TIME"` (현재 학년 대상) 또는 `"LATE"` (지연 이수). `targetGrades`에 `basicInfo.grade`가 포함되면 `ON_TIME`, 아니면 `LATE`. `RETAKE`에서는 `null`. |
| `professors`     | string[]  | No       | 모든 분반의 교수명을 중복 제거한 리스트. 썸네일 카드 표시용.                                                                                                           |
| `department`     | string    | Yes      | 개설 학과명. 전공과목(전기/전필/전선)일 경우에만 포함, 교양과목은 `null`.                                                                                            |
| `sections`       | Section[] | No       | 분반 목록. 분반 모달 표시용.                                                                                                                                           |

---

### Section

**실제 수강 신청 단위(분반)** 정보. 분반 모달 표시용.

```json
{
  "courseCode": 2150545501,
  "professor": "홍길동",
  "division": "01분반",
  "schedule": "화09:00-10:15, 수09:00-10:15",
  "isStrictRestriction": false
}
```

| Name                    | Type    | Nullable | Description                                                                                                            |
| ----------------------- | ------- | -------- | ---------------------------------------------------------------------------------------------------------------------- |
| `courseCode`          | long    | No       | 분반별 과목 코드.                                                                                                      |
| `professor`           | string  | Yes      | 담당 교수.                                                                                                             |
| `division`            | string  | Yes      | 분반명 (예:`"01분반"`). 분반 모달 표시용.                                                                            |
| `schedule`            | string  | No       | 가공된 시간/요일 (예:`"화09:00-10:15, 수09:00-10:15"`). `CourseTime` 기반으로 파싱한 결과.                         |
| `isStrictRestriction` | boolean | No       | 엄격한 수강 제한 여부.`Target.isStrict` 값. **반드시 포함되어야 하며**, `true`면 분반 모달에 제한 태그 표시. |

> **참고**: 레거시 호환을 위해 `Course` 엔티티의 추가 필드(`category`, `subCategory`, `field`, `department`, `division`, `time`, `point`, `personeel`, `scheduleRoom`, `target` 등)가 포함될 수 있으나, 명세서에서는 핵심 필드만 명시한다.

---

### GradeGroup (전공선택 전용)

전공선택(`MAJOR_ELECTIVE`) 카테고리에서만 사용된다. 프론트에서 `courses`의 `targetGrades`로도 그룹핑 가능하지만, 백엔드에서 미리 그룹핑해주는 편의 필드.

```json
{
  "grade": 3,
  "courses": [ /* RecommendedCourse[] */ ]
}
```

| Name        | Type                | Nullable | Description                                          |
| ----------- | ------------------- | -------- | ---------------------------------------------------- |
| `grade`   | int                 | No       | 대상 학년.`targetGrades`의 최대값 기준으로 그룹핑. |
| `courses` | RecommendedCourse[] | No       | 해당 학년 대상 과목 목록.                            |

---

## 예시 응답

### 1. 정상 응답 (모든 카테고리에 과목 있음)

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "categories": [
      {
        "category": "MAJOR_BASIC",
        "progress": {
          "required": 15.0,
          "completed": 9.0,
          "satisfied": false
        },
        "courses": [
          {
            "baseCourseCode": 2150545500,
            "courseName": "컴퓨터구조",
            "credits": 3.0,
            "targetGrades": [2],
            "timing": "LATE",
            "professors": ["홍길동", "김철수"],
            "department": "소프트웨어학부",
            "sections": [
              {
                "courseCode": 2150545501,
                "professor": "홍길동",
                "schedule": "화09:00-10:15, 수09:00-10:15",
                "isStrictRestriction": false
              },
              {
                "courseCode": 2150545502,
                "professor": "김철수",
                "schedule": "목13:30-14:45, 금13:30-14:45",
                "isStrictRestriction": true
              }
            ]
          }
        ],
        "gradeGroups": null,
        "message": null
      },
      {
        "category": "MAJOR_REQUIRED",
        "progress": {
          "required": 21.0,
          "completed": 21.0,
          "satisfied": true
        },
        "courses": [],
        "gradeGroups": null,
        "message": "전공필수 학점을 이미 모두 이수하셨습니다."
      },
      {
        "category": "MAJOR_ELECTIVE",
        "progress": {
          "required": 30.0,
          "completed": 12.0,
          "satisfied": false
        },
        "courses": [
          {
            "baseCourseCode": 2150546700,
            "courseName": "인공지능",
            "credits": 3.0,
            "targetGrades": [3, 4],
            "timing": "ON_TIME",
            "professors": ["박지훈"],
            "department": "소프트웨어학부",
            "sections": [
              {
                "courseCode": 2150546701,
                "professor": "박지훈",
                "schedule": "월13:30-14:45, 수13:30-14:45",
                "isStrictRestriction": false
              }
            ]
          }
        ],
        "gradeGroups": [
          {
            "grade": 3,
            "courses": [
              {
                "baseCourseCode": 2150546700,
                "courseName": "인공지능",
                "credits": 3.0,
                "targetGrades": [3, 4],
                "timing": "ON_TIME",
                "sections": [ /* Section[] */ ]
              }
            ]
          }
        ],
        "message": null
      }
    ]
  }
}
```

### 2. RETAKE 카테고리 응답

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "categories": [
      {
        "category": "RETAKE",
        "progress": null,
        "courses": [
          {
            "baseCourseCode": 2150545500,
            "courseName": "컴퓨터구조",
            "credits": 3.0,
            "targetGrades": [2],
            "timing": null,
            "professors": ["홍길동", "김철수"],
            "department": "소프트웨어학부",
            "sections": [
              {
                "courseCode": 2150545501,
                "professor": "홍길동",
                "division": "01분반",
                "schedule": "화09:00-10:15, 수09:00-10:15",
                "isStrictRestriction": false
              }
            ]
          }
        ],
        "gradeGroups": null,
        "message": null
      }
    ]
  }
}
```

### 3. 이번 학기 개설 과목 없음

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "categories": [
      {
        "category": "MAJOR_BASIC",
        "progress": {
          "required": 15.0,
          "completed": 6.0,
          "satisfied": false
        },
        "courses": [],
        "gradeGroups": null,
        "message": "이번 학기에 수강 가능한 전공기초 과목이 없습니다."
      }
    ]
  }
}
```

---

## 에러 응답

### 401 Unauthorized – 쿠키 없음 / JWT 만료 (재인증 필요)

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": null,
  "error": {
    "message": "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요."
  }
}
```

### 409 Conflict – 동기화 미완료

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": null,
  "error": {
    "message": "유세인트 데이터 동기화가 아직 완료되지 않았습니다."
  }
}
```

---

## 비즈니스 로직 요약

```
soongpt_auth 쿠키 → JWT에서 pseudonym 추출
    ↓
SyncSessionStore.getUsaintData(pseudonym) 조회
    ├─ graduationSummary.majorFoundation / majorRequired / majorElective
    │    → 카테고리별 Progress(required, completed, satisfied)
    ├─ takenCourses[].subjectCodes
    │    → 모든 subjectCodes를 flatMap 후 distinct()로 중복 제거
    └─ basicInfo.department / grade
         → 주전공 학과 / 현재 학년
    ↓
카테고리 목록 결정 (전기/전필/전선)
    - 쿼리파라미터 category가 있으면 해당 카테고리만 (RETAKE 제외)
    - 없으면 [전기, 전필, 전선] 전체 (RETAKE 미포함)
    ↓
각 카테고리에 대해:
    ├─ graduationSummary.*.satisfied == true
    │    → courses = [], message = "이미 모두 이수하셨습니다." (카테고리별 문구)
    └─ graduationSummary.*.satisfied == false
         ├─ DB에서 Course + Target JOIN (해당 카테고리, 주전공, 학년 범위)
         │    - Allow 과목만 조회 (isDenied=false, studentType=GENERAL)
         │    - 각 분반의 isStrict 값을 Section에 반드시 표시 (isStrictRestriction)
         ├─ takenSubjectCodes(distinct)로 미이수 과목 필터링
         ├─ CourseTime으로 schedule 파싱, scheduleRoom 레거시 값 보존
         ├─ baseCode 단위로 분반 그룹핑 → RecommendedCourse
         │    - targetGrades는 Target의 grade1~5에서 추출
         │    - timing은 targetGrades에 basicInfo.grade 포함 여부로 판단
         │    - professors는 모든 분반의 professor를 수집 후 distinct()로 중복 제거
         │    - department는 전공과목(전기/전필/전선)일 경우 Course.department, 교양은 null
         │    - 각 Section에 isStrictRestriction은 해당 분반의 Target.isStrict 값 (반드시 포함)
         └─ 전공선택일 경우 GradeGroup(학년별) 구성
            - targetGrades의 최대값 기준으로 그룹핑
    ↓
RETAKE 처리 (category에 RETAKE 포함 시):
    ├─ lowGradeSubjectCodes가 비어있으면
    │    → courses = [], message = "재수강 가능한 C+ 이하 과목이 없습니다."
    └─ lowGradeSubjectCodes가 있으면
         ├─ DB에서 Course + Target JOIN (baseCode IN lowGradeSubjectCodes)
         │    - Allow 과목만 조회 (isDenied=false, studentType=GENERAL)
         ├─ 매칭 과목 없으면 → message = "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
         └─ baseCode 단위로 분반 그룹핑 → RecommendedCourse
              - timing = null (RETAKE는 timing 미표시)
              - progress = null (RETAKE는 졸업 요건 진행률 미표시)
              - courseName 기준 정렬
    ↓
CategoryGroup[] 응답
```

---

## 엔티티 매핑

### CategoryGroup

- `category`: `Category` enum의 `name` 값 (`MAJOR_BASIC`, `MAJOR_REQUIRED`, `MAJOR_ELECTIVE`) 또는 `"RETAKE"`
- `progress`: `RusaintGraduationSummaryDto.majorFoundation/majorRequired/majorElective` 그대로. `RETAKE`에서는 `null`.

### RecommendedCourse

- `baseCourseCode`: `Course.code`의 하위 2자리 제거
- `courseName`: `Course.name`
- `credits`: `Course.credit`
- `targetGrades`: `Target.grade1~5`에서 `true`인 학년들을 `List<Int>`로 변환
- `timing`: `targetGrades`에 `basicInfo.grade` 포함 여부로 계산. `RETAKE`에서는 `null`.
- `professors`: 모든 분반의 `Course.professor`를 수집 후 `distinct()`로 중복 제거
- `department`: 전공과목(`MAJOR_BASIC`, `MAJOR_REQUIRED`, `MAJOR_ELECTIVE`)일 경우 `Course.department`, 교양은 `null`. `RETAKE`에서는 항상 포함.

### Section

- `courseCode`: `Course.code`
- `professor`: `Course.professor`
- `division`: `Course.division` (분반명)
- `schedule`: `Course.scheduleRoom`을 `CourseTimes`로 파싱 후 가공
- `isStrictRestriction`: 해당 분반의 `Target.isStrict` 값 (분반별로 다를 수 있음)
- 레거시 호환을 위해 `Course` 엔티티의 추가 필드가 포함될 수 있음
