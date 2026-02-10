# PT-115 전체 과목 재리뷰: 의심 매핑 보고서

- 기준 체크리스트: `script/26-1/reviews/checklists/PT115_ALL_COURSE_LIST_CHECKLIST.md`
- 점검 과목 수: **3048**
- DB course 조회 성공: **3048**
- 의심 과목 수: **393**

## 기존 적용 필터

- 규칙: 국어국문학과/철학과/사학과/불어불문학과/평생교육학과/문예창작학과 중 ssu26-1.csv 과정이 석박과정인 항목 제외
- 제외 수: **38건**

## 기존 학과 제외

- 규칙: 개설학과가 생활체육학과/컴퓨터학과/사회복지대학원/실내디자인학과인 항목 제외
- 제외 수: **24건**
  - 생활체육학과: 6건
  - 컴퓨터학과: 6건
  - 실내디자인학과: 2건
  - 사회복지대학원: 10건

## 추가 학과 제외 (요청 반영)

- 제외 학과:
  - `교육대학원`
  - `유아교육전공`
  - `평생교육·HRD전공`
  - `기독교상담학과`
  - `성서·신학과`
  - `회계·세무학과`
  - `프로젝트경영학과`
  - `생명정보학과`
  - `인공지능IT융합학과`
  - `정보통신공학과`
  - `글로벌법률학과`
  - `재난안전관리학과`
  - `융합영재교육전공`
  - `안전·보건융합공학과`
  - `기독교통일지도자학과`
  - `커리어·학습코칭전공`
- 제외된 항목: **110건**
  - 교육대학원: 3건
  - 글로벌법률학과: 7건
  - 기독교상담학과: 8건
  - 기독교통일지도자학과: 9건
  - 생명정보학과: 3건
  - 성서·신학과: 3건
  - 안전·보건융합공학과: 10건
  - 유아교육전공: 9건
  - 융합영재교육전공: 6건
  - 인공지능IT융합학과(계약학과): 9건
  - 재난안전관리학과: 13건
  - 정보통신공학과: 4건
  - 커리어·학습코칭전공: 5건
  - 평생교육·HRD전공: 6건
  - 프로젝트경영학과: 8건
  - 프로젝트경영학과(계약학과): 5건
  - 회계·세무학과: 2건

## 이슈 집계

- `TARGET_ROW_MISSING`: **152건**
- `TARGET_PARSE_MISMATCH`: **148건**
- `UNMAPPED_NON_FUSION_TOKEN`: **95건**

## 의심 과목 목록

1. [5022800101] `암호화정책론`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

2. [5027705201] `클라우드컴퓨팅론`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

3. [5028121101] `소프트웨어공학`
   - 개설학과: `소프트웨어공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_소프트공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

4. [5029153601] `IT정책경영세미나`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

5. [5029377601] `빅데이터전략`
   - 개설학과: `소프트웨어공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_소프트공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

6. [5030186201] `창의적 문제해결과 경영의사결정`
   - 개설학과: `이노비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_이노비즈니스학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

7. [5033858001] `UI/UX 디자인`
   - 개설학과: `IT유통물류학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT유통물류학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

8. [5034855201] `위성통신 송수신시스템`
   - 개설학과: `정보통신융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보통신융합학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

9. [5035065501] `안보공익경영특강`
   - 개설학과: `안보·공익경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_안보공익경영학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

10. [5035066101] `안보공익리더십특강`
   - 개설학과: `안보·공익경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_안보공익경영학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

11. [5036698701] `이상심리`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

12. [5037695601] `컬러경영`
   - 개설학과: `이미지경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_이미지경영학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

13. [5037702601] `놀이치료세미나`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

14. [5038327901] `가족상담`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

15. [5038963201] `IT정책경영 위기관리 리더십`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

16. [5039026101] `상담의이론과실제`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

17. [5042801801] `IT정책경영부트캠프Ⅱ`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

18. [5043723901] `미래IT기술 인사이트`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

19. [5043943501] `경영정보시스템`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

20. [5043943701] `운영관리`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

21. [5044001301] `기업금융`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

22. [5044179102] `사고조사 및 원인 분석론`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

23. [5044500901] `인문학과 성서`
   - 개설학과: `기독교인문사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기독인문`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

24. [5044571601] `청소년미술치료`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

25. [5044574601] `진로 상담`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

26. [5045911901] `에너지와환경(계약)`
   - 개설학과: `경제학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공_경제(계약)`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

27. [5045930401] `IoT기초`
   - 개설학과: `IoT학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_정보통신학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

28. [5045960901] `한국교회의 사회학적이해`
   - 개설학과: `기독교인문사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기독인문`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

29. [5046552901] `문화와 예술`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

30. [5046553301] `문화치유개론`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

31. [5046553601] `상담학`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

32. [5046861901] `전력경제(계약)`
   - 개설학과: `경제학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공_경제(계약)`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

33. [5046862101] `에너지통계(계약)`
   - 개설학과: `경제학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공_경제(계약)`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

34. [5046862501] `에너지경제세미나1(계약)`
   - 개설학과: `경제학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공_경제(계약)`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

35. [5047035003] `소방학개론`
   - 개설학과: `소방방재안전학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_소방방재안전학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

36. [5047049001] `Deep Learning Network Design Application`
   - 개설학과: `인공지능학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

37. [5047053403] `안전환경융합세미나 I`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

38. [5048399101] `안보공익자료분석특강`
   - 개설학과: `안보·공익경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_안보공익경영학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

39. [5048444801] `미래사회와 교육의역할`
   - 개설학과: `교육대학원 교학팀`
   - 분류: `OTHER` / `전공선택`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

40. [5048720703] `작업환경관리특론`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

41. [5049090801] `부모상담및 교육`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

