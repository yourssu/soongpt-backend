# 전공 과목 추천 조회 (GET /api/courses/recommend)

## 개요

- **목적**: SSO 인증된 사용자의 학적정보(학과·학년·수강이력·졸업사정표)를 기반으로, 이번 학기에 수강해야 할 전공 과목을 카테고리별로 추천한다.
- **전제**: 클라이언트는 [SSO 콜백](../sso/sso_callback.md)을 통해 인증 후, `soongpt_auth` 쿠키(JWT)를 발급받는다. [동기화 상태 조회](../sso/sync_status.md)에서 `COMPLETED`를 받은 뒤 호출한다.
- **데이터 소스**:
  - **캐시된 rusaint 데이터**: 졸업사정표(`graduationSummary`) → 카테고리별 이수/필요 학점
  - **캐시된 rusaint 데이터**: 수강이력(`takenCourses`) → 미이수 과목 필터링
  - **DB**: 이번 학기 개설 과목 + Target(수강 대상/대상외수강제한) 정보

---

## Request

### Headers / Cookie

| Name            | Type   | Required | Description                      |
|-----------------|--------|----------|----------------------------------|
| `soongpt_auth`  | cookie | Yes      | SSO 콜백에서 발급된 JWT 쿠키 (HttpOnly) |

### 예시

```
GET /api/courses/recommend
Cookie: soongpt_auth={JWT}
```

---

## Response

### 공통 구조

- 응답 본문: `result` = `{ categories: CategoryGroup[] }`
- 각 `CategoryGroup`은 하나의 전공 카테고리(전기·전필·전선)에 대한 추천 결과.
- 카테고리 순서: 전공기초 → 전공필수 → 전공선택 (고정).

```json
{
  "timestamp": "...",
  "result": {
    "categories": [ CategoryGroup, ... ]
  }
}
```

---

### CategoryGroup

| Name          | Type                       | Nullable | Description                                        |
|---------------|----------------------------|----------|----------------------------------------------------|
| `category`    | string                     | No       | 카테고리명 (`"전공기초"`, `"전공필수"`, `"전공선택"`) |
| `progress`    | string                     | Yes      | 학점 이수 현황 (예: `"15학점 중 9학점 이수"`)       |
| `satisfied`   | boolean                    | No       | 졸업 요건 충족 여부                                |
| `courses`     | RecommendedCourse[]        | No       | 추천 과목 목록 (미이수 + 이번 학기 개설)            |
| `gradeGroups` | GradeGroup[]               | Yes      | 학년별 그룹핑 (전공선택에만 포함)                   |
| `message`     | string                     | Yes      | 안내 문구 (satisfied 또는 빈 목록일 때)            |

- `progress`: rusaint 졸업사정표의 `required`/`completed` 값으로 생성. 예: `"15학점 중 9학점 이수"`.
- `satisfied`가 `true`이면 `courses`는 빈 배열, `message`에 안내 문구 포함.
- `gradeGroups`는 전공선택(`MAJOR_ELECTIVE`)에만 포함. 대상 학년 기준으로 과목을 그룹핑.

---

### RecommendedCourse

| Name             | Type           | Nullable | Description                                           |
|------------------|----------------|----------|-------------------------------------------------------|
| `baseCourseCode` | long           | No       | 과목 기본 코드 (분반 공통, 하위 2자리 제거)           |
| `courseName`     | string         | No       | 과목명                                                |
| `credits`        | double         | Yes      | 학점                                                  |
| `targetGrades`   | int[]          | No       | 대상 학년 목록 (예: `[2, 3]`)                         |
| `timing`         | string         | No       | `"ON_TIME"` (현재 학년 대상) \| `"LATE"` (지연 이수)  |
| `isDenied`       | boolean        | No       | 대상외수강제한 여부. `true`면 수강신청 불가능할 수 있음 |
| `sections`       | Section[]      | No       | 분반 목록                                             |

- **`timing`**: 사용자 학년이 `targetGrades`에 포함되면 `ON_TIME`, 아니면 `LATE`.
- **`isDenied`**: Target 테이블의 `isDenied` 값. `true`인 과목은 해당 학과/학년 학생이 수강신청할 수 없음. 프론트에서 제한 안내 UI를 표시하는 용도.

---

### Section

