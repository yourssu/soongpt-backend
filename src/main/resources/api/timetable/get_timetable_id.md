# get_timetable (GET /api/timetables/{timetableId})

## Path Variable

| Name          | Type    | Required | Description                |
|---------------|---------|----------|----------------------------|
| timetableId   | integer | true     | ID of the timetable to get |

---

## Response Body

| name        | type     | description                               |
|-------------|----------|-------------------------------------------|
| timetableId | integer  | Unique ID of the timetable                |
| tag         | string   | Timetable tag (e.g., NO_MORNING_CLASSES)  |
| score       | integer  | Timetable preference score                |
| courses     | Course[] | List of courses included in the timetable |

### Course

| Name           | Type                 | Nullable | Description                                        |
|----------------|----------------------|----------|----------------------------------------------------| 
| `category`     | string               | No       | Course category                                    |
| `subCategory`  | string               | Yes      | Course sub category                                |
| `field`        | string               | Yes      | Curriculum field by admission year                 |
| `code`         | integer              | No       | Unique course code identifier                      |
| `name`         | string               | No       | Course name                                        |
| `professor`    | string               | Yes      | Name of the professor in charge                    |
| `department`   | string               | No       | Department                                         |
| `division`     | string               | Yes      | Course division                                    |
| `time`         | string               | No       | Course time information                            |
| `point`        | string               | No       | Course point information                           |
| `personeel`    | integer              | No       | Personnel information                              |
| `scheduleRoom` | string               | No       | Schedule and room information                      |
| `target`       | string               | No       | Target students for the course                     |
| `courseTimes`   | CourseTimeResponse[] | No       | Array of course schedule and classroom information |

#### CourseTimeResponse

| Name        | Type   | Nullable | Description                                     |
|-------------|--------|----------|-------------------------------------------------|
| `week`      | string | No       | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | No       | Start time of the class (in HH:mm format)       |
| `end`       | string | No       | End time of the class (in HH:mm format)         |
| `classroom` | string | No       | Classroom location                              |

---

## 200 OK

