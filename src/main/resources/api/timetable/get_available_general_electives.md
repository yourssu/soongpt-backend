# 수강 가능한 교양 과목 목록 조회

## 개요

특정 시간표 기준으로 **수강 가능한 교양선택 과목**과 **교양 이수현황(progress)** 을 반환합니다.

## Endpoint

```
GET /api/timetables/{id}/available-general-electives
```

## 인증

- `soongpt_auth` 쿠키 (JWT) 필수

## Request

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long (path) | 시간표 ID |

## Response

```
Response<AvailableGeneralElectivesResponse>
```

### AvailableGeneralElectivesResponse

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `progress` | GeneralElectiveProgress | **X** | 교양 이수 진행 상황. **항상 존재.** rusaint/졸업사정 없으면 아래 센티널 |
| `courses` | GeneralElectiveDto[] | X | 영역별 수강 가능한 교양 과목 목록 |

### GeneralElectiveProgress (이수현황)

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `required` | Int | O | 필수 학점. **제공 불가 시 `-2`** (recommend/all과 동일 센티널) |
| `completed` | Int | O | 이수 학점. **제공 불가 시 `-2`** |
| `satisfied` | Boolean | X | 이수 완료 여부 |
| `fieldCredits` | Map&lt;String, Int&gt; | X | 분야별 이수 학점. **제공 불가 시 `{}`** |

**progress는 null이 되지 않습니다.** rusaint/졸업사정표가 없을 때는
`progress = { required: -2, completed: -2, satisfied: false, fieldCredits: {} }` 로 내려갑니다.

### 프론트 해석

- `progress.required === -2` (또는 `completed === -2`) → 이수현황 제공 불가. progress bar 미표시 후 안내 문구 표시.
- 그 외 → `required`/`completed`/`fieldCredits`로 progress bar 및 분야별 학점 표시.

자세한 progress 공통 규약: [progress 프론트 가이드](../requirements/progress_프론트_가이드.md)

## 응답 예시

```json
{
  "timestamp": "2025-03-01 12:00:00",
  "result": {
    "progress": {
      "required": 9,
      "completed": 6,
      "satisfied": false,
      "fieldCredits": { "인간·언어": 3, "문화": 3 }
    },
    "courses": [
      {
        "trackName": "인간·언어",
        "courses": [ { "courseCode": 12345678, "courseName": "…", "…" } ]
      }
    ]
  }
}
```

제공 불가 시:

```json
{
  "result": {
    "progress": {
      "required": -2,
      "completed": -2,
      "satisfied": false,
      "fieldCredits": {}
    },
    "courses": [ … ]
  }
}
```
