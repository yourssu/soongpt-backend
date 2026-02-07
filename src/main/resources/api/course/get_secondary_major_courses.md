# getSecondaryMajorCourses (GET /api/courses/secondary-major/recommend)

## Request

### Query Parameters

| Name | Type | Required | Constraint |
|------|------|----------|------------|
| `department` | string | true | @NotBlank |
| `grade` | integer | true | @Range(min = 1, max = 5) |
| `trackType` | string | true | `DOUBLE_MAJOR` \| `MINOR` \| `CROSS_MAJOR` (한글 별칭 지원) |
| `completionType` | string | true | `REQUIRED` \| `ELECTIVE` \| `RECOGNIZED` (복필/복선/부필/부선/타전공인정과목 지원) |
| `takenSubjectCodes` | string[] | false | 기이수 과목 코드 |
| `progress` | string | false | 이수 현황 문자열 |
| `satisfied` | boolean | false | 이미 이수 완료 여부 |

## Reply

### Response Body

| Name | Type | Nullable | Description |
|------|------|----------|-------------|
| `trackType` | string | No | 다전공 유형(복수전공/부전공/타전공인정) |
| `completionType` | string | No | 이수구분(필수/선택/타전공인정) |
| `classification` | string | No | 실제 분류 라벨(복필/복선/부필/부선/타전공인정과목) |
| `progress` | string | Yes | 이수 현황 |
| `satisfied` | boolean | No | 이수 완료 여부 |
| `courses` | RecommendedCourseResponse[] | No | 추천 과목(분반 묶음) |
| `gradeGroups` | GradeGroupResponse[] | Yes | 학년 그룹(복선/부선) |
| `message` | string | Yes | 안내 메시지 |

### 200 OK

```json
{
  "timestamp": "2026-02-07 13:00:00",
  "result": {
    "trackType": "복수전공",
    "completionType": "선택",
    "classification": "복선",
    "progress": "9/39",
    "satisfied": false,
    "courses": [
      {
        "baseCourseCode": 2150123400,
        "courseName": "데이터구조",
        "credits": 3.0,
        "targetGrades": [2, 3],
        "timing": "ON_TIME",
        "sections": [
          {
            "courseCode": 2150123401,
            "professor": "홍길동",
            "schedule": "화10:30-11:45, 목10:30-11:45"
          }
        ]
      }
    ],
    "gradeGroups": [
      {
        "grade": 2,
        "courses": []
      }
    ]
  }
}
```