```json
{
  "timestamp": "2025-07-29 03:26:15",
  "result": {
    "timetableId": 10,
    "tag": "점심시간 보장되는 시간표",
    "score": 10,
    "totalPoint": 21.0,
    "courses": [
      {
        "id": 529,
        "category": "GENERAL_ELECTIVE",
        "subCategory": null,
        "field": "[‘23이후]문화·예술\n[''20,''21~''22]창의/융합,균형교양-문학·예술\n[''19]균형교양-인문학(인간/문화/사고력)\n[''16-''18]균형교양(인문학-문학/어학/예술)\n[''15이전]문학과예술(융합-인문)",
        "code": 2150081501,
        "name": "서사와메타모포시스",
        "professor": "홍길동",
        "department": "숭실학술원 행정팀",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "목 15:00-16:15 (진리관 11302-홍길동)\n목 16:30-17:45 (진리관 11302-홍길동)",
        "target": "전체",
        "courseTimes": [
          {
            "week": "목",
            "start": "15:00",
            "end": "16:15",
            "classroom": "진리관 11302-홍길동"
          },
          {
            "week": "목",
            "start": "16:30",
            "end": "17:45",
            "classroom": "진리관 11302-홍길동"
          }
        ]
      },
      {
        "id": 995,
        "category": "GENERAL_ELECTIVE",
        "subCategory": null,
        "field": "[‘23이후]문화·예술\n[''20,''21~''22]창의/융합,균형교양-사회·문화·심리\n[''19]균형교양-인문학(인간/문화/사고력)\n[''16-''18]균형교양(사회과학-문화및문명)\n[''15이전]세계의문화와국제관계(핵심-창의)",
        "code": 2150152601,
        "name": "한중문화산책",
        "professor": "홍길동",
        "department": "중어중문학과",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "월 수 15:00-16:15 (진리관 11307-홍길동)",
        "target": "전체",
        "courseTimes": [
          {
            "week": "월",
            "start": "15:00",
            "end": "16:15",
            "classroom": "진리관 11307-홍길동"
          },
          {
            "week": "수",
            "start": "15:00",
            "end": "16:15",
            "classroom": "진리관 11307-홍길동"
          }
        ]
      },
      {
        "id": 1025,
        "category": "GENERAL_ELECTIVE",
        "subCategory": null,
        "field": "숭실사이버대과목\n[‘23이후]사회·정치·경제\n[''20,''21~''22]창의/융합,균형교양-사회·문화·심리\n[''19]균형교양-사회과학(사회/역사)\n[''16-''18]균형교양(사회과학-사회/정치/경제)\n[''15이전]인간과사회(융합-사회)",
        "code": 2150145301,
        "name": "환경과기후(숭실사이버대)",
        "professor": null,
        "department": "학사팀",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "",
        "target": "국내 대학 학점교류생 수강 제한",
        "courseTimes": []
      },
      {
        "id": 1575,
        "category": "GENERAL_REQUIRED",
        "subCategory": null,
        "field": "교필-[''23이후]품격(글로벌소통과언어)",
        "code": 2150102702,
        "name": "[글로벌소통과언어]CTE for IT, Engineering&Natura",
        "professor": "홍길동",
        "department": "교양교육운영팀",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "화 목 10:30-11:45 (진리관 11110-홍길동)",
        "target": "2학년 법학 ,국제법무학과",
        "courseTimes": [
          {
            "week": "화",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11110-홍길동"
          },
          {
            "week": "목",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11110-홍길동"
          }
        ]
      },
      {
        "id": 1816,
        "category": "MAJOR_ELECTIVE",
        "subCategory": "복선-법학/부선-법학",
        "field": "법학과목",
        "code": 2150143402,
        "name": "물권법",
        "professor": "홍길동",
        "department": "법학과",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "화 13:30-14:45 (진리관 11404-홍길동)\n수 13:30-14:45 (진리관 11410-홍길동)",
        "target": "2학년 법학",
        "courseTimes": [
          {
            "week": "화",
            "start": "13:30",
            "end": "14:45",
            "classroom": "진리관 11404-홍길동"
          },
          {
            "week": "수",
            "start": "13:30",
            "end": "14:45",
            "classroom": "진리관 11410-홍길동"
          }
        ]
      },
      {
        "id": 2084,
        "category": "MAJOR_REQUIRED",
        "subCategory": "복선-법학/부선-법학",
        "field": "법학과목",
        "code": 2150545501,
        "name": "행정법1",
        "professor": "홍길동",
        "department": "법학과",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "월 13:30-14:45 (진리관 11522 (이효계강의실)-홍길동)\n수 10:30-11:45 (진리관 11407-홍길동)",
        "target": "2학년 법학",
        "courseTimes": [
          {
            "week": "월",
            "start": "13:30",
            "end": "14:45",
            "classroom": "진리관 11522 (이효계강의실)-홍길동"
          },
          {
            "week": "수",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11407-홍길동"
          }
        ]
      },
      {
        "id": 2095,
        "category": "MAJOR_ELECTIVE",
        "subCategory": "복선-법학/부선-법학",
        "field": "법학과목",
        "code": 2150225101,
        "name": "형사소송법",
        "professor": "홍길동",
        "department": "법학과",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 0,
        "scheduleRoom": "수 18:30-19:45 (진리관 11410-홍길동)\n수 20:00-21:15 (진리관 11410-홍길동)",
        "target": "2학년 법학",
        "courseTimes": [
          {
            "week": "수",
            "start": "18:30",
            "end": "19:45",
            "classroom": "진리관 11410-홍길동"
          },
          {
            "week": "수",
            "start": "20:00",
            "end": "21:15",
            "classroom": "진리관 11410-홍길동"
          }
        ]
      }
    ]
  }
}
```