42. [5049120801] `발달심리와상담`
   - 개설학과: `상담교육심리전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담교육심리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

43. [5049410401] `공간이미지경영`
   - 개설학과: `이미지경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_이미지경영학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

44. [5050360001] `빅데이터분석`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

45. [5050484101] `웹해킹`
   - 개설학과: `정보보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_정보보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

46. [5050989601] `AI 기반 교육경영전략`
   - 개설학과: `교육경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공-교육경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

47. [5051052601] `반도체공정및소자`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

48. [5051057001] `인적자원관리`
   - 개설학과: `교육경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공-교육경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

49. [5051237101] `중소기업 글로벌진출전략`
   - 개설학과: `글로벌경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_글로벌경영학과/전선_글로벌스타트업선교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

50. [5051238801] `국제상무론`
   - 개설학과: `글로벌비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_글로벌비즈니스학과`
   - 원본 대상: `전체 (대상외수강제한)`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

51. [5051239901] `고객경험 서비스디자인`
   - 개설학과: `서비스경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_서비스경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

52. [5051243201] `디지털시대의 소비자 행동 분석`
   - 개설학과: `디지털매니지먼트학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_디지털매니지먼트`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

53. [5051317503] `중대재해처벌법규론ΙΙ`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

54. [5051328401] `소방방재 및 재난안전 세미나Ι`
   - 개설학과: `안전융합대학원`
   - 분류: `MAJOR_ELECTIVE` / `전선_소방방재안전학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

55. [5052301701] `고급발달심리학`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

56. [5052312801] `에너지와 기후변화(계약)`
   - 개설학과: `경제학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전공_경제(계약)`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

57. [5052325001] `교육경영연구`
   - 개설학과: `교육경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_교육경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

58. [5052404501] `사례로 보는 행정과 정책의 이해`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

59. [5052406101] `중소기업 포인트세무`
   - 개설학과: `강소기업경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_강소기업경영학과/전선_중소기업경영혁신`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

60. [5052435901] `에너지기술정책특강`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

61. [5053123201] `인공지능과금융1`
   - 개설학과: `금융학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_금융`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

62. [5053165501] `에너지정책분석방법론`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

63. [5053165701] `글로벌에너지이슈분석`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

64. [5053165901] `태양에너지전환`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

65. [5053177501] `브랜드 커뮤니케이션 전략`
   - 개설학과: `서비스경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_서비스경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

66. [5053178101] `경영정보시스템전략`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

67. [5053184001] `기업가정신과 창업경영`
   - 개설학과: `기업승계학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기업승계학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

68. [5053185701] `기업승계원론`
   - 개설학과: `기업승계학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기업승계학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

69. [5053192801] `생성형AI와 비즈니스혁신`
   - 개설학과: `중소기업경영혁신학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI비즈니스혁신/전선_중소기업경영혁신`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

70. [5053193201] `중소벤처 브랜드 개발과 관리`
   - 개설학과: `중소기업경영혁신학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_중소기업경영혁신`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

71. [5053208101] `스타트업 펀딩과 투자`
   - 개설학과: `스타트업비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_스타트업비즈니스학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

72. [5053209101] `벤처중소 마케팅`
   - 개설학과: `스타트업비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_스타트업비즈니스학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

73. [5053215101] `디지털 통상론`
   - 개설학과: `글로벌비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_글로벌비즈니스학과`
   - 원본 대상: `전체 (대상외수강제한)`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

74. [5053218601] `중소벤처마케팅`
   - 개설학과: `글로벌비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_글로벌비즈니스학과`
   - 원본 대상: `전체 (대상외수강제한)`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

75. [5053338501] `데이터모델링(기초)`
   - 개설학과: `소프트웨어공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_소프트공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

76. [5054020502] `재난안전경영시스템`
   - 개설학과: `소방방재안전학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_소방방재안전학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

77. [5054508101] `AI시대 인플루언서 콘텐츠 비즈니스`
   - 개설학과: `인플루언서학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_인플루언서`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

78. [5054539301] `ESG 경영전략`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

79. [5054539501] `디지털 전환사회와 법`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

80. [5055010301] `스마트팩토리기계설비구조 및 원리`
   - 개설학과: `첨단융합안전공학과(계약학과)`
   - 분류: `MAJOR_REQUIRED` / `전필_첨단융합안전공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

81. [5055012501] `교육커뮤니케이션 및 교육평가분석`
   - 개설학과: `첨단융합안전공학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선_첨단융합안전공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

82. [5055137001] `건설안전관리론`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

83. [5055165601] `건설안전융합특론`
   - 개설학과: `첨단융합안전공학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선_첨단융합안전공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

84. [5055166201] `스마트안전기술특론`
   - 개설학과: `스마트산업안전공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_스마트산업안전공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

85. [5055166601] `스마트산업안전실무 I`
   - 개설학과: `스마트산업안전공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_스마트산업안전공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

86. [5055175701] `안전문화평가방법론`
   - 개설학과: `안전환경융합공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_안전환경융합공학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

87. [5055223301] `노인복지세미나`
   - 개설학과: `노인복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_노인복지전공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

88. [5055223601] `노인심리와 상담`
   - 개설학과: `노인복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_노인복지전공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

89. [5055223901] `노인복지와 테크놀로지·AI`
   - 개설학과: `노인복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_노인복지전공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

90. [5055270001] `서비스 커뮤니케이션과 고객경험`
   - 개설학과: `서비스경영학과`
   - 분류: `OTHER` / `교과목 전체/전공_서비스경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

91. [5055338301] `콘텐츠비즈니스와 IP`
   - 개설학과: `감성콘텐츠경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_감성콘텐츠경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

92. [5055338701] `감성지능`
   - 개설학과: `감성콘텐츠경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_감성콘텐츠경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

