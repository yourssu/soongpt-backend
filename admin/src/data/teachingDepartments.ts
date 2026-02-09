// 교직 이수 가능 학과 목록
// 단과대학별 설치 학과 및 표시과목
export const teachingEligibleDepartments = [
  // 인문대학
  '국어국문학과', // 국어
  '영어영문학과', // 영어
  '독어독문학과', // 독일어
  '불어불문학과', // 프랑스어
  '중어중문학과', // 중국어
  '일어일문학과', // 일본어
  '철학과', // 철학
  '사학과', // 역사

  // 자연과학대학
  '수학과', // 수학
  '물리학과', // 물리
  '화학과', // 화학

  // 경제통상대학
  '경제학과', // 일반사회
  '글로벌통상학과', // 상업

  // 경영대학
  '경영학부', // 상업
  '회계학과', // 상업

  // 공과대학
  '화학공학과', // 화공
  '전기공학부', // 전기

  // IT대학
  '컴퓨터학부', // 정보·컴퓨터
  '전자정보공학부', // 전자, 통신 (전자공학전공, IT융합전공 포함)
  'IT융합전공', // 전자, 통신
];

export const isTeachingEligible = (department: string): boolean => {
  return teachingEligibleDepartments.some(dept =>
    department.includes(dept) || dept.includes(department)
  );
};
