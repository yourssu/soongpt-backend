# 재수강 가능 과목 조회 (GET /api/usaint/retake)

## 개요

- **목적**: 사용자가 이번 학기에 재수강할 수 있는 과목을 조회한다.
- **전제**: 클라이언트는 [SSO 콜백](../sso/sso_callback.md)을 통해 인증 후, `soongpt_auth` 쿠키(JWT)를 발급받는다. JWT claim에 pseudonym이 포함되어 있으며, 서버가 쿠키에서 자동 추출한다.
- **식별자 보안**: pseudonym은 HttpOnly 쿠키 내 JWT에만 존재하므로, 클라이언트가 직접 다루지 않는다 (XSS·쿼리 노출·로그·레퍼러 유출 방지).

---

## Request

### Headers / Cookie

| Name            | Type   | Required | Description                      |
|-----------------|--------|----------|----------------------------------|
| `soongpt_auth`  | cookie | Yes      | SSO 콜백에서 발급된 JWT 쿠키 (HttpOnly) |

### 예시

```
GET /api/usaint/retake
Cookie: soongpt_auth={JWT}
```

---

## Response

### 공통 구조

- **status 필드 없음**: 프론트 분기 최소화. `courses`가 비어 있으면 `message`만 보고 안내하면 되고, 있으면 목록만 표시하면 된다.
- 응답 본문: `result` = `{ courses: CourseItem[], message?: string }`
  - `courses`: 재수강 가능 과목 목록(분반 단위, 기본 course API와 동일 스키마 + `pastGradeBand`).
  - `message`: `courses`가 비었을 때만 존재. 사유 문구(백엔드가 두 Edge case별로 적절한 문구 설정). 프론트는 빈 배열이면 `message`를 그대로 표시하면 됨.

| Name      | Type        | Description |
|-----------|-------------|-------------|
| `courses` | CourseItem[] | 재수강 가능 과목(분반별 1건). 기본 course API와 동일 필드 + `pastGradeBand`. |
| `message` | string?     | `courses`가 비었을 때만 포함. 표시할 안내 문구. |

### Output (재수강 가능 과목 정보)

- 재수강 가능한 **이번 학기 개설 과목**만 포함.
- **과목 정보(시간, 교수)** 는 **분반(division)** 단위로, 기본 API와 동일한 한 건씩 나열(평면 배열).
- 각 항목은 [GET /api/courses/major/elective](../course/get_major_elective_courses.md) 응답 한 건과 **동일 스키마**를 재사용하고, 재수강용으로 `pastGradeBand`만 추가.

#### CourseItem (기본 course API와 동일 + pastGradeBand)

| Name           | Type    | Nullable | Description |
|----------------|---------|----------|-------------|
| `category`     | string  | No       | Course category |
| `subCategory`  | string  | Yes      | Course sub category |
| `field`        | string  | Yes      | Curriculum field by admission year |
| `code`         | integer | No       | Unique course code identifier |
| `name`         | string  | No       | Course name |
| `professor`    | string  | Yes      | Name of the professor in charge |
| `department`   | string  | No       | Department (학과) |
| `division`     | string  | Yes      | Course division (분반) |
| `time`         | string  | No       | Course time information |
| `point`        | string  | No       | Course point information |
| `personeel`    | integer | No       | Personnel information |
| `scheduleRoom` | string  | No       | Schedule and room information |
| `target`       | string  | No       | Target students (과목 학년/타겟, 전체일 수 있음) |
| `pastGradeBand`| string  | No       | 재수강용. `"passLow"`(C/D) \| `"fail"`(F) |

- **과목 학년(타겟)**: `target` 필드로 제공(전체 포함).
- **대상외수강제한여부**: 기본 course API에 동일 필드가 있으면 그대로 사용; 없으면 추후 확장.

---

## Edge Case 응답

모든 경우 `result`는 `{ courses, message? }` 형태로 통일. **status 없음**. 프론트: `result.courses.length === 0`이면 `result.message` 표시 후 재수강 블록 비표시, 아니면 `result.courses` 목록 표시.

### 1. C 이하 과목 없음 (No low grades)

- **의미**: 사용자에게 C 이하로 이수한 과목이 없음.
- **UI**: 재수강 과목 모음 블록 비표시, `message` 내용으로 안내.

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "courses": [],
    "message": "재수강 가능한 C 이하 과목이 없습니다."
  }
}
```

---

### 2. C 이하는 있으나 이번 학기 개설 없음 (No retake offered)

- **의미**: C 이하 과목은 있으나, 동일/동일대체 과목 중 이번 학기에 개설·수강 대상에 맞는 과목이 없음.
- **UI**: 재수강 과목 모음 블록 비표시, `message` 내용으로 안내.

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "courses": [],
    "message": "C 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
  }
}
```

---

### 3. 재수강 가능 과목 있음 (Has retake)

- **의미**: 재수강 가능한 이번 학기 개설 과목이 1개 이상 있음.
- **UI**: 재수강 과목 모음 블록 표시. `courses`는 기본 course API와 동일 스키마(분반별 1건) + `pastGradeBand`만 추가.

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "courses": [
      {
        "category": "전필-컴퓨터",
        "subCategory": null,
        "field": null,
        "code": 2150545501,
        "name": "데이터구조",
        "professor": "홍길동",
        "department": "컴퓨터학부",
        "division": "01분반",
        "time": "3.0",
        "point": "3",
        "personeel": 40,
        "scheduleRoom": "화 09:00-10:15 (정보과학관 21001)\n수 09:00-10:15 (정보과학관 21001)",
        "target": "컴퓨터학부 2학년",
        "pastGradeBand": "fail"
      },
      {
        "category": "전필-컴퓨터",
        "subCategory": null,
        "field": null,
        "code": 2150545502,
        "name": "데이터구조",
        "professor": "김철수",
        "department": "컴퓨터학부",
        "division": "02분반",
        "time": "3.0",
        "point": "3",
        "personeel": 40,
        "scheduleRoom": "목 13:30-14:45 (정보과학관 21002)\n금 13:30-14:45 (정보과학관 21002)",
        "target": "컴퓨터학부 2학년",
        "pastGradeBand": "fail"
      },
      {
        "category": "전선-컴퓨터",
        "subCategory": null,
        "field": null,
        "code": 2150545601,
        "name": "알고리즘",
        "professor": "이영희",
        "department": "컴퓨터학부",
        "division": "01분반",
        "time": "3.0",
        "point": "3",
        "personeel": 35,
        "scheduleRoom": "월 10:30-11:45 (정보과학관 21003)\n수 10:30-11:45 (정보과학관 21003)",
        "target": "컴퓨터학부 전체",
        "pastGradeBand": "passLow"
      }
    ]
  }
}
```

- `message`는 비었을 때만 포함하므로, 재수강이 있을 땐 생략 가능.

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

- 클라이언트: SSO 재로그인으로 유도 후, 새 쿠키 발급 받아 재요청.
