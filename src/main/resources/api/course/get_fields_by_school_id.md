# getFieldsBySchoolId (GET /api/courses/fields/schoolId/{schoolId})

## Request

### Path Parameters

| Name       | Type    | Required | Constraint       | Description                                           |
|------------|---------|----------|------------------|-------------------------------------------------------|
| `schoolId` | integer | true     | 15 <= value <=25 | School ID (admission year 2 digits: 15, 16, 17, etc.) |

### Usage Examples

```
GET /api/courses/fields/schoolId/20    # Get fields for 2020 admission year
GET /api/courses/fields/schoolId/21    # Get fields for 2021 admission year
GET /api/courses/fields/schoolId/23    # Get fields for 2023 admission year
```

## Reply

### Response Body

| Name     | Type     | Nullable | Description                                         |
|----------|----------|----------|-----------------------------------------------------|
| `result` | string[] | No       | Array of curriculum field names for the school year |

---

### 200 OK

```json
{
  "timestamp": "2025-07-29 10:30:15",
  "result": [
    "교필-디지털테크놀로지(SW와AI)",
    "교필-디지털테크놀로지(컴퓨팅적사고)",
    "교필-품격(글로벌소통과언어)",
    "교필-품격(글로벌시민의식)",
    "교필-품격(인간과성서)",
    "교필-창의(비판적사고와표현)",
    "교필-창의(인문적상상력과소통)",
    "교필-창의(창의적사고와혁신)",
    "과학·기술",
    "문화·예술",
    "사회·정치·경제",
    "인간·언어",
    "자기개발·진로탐색"
  ]
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-07-29 10:30:15",
  "result": []
}
```

## Description

Retrieves the list of available curriculum fields for a specific admission year (school ID). The fields represent
different categories of courses available in the curriculum structure for students who entered in the specified year.

The system uses a 2-digit year format where:

- 15 = 2015 admission year
- 20 = 2020 admission year
- 25 = 2025 admission year

Different admission years may have different curriculum structures and field categories due to curriculum reforms and
updates.