93. [5055339101] `교육에 대한 심리학적 접근`
   - 개설학과: `교육경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_교육경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

94. [5055339401] `부동산입지론`
   - 개설학과: `금융·부동산학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_금융·부동산`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

95. [5055339701] `기업재무관리 사례연구`
   - 개설학과: `금융·부동산학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_금융·부동산`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

96. [5055357201] `실전에서 검증된 B2B 경영전략`
   - 개설학과: `IT경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

97. [5055357801] `의사결정과학`
   - 개설학과: `이노비즈니스학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_이노비즈니스학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

98. [5055358101] `퍼스널브랜딩과 콘텐츠전략`
   - 개설학과: `인플루언서학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_인플루언서`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

99. [5055358401] `기획 및 조정관리`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

100. [5055358701] `글로벌 서비스경영전략`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

101. [5055359001] `기업의 경영과 혁신`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

102. [5055359301] `사회서비스와 공공경영`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

103. [5055359701] `글로벌경영`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

104. [5055360001] `경영전략과 문화`
   - 개설학과: `전문경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전문경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

105. [5055369101] `AI기술과 전문가`
   - 개설학과: `IT경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

106. [5055369301] `AI시대 딥테크 스타트업 전략`
   - 개설학과: `IT경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

107. [5055369501] `정보보안관리체계`
   - 개설학과: `정보보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_정보보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

108. [5055369901] `금융AI`
   - 개설학과: `금융IT학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI금융`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

109. [5055371201] `중소기업인사노무관리`
   - 개설학과: `중소기업경영혁신학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_중소기업경영혁신`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

110. [5055371801] `재난관리론`
   - 개설학과: `기업재난관리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기업재난관리학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

111. [5055372301] `재난관리 법제와 중대재해`
   - 개설학과: `기업재난관리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_기업재난관리학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

112. [5055372801] `콘텐츠커머스`
   - 개설학과: `감성콘텐츠경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_감성콘텐츠경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

113. [5055375301] `생성형 AI 기반 바이브코딩`
   - 개설학과: `AI비즈니스혁신학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI비즈니스혁신`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

114. [5055395101] `딥러닝과 LLM`
   - 개설학과: `인공지능학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

115. [5055396601] `인공지능 시장의 진화와 기업 및 정책 이슈`
   - 개설학과: `인공지능학과`
   - 분류: `MAJOR_ELECTIVE` / `전선_AI학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

116. [5055423901] `경영연구를위한인과추론`
   - 개설학과: `안보·공익경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_안보공익경영학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

117. [5055424101] `안보공익사회조사방법론`
   - 개설학과: `안보·공익경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_안보공익경영학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

118. [5055444101] `하이테크섬유개론`
   - 개설학과: `친환경화학소재융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_친환경화학소재융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

119. [5055444301] `기능성 염료 및 색소화학`
   - 개설학과: `친환경화학소재융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_친환경화학소재융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

120. [5055446801] `AI와 법`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

121. [5055447001] `AI와 크립토 논문세미나`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

122. [5055447201] `AI 테크 타이탄과 미래`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

123. [5055447401] `빅테크기업과 기술경영`
   - 개설학과: `IT정책경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

124. [5055449501] `국제통상환경협상론`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

125. [5055449701] `전력시스템운영및제어`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

126. [5055449901] `데이터센터와전력망`
   - 개설학과: `에너지정책·기술융합학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_에너지정책기술융합`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

127. [5055451801] `느낌의 인공지능`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

128. [5055452001] `XR스토리텔링`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

129. [5055452201] `자연어 처리 특론`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

130. [5055452401] `Agentic AI 응용`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

131. [5055452601] `메타버스 문화치유 콘텐츠`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

132. [5055452801] `사회정서학습 이론과 실제`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

133. [5055453001] `대학원논문연구:로봇지능`
   - 개설학과: `메타버스·문화콘텐츠학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_문화콘텐츠`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

134. [5055456901] `SoC설계프로젝트`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

135. [5055457101] `유선송수신기`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

136. [5055457301] `메모리신뢰성특론`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

137. [5055457501] `Process Integration 실무양성`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

138. [5055460201] `최신 반도체 공정 및 분석`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

139. [5055460401] `멀티모달인공지능`
   - 개설학과: `지능형반도체학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_지능형반도체학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

140. [5055463501] `대학원 논문연구: 프라이버시 강화 기술`
   - 개설학과: `미디어학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_미디어`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

141. [5055463701] `AI와 디지털미디어`
   - 개설학과: `미디어학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_미디어`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

142. [5055501601] `의학통계학`
   - 개설학과: `AI바이오학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI바이오`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

143. [5055501801] `AI와 국가데이터`
   - 개설학과: `AI바이오학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI바이오`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

144. [5055502001] `바이오 머신러닝`
   - 개설학과: `AI바이오학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI바이오`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

145. [5055504001] `IT유통물류세미나`
   - 개설학과: `IT유통물류학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_IT유통물류학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

146. [5055505901] `클라우드보안특론`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

147. [5055506201] `머신러닝특론`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

148. [5055506401] `Co-op 프로젝트1`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

149. [5055510801] `인공지능융합프로젝트`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

150. [5055511001] `데이터마이닝특론`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

151. [5055511201] `자동차위협분석`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

152. [5055511401] `고급운영체제`
   - 개설학과: `AI융합보안학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_AI융학보안`
   - 원본 대상: `전체`
   - 이슈: `TARGET_ROW_MISSING`
   - target diff: missing=1, extra=0

153. [2150017501] `벤처중소기업 M&A 및 Exit`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

154. [2150048701] `신기술벤처창업`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

155. [2150048801] `Social Entrepreneurship`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

156. [2150059401] `[심화]Co-op SAP 트랙`
   - 개설학과: `SW교육팀`
   - 분류: `MAJOR_ELECTIVE` / `전선-AI융합/전선-IT융합/전선-경영학부/전선-글로벌미디어/전선-산업·정보/전선-소프트/전선-컴퓨터`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=7

157. [2150080001] `ICT및스마트기술`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

158. [2150080501] `기술경영과혁신전략`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

159. [2150156801] `Co-op클라우드트랙`
   - 개설학과: `SW교육팀`
   - 분류: `MAJOR_ELECTIVE` / `전선-AI융합/전선-IT융합/전선-글로벌미디어/전선-소프트/전선-컴퓨터`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=5

160. [2150636601] `벤처중소마케팅`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

161. [2150648001] `경제원론`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

162. [2150648101] `회계원리`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

163. [2150649801] `공급사슬관리`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

164. [2150656601] `Entrepreneurship`
   - 개설학과: `벤처경영학과(계약학과)`
   - 분류: `MAJOR_ELECTIVE` / `전선-벤처경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

