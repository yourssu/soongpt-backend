# Rusaint Service API 명세서

## 개요

유세인트(u-Saint) 데이터를 크롤링하여 제공하는 Python FastAPI 서비스입니다.

**Base URL**: `http://localhost:8000` (로컬), `http://localhost:8001` (개발), `http://rusaint-service:8001` (프로덕션)

**인증**: 모든 API는 내부 JWT 인증이 필요합니다.

- **Header**: `Authorization: Bearer {internal-jwt-token}`
- **JWT 발급**: WAS(Kotlin)의 `InternalJwtIssuer`가 HS256으로 발급. 유효기간은 `rusaint.internal-jwt-validity-minutes`(env: `RUSAINT_INTERNAL_JWT_VALIDITY_MINUTES`, 기본 15분)로 설정. WAS와 rusaint-service는 **동일한 시크릿**(`RUSAINT_INTERNAL_JWT_SECRET` / `INTERNAL_JWT_SECRET`)으로 서명·검증합니다.
- **개발 모드**: `DEBUG=true`일 때만 `Bearer internal-jwt-placeholder` 허용 (프로덕션에서는 유효한 JWT 필수).

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
Authorization: Bearer {WAS가 발급한 내부 JWT}
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

**Timeout**: 클라이언트는 최소 **8초** 타임아웃 권장

**Body (일반 케이스)**

```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
  "takenCourses": [
    {
      "year": 2024,
      "semester": "1",
      "subjectCodes": ["21000", "21001", "21002"]
    },
    {
      "year": 2024,
      "semester": "2",
      "subjectCodes": ["21010", "21011"]
    }
  ],
  "lowGradeSubjectCodes": {
    "passLow": ["21001", "21002"],
    "fail": ["21010"]
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

**Body (빈 데이터 케이스)**

```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
  "takenCourses": [],
  "lowGradeSubjectCodes": {
    "passLow": [],
    "fail": []
  },
  "flags": {
    "doubleMajorDepartment": null,
    "minorDepartment": null,
    "teaching": false
  },
  "availableCredits": {
    "previousGpa": 0.0,
    "carriedOverCredits": 0,
    "maxAvailableCredits": 19.0
  },
  "basicInfo": {
    "year": 2024,
    "grade": 1,
    "semester": 1,
    "department": "알 수 없음"
  }
}
```

#### 응답 스키마

##### pseudonym (학번 식별자)

| 필드      | 타입   | 설명                                                                                                                                               |
| --------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| pseudonym | string | WAS와 동일한 시크릿(`PSEUDONYM_SECRET`)으로 HMAC-SHA256(studentId) → base64url 생성. **PSEUDONYM_SECRET 미설정 시 서버 기동 실패(에러).** |

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
Authorization: Bearer {WAS가 발급한 내부 JWT}
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

**Timeout**: 클라이언트는 최소 **8초** 타임아웃 권장

**Body (일반 케이스)**

```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
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
      },
      {
        "name": "학부-전공선택 24",
        "requirement": 24,
        "calculation": 18.0,
        "difference": -6.0,
        "result": false,
        "category": "전공"
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

**Body (null 값 포함 케이스)**

```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
  "graduationRequirements": {
    "requirements": [
      {
        "name": "학부-졸업논문",
        "requirement": null,
        "calculation": null,
        "difference": null,
        "result": false,
        "category": "기타"
      }
    ],
    "remainingCredits": {
      "majorRequired": 0,
      "majorElective": 0,
      "generalRequired": 0,
      "generalElective": 0
    }
  }
}
```

**Body (빈 요건 케이스)**

```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
  "graduationRequirements": {
    "requirements": [],
    "remainingCredits": {
      "majorRequired": 0,
      "majorElective": 0,
      "generalRequired": 0,
      "generalElective": 0
    }
  }
}
```

#### 응답 스키마

##### pseudonym (학번 식별자)

| 필드      | 타입   | 설명                                                                           |
| --------- | ------ | ------------------------------------------------------------------------------ |
| pseudonym | string | Academic API와 동일.**PSEUDONYM_SECRET 미설정 시 서버 기동 실패(에러).** |

##### graduationRequirements (졸업 요건 전체)

| 필드             | 타입                        | 설명                     |
| ---------------- | --------------------------- | ------------------------ |
| requirements     | GraduationRequirementItem[] | 개별 졸업 요건 항목 배열 |
| remainingCredits | RemainingCredits            | 남은 학점 요약           |

##### GraduationRequirementItem (개별 졸업 요건)

| 필드        | 타입    | 설명                                                       | 예시                   |
| ----------- | ------- | ---------------------------------------------------------- | ---------------------- |
| name        | string  | 졸업 요건 이름                                             | `"학부-교양필수 19"` |
| requirement | int?    | 기준 학점 (**null 가능**: 학점 요구사항이 없는 경우) | `19` or `null`     |
| calculation | float?  | 현재 이수 학점 (**null 가능**: 계산 불가능한 경우)   | `17.0` or `null`   |
| difference  | float?  | 차이 (이수-기준, 음수면 부족,**null 가능**)          | `-2.0` or `null`   |
| result      | boolean | 충족 여부                                                  | `false`              |
| category    | string  | 이수구분                                                   | `"교양필수"`         |

> **Note**: `requirement`, `calculation`, `difference` 필드는 `null` 값을 가질 수 있습니다.
>
> - **졸업논문**, **어학시험** 등 학점이 아닌 요건의 경우 `null`이 반환됩니다.
> - 빈 배열(`[]`)이 반환될 수도 있으므로 클라이언트에서 처리 필요합니다.

##### remainingCredits (남은 학점 요약)

| 필드            | 타입 | 설명               |
| --------------- | ---- | ------------------ |
| majorRequired   | int  | 남은 전공필수 학점 |
| majorElective   | int  | 남은 전공선택 학점 |
| generalRequired | int  | 남은 교양필수 학점 |
| generalElective | int  | 남은 교양선택 학점 |

###### 에러 응답

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

---

## ⚠️ Rate Limiting & 제약사항

### 동시 요청 제한

유세인트 서버의 동시 요청 제한을 회피하기 위해 다음을 준수하세요:

- **권장 간격**: Academic API 호출 후 **0.5초** 대기 후 Graduation API 호출
- **동시 호출**: 동일 사용자의 여러 API를 동시 호출하지 마세요
- **재시도**: 실패 시 **2초** 이상 간격을 두고 재시도

```
✅ 올바른 패턴:
Academic API → 0.5초 대기 → Graduation API

❌ 잘못된 패턴:
Academic API + Graduation API (병렬 호출)
```

### Timeout 설정

| API         | 정상 응답 시간 | 권장 Timeout | 최대 Timeout |
| ----------- | -------------- | ------------ | ------------ |
| Academic    | 4-5초          | 8초          | 10초         |
| Graduation  | 5-6초          | 8초          | 10초         |
| 전체 (조합) | 9.5-11초       | 15초         | 20초         |

**권장 Timeout 설정 (Kotlin)**:

```kotlin
private val restTemplate = restTemplateBuilder
    .rootUri(rusaintProperties.baseUrl)
    .setConnectTimeout(Duration.ofSeconds(3))
    .setReadTimeout(Duration.ofSeconds(8))  // ← 권장 8초
    .build()
```

### SSO 토큰 유효성

- SSO 토큰은 **단기 유효** (일반적으로 1-2시간)
- 토큰 만료 시 `401 Unauthorized` 반환
- 클라이언트는 토큰 갱신 후 재시도 필요
