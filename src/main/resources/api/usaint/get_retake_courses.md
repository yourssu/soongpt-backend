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

- 응답 본문: `result` = `{ status, courses: RetakeCourseGroup[], message? }`
  - **status**: 케이스 구분용. 프론트는 `status`로 분기하면 됨.
  - **courses**: **과목별로 묶은 배열**. 각 요소는 한 과목 + 그 과목의 분반 배열(`divisions`). object key가 아닌 **array**.
  - **message**: `status`가 비어 있는 케이스일 때만 포함. 표시할 안내 문구.

| Name      | Type                 | Description |
|-----------|----------------------|-------------|
| `status`  | string                | `"NO_LOW_GRADES"` \| `"NO_RETAKE_OFFERED"` \| `"HAS_RETAKE"`. 프론트 분기용. |
| `courses` | RetakeCourseGroup[]   | 재수강 가능 과목을 **과목별로 묶은 배열**. 각 항목은 공통 정보 + `divisions[]`. |
| `message` | string?               | 비어 있는 케이스일 때만 포함. 표시할 안내 문구. |

### 필수 표시 정보 (UI에서 반드시 노출할 값)

다음 정보는 **반드시** 표시해야 한다.

1. **과목 정보(학점, 교수)** — 과목 공통은 `point`/`time` 등, 교수·시간표는 **분반(`divisions[]`) 안**에서 표시.
2. **과목정보를 분반으로 묶어서** — `courses` 배열 한 요소 = 한 과목, 그 안의 `divisions` 배열 = 분반별 상세(학점, 교수, 강의실 등).
3. **과목 학년(타겟)** — `target` (예: "3학년 글로벌미디어", "전체" 등)
4. **대상외수강제한여부** — `isStrictRestriction` (boolean). 구현 시 Target의 `isStrict` 값을 사용함(과목별 Target 조회 API의 `TargetInfo.isStrict`와 동일 의미).
5. **개설학과** — `department`

### Output (재수강 가능 과목 정보)

- 재수강 가능한 **이번 학기 개설 과목**만 포함.
- **분반별로 묶어서** 보낸다. `courses`는 **array**이며, 각 요소(RetakeCourseGroup)는 **한 과목**에 대한 공통 정보 + 그 과목의 **분반 목록(`divisions`)**.
- object key로 과목을 구분하지 않고, **과목당 한 요소 + 내부에 `divisions` 배열**로 분반을 나열한다.

#### RetakeCourseGroup (과목 한 건 = 공통 정보 + 분반 배열)

| Name                  | Type            | Nullable | Description |
|-----------------------|-----------------|----------|-------------|
| `category`            | string          | No       | 과목 구분 (예: `"MAJOR_ELECTIVE"` 또는 한글 "전선-컴퓨터") |
| `subCategory`         | string          | Yes      | Course sub category |
| `field`               | string          | Yes      | Curriculum field by admission year |
| `name`                | string          | No       | 과목명 |
| `department`          | string          | No       | 개설학과 |
| `target`              | string          | No       | 수강 대상 학년/대상 (전체일 수 있음) |
| `isStrictRestriction` | boolean         | No       | 대상외수강제한 여부. (Target.isStrict와 동일) |
| `divisions`           | DivisionItem[]  | No       | **분반별 상세 배열**. 학점·교수·시간·강의실 등은 여기. |

#### DivisionItem (분반 한 건)

| Name           | Type    | Nullable | Description |
|----------------|---------|----------|-------------|
| `code`         | number  | No       | 분반별 고유 과목 코드 (integer) |
| `division`     | string  | Yes      | 분반 (예: "01분반") |
| `professor`    | string  | Yes      | 담당 교수명 |
| `time`         | string  | No       | 수업 시간 정보 (예: "3.0") |
| `point`        | string  | No       | 학점 (예: "3.0") |
| `personeel`    | integer | No       | 인원 정보 |
| `scheduleRoom` | string  | No       | 요일·시간·강의실 (예: "월 10:30-11:45 (정보과학관 21601-)") |

- **과목 학년(타겟)**: 그룹의 `target` 필드로 제공. "전체", "3학년 글로벌미디어" 등.
- **대상외수강제한여부**: 그룹의 `isStrictRestriction` 필드 사용. 다른 코드(과목별 Target 조회 등)에서는 Target 엔티티/`TargetInfo.isStrict`를 사용함.

---

## Edge Case 응답

모든 경우 `result`는 `{ status, courses, message? }` 형태로 통일. 프론트: `result.status === "HAS_RETAKE"`이면 목록 표시, 아니면 재수강 블록 비표시 후 `result.message` 표시.

### 1. C 이하 과목 없음 (No low grades)

- **의미**: 사용자에게 C 이하로 이수한 과목이 없음.
- **status**: `"NO_LOW_GRADES"`
- **UI**: 재수강 과목 모음 블록 비표시, `message` 내용으로 안내.

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "NO_LOW_GRADES",
    "courses": [],
    "message": "재수강 가능한 C 이하 과목이 없습니다."
  }
}
```

---

### 2. C 이하는 있으나 이번 학기 개설 없음 (No retake offered)

- **의미**: C 이하 과목은 있으나, 동일/동일대체 과목 중 이번 학기에 개설·수강 대상에 맞는 과목이 없음.
- **status**: `"NO_RETAKE_OFFERED"`
- **UI**: 재수강 과목 모음 블록 비표시, `message` 내용으로 안내.

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "NO_RETAKE_OFFERED",
    "courses": [],
    "message": "C 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
  }
}
```

---

### 3. 재수강 가능 과목 있음 (Has retake)

- **의미**: 재수강 가능한 이번 학기 개설 과목이 1개 이상 있음.
- **status**: `"HAS_RETAKE"`
- **UI**: 재수강 과목 모음 블록 표시. `courses`는 **과목별로 묶은 배열**(각 항목에 `divisions[]`).

**200 OK**

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "HAS_RETAKE",
    "courses": [
      {
        "category": "MAJOR_REQUIRED",
        "subCategory": null,
        "field": null,
        "name": "데이터구조",
        "department": "컴퓨터학부",
        "target": "컴퓨터학부 2학년",
        "isStrictRestriction": true,
        "divisions": [
          {
            "code": 2150545501,
            "division": "01분반",
            "professor": "홍길동",
            "time": "3.0",
            "point": "3",
            "personeel": 40,
            "scheduleRoom": "화 09:00-10:15 (정보과학관 21001)\n수 09:00-10:15 (정보과학관 21001)"
          },
          {
            "code": 2150545502,
            "division": "02분반",
            "professor": "김철수",
            "time": "3.0",
            "point": "3",
            "personeel": 40,
            "scheduleRoom": "목 13:30-14:45 (정보과학관 21002)\n금 13:30-14:45 (정보과학관 21002)"
          }
        ]
      },
      {
        "category": "MAJOR_ELECTIVE",
        "subCategory": null,
        "field": null,
        "name": "알고리즘",
        "department": "컴퓨터학부",
        "target": "컴퓨터학부 전체",
        "isStrictRestriction": false,
        "divisions": [
          {
            "code": 2150545601,
            "division": "01분반",
            "professor": "이영희",
            "time": "3.0",
            "point": "3",
            "personeel": 35,
            "scheduleRoom": "월 10:30-11:45 (정보과학관 21003)\n수 10:30-11:45 (정보과학관 21003)"
          }
        ]
      }
    ]
  }
}
```

- `category`는 enum 이름(예: `"MAJOR_ELECTIVE"`) 또는 한글 표기로 제공될 수 있음. 레거시 호환 시 한글 유지 가능.
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