165. [2160138501] `형사판례연구`
   - 개설학과: `법학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_법학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

166. [2160144801] `물권법특수연구`
   - 개설학과: `법학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_법학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

167. [2160151301] `M＆A법특수연구`
   - 개설학과: `법학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_법학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

168. [2160173901] `연구조사방법론`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

169. [2160175901] `서비스마케팅`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

170. [2160181801] `전략적품질경영`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

171. [2160182301] `SCM및응용`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

172. [2160186201] `이문화관리론`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

173. [2160186701] `재무이론`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

174. [2160186901] `고급금융시계열세미나`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

175. [2160188201] `실증가격결정론`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

176. [2160194201] `국제금융론`
   - 개설학과: `무역학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_무역`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

177. [2160197001] `국제계약론`
   - 개설학과: `무역학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_무역`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

178. [2160198401] `국제거래법연습`
   - 개설학과: `무역학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_무역`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

179. [2160200701] `국제마케팅연습`
   - 개설학과: `무역학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_무역`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

180. [2160206801] `고급사회복지조사론(박사)`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

181. [2160208401] `사회복지조사론`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

182. [2160210801] `사례관리론`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

183. [2160212001] `사회복지자료분석론`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

184. [2160212801] `복지국가론`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

185. [2160222001] `행정계량분석Ⅰ`
   - 개설학과: `행정학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_행정`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

186. [2160222901] `인사행정세미나`
   - 개설학과: `행정학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_행정`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

187. [2160225501] `고급정책이론`
   - 개설학과: `행정학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_행정`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

188. [2160228501] `회계학연구조사방법론Ⅱ`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_회계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

189. [2160229701] `재무회계세미나`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_회계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

190. [2160230501] `관리회계특수연구`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_회계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

191. [2160235401] `현대정치사상`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정외`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

192. [2160238302] `강대국과한반도분단문제`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정외`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

193. [2160238401] `한국정치론`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정외`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

194. [2160250401] `벤처 중소기업 마케팅 세미나`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

195. [2160251701] `중소.벤처기업지원육성정책세미나`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

196. [2160253901] `기업인수합병 및 전략적제휴`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

197. [2160277001] `복소수함수론`
   - 개설학과: `수학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_수학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

198. [2160278801] `편미분방정식론Ⅰ`
   - 개설학과: `수학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_수학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

199. [2160287601] `고전역학`
   - 개설학과: `물리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_물리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

200. [2160287901] `양자역학`
   - 개설학과: `물리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_물리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

201. [2160289701] `양자광학`
   - 개설학과: `물리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_물리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

202. [2160292801] `방사광물리학`
   - 개설학과: `물리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_물리`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

203. [2160303201] `세미나Ⅱ`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

204. [2160306301] `생물물리화학`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

205. [2160310701] `고급생유기화학`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

206. [2160313401] `단백질정제론`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

207. [2160324801] `재무수리Ⅰ`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보통계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

208. [2160326701] `보험수리학 I`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보통계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

209. [2160332501] `매스커뮤니케이션이론1`
   - 개설학과: `언론홍보학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_언론`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

210. [2160342701] `일본어학특강`
   - 개설학과: `일어일문학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_일어일문학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

211. [2160343001] `일본어통사론`
   - 개설학과: `일어일문학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_일어일문학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

212. [2160345301] `일본대중문화론`
   - 개설학과: `일어일문학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_일어일문학과`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

213. [2160349001] `정보화와한국사회`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보사회`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

214. [2160349201] `정보사회공동체의이해`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보사회`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

215. [2160349301] `인터넷과사회운동`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보사회`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

216. [2160350701] `디스플레이공학`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

217. [2160353701] `고분자물성론`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

218. [2160382401] `시계열분석`
   - 개설학과: `전자공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전자`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

219. [2160388201] `안테나이론및설계`
   - 개설학과: `전자공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전자`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

220. [2160391801] `인공지능`
   - 개설학과: `전자공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전자`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

221. [2160412401] `계장제어시스템`
   - 개설학과: `전기공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전기`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

222. [2160413401] `계측신호처리특론`
   - 개설학과: `전기공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전기`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

223. [2160418001] `전력계통설비의설계및응용`
   - 개설학과: `전기공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전기`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

224. [2160421601] `고급제어공학`
   - 개설학과: `전기공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전기`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

225. [2160459101] `신뢰성공학`
   - 개설학과: `기계공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

226. [2160460301] `로버스트제어`
   - 개설학과: `기계공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

227. [2160468001] `최적화특론`
   - 개설학과: `산업·정보시스템공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_산업정보`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

228. [2160484601] `주거단지계획론`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

229. [2160485301] `현대건축의결정적건물연구`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

230. [2160487701] `데이터베이스관리시스템응용건축설계`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

231. [2160489601] `내진공학`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

232. [2160655201] `지역사회복지론`
   - 개설학과: `사회복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지/전선_사회복지행정·정책`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

