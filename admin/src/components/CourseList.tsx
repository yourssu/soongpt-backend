import { useState, useEffect } from 'react';
import { courseApi } from '../api/courseApi';
import type { Course, CoursesResponse } from '../types/course';
import './CourseList.css';

export const CourseList = () => {
  const [courses, setCourses] = useState<CoursesResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);

  const fetchCourses = async (page: number, query: string) => {
    try {
      setLoading(true);
      setError(null);
      const data = await courseApi.getAllCourses({
        q: query,
        page,
        size: pageSize,
        sort: 'ASC',
      });
      setCourses(data);
    } catch (err) {
      setError('과목을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses(currentPage, searchQuery);
  }, [currentPage, searchQuery]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
  };

  const getCategoryLabel = (category: string): string => {
    const labels: Record<string, string> = {
      MAJOR_REQUIRED: '전필',
      MAJOR_ELECTIVE: '전선',
      MAJOR_BASIC: '전기',
      GENERAL_REQUIRED: '교필',
      GENERAL_ELECTIVE: '교선',
      CHAPEL: '채플',
      OTHER: '기타',
    };
    return labels[category] || category;
  };

  return (
    <div className="course-list-container">
      <h1>과목 관리</h1>

      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          placeholder="과목명 또는 교수명으로 검색"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="search-input"
        />
        <button type="submit" className="search-button">검색</button>
      </form>

      {loading && <div className="loading">로딩 중...</div>}
      {error && <div className="error">{error}</div>}

      {courses && !loading && (
        <>
          <div className="course-info">
            총 {courses.totalElements}개의 과목 (페이지 {courses.page + 1} / {courses.totalPages})
          </div>

          <div className="table-container">
            <table className="course-table">
              <thead>
                <tr>
                  <th>코드</th>
                  <th>과목명</th>
                  <th>교수</th>
                  <th>이수구분</th>
                  <th>학과</th>
                  <th>학점</th>
                  <th>시간</th>
                  <th>정원</th>
                  <th>강의실</th>
                </tr>
              </thead>
              <tbody>
                {courses.content.map((course: Course) => (
                  <tr key={course.id || course.code}>
                    <td>{course.code}</td>
                    <td>{course.name}</td>
                    <td>{course.professor || '-'}</td>
                    <td>{getCategoryLabel(course.category)}</td>
                    <td>{course.department}</td>
                    <td>{course.point}</td>
                    <td>{course.time}</td>
                    <td>{course.personeel}</td>
                    <td>{course.scheduleRoom}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="pagination-button"
            >
              이전
            </button>
            <span className="pagination-info">
              {currentPage + 1} / {courses.totalPages}
            </span>
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage >= courses.totalPages - 1}
              className="pagination-button"
            >
              다음
            </button>
          </div>
        </>
      )}
    </div>
  );
};