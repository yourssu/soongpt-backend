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
| `fieldCredits` | Map&lt;String, Any&gt; | **O** | 분야별 이수 과목 수. 20학번: 창의·융합역량(세부필드). 21~22: 균형교양교과(세부필드). 23~: 인간/문화/사회/과학/자기개발. **19학번 이하·제공 불가: 필드 생략** |

**progress는 null이 되지 않습니다.** rusaint/졸업사정표가 없을 때는
`progress = { required: -2, completed: -2, satisfied: false }` 로 내려갑니다. (fieldCredits 생략)

### 프론트 해석

- `progress.required === -2` (또는 `completed === -2`) → 이수현황 제공 불가. progress bar 미표시 후 안내 문구 표시.
- `progress.fieldCredits` 없음 (undefined) → 19학번 이하 또는 제공 불가. `required`/`completed`/`satisfied`만 표시.
- `progress.fieldCredits` 존재 시 → `required`/`completed`/`fieldCredits`로 progress bar 및 분야별 이수 과목 수 표시.

자세한 progress 공통 규약: [progress 프론트 가이드](../requirements/progress_프론트_가이드.md)

## 응답 예시

### 21~22학번

```json
{
  "timestamp": "2025-03-01 12:00:00",
  "result": {
    "progress": {
      "required": 9,
      "completed": 6,
      "satisfied": false,
      "fieldCredits": {
        "숭실품성교과": 0,
        "균형교양교과": {
          "문학·예술": 1,
          "역사·철학·종교": 0,
          "정치·경제·경영": 2,
          "사회·문화·심리": 0,
          "자연과학·공학·기술": 0
        },
        "기초역량교과": 0
      }
    },
    "courses": [ { "trackName": "인간·언어", "courses": [ … ] } ]
  }
}
```

### 23학번 이상

```json
{
  "result": {
    "progress": {
      "required": 9,
      "completed": 6,
      "satisfied": false,
      "fieldCredits": {
        "인간": 1,
        "문화": 2,
        "사회": 0,
        "과학": 0,
        "자기개발": 0
      }
    },
    "courses": [ … ]
  }
}
```

### 20학번

```json
{
  "result": {
    "progress": {
      "required": 9,
      "completed": 3,
      "satisfied": false,
      "fieldCredits": {
        "공동체/리더십역량": 0,
        "의사소통/글로벌역량": 0,
        "창의/융합역량": {
          "문학·예술": 1,
          "역사·철학·종교": 0,
          "정치·경제·경영": 0,
          "사회·문화·심리": 1,
          "자연과학·공학·기술": 0
        }
      }
    },
    "courses": [ … ]
  }
}
```

### 19학번 이하

```json
{
  "result": {
    "progress": {
      "required": 9,
      "completed": 6,
      "satisfied": false
    },
    "courses": [ … ]
  }
}
```

### 제공 불가 시 (rusaint/졸업사정 없음)

```json
{
  "result": {
    "progress": {
      "required": -2,
      "completed": -2,
      "satisfied": false
    },
    "courses": [ … ]
  }
}
```