| Name         | Type   | Nullable | Description            |
|--------------|--------|----------|------------------------|
| `courseCode`  | long   | No       | 분반별 과목 코드       |
| `professor`   | string | Yes      | 담당 교수              |
| `schedule`    | string | No       | 시간/요일 (예: `"화09:00-10:15, 수09:00-10:15"`) |

---

### GradeGroup (전공선택 전용)

| Name      | Type                  | Nullable | Description                |
|-----------|-----------------------|----------|----------------------------|
| `grade`   | int                   | No       | 대상 학년                  |
| `courses` | RecommendedCourse[]   | No       | 해당 학년 대상 과목 목록   |

---

## 응답 예시

### 1. 정상 응답 (과목 있음)

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "categories": [
      {
        "category": "전공기초",
        "progress": "15학점 중 9학점 이수",
        "satisfied": false,
        "courses": [
          {
            "baseCourseCode": 2150545500,
            "courseName": "컴퓨터구조",
            "credits": 3.0,
            "targetGrades": [2],
            "timing": "LATE",
            "isDenied": false,
            "sections": [
              {
                "courseCode": 2150545501,
                "professor": "홍길동",
                "schedule": "화09:00-10:15, 수09:00-10:15"
              },
              {
                "courseCode": 2150545502,
                "professor": "김철수",
                "schedule": "목13:30-14:45, 금13:30-14:45"
              }
            ]
          },
          {
            "baseCourseCode": 2150545600,
            "courseName": "운영체제",
            "credits": 3.0,
            "targetGrades": [3],
            "timing": "ON_TIME",
            "isDenied": true,
            "sections": [
              {
                "courseCode": 2150545601,
                "professor": "이영희",
                "schedule": "월10:30-11:45, 수10:30-11:45"
              }
            ]
          }
        ],
        "gradeGroups": null,
        "message": null
      },
      {
        "category": "전공필수",
        "progress": "21학점 중 21학점 이수",
        "satisfied": true,
        "courses": [],
        "gradeGroups": null,
        "message": "전공필수 학점을 이미 모두 이수하셨습니다."
      },
      {
        "category": "전공선택",
        "progress": "30학점 중 12학점 이수",
        "satisfied": false,
        "courses": [
          {
            "baseCourseCode": 2150546700,
            "courseName": "인공지능",
            "credits": 3.0,
            "targetGrades": [3, 4],
            "timing": "ON_TIME",
            "isDenied": false,
            "sections": [
              {
                "courseCode": 2150546701,
                "professor": "박지훈",
                "schedule": "월13:30-14:45, 수13:30-14:45"
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
                "isDenied": false,
                "sections": [
                  {
                    "courseCode": 2150546701,
                    "professor": "박지훈",
                    "schedule": "월13:30-14:45, 수13:30-14:45"
                  }
                ]
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

---

### 2. 이번 학기 개설 과목 없음

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "categories": [
      {
        "category": "전공기초",
        "progress": "15학점 중 6학점 이수",
        "satisfied": false,
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

### 쿠키 없음 / JWT 만료 (재인증 필요)

**401 Unauthorized**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": null,
  "error": {
    "message": "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요."
  }
}
```

- 클라이언트: SSO 재로그인으로 유도.

### 동기화 미완료

**409 Conflict**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": null,
  "error": {
    "message": "유세인트 데이터 동기화가 아직 완료되지 않았습니다."
  }
}
```

- 클라이언트: `/sync/status` 폴링으로 돌아가서 COMPLETED 확인 후 재요청.

---

## 비즈니스 로직 요약

```
soongpt_auth 쿠키 → JWT에서 pseudonym 추출
    ↓
SyncSessionStore에서 캐시된 rusaint 데이터 조회
    ├─ graduationSummary → 카테고리별 {required, completed, satisfied}
    ├─ takenCourses → 수강 이력 (subjectCodes)
    └─ basicInfo → {department, grade}
    ↓
카테고리별 처리 (전기 → 전필 → 전선):
    ├─ satisfied == true → 이수 완료 메시지
    └─ satisfied == false:
        ├─ DB에서 개설 과목 조회 (Course + Target JOIN)
        ├─ 미이수 과목 필터링 (takenCourses 제외)
        ├─ isDenied 과목 포함 (boolean 플래그로 표시)
        ├─ timing 판단 (학년 대비 ON_TIME/LATE)
        └─ 분반 그룹핑 (baseCode 기준)
    ↓
CategoryGroup[] 응답
```
