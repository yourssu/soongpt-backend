# 과목 추천 엣지케이스 응답 매핑

## 센티널 값 기반 Progress 매핑

| Case                    | required | completed | satisfied |         message         |     courses     | FE 동작                       |
| ----------------------- | :------: | :-------: | :-------: | :---------------------: | :--------------: | ----------------------------- |
| **1** 해당없음    |  `0`  |   `0`   | `true` |        있음/없음        |      `[]`      | `required==0` → 숨김       |
| **2** 안열림      |  `>0`  |     N     | `false` |    `"...없습니다"`    |      `[]`      | 메시지+progress bar           |
| **3** 다들음      |  `>0`  |     N     | `true` | `"...이수하셨습니다"` |      `[]`      | 메시지+progress bar           |
| **4** 재수강/교직 |  `-1`  |  `-1`  | `false` |     `null`/메시지     | `[]`/`[...]` | progress bar 미표시           |
| **6** 사정표없음  |  `-2`  |  `-2`  | `false` |         메시지         |      `[]`      | warn 확인, 이수현황 로딩 불가 |

- progress는 항상 non-null (센티널 값 사용)
- Case 6만 top-level `warnings`에 `"NO_GRADUATION_REPORT"` 추가

## 각 케이스 상세

### Case 1: 해당없음

- 졸업사정표를 불러올 수 있으나 해당 이수구분이 커리큘럼 자체에 없음
- ex) 24학번 경영학부는 전공기초 자체가 없음, 복수전공/부전공 미등록자
- `progress = { required: 0, completed: 0, satisfied: true }`
- FE: `required == 0` → 해당 카테고리 숨김

### Case 2: 안열림

- 이수해야 할 학점이 남아있지만 이번 학기에 개설된 과목이 없음
- `progress = { required: >0, completed: N, satisfied: false }`
- `message = "이번 학기에 수강 가능한 ... 과목이 없습니다."`
- FE: progress bar + message 표시

### Case 3: 다들음

- 졸업사정표 상 해당 이수구분을 모두 충족
- `progress = { required: >0, completed: N, satisfied: true }`
- `message = "... 학점을 이미 모두 이수하셨습니다."`
- FE: progress bar + message 표시

### Case 4: 재수강/교직

- 졸업사정표에 이수현황이 없는 카테고리 (재수강, 교직)
- `progress = { required: -1, completed: -1, satisfied: false }`
- FE: `required == -1` → progress bar 미표시, courses/message만 렌더링

### Case 6: 졸업사정표 없음

- 졸업사정표 자체를 불러올 수 없음 (rusaint 오류 등)
- `progress = { required: -2, completed: -2, satisfied: false }`
- `warnings`에 `"NO_GRADUATION_REPORT"` 포함
- FE: `required == -2` → 이수현황 로딩 불가 안내

## FE 분기 pseudocode

```typescript
// top-level
if (warnings.includes('NO_GRADUATION_REPORT')) {
  showWarningBanner('졸업사정표를 불러올 수 없습니다.');
}

// per category
for (const cat of categories) {
  if (cat.progress.required === 0 && cat.progress.satisfied) {
    continue; // 해당 없음 → 숨김
  }
  if (cat.progress.required === -2) {
    renderUnavailable(cat); // 졸업사정표 없음
    continue;
  }
  if (cat.progress.required === -1) {
    renderWithoutProgressBar(cat); // 재수강/교직
  } else {
    renderWithProgressBar(cat); // 정상 progress
  }
  if (cat.message) {
    showMessage(cat.message);
  } else {
    renderCourseCards(cat.courses);
  }
}
```
