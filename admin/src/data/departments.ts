export interface College {
  name: string;
  departments: string[];
}

export const colleges: College[] = [
  {
    name: '차세대반도체학과',
    departments: ['차세대반도체학과']
  },
  {
    name: 'IT대학',
    departments: [
      'AI융합학부',
      '글로벌미디어학부',
      '미디어경영학과',
      '소프트웨어학부',
      '전자정보공학부 IT융합전공',
      '전자정보공학부 전자공학전공',
      '컴퓨터학부'
    ]
  },
  {
    name: 'AI대학',
    departments: [
      'AI소프트웨어학부',
      '정보보호학과'
    ]
  },
  {
    name: '경영대학',
    departments: [
      '경영학부',
      '금융학부',
      '벤처경영학과',
      '벤처중소기업학과',
      '복지경영학과',
      '혁신경영학과',
      '회계세무학과',
      '회계학과'
    ]
  },
  {
    name: '경제통상대학',
    departments: [
      '경제학과',
      '국제무역학과',
      '글로벌통상학과',
      '금융경제학과',
      '통상산업학과'
    ]
  },
  {
    name: '공과대학',
    departments: [
      '건축학부 건축공학전공',
      '건축학부 건축학부',
      '건축학부 건축학전공',
      '건축학부 실내건축전공',
      '기계공학부',
      '산업정보시스템공학과',
      '신소재공학과',
      '전기공학부',
      '화학공학과'
    ]
  },
  {
    name: '법과대학',
    departments: [
      '국제법무학과',
      '법학과'
    ]
  },
  {
    name: '자유전공학부',
    departments: [
      '자유전공학부'
    ]
  },
  {
    name: '사회과학대학',
    departments: [
      '사회복지학부',
      '언론홍보학과',
      '정보사회학과',
      '정치외교학과',
      '평생교육학과',
      '행정학부'
    ]
  },
  {
    name: '인문대학',
    departments: [
      '국어국문학과',
      '기독교학과',
      '독어독문학과',
      '불어불문학과',
      '사학과',
      '스포츠학부',
      '영어영문학과',
      '예술창작학부 문예창작전공',
      '예술창작학부 영화예술전공',
      '일어일문학과',
      '중어중문학과',
      '철학과'
    ]
  },
  {
    name: '자연과학대학',
    departments: [
      '물리학과',
      '수학과',
      '의생명시스템학부',
      '정보통계보험수리학과',
      '화학과'
    ]
  }
];

export const departments = colleges.flatMap(college => college.departments).sort();

export const categories = [
  { value: 'MAJOR_BASIC', label: '전기' },
  { value: 'MAJOR_ELECTIVE', label: '전선' },
  { value: 'MAJOR_REQUIRED', label: '전필' },
  { value: 'GENERAL_REQUIRED', label: '교필' },
  { value: 'GENERAL_ELECTIVE', label: '교선' },
  { value: 'CHAPEL', label: '채플' },
  { value: 'OTHER', label: '기타' },
];

export const grades = [1, 2, 3, 4, 5];