233. [2160656101] `사회복지정책론`
   - 개설학과: `사회복지전공`
   - 분류: `MAJOR_REQUIRED` / `전필_사회복지/전필_사회복지행정·정책`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

234. [2160657101] `강점관점실천실기`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

235. [2160657501] `미술치료`
   - 개설학과: `상담복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

236. [2160664401] `이상심리`
   - 개설학과: `상담복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

237. [2160665401] `호스피스연구`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

238. [2160666101] `사례관리론`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

239. [5006762501] `기초공학수학1`
   - 개설학과: `수학과`
   - 분류: `MAJOR_BASIC` / `전기-AI융합/전기-IT융합/전기-건축공학/전기-건축학부/전기-기계/전기-산업·정보/전기-신소재/전기-전기/전기-전자공학/전기-컴퓨터/전기-화공`
   - 원본 대상: `2학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전자정보공학부-IT융합, 전자정보공학부-전자공학, 컴퓨터 / 3학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전자정보공학부-IT융합, 전자정보공학부-전자공학, 컴퓨터 / 4학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전`
   - 이슈: `TARGET_PARSE_MISMATCH, UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `전`
   - target diff: missing=0, extra=3

240. [5006762502] `기초공학수학1`
   - 개설학과: `수학과`
   - 분류: `MAJOR_BASIC` / `전기-AI융합/전기-IT융합/전기-건축공학/전기-건축학부/전기-기계/전기-산업·정보/전기-신소재/전기-전기/전기-전자공학/전기-컴퓨터/전기-화공`
   - 원본 대상: `2학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전자정보공학부-IT융합, 전자정보공학부-전자공학, 컴퓨터 / 3학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전자정보공학부-IT융합, 전자정보공학부-전자공학, 컴퓨터 / 4학년 신소재, 전기, 기계, 화공, 산업정보 ,건축학부, 건축공학, AI융합, 전`
   - 이슈: `TARGET_PARSE_MISMATCH, UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `전`
   - target diff: missing=0, extra=3

241. [5008407901] `건축환경론`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

242. [5008418701] `소비자 정보처리이론`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

243. [5010623101] `사회복지실천론(3학점)`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_REQUIRED` / `전필_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

244. [5011906601] `사회복지실천기술론(3학점)`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_REQUIRED` / `전필_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

245. [5012482401] `할리우드 영화 커뮤니케이션`
   - 개설학과: `언론홍보학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_언론`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

246. [5022822702] `법사상사연구`
   - 개설학과: `법학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_법학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

247. [5023547901] `계리실무Ⅰ`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_정보통계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

248. [5023557901] `건물에너지해석세미나Ⅱ`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

249. [5023619101] `여성복지론`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

250. [5027628101] `기독론`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

251. [5031664301] `사회복지실천세미나`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

252. [5036647601] `기계학습과 인공지능`
   - 개설학과: `전자공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전자`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

253. [5036706201] `심리측정 및 평가`
   - 개설학과: `상담복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_상담복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

254. [5037708901] `교육복지론`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

255. [5038951701] `개인행동의 이해`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

256. [5039937701] `고급진로상담세미나`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

257. [5040638801] `심리검사와 사례적용`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

258. [5042265501] `청소년심리 및 상담 세미나`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

259. [5042544701] `사회복지인적자원관리`
   - 개설학과: `복지경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_복지경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

260. [5043732001] `디지털 마케팅`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

261. [5043745401] `연료전지시스템공학`
   - 개설학과: `기계공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기계`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

262. [5044741501] `상담심리학특론`
   - 개설학과: `복지경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_복지경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

263. [5045441401] `성격심리학`
   - 개설학과: `복지경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_복지경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

264. [5048225101] `스타트업 세미나`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

265. [5048287501] `현대복지국가의 이슈와 쟁점`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

266. [5049632201] `전기분석화학개론`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

267. [5051008401] `대상관계이론`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

268. [5051036801] `다문화와 사회복지`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

269. [5051039201] `지진방재 산학 특강`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

270. [5051039401] `비파괴시험`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

271. [5051041701] `박막공학특론`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_신소재·파이버`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

272. [5051056301] `융복합디지털사회복지`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

273. [5052210101] `가족상담 및 가족치료`
   - 개설학과: `상담복지전공`
   - 분류: `MAJOR_REQUIRED` / `전필_상담복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

274. [5052210801] `지속가능경영과 사회공헌`
   - 개설학과: `NGO·기업사회공헌전공`
   - 분류: `MAJOR_REQUIRED` / `전필_NGO·기업사회공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

275. [5052309401] `정보보안과경영`
   - 개설학과: `경영학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_경영`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

276. [5052409501] `구조지질학`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

277. [5053160001] `극자외선 리소그래피`
   - 개설학과: `전자공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전자`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

278. [5053169001] `에너지 재료공학개론`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_신소재·파이버`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

279. [5053185501] `비영리조직과 국제개발협력`
   - 개설학과: `NGO·기업사회공헌전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_NGO·기업사회공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

280. [5053201401] `중독과 사회복지실천`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

281. [5053201801] `NGO 모금과 자원개발`
   - 개설학과: `NGO·기업사회공헌전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_NGO·기업사회공`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

282. [5053328001] `재난관리론`
   - 개설학과: `건축학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_건축`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

283. [5054495201] `사회복지와 공공행정`
   - 개설학과: `사회복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

284. [5054542501] `엔지니어링섬유소재`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_섬유`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

285. [5055184601] `비영리조직의 마케팅`
   - 개설학과: `사회복지전공`
   - 분류: `OTHER` / `공통 전공선택`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

286. [5055222701] `사회복지역사`
   - 개설학과: `사회복지전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

287. [5055223001] `인지행동치료`
   - 개설학과: `사회복지실천전공`
   - 분류: `MAJOR_ELECTIVE` / `전선_사회복지실천`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

288. [5055409601] `누가복음`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

289. [5055409801] `엘리야-엘리사 내러티브`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

290. [5055410001] `고급심리통계`
   - 개설학과: `기독교학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_기독교`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

291. [5055438601] `최적화 알고리즘`
   - 개설학과: `수학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_수학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

