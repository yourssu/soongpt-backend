# 수강 가능한 채플 과목 목록 조회

## 개요

특정 시간표 기준으로 **수강 가능한 채플 과목**과 **채플 이수 여부(progress)** 를 반환합니다.

## Endpoint

```
GET /api/timetables/{id}/available-chapels
```

## 인증

- `soongpt_auth` 쿠키 (JWT) 필수

## Request

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long (path) | 시간표 ID |

## Response

```
Response<AvailableChapelsResponse>
```

### AvailableChapelsResponse

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `progress` | ChapelProgress | **X** | 채플 이수 진행 상황. **항상 존재.** rusaint/졸업사정 없으면 `satisfied: false` |
| `courses` | TimetableCourseResponse[] | X | 수강 가능한 채플 과목 목록 |

### ChapelProgress (이수현황)

채플은 **충족 여부만** 있으므로 필드는 하나입니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `satisfied` | Boolean | 채플 이수 충족 여부. rusaint/졸업사정 없으면 `false` |

**progress는 null이 되지 않습니다.** rusaint/졸업사정표가 없거나 판단 불가일 때는
`progress = { satisfied: false }` 로 내려갑니다.

### 프론트 해석

- `progress.satisfied === true` → 이미 채플 이수 충족. `courses`는 빈 배열일 수 있음.
- `progress.satisfied === false` → 미충족 또는 판단 불가. `courses`에 수강 가능 채플 목록 표시.

자세한 progress 공통 규약: [progress 프론트 가이드](../requirements/progress_프론트_가이드.md)

## 응답 예시

```json
{
  "timestamp": "2025-03-01 12:00:00",
  "result": {
    "progress": { "satisfied": false },
    "courses": [
      { "courseCode": 12345678, "courseName": "채플", "…" }
    ]
  }
}
```

이미 충족 시:

```json
{
  "result": {
    "progress": { "satisfied": true },
    "courses": []
  }
}
```
