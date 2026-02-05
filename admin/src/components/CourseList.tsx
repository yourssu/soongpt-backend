import { useState, useEffect } from 'react';
import { courseApi } from '../api/courseApi';
import type { Course, CoursesResponse, CourseTargetResponse, TargetInfo } from '../types/course';
import { FilterTab } from './FilterTab';
import { colleges, departments, categories } from '../data/departments';
import './CourseList.css';

export const CourseList = () => {
  const [activeTab, setActiveTab] = useState<'search' | 'filter'>('search');
  const [courses, setCourses] = useState<CoursesResponse | null>(null);
  const [filteredCourses, setFilteredCourses] = useState<Course[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);
  const [pageInput, setPageInput] = useState('');
  const [selectedCourse, setSelectedCourse] = useState<CourseTargetResponse | null>(null);
  const [targetLoading, setTargetLoading] = useState(false);
  const [showPolicyInfo, setShowPolicyInfo] = useState(false);
  const [showCourseTimes, setShowCourseTimes] = useState(true);
  const [currentCourseIndex, setCurrentCourseIndex] = useState<number>(-1);
  const [editMode, setEditMode] = useState(false);
  const [editedCourse, setEditedCourse] = useState<CourseTargetResponse | null>(null);

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

  const handleCourseClick = async (course: Course, index?: number) => {
    try {
      setTargetLoading(true);
      const targetData = await courseApi.getCourseTarget(course.code);
      console.log('수강 대상 데이터:', targetData);
      setSelectedCourse(targetData);
      if (index !== undefined) {
        setCurrentCourseIndex(index);
      }
    } catch (err) {
      console.error('수강 대상 조회 실패:', err);
      alert('수강 대상 정보를 불러오는데 실패했습니다.');
    } finally {
      setTargetLoading(false);
    }
  };

  const handleFilterResults = (results: Course[]) => {
    setFilteredCourses(results);
  };

  const navigateToCourse = async (direction: 'prev' | 'next') => {
    // 필터 탭에서는 filteredCourses 사용, 검색 탭에서는 courses 사용
    const currentList = activeTab === 'filter' ? filteredCourses : courses?.content;
    if (!currentList) return;

    const newIndex = direction === 'prev' ? currentCourseIndex - 1 : currentCourseIndex + 1;

    // 필터 탭인 경우 - 페이지네이션 없이 간단하게 처리
    if (activeTab === 'filter') {
      if (newIndex < 0 || newIndex >= currentList.length) return;
      const newCourse = currentList[newIndex];
      await handleCourseClick(newCourse, newIndex);
      return;
    }

    // 검색 탭인 경우 - 기존 페이지네이션 로직
    if (!courses) return;

    // 현재 페이지 범위를 벗어나면 페이지 이동
    if (newIndex < 0) {
      // 이전 페이지로 이동
      if (currentPage > 0) {
        const newPage = currentPage - 1;
        setCurrentPage(newPage);
        setTargetLoading(true);
        try {
          const data = await courseApi.getAllCourses({
            q: debouncedQuery,
            page: newPage,
            size: pageSize,
            sort: 'ASC',
          });
          setCourses(data);
          // 새 페이지의 마지막 과목 선택
          const lastIndex = data.content.length - 1;
          const lastCourse = data.content[lastIndex];
          const targetData = await courseApi.getCourseTarget(lastCourse.code);
          setSelectedCourse(targetData);
          setCurrentCourseIndex(lastIndex);
        } catch (err) {
          console.error('페이지 이동 실패:', err);
        } finally {
          setTargetLoading(false);
        }
      }
      return;
    }

    if (newIndex >= courses.content.length) {
      // 다음 페이지로 이동
      if (currentPage < courses.totalPages - 1) {
        const newPage = currentPage + 1;
        setCurrentPage(newPage);
        setTargetLoading(true);
        try {
          const data = await courseApi.getAllCourses({
            q: debouncedQuery,
            page: newPage,
            size: pageSize,
            sort: 'ASC',
          });
          setCourses(data);
          // 새 페이지의 첫 번째 과목 선택
          const firstCourse = data.content[0];
          const targetData = await courseApi.getCourseTarget(firstCourse.code);
          setSelectedCourse(targetData);
          setCurrentCourseIndex(0);
        } catch (err) {
          console.error('페이지 이동 실패:', err);
        } finally {
          setTargetLoading(false);
        }
      }
      return;
    }

    // 현재 페이지 내에서 이동
    const newCourse = courses.content[newIndex];
    await handleCourseClick(newCourse, newIndex);
  };

  const closeModal = () => {
    setSelectedCourse(null);
  };

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (!selectedCourse) return;

      if (event.key === 'Escape') {
        closeModal();
      } else if (event.key === 'ArrowLeft') {
        event.preventDefault();
        navigateToCourse('prev');
      } else if (event.key === 'ArrowRight') {
        event.preventDefault();
        navigateToCourse('next');
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [selectedCourse, currentCourseIndex, courses]);

  const getStudentTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      GENERAL: '일반',
      FOREIGNER: '외국인',
      MILITARY: '군위탁',
      TEACHING_CERT: '교직이수자',
    };
    return labels[type] || type;
  };



  const startEdit = () => {
    if (selectedCourse) {
      setEditedCourse(JSON.parse(JSON.stringify(selectedCourse)));
      setEditMode(true);
    }
  };

  const cancelEdit = () => {
    setEditMode(false);
    setEditedCourse(null);
  };

  const saveEdit = async () => {
    if (!editedCourse || !selectedCourse) return;

    try {
      setTargetLoading(true);

      // 1. Update Course Info
      const courseUpdateData = {
        category: editedCourse.category,
        subCategory: editedCourse.subCategory || null,
        field: editedCourse.field || null,
        name: editedCourse.name,
        professor: editedCourse.professor || null,
        department: editedCourse.department,
        division: editedCourse.division || null,
        time: editedCourse.time,
        point: editedCourse.point,
        personeel: editedCourse.personeel,
        scheduleRoom: editedCourse.scheduleRoom,
        target: editedCourse.targetText,
      };
      await courseApi.updateCourse(editedCourse.code, courseUpdateData);

      // 2. Update Targets
      const targetUpdateData = {
        targets: editedCourse.targets.map(t => ({
          scopeType: t.scopeType,
          scopeId: t.scopeId,
          scopeName: t.scopeName,
          grade1: t.grade1,
          grade2: t.grade2,
          grade3: t.grade3,
          grade4: t.grade4,
          grade5: t.grade5,
          studentType: t.studentType,
          isStrict: t.isStrict,
          isDenied: t.isDenied,
        }))
      };
      await courseApi.updateTargets(editedCourse.code, targetUpdateData);

      // 3. Refresh data
      const updatedData = await courseApi.getCourseTarget(editedCourse.code);
      setSelectedCourse(updatedData);
      setEditMode(false);
      setEditedCourse(null);
      alert('저장되었습니다.');

      // Refresh list logic if needed (e.g., if name changed)
      // fetchCourses(currentPage, debouncedQuery); 
    } catch (err) {
      console.error('저장 실패:', err);
      alert('저장에 실패했습니다.');
    } finally {
      setTargetLoading(false);
    }
  };

  const handleInputChange = (field: keyof CourseTargetResponse, value: any) => {
    if (!editedCourse) return;
    setEditedCourse({
      ...editedCourse,
      [field]: value
    });
  };

  const handleTargetChange = (index: number, field: keyof TargetInfo, value: any) => {
    if (!editedCourse) return;
    const newTargets = [...editedCourse.targets];
    newTargets[index] = {
      ...newTargets[index],
      [field]: value
    };
    setEditedCourse({
      ...editedCourse,
      targets: newTargets
    });
  };

  const handleAddTarget = () => {
    if (!editedCourse) return;
    const newTarget: TargetInfo = {
      id: null,
      scopeType: 'DEPARTMENT', // Default
      scopeId: null,
      scopeName: '',
      grade1: false,
      grade2: false,
      grade3: false,
      grade4: false,
      grade5: false,
      studentType: 'GENERAL',
      isStrict: false,
      isDenied: false
    };
    setEditedCourse({
      ...editedCourse,
      targets: [...editedCourse.targets, newTarget]
    });
  };

  const handleDeleteTarget = (index: number) => {
    if (!editedCourse) return;
    const newTargets = editedCourse.targets.filter((_, i) => i !== index);
    setEditedCourse({
      ...editedCourse,
      targets: newTargets
    });
  };

  const getWeekColor = (week: string): string => {
    const colors: Record<string, string> = {
      '월': 'red',
      '월요일': 'red',
      '화': 'orange',
      '화요일': 'orange',
      '수': 'yellow',
      '수요일': 'yellow',
      '목': 'green',
      '목요일': 'green',
      '금': 'blue',
      '금요일': 'blue',
      '토': 'indigo',
      '토요일': 'indigo',
      '일': 'violet',
      '일요일': 'violet',
    };
    return colors[week] || 'gray';
  };

  return (
    <div className="course-list-container">
      <h1>과목 관리</h1>

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'search' ? 'active' : ''}`}
          onClick={() => setActiveTab('search')}
        >
          검색
        </button>
        <button
          className={`tab ${activeTab === 'filter' ? 'active' : ''}`}
          onClick={() => setActiveTab('filter')}
        >
          필터
        </button>
      </div>

      {activeTab === 'search' ? (
        <>
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
                    {courses.content.map((course: Course, index: number) => (
                      <tr
                        key={course.id || course.code}
                        onClick={() => handleCourseClick(course, index)}
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
        </>
      ) : (
        <FilterTab
          onCourseClick={handleCourseClick}
          getCategoryLabel={getCategoryLabel}
          onFilterResults={handleFilterResults}
        />
      )}

      {selectedCourse && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <button
                className="nav-button nav-prev"
                onClick={() => navigateToCourse('prev')}
                disabled={activeTab === 'filter' ? currentCourseIndex <= 0 : (currentCourseIndex <= 0 && currentPage === 0)}
                title="이전 과목 (←)"
              >
                ←
              </button>
              <div className="modal-title-container">
                <h2>{editMode ? '과목 정보 수정' : '수강 대상 정보'}</h2>
                {activeTab === 'filter' && filteredCourses && currentCourseIndex >= 0 && (
                  <span className="course-counter">
                    {currentCourseIndex + 1} / {filteredCourses.length}
                  </span>
                )}
                {activeTab === 'search' && courses && currentCourseIndex >= 0 && (
                  <span className="course-counter">
                    {courses.page * courses.size + currentCourseIndex + 1} / {courses.totalElements}
                    <span className="page-info"> (페이지 {courses.page + 1}/{courses.totalPages})</span>
                  </span>
                )}
              </div>
              <div className="header-right">
                <button
                  className="nav-button nav-next"
                  onClick={() => navigateToCourse('next')}
                  disabled={
                    activeTab === 'filter'
                      ? !filteredCourses || currentCourseIndex >= filteredCourses.length - 1
                      : !courses || (currentCourseIndex >= courses.content.length - 1 && currentPage >= courses.totalPages - 1)
                  }
                  title="다음 과목 (→)"
                >
                  →
                </button>
              </div>
            </div>
            <div className="modal-body">
              <div className="course-info-detail">
                <div className="info-grid">
                  <div className="info-item">
                    <strong>코드:</strong>
                    <span>{selectedCourse.code}</span>
                  </div>

                  <div className="info-item">
                    <strong>과목명:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.name || ''}
                        onChange={(e) => handleInputChange('name', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.name}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>교수:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.professor || ''}
                        onChange={(e) => handleInputChange('professor', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.professor || '-'}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>이수구분:</strong>
                    {editMode ? (
                      <select
                        value={editedCourse?.category || ''}
                        onChange={(e) => handleInputChange('category', e.target.value)}
                      >
                        {categories.map(cat => (
                          <option key={cat.value} value={cat.value}>{cat.label}</option>
                        ))}
                      </select>
                    ) : (
                      <span>{getCategoryLabel(selectedCourse.category)}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>학과:</strong>
                    {editMode ? (
                      <select
                        value={editedCourse?.department || ''}
                        onChange={(e) => handleInputChange('department', e.target.value)}
                      >
                        {departments.map(dept => (
                          <option key={dept} value={dept}>{dept}</option>
                        ))}
                      </select>
                    ) : (
                      <span>{selectedCourse.department}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>학점:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.point || ''}
                        onChange={(e) => handleInputChange('point', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.point}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>시간:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.time || ''}
                        onChange={(e) => handleInputChange('time', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.time}</span>
                    )}
                  </div>

                  <div className="info-item">
                    <strong>정원:</strong>
                    {editMode ? (
                      <input
                        type="number"
                        value={editedCourse?.personeel || 0}
                        onChange={(e) => handleInputChange('personeel', parseInt(e.target.value))}
                      />
                    ) : (
                      <span>{selectedCourse.personeel}</span>
                    )}
                  </div>

                  <div className="info-item full-width">
                    <strong>강의실:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.scheduleRoom || ''}
                        onChange={(e) => handleInputChange('scheduleRoom', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.scheduleRoom}</span>
                    )}
                  </div>

                  <div className="info-item full-width">
                    <strong>원본 수강대상:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.targetText || ''}
                        onChange={(e) => handleInputChange('targetText', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.targetText || '-'}</span>
                    )}
                  </div>

                  <div className="info-item full-width">
                    <strong>교과영역:</strong>
                    {editMode ? (
                      <input
                        type="text"
                        value={editedCourse?.field || ''}
                        onChange={(e) => handleInputChange('field', e.target.value)}
                      />
                    ) : (
                      <span>{selectedCourse.field || '-'}</span>
                    )}
                  </div>
                </div>
              </div>

              {targetLoading ? (
                <div className="loading-text">로딩 중...</div>
              ) : (
                <>
                  {/* Course Times Section - 위로 이동 */}
                  <div className="course-times-section">
                    <div className="section-header">
                      <h3>강의 시간</h3>
                      <div className="spacer" style={{ flex: 1 }}></div>
                      {editMode ? (
                        <div className="edit-actions">
                          <button className="edit-button save" onClick={saveEdit}>저장</button>
                          <button className="edit-button cancel" onClick={cancelEdit}>취소</button>
                        </div>
                      ) : (
                        <div className="edit-actions">
                          <button className="edit-button" onClick={startEdit}>수정</button>
                        </div>
                      )}
                      <button
                        className="toggle-button"
                        onClick={() => setShowCourseTimes(!showCourseTimes)}
                        title={showCourseTimes ? "접기" : "펼치기"}
                      >
                        {showCourseTimes ? '▼' : '▶'}
                      </button>
                    </div>
                    {showCourseTimes && (
                      selectedCourse.courseTimes.length === 0 ? (
                        <div className="no-times-message">
                          <p>강의 시간 정보가 없습니다.</p>
                        </div>
                      ) : (
                        <div className="course-times-grid">
                          {selectedCourse.courseTimes.map((courseTime, index) => (
                            <div key={index} className="course-time-card">
                              <div className={`time-week-badge ${getWeekColor(courseTime.week)}`}>
                                {courseTime.week}
                              </div>
                              <div className="time-range">
                                {courseTime.start} - {courseTime.end}
                              </div>
                              {courseTime.classroom && (
                                <div className="time-classroom">{courseTime.classroom}</div>
                              )}
                            </div>
                          ))}
                        </div>
                      )
                    )}
                  </div>

                  {/* Target Policy Section */}
                  <div className="target-table-container">
                    <div className="header-with-help">
                      <h3>수강 대상 정책 (Course Target Policy)</h3>
                      <button
                        className="help-button"
                        onClick={() => setShowPolicyInfo(!showPolicyInfo)}
                        title="정책 평가 로직 설명"
                      >
                        ?
                      </button>
                    </div>

                    {showPolicyInfo && (
                      <div className="policy-info">
                        <h4>정책 평가 로직</h4>
                        <ol>
                          <li>모든 <strong className="deny-text">Deny</strong> 정책을 먼저 평가</li>
                          <li>하나라도 Deny에 매칭되면 → <strong>수강 불가</strong></li>
                          <li><strong className="allow-text">Allow</strong> 정책 중 하나라도 매칭되면 → <strong>수강 가능</strong></li>
                          <li>아무것도 매칭되지 않으면 → <strong>수강 불가</strong> (기본 거부)</li>
                        </ol>
                        <p className="policy-note-inline">
                          <strong>참고:</strong> Strict가 체크된 정책은 명시된 조건 외에는 수강이 불가능합니다 (대상외수강제한).
                        </p>
                      </div>
                    )}

                    <div className="legend">
                      <div className="legend-item">
                        <span className="legend-color allowed"></span>
                        <span><strong>Allow</strong>: 수강 허용 정책</span>
                      </div>
                      <div className="legend-item">
                        <span className="legend-color denied"></span>
                        <span><strong>Deny</strong>: 수강 제한 정책 (우선순위 높음)</span>
                      </div>
                    </div>

                    {!selectedCourse.targets || selectedCourse.targets.length === 0 ? (
                      editMode ? (
                        <div className="no-targets-message">
                          <p>수강 대상 정책을 추가해주세요.</p>
                        </div>
                      ) : (
                        <div className="no-targets-message">
                          <p>수강 대상이 없습니다.</p>
                        </div>
                      )
                    ) : (
                      <table className="target-table">
                        <thead>
                          <tr>
                            {editMode && <th>ID</th>}
                            <th>정책 유형</th>
                            <th>적용 범위</th>
                            <th>대상</th>
                            <th>1학년</th>
                            <th>2학년</th>
                            <th>3학년</th>
                            <th>4학년</th>
                            <th>5학년</th>
                            <th>학생 구분</th>
                            <th>대상외 제한</th>
                            {editMode && <th>삭제</th>}
                          </tr>
                        </thead>
                        <tbody>
                          {editMode ? (
                            // Edit Mode: Show all targets with inputs
                            editedCourse?.targets.map((target, index) => (
                              <tr key={index} className={target.isDenied ? 'denied-row' : 'allowed-row'}>
                                {editMode && <td>{target.id || '-'}</td>}
                                <td>
                                  <div className="toggle-group">
                                    <button
                                      className={`toggle-btn allow ${!target.isDenied ? 'active' : ''}`}
                                      onClick={() => handleTargetChange(index, 'isDenied', false)}
                                    >
                                      허용
                                    </button>
                                    <button
                                      className={`toggle-btn deny ${target.isDenied ? 'active' : ''}`}
                                      onClick={() => handleTargetChange(index, 'isDenied', true)}
                                    >
                                      거부
                                    </button>
                                  </div>
                                </td>
                                <td>
                                  <select
                                    value={target.scopeType}
                                    onChange={(e) => handleTargetChange(index, 'scopeType', e.target.value)}
                                    className="scope-select"
                                  >
                                    <option value="UNIVERSITY">전체</option>
                                    <option value="COLLEGE">단과대</option>
                                    <option value="DEPARTMENT">학과</option>
                                  </select>
                                </td>
                                <td>
                                  {target.scopeType === 'UNIVERSITY' ? (
                                    <span>전체</span>
                                  ) : target.scopeType === 'COLLEGE' ? (
                                    <select
                                      value={target.scopeName || ''}
                                      onChange={(e) => handleTargetChange(index, 'scopeName', e.target.value)}
                                      className="scope-detail-select"
                                    >
                                      <option value="">선택</option>
                                      {colleges.map(c => (
                                        <option key={c.name} value={c.name}>{c.name}</option>
                                      ))}
                                    </select>
                                  ) : (
                                    <select
                                      value={target.scopeName || ''}
                                      onChange={(e) => handleTargetChange(index, 'scopeName', e.target.value)}
                                      className="scope-detail-select"
                                    >
                                      <option value="">선택</option>
                                      {colleges.map(college => (
                                        <optgroup key={college.name} label={college.name}>
                                          {college.departments.map(dept => (
                                            <option key={dept} value={dept}>{dept}</option>
                                          ))}
                                        </optgroup>
                                      ))}
                                    </select>
                                  )}
                                </td>
                                <td>
                                  <input type="checkbox" checked={target.grade1} onChange={(e) => handleTargetChange(index, 'grade1', e.target.checked)} />
                                </td>
                                <td>
                                  <input type="checkbox" checked={target.grade2} onChange={(e) => handleTargetChange(index, 'grade2', e.target.checked)} />
                                </td>
                                <td>
                                  <input type="checkbox" checked={target.grade3} onChange={(e) => handleTargetChange(index, 'grade3', e.target.checked)} />
                                </td>
                                <td>
                                  <input type="checkbox" checked={target.grade4} onChange={(e) => handleTargetChange(index, 'grade4', e.target.checked)} />
                                </td>
                                <td>
                                  <input type="checkbox" checked={target.grade5} onChange={(e) => handleTargetChange(index, 'grade5', e.target.checked)} />
                                </td>
                                <td>
                                  <select
                                    value={target.studentType}
                                    onChange={(e) => handleTargetChange(index, 'studentType', e.target.value)}
                                    className="student-type-select"
                                  >
                                    <option value="GENERAL">일반</option>
                                    <option value="FOREIGNER">외국인</option>
                                    <option value="MILITARY">군위탁</option>
                                    <option value="TEACHING_CERT">교직</option>
                                  </select>
                                </td>
                                <td>
                                  <button
                                    className={`toggle-btn strict ${target.isStrict ? 'active' : ''}`}
                                    onClick={() => handleTargetChange(index, 'isStrict', !target.isStrict)}
                                  >
                                    {target.isStrict ? '제한' : '해제'}
                                  </button>
                                </td>
                                <td>
                                  <button className="delete-button" onClick={() => handleDeleteTarget(index)}>삭제</button>
                                </td>
                              </tr>
                            ))
                          ) : (
                            // View Mode: Existing logic (Sorted by Deny)
                            selectedCourse.targets
                              .sort((a, b) => (Number(b.isDenied) - Number(a.isDenied)))
                              .map((target: TargetInfo, index: number) => (
                                <tr key={index} className={target.isDenied ? 'denied-row' : 'allowed-row'}>
                                  {editMode && <td>{target.id || '-'}</td>}
                                  <td>
                                    <span className={`badge ${target.isDenied ? 'badge-deny' : 'badge-allow'}`}>
                                      {target.isDenied ? 'Deny' : 'Allow'}
                                    </span>
                                  </td>
                                  <td>{target.scopeType === 'UNIVERSITY' ? '전체' :
                                    target.scopeType === 'COLLEGE' ? '단과대' : '학과'}</td>
                                  <td>{target.scopeName || '-'}</td>
                                  <td>{target.grade1 ? 'O' : '-'}</td>
                                  <td>{target.grade2 ? 'O' : '-'}</td>
                                  <td>{target.grade3 ? 'O' : '-'}</td>
                                  <td>{target.grade4 ? 'O' : '-'}</td>
                                  <td>{target.grade5 ? 'O' : '-'}</td>
                                  <td>{getStudentTypeLabel(target.studentType)}</td>
                                  <td>{target.isStrict ? '제한' : '-'}</td>
                                </tr>
                              ))
                          )}
                        </tbody>
                      </table>
                    )}

                    {editMode && (
                      <div className="add-target-container">
                        <button className="add-target-button" onClick={handleAddTarget}>+ 정책 추가</button>
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};