292. [5055438801] `딥러닝의 수학적 기초`
   - 개설학과: `수학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_수학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

293. [5055444501] `고급분리공정특론`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

294. [5055444701] `화공최적화및머신러닝`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_화학공학`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

295. [5055465001] `구조방정식 개론`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

296. [5055471401] `개별지도연구 Ⅵ`
   - 개설학과: `언론홍보학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_언론`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

297. [5055471901] `인지행동치료`
   - 개설학과: `사회복지학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_사복`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

298. [5055495501] `광정보처리`
   - 개설학과: `전기공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_전기`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

299. [5055496401] `AI 논문 연구 I`
   - 개설학과: `벤처중소기업학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_벤처`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

300. [5055503201] `창의공학연습`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전공_섬유`
   - 원본 대상: `전체`
   - 이슈: `TARGET_PARSE_MISMATCH`
   - target diff: missing=1, extra=1

301. [2150040801] `고분자물성`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화공`
   - 원본 대상: `4학년 화공,스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

302. [2150040802] `고분자물성`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화공`
   - 원본 대상: `4학년 화공,스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

303. [2150043301] `문화와철학`
   - 개설학과: `철학과`
   - 분류: `MAJOR_BASIC` / `전기-철학`
   - 원본 대상: `1학년 철학, 뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

304. [2150046801] `International organization & Treaty`
   - 개설학과: `국제법무학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국제법무`
   - 원본 대상: `2학년 국제법무,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

305. [2150052001] `박물관학`
   - 개설학과: `사학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-사학`
   - 원본 대상: `4학년 사학,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

306. [2150078501] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 자유전공학부(A그룹/수) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `A그룹, 수`

307. [2150078504] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 법과대(법학,국제법무(A그룹/목), 자유전공(A그룹/목)) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `A그룹, 목`

308. [2150078505] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 경영대(경영,회계,벤처중소,금융(A그룹/목)) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `A그룹, 목`

309. [2150078507] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 자유전공학부(B그룹/수) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `B그룹, 수`

310. [2150078510] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 법과대(법학,국제법무(B그룹/목), 자유전공학부(B그룹/목)) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `B그룹, 목`

311. [2150078511] `소그룹채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 경영대(경영,회계,벤처중소,금융(B그룹/목)) (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `B그룹, 목`

312. [2150081801] `건축환경과안전`
   - 개설학과: `건축학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-건축공학`
   - 원본 대상: `전체학년 건축공학,스마트안전보건환경`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트안전보건환경`

313. [2150101501] `비전채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `2학년 경통대, IT대, AI대학(정보보호학과(계약) 포함)  (수강제한:1학년 인문,법,사회,경통,경영,자유전공)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `계약`

314. [2150101506] `비전채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `3학년 경통대, IT대, AI대(정보보호학과(계약) 포함)  (수강제한:1학년 인문,법,사회,경통,경영,자유전공)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `계약`

315. [2150101508] `비전채플`
   - 개설학과: `학원선교팀`
   - 분류: `CHAPEL` / `채플`
   - 원본 대상: `1학년 외국국적학생(1학년), IT대, AI대(정보보호학과(계약) 포함)  (수강제한:1학년 인문,법,사회,경통,경영,자유전공)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `계약`

316. [2150112701] `시장경제와지속가능한규제`
   - 개설학과: `행정학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-행정학부`
   - 원본 대상: `4학년 행정학부,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

317. [2150118301] `교육실습`
   - 개설학과: `학사팀`
   - 분류: `TEACHING` / `교직`
   - 원본 대상: `4학년 ;교직이수자 (실습학교 확정된 학생만 수강 가능)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `수강`

318. [2150129401] `노동경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

319. [2150129402] `노동경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

320. [2150140401] `무기화학`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화학`
   - 원본 대상: `3학년 화학, 순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

321. [2150141201] `빅데이터와경제분석기초`
   - 개설학과: `경제학과`
   - 분류: `OTHER` / ``
   - 원본 대상: `전체학년 순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

322. [2150152001] `분석화학1`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화학`
   - 원본 대상: `2학년 화학, 양자나노융합, 인공지능반도체융합, 순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

323. [2150185201] `유기화학1`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화학`
   - 원본 대상: `2학년 화학, 양자나노융합, 순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

324. [2150189001] `인식론`
   - 개설학과: `철학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-철학`
   - 원본 대상: `3학년 철학, 뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

325. [2150191501] `재정거버넌스와전략`
   - 개설학과: `행정학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-행정학부`
   - 원본 대상: `2학년 행정학부,사회적기업과사회혁신융합,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

326. [2150212401] `표본론`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-통계·보험`
   - 원본 대상: `3학년 통계보험,빅데이터융합,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

327. [2150220601] `행정학특강`
   - 개설학과: `행정학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-행정학부`
   - 원본 대상: `4학년 행정학부,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

328. [2150236201] `회귀분석1`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_REQUIRED` / `전필-통계·보험`
   - 원본 대상: `2학년 통계보험,빅데이터융합,AI모빌리티융합,여론조사컨설팅 (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

329. [2150236202] `회귀분석1`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_REQUIRED` / `전필-통계·보험`
   - 원본 대상: `2학년 통계보험,빅데이터융합,AI모빌리티융합,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

330. [2150267101] `미시경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_REQUIRED` / `전필-경제`
   - 원본 대상: `2학년 경제,동아시아경제통상융합,순환경제·친환경화학소재 ,경제`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

331. [2150267102] `미시경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_REQUIRED` / `전필-경제`
   - 원본 대상: `2학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

