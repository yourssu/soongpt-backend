import { useState, useEffect } from 'react';
import { courseApi } from '../api/courseApi';
import type { Course, CoursesResponse, CourseTargetResponse, TargetInfo } from '../types/course';
import './CourseList.css';

export const CourseList = () => {
  const [courses, setCourses] = useState<CoursesResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);
  const [pageInput, setPageInput] = useState('');
  const [selectedCourse, setSelectedCourse] = useState<CourseTargetResponse | null>(null);
  const [targetLoading, setTargetLoading] = useState(false);

  // 검색어 디바운싱
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(searchQuery);
      setCurrentPage(0); // 검색어 변경 시 첫 페이지로
    }, 500);

    return () => clearTimeout(timer);
  }, [searchQuery]);

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
    fetchCourses(currentPage, debouncedQuery);
  }, [currentPage, debouncedQuery]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    setPageInput('');
  };

  const handlePageInputSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!courses) return;

    const page = parseInt(pageInput, 10);
    if (!isNaN(page) && page >= 1 && page <= courses.totalPages) {
      setCurrentPage(page - 1);
    } else {
      alert(`1부터 ${courses.totalPages}까지의 숫자를 입력해주세요.`);
    }
  };

  const handlePageJump = (offset: number) => {
    if (!courses) return;
    const newPage = currentPage + offset;
    if (newPage >= 0 && newPage < courses.totalPages) {
      setCurrentPage(newPage);
    }
  };

  const renderPageNumbers = () => {
    if (!courses) return null;

    const totalPages = courses.totalPages;
    const current = currentPage;
    const pageNumbers: (number | string)[] = [];
    const minDisplay = 5; // 최소 5개 표시

    if (totalPages <= 7) {
      // 전체 페이지가 7개 이하면 모두 표시
      for (let i = 0; i < totalPages; i++) {
        pageNumbers.push(i);
      }
    } else {
      // 첫 페이지는 항상 표시
      pageNumbers.push(0);

      // 현재 페이지 기준으로 최소 5개 표시
      let start = Math.max(1, current - 2);
      let end = Math.min(totalPages - 2, current + 2);

      // 최소 5개를 보장
      if (end - start + 1 < minDisplay) {
        if (start === 1) {
          end = Math.min(totalPages - 2, start + minDisplay - 1);
        } else if (end === totalPages - 2) {
          start = Math.max(1, end - minDisplay + 1);
        }
      }

      if (start > 1) {
        pageNumbers.push('...');
      }

      for (let i = start; i <= end; i++) {
        pageNumbers.push(i);
      }

      if (end < totalPages - 2) {
        pageNumbers.push('...');
      }

      // 마지막 페이지는 항상 표시
      pageNumbers.push(totalPages - 1);
    }

    return pageNumbers.map((pageNum, index) => {
      if (pageNum === '...') {
        return (
          <span key={`ellipsis-${index}`} className="pagination-ellipsis">
            ...
          </span>
        );
      }

      const page = pageNum as number;
      return (
        <button
          key={page}
          onClick={() => handlePageChange(page)}
          className={`pagination-number ${current === page ? 'active' : ''}`}
        >
          {page + 1}
        </button>
      );
    });
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

  const handleCourseClick = async (course: Course) => {
    try {
      setTargetLoading(true);
      const targetData = await courseApi.getCourseTarget(course.code);
      setSelectedCourse(targetData);
    } catch (err) {
      console.error('수강 대상 조회 실패:', err);
      alert('수강 대상 정보를 불러오는데 실패했습니다.');
    } finally {
      setTargetLoading(false);
    }
  };

  const closeModal = () => {
    setSelectedCourse(null);
  };

  const getStudentTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      GENERAL: '일반',
      FOREIGNER: '외국인',
      MILITARY: '군위탁',
      TEACHING_CERT: '교직이수자',
    };
    return labels[type] || type;
  };

  return (
    <div className="course-list-container">
      <h1>과목 관리</h1>

      <form onSubmit={handleSearch} className="search-form">
        <div className="search-input-wrapper">
          <input
            type="text"
            placeholder="과목명 또는 교수명으로 검색"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
          {searchQuery !== debouncedQuery && (
            <div className="search-spinner"></div>
          )}
        </div>
        <button type="submit" className="search-button">검색</button>
      </form>

      {error && <div className="error">{error}</div>}

      {loading && (
        <div className="loading-overlay">
          <div className="spinner"></div>
          <div className="loading-text">로딩 중...</div>
        </div>
      )}

      {courses && (
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
                  <th>수강대상</th>
                </tr>
              </thead>
              <tbody>
                {courses.content.map((course: Course) => (
                  <tr
                    key={course.id || course.code}
                    onClick={() => handleCourseClick(course)}
                    style={{ cursor: 'pointer' }}
                  >
                    <td>{course.code}</td>
                    <td>{course.name}</td>
                    <td>{course.professor || '-'}</td>
                    <td>{getCategoryLabel(course.category)}</td>
                    <td>{course.department}</td>
                    <td>{course.point}</td>
                    <td>{course.time}</td>
                    <td>{course.personeel}</td>
                    <td>{course.scheduleRoom}</td>
                    <td>{course.target}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination-wrapper">
            <div className="pagination">
              <button
                onClick={() => handlePageJump(-10)}
                disabled={currentPage < 10}
                className="pagination-button"
                title="10페이지 이전"
              >
                ≪
              </button>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="pagination-button"
              >
                이전
              </button>

              <div className="pagination-numbers">
                {renderPageNumbers()}
              </div>

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= courses.totalPages - 1}
                className="pagination-button"
              >
                다음
              </button>
              <button
                onClick={() => handlePageJump(10)}
                disabled={currentPage >= courses.totalPages - 10}
                className="pagination-button"
                title="10페이지 다음"
              >
                ≫
              </button>
            </div>

            <form onSubmit={handlePageInputSubmit} className="page-jump">
              <span className="page-jump-label">페이지 이동:</span>
              <input
                type="number"
                min="1"
                max={courses.totalPages}
                placeholder={String(currentPage + 1)}
                value={pageInput || currentPage + 1}
                onChange={(e) => setPageInput(e.target.value)}
                className="page-input"
              />
              <button type="submit" className="page-jump-button">이동</button>
            </form>
          </div>
        </>
      )}

      {selectedCourse && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>수강 대상 정보</h2>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="course-info-detail">
                <p><strong>과목코드:</strong> {selectedCourse.code}</p>
                <p><strong>과목명:</strong> {selectedCourse.name}</p>
                <p><strong>개설학과:</strong> {selectedCourse.department}</p>
              </div>

              {targetLoading ? (
                <div className="loading-text">로딩 중...</div>
              ) : (
                <div className="target-table-container">
                  <h3>학과/학년별 수강 대상</h3>
                  <table className="target-table">
                    <thead>
                      <tr>
                        <th>범위</th>
                        <th>대상</th>
                        <th>1학년</th>
                        <th>2학년</th>
                        <th>3학년</th>
                        <th>4학년</th>
                        <th>5학년</th>
                        <th>학생구분</th>
                        <th>제한</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedCourse.targets.map((target: TargetInfo, index: number) => (
                        <tr key={index} className={target.isDenied ? 'denied-row' : ''}>
                          <td>{target.scopeType === 'UNIVERSITY' ? '전체' : target.scopeType === 'COLLEGE' ? '단과대' : '학과'}</td>
                          <td>{target.scopeName || '-'}</td>
                          <td>{target.grade1 ? '✓' : '-'}</td>
                          <td>{target.grade2 ? '✓' : '-'}</td>
                          <td>{target.grade3 ? '✓' : '-'}</td>
                          <td>{target.grade4 ? '✓' : '-'}</td>
                          <td>{target.grade5 ? '✓' : '-'}</td>
                          <td>{getStudentTypeLabel(target.studentType)}</td>
                          <td>{target.isStrict ? '엄격' : target.isDenied ? '거부' : '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};