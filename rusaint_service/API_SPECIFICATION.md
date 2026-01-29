# Rusaint Service API 명세서

## 개요

유세인트(u-Saint) 데이터를 크롤링하여 제공하는 Python FastAPI 서비스입니다.

**Base URL**: `http://localhost:8001` (개발), `http://rusaint-service:8001` (프로덕션)

**인증**: 모든 API는 내부 JWT 인증이 필요합니다.

- Header: `Authorization: Bearer {internal-jwt-token}`

---

## 📌 API 엔드포인트

### 1. Academic API (학적/성적 이력)

**빠른 응답을 위한 학적 및 성적 데이터 조회**

```
POST /api/usaint/snapshot/academic
```

#### 요청

**Headers**

```
Authorization: Bearer internal-jwt-placeholder
Content-Type: application/json
```

**Body**

```json
{
  "studentId": "20233009",
  "sToken": "SSO_TOKEN_HERE"
}
```

| 필드      | 타입   | 필수 | 설명              |
| --------- | ------ | ---- | ----------------- |
| studentId | string | ✅   | 학번 (8자리 숫자) |
| sToken    | string | ✅   | SSO 토큰          |

#### 응답

**Status Code**: `200 OK`

**Response Time**: 약 4-5초

**Body**

```json
{
  "takenCourses": [
    {
      "year": 2024,
      "semester": "1",
      "subjectCodes": ["21000", "21001", "21002"]
    }
  ],
  "lowGradeSubjectCodes": {
    "passLow": ["21003", "21004"],
    "fail": ["21005"]
  },
  "flags": {
    "doubleMajorDepartment": "컴퓨터학부",
    "minorDepartment": null,
    "teaching": false
  },
  "availableCredits": {
    "previousGpa": 3.85,
    "carriedOverCredits": 0,
    "maxAvailableCredits": 19.5
  },
  "basicInfo": {
    "year": 2023,
    "grade": 2,
    "semester": 4,
    "department": "AI융합학부"
  }
}
```

#### 응답 스키마

##### takenCourses (학기별 수강 과목)

| 필드         | 타입     | 설명                                                |
| ------------ | -------- | --------------------------------------------------- |
| year         | int      | 기준 학년도                                         |
| semester     | string   | 학기 (`"1"`, `"2"`, `"SUMMER"`, `"WINTER"`) |
| subjectCodes | string[] | 해당 학기 수강 과목 코드 리스트                     |

##### lowGradeSubjectCodes (저성적 과목)

| 필드    | 타입     | 설명                      |
| ------- | -------- | ------------------------- |
| passLow | string[] | C/D 성적 과목 코드 리스트 |
| fail    | string[] | F 성적 과목 코드 리스트   |

##### flags (복수전공/부전공 정보)

| 필드                  | 타입    | 설명            |
| --------------------- | ------- | --------------- |
| doubleMajorDepartment | string? | 복수전공 학과명 |
| minorDepartment       | string? | 부전공 학과명   |
| teaching              | boolean | 교직 이수 여부  |

##### availableCredits (신청 가능 학점)

| 필드                | 타입  | 설명                          |
| ------------------- | ----- | ----------------------------- |
| previousGpa         | float | 직전 학기 평점                |
| carriedOverCredits  | int   | 이월 학점                     |
| maxAvailableCredits | float | 이번 학기 최대 신청 가능 학점 |

##### basicInfo (기본 학적 정보)

| 필드       | 타입   | 설명                 |
| ---------- | ------ | -------------------- |
| year       | int    | 입학 연도            |
| grade      | int    | 학년 (1-4)           |
| semester   | int    | 재학 누적 학기 (1-8) |
| department | string | 주전공 학과명        |

#### 에러 응답

**401 Unauthorized** - SSO 토큰 오류

```json
{
  "detail": "SSO token is invalid or expired"
}
```

**500 Internal Server Error** - 서버 오류

```json
{
  "detail": "Failed to fetch usaint academic data"
}
```

---

### 2. Graduation API (졸업사정표)

**졸업 요건 상세 정보 조회**

```
POST /api/usaint/snapshot/graduation
```

#### 요청

**Headers**

```
Authorization: Bearer internal-jwt-placeholder
Content-Type: application/json
```

**Body**

```json
{
  "studentId": "20233009",
  "sToken": "SSO_TOKEN_HERE"
}
```

| 필드      | 타입   | 필수 | 설명              |
| --------- | ------ | ---- | ----------------- |
| studentId | string | ✅   | 학번 (8자리 숫자) |
| sToken    | string | ✅   | SSO 토큰          |

#### 응답

**Status Code**: `200 OK`

**Response Time**: 약 5-6초

**Body**

```json
{
  "graduationRequirements": {
    "requirements": [
      {
        "name": "학부-교양필수 19",
        "requirement": 19,
        "calculation": 17.0,
        "difference": -2.0,
        "result": false,
        "category": "교양필수"
      },
      {
        "name": "학부-전공필수 60",
        "requirement": 60,
        "calculation": 63.0,
        "difference": 3.0,
        "result": true,
        "category": "전공필수"
      }
    ],
    "remainingCredits": {
      "majorRequired": 0,
      "majorElective": 6,
      "generalRequired": 2,
      "generalElective": 0
    }
  }
}
```

#### 응답 스키마

##### graduationRequirements (졸업 요건 전체)

| 필드             | 타입                        | 설명                     |
| ---------------- | --------------------------- | ------------------------ |
| requirements     | GraduationRequirementItem[] | 개별 졸업 요건 항목 배열 |
| remainingCredits | RemainingCredits            | 남은 학점 요약           |

##### GraduationRequirementItem (개별 졸업 요건)

| 필드        | 타입    | 설명                          | 예시                   |
| ----------- | ------- | ----------------------------- | ---------------------- |
| name        | string  | 졸업 요건 이름                | `"학부-교양필수 19"` |
| requirement | int?    | 기준 학점 (null 가능)         | `19`                 |
| calculation | float?  | 현재 이수 학점 (null 가능)    | `17.0`               |
| difference  | float?  | 차이 (이수-기준, 음수면 부족) | `-2.0`               |
| result      | boolean | 충족 여부                     | `false`              |
| category    | string  | 이수구분                      | `"교양필수"`         |

##### remainingCredits (남은 학점 요약)

| 필드            | 타입 | 설명               |
| --------------- | ---- | ------------------ |
| majorRequired   | int  | 남은 전공필수 학점 |
| majorElective   | int  | 남은 전공선택 학점 |
| generalRequired | int  | 남은 교양필수 학점 |
| generalElective | int  | 남은 교양선택 학점 |

#### 에러 응답

**401 Unauthorized** - SSO 토큰 오류

```json
{
  "detail": "SSO token is invalid or expired"
}
```

**500 Internal Server Error** - 서버 오류

```json
{
  "detail": "Failed to fetch usaint graduation data"
}
```
