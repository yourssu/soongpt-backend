# fetchUsaintSnapshot (POST /api/usaint/snapshot)

> **Internal API**
> 이 엔드포인트는 **WAS(Kotlin) ↔ rusaint-service(Python)** 간 통신에만 사용됩니다.
> 외부 클라이언트에는 노출되지 않습니다.
> u-saint로부터 **학적/성적 정보를 조회하여 “스냅샷 데이터”를 가져오는 역할**만 하며,
> SoongPT DB에 실제로 반영할지 여부는 상위 비즈니스 레이어에서 결정합니다.

---

## Request

### Headers

| Name              | Required | Description                                    |
| ----------------- | -------- | ---------------------------------------------- |
| `Authorization` | true     | `Bearer {internal-jwt}` (WAS 발급 내부 토큰) |
| `Content-Type`  | true     | `application/json`                           |

### Request Body

| Name          | Type   | Required | Constraint                             |
| ------------- | ------ | -------- | -------------------------------------- |
| `studentId` | string | true     | @Range(min = 20150000, max = 20259999) |
| `sToken`    | string | true     | @NotBlank                              |

WAS는 클라이언트로부터 받은 `studentId`, `sToken`을 그대로 rusaint-service에 전달합니다.
`pseudonym`은 WAS 내부에서만 사용하는 식별자이며, 외부에는 노출되지 않습니다.

---

## Reply

### Status

- **200 OK**: u-saint 데이터 스냅샷을 정상적으로 조회하여 반환합니다.

---

## Response Body

| Name                     | Type                 | Description                                                                                                     |
| ------------------------ | -------------------- | --------------------------------------------------------------------------------------------------------------- |
| `takenCourses`         | TakenCourse[]        | 학기별 수강 과목 코드 목록                                                                                      |
| `lowGradeSubjectCodes` | LowGradeSubjectCodes | C/D(통과 저성적)와 F(재수강 필요) 과목 코드 목록 (이수구분 없이 과목 코드만, 실제 성적 값은 절대 포함하지 않음) |
| `flags`                | Flags                | 복수전공 / 부전공 전공 정보 및 교직 이수 여부                                                                   |
| `availableCredits`     | AvailableCredits     | 직전 성적 및 올해 최대 신청 가능 학점 정보                                                                      |
| `basicInfo`            | BasicInfo            | 기본 학적 정보 (학년, 학기, 학과 등)                                                                            |
| `remainingCredits`     | RemainingCredits     | 졸업까지 남은 전공/교양 이수 학점 정보                                                                          |

### TakenCourse

| Name             | Type     | Required | Description                                                         |
| ---------------- | -------- | -------- | ------------------------------------------------------------------- |
| `year`         | integer  | true     | 기준 학년도 (예: 2024)                                              |
| `semester`     | string   | true     | 학기 (1: "1", 2: "2", SUMMER: "SUMMER", WINTER: "WINTER")           |
| `subjectCodes` | string[] | true     | 해당 학기 수강 과목 코드 리스트 (예:`"21505455"`, `"21500133"`) |

### Flags

| Name                      | Type    | Required | Description                         |
| ------------------------- | ------- | -------- | ----------------------------------- |
| `doubleMajorDepartment` | string  | false    | 복수전공 학과명 (없으면 `null`)   |
| `minorDepartment`       | string  | false    | 부전공 학과명 (없으면 `null`)     |
| `teaching`              | boolean | true     | 교직 이수 여부 (`true`/`false`) |

### AvailableCredits

| Name                    | Type    | Required | Description                                                |
| ----------------------- | ------- | -------- | ---------------------------------------------------------- |
| `previousGpa`         | number  | true     | 직전 학기 평점                                             |
| `carriedOverCredits`  | integer | true     | 이월 학점                                                  |
| `maxAvailableCredits` | number  | true     | 이번 학기 최대 신청 가능 학점 (소수점 가능: 19.5, 22.5 등) |

### BasicInfo

| Name           | Type    | Required | Description                                                                 |
| -------------- | ------- | -------- | --------------------------------------------------------------------------- |
| `year`       | integer | true     | 기준 연도 (예: 2025)                                                        |
| `grade`      | integer | true     | 학년 (1~4)                                                                  |
| `semester`   | integer | true     | 재학 누적 학기 (1~8)<br />예: 3학년 2학기 →`grade` = 3, `semester` = 6 |
| `department` | string  | true     | 주전공 학과명                                                               |

### RemainingCredits

| Name                | Type    | Required | Description        |
| ------------------- | ------- | -------- | ------------------ |
| `majorRequired`   | integer | true     | 남은 전공필수 학점 |
| `majorElective`   | integer | true     | 남은 전공선택 학점 |
| `generalRequired` | integer | true     | 남은 교양필수 학점 |
| `generalElective` | integer | true     | 남은 교양선택 학점 |

### LowGradeSubjectCodes

| Name        | Type                  | Required | Description                                            |
| ----------- | --------------------- | -------- | ------------------------------------------------------ |
| `passLow` | GradeBandSubjectCodes | true     | C/D 성적(통과 저성적) 과목 코드 리스트 (이수구분 제외) |
| `fail`    | GradeBandSubjectCodes | true     | F 성적(재수강 필요) 과목 코드 리스트 (이수구분 제외)   |

### Response Body 예시

```json
{
  "takenCourses": [
    {
      "year": 2024,
      "semester": "1",
      "subjectCodes": [
        "21505455",
        "21501027",
        "21504515"
      ]
    },
    {
      "year": 2024,
      "semester": "2",
      "subjectCodes": [
        "21501021",
        "21504516"
      ]
    },
    {
      "year": 2024,
      "semester": "SUMMER",
      "subjectCodes": [
        "21501006"
      ]
    }
  ],
  "lowGradeSubjectCodes": {
    "passLow": [
      "21505395"
    ],
    "fail": [
      "21501015",
      "21500785",
      "21501008"
    ]
  },
  "flags": {
    "doubleMajorDepartment": "법학과",
    "minorDepartment": null,
    "teaching": true
  },
  "availableCredits": {
    "previousGpa": 4.3,
    "carriedOverCredits": 3,
    "maxAvailableCredits": 19.5
  },
  "basicInfo": {
    "year": 2025,
    "grade": 3,
    "semester": 6,
    "department": "컴퓨터학부"
  },
  "remainingCredits": {
    "majorRequired": 12,
    "majorElective": 18,
    "generalRequired": 6,
    "generalElective": 10
  }
}
```

---

## Notes

- rusaint-service는 `studentId` + `sToken`을 사용하여 u-saint에 로그인/토큰 발급 후,
  위와 같은 형태로 u-saint 데이터를 수집하여 **동일 HTTP 응답 바디로 반환**합니다.
- WAS는 이 응답을 받아 pseudonym과 함께 DB/Redis에 저장하고,
  이후 `POST /api/timetables/usaint` 요청 시 해당 정보를 기반으로 시간표를 생성합니다.
