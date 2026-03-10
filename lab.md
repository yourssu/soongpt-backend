과제: 시간표 추천 페이지 구현하기
과제 진행 요구 사항
과제 구현에 사용할 기술 스택은 자유이지만 해당 기술을 사용한 이유를 README.md에 작성해주세요.
과제를 수행하면서 느낀 점, 배운 점, 많은 시간을 투자한 부분 등이 있다면 README.md에 자유롭게 작성해주세요.
기능 요구 사항
주어진 Figma 링크를 참고하여 시간표 추천 페이지를 구현해주세요.
2025-2 Web FE 팀 리크루팅 과제 Figma
시간표 추천 페이지에서 보여줄 시간표 데이터는 주어진 API를 통해 받아와주세요.
기능 요구 사항에 기재되지 않은 내용은 스스로 판단하여 구현해주세요.
시간표
시간표의 요일은 월, 화, 수, 목, 금 5개의 요일만 존재해요.

시간표에 표시할 모든 수업은 9시 이후에 시작하고 23시 이전에 종료해요.

시간표의 시간은 9시부터 표시해야 해요.

9시에 시작하는 수업이 없더라도 시간표에 9~10시 칸을 표시해야 해요.
시간표의 시간은 가장 늦게 종료되는 수업의 종료 시각을 포함한 시간까지 표시해야 해요.

예) Figma 예시 시간표에서 가장 늦게 종료되는 수업인 컴퓨터시스템기초는 4~5시 사이에 종료되므로 시간표에 4~5시 칸까지 표시해야 해요.
시간표에 표시할 수업들은 수업 시간에 비례한 크기를 가져야 해요.

예) Figma 예시 시간표에서 11시에 시작해서 12시 30분에 끝나는 수업인 C프로그래밍 및 실습 수업은 11~12시 칸을 넘어 12~1시 칸의 절반 지점까지 표시해야 해요.
API 응답 데이터의 tag 값에 따라 적절한 태그를 표시해야 해요.

아래 TIME_TABLE_TAG 객체를 참고하여 적절한 태그를 표시해 주세요.
const TIME_TABLE_TAG = {
  DEFAULT: "🤔 뭔가 좋아보이는 시간표",
  HAS_FREE_DAY: "🥳 공강 날이 있는 시간표",
  NO_MORNING_CLASSES: "⏰ 아침 수업이 없는 시간표",
  NO_LONG_BREAKS: "🚀 우주 공강이 없는 시간표 ",
  EVENLY_DISTRIBUTED: "⚖️ 균등하게 배분되어 있는 시간표",
  GUARANTEED_LUNCH_TIME: "🍔 점심시간 보장되는 시간표",
  NO_EVENING_CLASSES: "🛏 저녁수업이 없는 시간표",
};
API 요구 사항
시간표 추천 페이지에서 보여줄 시간표 데이터는 https://api.dev.soongpt.yourssu.com/api/timetables 로 GET 요청을 보내 받아와주세요.
응답 데이터에는 시간이 겹치는 수업이 없으니 시간 중복에 대한 예외 처리는 고려하지 않아도 돼요
API 응답 데이터
API 응답 데이터는 아래와 같은 형식이에요.
{
  "timestamp": "2025-09-02 01:35:43",
  "result": {
    "timetableId": 2422,
    "tag": "NO_EVENING_CLASSES",
    "totalCredit": 18,
    "courses": [
      {
        "courseName": "영화 Shot by Shot",
        "professorName": null,
        "classification": "GENERAL_ELECTIVE",
        "credit": 3,
        "courseTime": [
          {
            "week": "월",
            "start": "15:00",
            "end": "16:15",
            "classroom": "베어드홀 01102"
          },
          {
            "week": "월",
            "start": "16:30",
            "end": "17:45",
            "classroom": "베어드홀 01102"
          }
        ]
      },
      {
        "courseName": "캐릭터와연기",
        "professorName": "강혜연",
        "classification": "GENERAL_ELECTIVE",
        "credit": 3,
        "courseTime": [
          {
            "week": "금",
            "start": "15:00",
            "end": "16:15",
            "classroom": "베어드홀 01102"
          },
          {
            "week": "금",
            "start": "16:30",
            "end": "17:45",
            "classroom": "베어드홀 01102"
          }
        ]
      },
      {
        "courseName": "[글로벌소통과언어]CTE for IT, Engineering&Natura",
        "professorName": "Jay Fraser",
        "classification": "GENERAL_REQUIRED",
        "credit": 3,
        "courseTime": [
          {
            "week": "화",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11110"
          },
          {
            "week": "목",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11110"
          }
        ]
      },
      {
        "courseName": "물권법",
        "professorName": "이지은",
        "classification": "MAJOR_ELECTIVE",
        "credit": 3,
        "courseTime": [
          {
            "week": "화",
            "start": "12:00",
            "end": "13:15",
            "classroom": "진리관 11404"
          },
          {
            "week": "수",
            "start": "12:00",
            "end": "13:15",
            "classroom": "진리관 11410"
          }
        ]
      },
      {
        "courseName": "행정법1",
        "professorName": "채우석",
        "classification": "MAJOR_REQUIRED",
        "credit": 3,
        "courseTime": [
          {
            "week": "월",
            "start": "13:30",
            "end": "14:45",
            "classroom": "진리관 11522"
          },
          {
            "week": "수",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11407"
          }
        ]
      },
      {
        "courseName": "형사소송법",
        "professorName": null,
        "classification": "MAJOR_ELECTIVE",
        "credit": 3,
        "courseTime": [
          {
            "week": "금",
            "start": "10:30",
            "end": "11:45",
            "classroom": "진리관 11410"
          },
          {
            "week": "금",
            "start": "12:00",
            "end": "13:15",
            "classroom": "진리관 11410"
          }
        ]
      }
    ]
  }
}
응답 데이터의 tag는 아래와 같은 값을 가질 수 있어요.
  | "DEFAULT"
  | "HAS_FREE_DAY"
  | "NO_MORNING_CLASSES"
  | "NO_LONG_BREAKS"
  | "EVENLY_DISTRIBUTED"
  | "GUARANTEED_LUNCH_TIME"
  | "NO_EVENING_CLASSES"
응답 데이터의 classification은 아래와 같은 값을 가질 수 있어요.
  | "MAJOR_REQUIRED" // 전공필수
  | "MAJOR_ELECTIVE" // 전공선택
  | "GENERAL_REQUIRED" // 교양필수
  | "GENERAL_ELECTIVE" // 교양선택
  | "CHAPEL" // 채플
