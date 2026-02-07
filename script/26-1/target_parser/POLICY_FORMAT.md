# 수강대상 정책 형식 (Course Target Policy Format)

숭실대학교 수강대상 파싱 결과는 AWS IAM 정책 형식을 기반으로 합니다.

## 정책 구조

```json
{
  "Effect": "Allow" | "Deny",
  "Resource": "<resource_type>/<resource_name>",
  "Condition": {
    "Grade": { "min": <number>, "max": <number> },
    "StudentType": [<student_types>]
  },
  "Strict": true  // Optional
}
```

## 필드 설명

### Effect
- `"Allow"`: 해당 대상에게 수강을 **허용**
- `"Deny"`: 해당 대상에게 수강을 **제한**

### Resource
| 타입 | 형식 | 예시 |
|-----|------|-----|
| 전체대학 | `"university"` | `"university"` |
| 단과대학 | `"college/{이름}"` | `"college/IT대학"` |
| 학과/학부 | `"department/{이름}"` | `"department/컴퓨터학부"` |

### Condition.Grade
학년 범위를 지정합니다.
- `min`: 최소 학년 (1-5)
- `max`: 최대 학년 (1-5)

```json
{ "min": 2, "max": 4 }  // 2~4학년
```

### Condition.StudentType
학생 유형을 배열로 지정합니다.

| 값 | 설명 |
|----|------|
| `"general"` | 일반 학생 |
| `"foreigner"` | 외국인/교환학생 |
| `"military"` | 군위탁생 |
| `"teaching_cert"` | 교직이수자 |

### Strict (Optional)
`"대상외수강제한"` 또는 `"타학과수강제한"` 키워드가 포함된 경우 `true`로 설정됩니다.
엄격한 수강 제한을 의미합니다.

---

## 예시

### 1. 전체 허용
**원문**: `"전체"`
```json
{
  "Effect": "Allow",
  "Resource": "university",
  "Condition": {
    "Grade": { "min": 1, "max": 5 },
    "StudentType": ["general"]
  }
}
```

### 2. 특정 단과대 허용 + 일부 제외
**원문**: `"2학년 IT대학 (수강제한:1학년 인문)"`
```json
[
  {
    "Effect": "Allow",
    "Resource": "college/IT대학",
    "Condition": {
      "Grade": { "min": 2, "max": 2 },
      "StudentType": ["general"]
    }
  },
  {
    "Effect": "Deny",
    "Resource": "college/인문대학",
    "Condition": {
      "Grade": { "min": 1, "max": 1 },
      "StudentType": ["general"]
    }
  }
]
```

### 3. 외국인 전용 (엄격한 제한)
**원문**: `"전체학년 ;순수외국인입학생 (대상외수강제한)"`
```json
{
  "Effect": "Allow",
  "Resource": "university",
  "Condition": {
    "Grade": { "min": 1, "max": 5 },
    "StudentType": ["foreigner"]
  },
  "Strict": true
}
```

---

## 정책 평가 로직 (권장)

1. 모든 `Deny` 정책을 먼저 평가
2. 하나라도 `Deny`에 매칭되면 → **수강 불가**
3. `Allow` 정책 중 하나라도 매칭되면 → **수강 가능**
4. 아무것도 매칭되지 않으면 → **수강 불가** (기본 거부)

`Strict: true`인 경우, 명시된 조건 외에는 수강이 불가능합니다.
