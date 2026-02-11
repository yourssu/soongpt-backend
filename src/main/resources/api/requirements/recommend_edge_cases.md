# 과목 추천 엣지케이스 응답 매핑

## 원칙

- 서버는 action/status를 결정하지 않음
- 프론트가 `warnings`, `progress`, `courses`/`fieldGroups` 조합으로 화면 표시/스킵/경고를 판단
- `progress` 값 자체로 "해당 없음(0,0,true)" / "다 들음" / "아직" 구분 가능
- `progress == null`이면 "판단 불가" (졸업사정표 없음)
- `courses`/`fieldGroups` 비었으면 → 이번 학기 개설 없음

## top-level `warnings` 필드

rusaint 동기화 과정에서 발생한 경고를 그대로 전달합니다.

```json
{
  "warnings": ["NO_GRADUATION_DATA", "PARTIAL_SYNC"],
  "categories": [...]
}
```

- 빈 배열이면 경고 없음
- 프론트는 warnings 내용에 따라 경고 배너를 표시

## 프론트 판단 로직

```
1. warnings에 값 있음 → 경고 배너 표시
2. progress == null && category != RETAKE → 졸업사정표 없어서 판단 불가
3. progress.required == 0 && satisfied == true → 해당 없는 이수구분 → 스킵
4. progress.satisfied == true && required > 0 → 이미 이수 완료
5. courses/fieldGroups 비어있음 → 이번 학기 개설 없음
6. courses/fieldGroups 있음 → 정상 렌더링
```

## 엣지케이스별 응답 예시

### 1. 졸업사정표 없음 (progress == null)

졸업사정표 데이터를 받아오지 못한 경우. 재수강은 졸업사정표 불필요하므로 영향 없음.

```json
{
  "warnings": ["NO_GRADUATION_DATA"],
  "categories": [
    {
      "category": "MAJOR_BASIC",
      "progress": null,
      "message": null,
      "userGrade": null,
      "courses": [],
      "gradeGroups": null,
      "fieldGroups": null,
      "lateFields": null
    },
    {
      "category": "RETAKE",
      "progress": null,
      "message": null,
      "userGrade": null,
      "courses": [...],
      "gradeGroups": null,
      "fieldGroups": null,
      "lateFields": null
    }
  ]
}
```

프론트 동작:
- MAJOR_BASIC: `progress == null` → "졸업사정 정보를 확인할 수 없습니다" 안내
- RETAKE: 재수강은 원래 `progress == null` → 정상 렌더링

### 2. 해당 없는 이수구분 (required=0, satisfied=true)

rusaint 졸업사정표에서 해당 이수구분의 요구 학점이 0인 경우.

```json
{
  "category": "MAJOR_BASIC",
  "progress": { "required": 0, "completed": 0, "satisfied": true },
  "message": "전공기초 학점을 이미 모두 이수하셨습니다.",
  "courses": []
}
```

프론트 동작: `required == 0 && satisfied == true` → 해당 이수구분 섹션 스킵

### 3. 이미 이수 완료 (satisfied=true, required > 0)

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 18, "satisfied": true },
  "message": "전공필수 학점을 이미 모두 이수하셨습니다.",
  "courses": []
}
```

프론트 동작: `message` 표시 ("이미 모두 이수하셨습니다")

### 4. 미충족이나 이번 학기 개설 없음

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 12, "satisfied": false },
  "message": "이번 학기에 수강 가능한 전공필수 과목이 없습니다.",
  "courses": []
}
```

프론트 동작: `message` 표시 ("이번 학기에 수강 가능한 과목이 없습니다")

### 5. 정상 — 추천 과목 있음

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 12, "satisfied": false },
  "message": null,
  "courses": [...]
}
```

프론트 동작: `message == null` → 과목 카드 렌더링
