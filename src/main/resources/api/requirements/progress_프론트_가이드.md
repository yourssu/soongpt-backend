# ㅇprogress 필드 공통 규약 (프론트 참고)

이수현황을 나타내는 **`progress`** 는 아래 세 API에서 모두 사용하며, **항상 non-null** 입니다.
API마다 **필드 구조는 다르지만**, 의미는 "이수현황"으로 동일하고, **제공 불가 시에도 null이 아니라 센티널 객체**를 내려줍니다.

---

## 1. 공통 원칙

| 항목                | 내용                                                                              |
| ------------------- | --------------------------------------------------------------------------------- |
| **네이밍**    | 세 API 모두 필드명 `progress` 사용                                              |
| **의미**      | 이수현황 (졸업사정/rusaint 기반)                                                  |
| **null**      | **없음.** 모든 API에서 `progress`는 항상 객체로 존재                      |
| **제공 불가** | rusaint/졸업사정 없을 때는**센티널 값**으로 동일한 해석 가능 (아래 표 참고) |

---

## 2. API별 progress 구조

### 2-1. GET /api/courses/recommend/all (과목 추천)

- **용도**: 카테고리별 추천 + 이수현황
- **구조**: 고정 3필드

| 필드          | 타입    | 설명                                                                     |
| ------------- | ------- | ------------------------------------------------------------------------ |
| `required`  | Int     | 요구 학점. 센티널:`-1`=재수강/교직(bar 미표시), `-2`=졸업사정표 없음 |
| `completed` | Int     | 이수 학점. 센티널:`-1`, `-2`                                         |
| `satisfied` | Boolean | 충족 여부                                                                |

**센티널 값:**
| required | completed | satisfied | 의미 |
|:---:|:---:|:---:|---|
| `0` | `0` | `true` | 해당 없는 이수구분 (FE에서 숨김) |
| `-1` | `-1` | `false` | 재수강/교직 — progress bar 미표시, 과목은 있을 수 있음 |
| `-2` | `-2` | `false` | 졸업사정표 없음 — 제공 불가, bar 미표시 |

---

### 2-2. GET /api/timetables/{id}/available-general-electives (수강 가능 교양)

- **용도**: 시간표 기준 수강 가능 교양 + 교양 이수현황
- **구조**: 3필드 + 분야별 학점

**제공 불가 시:** `progress = { required: -2, completed: -2, satisfied: false }` (fieldCredits 생략)

---

### 2-3. GET /api/timetables/{id}/available-chapels (수강 가능 채플)

- **용도**: 시간표 기준 수강 가능 채플 + 채플 이수 여부
- **구조**: satisfied만 사용 (채플은 충족 여부만 있음)

| 필드          | 타입    | 설명                                                   |
| ------------- | ------- | ------------------------------------------------------ |
| `satisfied` | Boolean | 채플 이수 충족 여부. rusaint/졸업사정 없으면 `false` |

**제공 불가/미판단 시:** `progress = { satisfied: false }`

---

## 3. 프론트 해석 요약

1. **progress는 절대 null이 아님** → null 체크 불필요, 항상 객체.
2. **recommend/all, available-general-electives**
   - `progress.required === -2` (또는 `completed === -2`) → 이수현황 제공 불가(졸업사정표 없음 등). bar 미표시 + 안내 문구.
   - `progress.required === -1` → 재수강/교직 등 bar 미표시(해당 API에서는 recommend만 해당).
3. **available-chapels**
   - `progress.satisfied === true` → 이미 충족, 과목 목록 빈 배열 가능.
   - `progress.satisfied === false` → 미충족 또는 판단 불가(rusaint 없음).
4. **recommend/all** 상세 분기: [recommend_edge_cases.md](recommend_edge_cases.md), [get_recommend_all.md](../course/get_recommend_all.md) 참고.

---

## 4. 관련 문서

- [통합 과목 추천 API (recommend/all)](../course/get_recommend_all.md) — Progress 센티널 상세
- [수강 가능한 교양 과목 목록](../timetable/get_available_general_electives.md)
- [수강 가능한 채플 과목 목록](../timetable/get_available_chapels.md)
- [졸업사정표 경고 가이드](졸업사정표_경고_가이드.md) — NO_GRADUATION_DATA / NO_GRADUATION_REPORT