332. [2150267103] `미시경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_REQUIRED` / `전필-경제`
   - 원본 대상: `2학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

333. [2150267104] `미시경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_REQUIRED` / `전필-경제`
   - 원본 대상: `2학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

334. [2150324801] `수리통계1`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_REQUIRED` / `전필-통계·보험`
   - 원본 대상: `2학년 통계보험,여론조사컨설팅 (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

335. [2150324802] `수리통계1`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_REQUIRED` / `전필-통계·보험`
   - 원본 대상: `2학년 통계보험,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

336. [2150337801] `국제정치의이해`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `2학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

337. [2150340204] `조직개발론`
   - 개설학과: `경영학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부`
   - 원본 대상: `전체학년 경영학부;시간제 (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `시간제`

338. [2150348701] `공정시스템`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화공`
   - 원본 대상: `4학년 화공, 스마트안전보건환경`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트안전보건환경`

339. [2150353301] `산업조직과공정거래`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

340. [2150353302] `산업조직과공정거래`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

341. [2150356001] `비교정치`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `2학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

342. [2150358801] `기호논리학`
   - 개설학과: `철학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-철학`
   - 원본 대상: `2학년 철학,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

343. [2150361401] `세라믹공학`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-신소재`
   - 원본 대상: `3학년 신소재, 스마트소재/제품융합, 인공지능반도체융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

344. [2150368901] `프랑스문학과예술`
   - 개설학과: `불어불문학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-불문`
   - 원본 대상: `2학년 불문,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

345. [2150370501] `Understanding Foreign Policy`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `3학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

346. [2150389801] `초현실주의와현대시`
   - 개설학과: `불어불문학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-불문`
   - 원본 대상: `4학년 불문,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

347. [2150394701] `TV드라마창작실습`
   - 개설학과: `예술창작학부 문예창작전공`
   - 분류: `MAJOR_ELECTIVE` / `전선-문예창작`
   - 원본 대상: `2학년 문예창작전공, 문화예술마케팅, 소셜벤처미디어`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화예술마케팅, 소셜벤처미디어`

348. [2150401201] `사회조사방법론`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정보사회`
   - 원본 대상: `2학년 정보사회, 문화예술마케팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화예술마케팅`

349. [2150431601] `비교정치경제`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `2학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

350. [2150434401] `기업경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

351. [2150434402] `기업경제학`
   - 개설학과: `경제학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경제`
   - 원본 대상: `3학년 경제,동아시아경제통상융합,순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

352. [2150446407] `품질경영`
   - 개설학과: `경영학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부`
   - 원본 대상: `전체학년 경영학부;시간제 (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `시간제`

353. [2150459101] `생명정보개론`
   - 개설학과: `의생명시스템학부`
   - 분류: `MAJOR_REQUIRED` / `전필-의생명시스템`
   - 원본 대상: `1학년 의생명시스템, 빅데이터컴퓨팅, 지식재산`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `빅데이터컴퓨팅, 지식재산`

354. [2150459102] `생명정보개론`
   - 개설학과: `의생명시스템학부`
   - 분류: `MAJOR_REQUIRED` / `전필-의생명시스템`
   - 원본 대상: `1학년 의생명시스템, 빅데이터컴퓨팅, 지식재산`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `빅데이터컴퓨팅, 지식재산`

355. [2150459701] `정보사회학`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정보사회`
   - 원본 대상: `2학년 정보사회,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

356. [2150465601] `PR론`
   - 개설학과: `언론홍보학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-언론홍보`
   - 원본 대상: `2학년 언론홍보, 순수 외국인 수강 제한`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `수강, 순수, 외국인`

357. [2150472601] `전산통계2`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-통계·보험`
   - 원본 대상: `3학년 통계보험,빅데이터융합,AI모빌리티융합,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

358. [2150472602] `전산통계2`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-통계·보험`
   - 원본 대상: `3학년 통계보험,빅데이터융합,AI모빌리티융합,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

359. [2150492501] `인터넷과사회운동`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정보사회`
   - 원본 대상: `3학년 정보사회,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

360. [2150501401] `비평론`
   - 개설학과: `예술창작학부 문예창작전공`
   - 분류: `MAJOR_REQUIRED` / `전필-문예창작`
   - 원본 대상: `3학년 문예창작전공,문화서비스산업`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화서비스산업`

361. [2150501701] `시세미나`
   - 개설학과: `예술창작학부 문예창작전공`
   - 분류: `MAJOR_ELECTIVE` / `전선-문예창작`
   - 원본 대상: `4학년 문예창작전공,문화서비스산업`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화서비스산업`

362. [2150516503] `고분자화학`
   - 개설학과: `화학공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화공`
   - 원본 대상: `3학년 화공, 스마트소재/제품융`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재, 제품융`

363. [2150530501] `사회통계1`
   - 개설학과: `정보사회학과`
   - 분류: `MAJOR_REQUIRED` / `전필-정보사회`
   - 원본 대상: `2학년 정보사회, 뉴미디어마케팅융합, 문화예술마케팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화예술마케팅`

364. [2150542301] `구비문학의이해`
   - 개설학과: `국어국문학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국문`
   - 원본 대상: `2학년 국문,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

365. [2150547204] `서비스마케팅`
   - 개설학과: `경영학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부`
   - 원본 대상: `전체학년 경영학부;시간제 (대상외수강제한)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `시간제`

366. [2150547801] `추정검정론`
   - 개설학과: `정보통계·보험수리학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-통계·보험`
   - 원본 대상: `3학년 통계보험,여론조사컨설팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `여론조사컨설팅`

367. [2150548301] `스마트웨어러블`
   - 개설학과: `신소재공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-신소재`
   - 원본 대상: `4학년 신소재, 스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

368. [2150560201] `북한의정치와경제`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `2학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

369. [2150562301] `담화와텍스트`
   - 개설학과: `국어국문학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국문`
   - 원본 대상: `3학년 국문, 뉴미디어콘텐츠 / 4학년 국문, 뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

370. [2150569601] `고전문학과콘텐츠`
   - 개설학과: `국어국문학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국문`
   - 원본 대상: `3학년 국문, 뉴미디어콘텐츠 / 4학년 국문, 뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

371. [2150580801] `기초생화학`
   - 개설학과: `화학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-화학`
   - 원본 대상: `3학년 화학, 양자나노융합, 순환경제·친환경화학소재`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `순환경제·친환경화학소재`

372. [2150581501] `사회보장론`
   - 개설학과: `사회복지학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-사회복지`
   - 원본 대상: `3학년 사회복지,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

373. [2150615901] `지역사회교육론`
   - 개설학과: `평생교육학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-평생교육`
   - 원본 대상: `2학년 평생교육,통일외교 및 개발협력융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교`

374. [2150626701] `동아시아법`
   - 개설학과: `국제법무학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국제법무`
   - 원본 대상: `4학년 국제법무학과,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

375. [2150635401] `디스플레이원리`
   - 개설학과: `전기공학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-IT융합/전선-전기/전선-전자공학`
   - 원본 대상: `3학년 전기,전자공학전공,IT융합전공(디스플레이트랙)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `디스플레이트랙`

376. [2150635402] `디스플레이원리`
   - 개설학과: `전기공학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-IT융합/전선-전기/전선-전자공학`
   - 원본 대상: `3학년 전기,전자공학전공,IT융합전공(디스플레이트랙)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `디스플레이트랙`

377. [2150635403] `디스플레이원리`
   - 개설학과: `전기공학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-IT융합/전선-전기/전선-전자공학`
   - 원본 대상: `3학년 전기,전자공학전공,IT융합전공(디스플레이트랙)`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `디스플레이트랙`

378. [2150642701] `재무회계`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부/전필-금융학부/전필-벤처중소/전필-회계학과`
   - 원본 대상: `2학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 3학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 4학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `보험계리리스크`

379. [2150642702] `재무회계`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부/전필-금융학부/전필-벤처중소/전필-회계학과`
   - 원본 대상: `2학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 3학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 4학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `보험계리리스크`

380. [2150642703] `재무회계`
   - 개설학과: `회계학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-경영학부/전필-금융학부/전필-벤처중소/전필-회계학과`
   - 원본 대상: `2학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 3학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크 / 4학년 경영학부 ,회계학과 ,벤처중소 ,금융, 보험계리리스크`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `보험계리리스크`

381. [2150647901] `투자론`
   - 개설학과: `금융학부`
   - 분류: `MAJOR_REQUIRED` / `전필-금융학부`
   - 원본 대상: `2학년 금융, 동아시아경제통상`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `동아시아경제통상`

382. [2150647902] `투자론`
   - 개설학과: `금융학부`
   - 분류: `MAJOR_REQUIRED` / `전필-금융학부`
   - 원본 대상: `2학년 금융, 동아시아경제통상`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `동아시아경제통상`

383. [2150660101] `AI예술과매체서사`
   - 개설학과: `예술창작학부 문예창작전공`
   - 분류: `MAJOR_ELECTIVE` / `전선-문예창작`
   - 원본 대상: `2학년 문예창작전공, 문화서비스산업, 문예예술마케팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문예예술마케팅, 문화서비스산업`

384. [2150660501] `Intellectual Property Law`
   - 개설학과: `국제법무학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국제법무`
   - 원본 대상: `4학년 국제법무학과, 지식재산융합, 통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

385. [2150664701] `Int'l Business Transaction`
   - 개설학과: `국제법무학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-국제법무`
   - 원본 대상: `4학년 국제법무학과,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

386. [2150664901] `확률통계2`
   - 개설학과: `산업·정보시스템공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-산업·정보`
   - 원본 대상: `2학년 산업정보, ICT유통물류융합, 스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

387. [2150664902] `확률통계2`
   - 개설학과: `산업·정보시스템공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-산업·정보`
   - 원본 대상: `2학년 산업정보, ICT유통물류융합, 스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

388. [2150664904] `확률통계2`
   - 개설학과: `산업·정보시스템공학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-산업·정보`
   - 원본 대상: `2학년 산업정보, ICT유통물류융합, 스마트소재/제품융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `스마트소재`

389. [2150667201] `산업기술정책`
   - 개설학과: `행정학부`
   - 분류: `MAJOR_ELECTIVE` / `전선-행정학부`
   - 원본 대상: `4학년 행정학부,통일외교및개발협력,사회적기업과사회혁신융합`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

390. [2150667301] `글로벌협력과발전`
   - 개설학과: `정치외교학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-정외`
   - 원본 대상: `3학년 정외,통일외교및개발협력`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `통일외교및개발협력`

391. [2150673401] `게임과서사`
   - 개설학과: `예술창작학부 문예창작전공`
   - 분류: `MAJOR_ELECTIVE` / `전선-문예창작`
   - 원본 대상: `3학년 문예창작전공,문화서비스산업,융합창업연계,문화예술마케팅`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `문화서비스산업, 문화예술마케팅`

392. [2150677501] `GTEP무역실습1`
   - 개설학과: `글로벌통상학과`
   - 분류: `MAJOR_ELECTIVE` / `전선-글로벌통상`
   - 원본 대상: `전체학년 글로벌통상, GTEP 실습생`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `GTEP, 실습생`

393. [2150820601] `중국사입문`
   - 개설학과: `사학과`
   - 분류: `MAJOR_BASIC` / `전기-사학`
   - 원본 대상: `1학년 사학,뉴미디어콘텐츠`
   - 이슈: `UNMAPPED_NON_FUSION_TOKEN`
   - 비-융합 잔여 토큰: `뉴미디어콘텐츠`

