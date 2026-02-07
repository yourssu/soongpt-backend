import { useState } from 'react';
import { courseApi } from '../api/courseApi';
import type { Course } from '../types/course';
import { colleges, categories, grades } from '../data/departments';

interface FilterTabProps {
  onCourseClick: (course: Course, index: number) => void;
  getCategoryLabel: (category: string) => string;
  onFilterResults: (results: Course[]) => void;
}

export const FilterTab = ({ onCourseClick, getCategoryLabel, onFilterResults }: FilterTabProps) => {
  const [filteredCourses, setFilteredCourses] = useState<Course[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedGrade, setSelectedGrade] = useState<number>(1);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [schoolId] = useState(20);

  const handleFilterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDepartment) {
      alert('학과를 선택해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await courseApi.getCoursesByCategory({
        schoolId,
        department: selectedDepartment,
        grade: selectedGrade,
        category: selectedCategory || undefined,  // 빈 문자열이면 undefined로 전달
      });
      setFilteredCourses(data);
      onFilterResults(data);
    } catch (err) {
      setError('과목을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <form onSubmit={handleFilterSubmit} className="filter-form">
        <div className="filter-row">
          <div className="filter-field">
            <label htmlFor="department">학과</label>
            <select
              id="department"
              value={selectedDepartment}
              onChange={(e) => setSelectedDepartment(e.target.value)}
              className="filter-select"
            >
              <option value="">학과 선택</option>
              {colleges.map((college) => (
                <optgroup key={college.name} label={college.name}>
                  {college.departments.map((dept) => (
                    <option key={dept} value={dept}>
                      {dept}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
          </div>

          <div className="filter-field">
            <label htmlFor="grade">학년</label>
            <select
              id="grade"
              value={selectedGrade}
              onChange={(e) => setSelectedGrade(Number(e.target.value))}
              className="filter-select"
            >
              {grades.map((grade) => (
                <option key={grade} value={grade}>
                  {grade}학년
                </option>
              ))}
            </select>
          </div>

          <div className="filter-field">
            <label htmlFor="category">이수구분</label>
            <select
              id="category"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="filter-select"
            >
              <option value="">전체</option>
              {categories.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </select>
          </div>

          <button type="submit" className="filter-button">조회</button>
        </div>
      </form>

      {error && <div className="error">{error}</div>}

      {loading && (
        <div className="loading-overlay">
          <div className="spinner"></div>
          <div className="loading-text">로딩 중...</div>
        </div>
      )}

      {filteredCourses && (
        <>
          <div className="course-info">
            총 {filteredCourses.length}개의 과목
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
                {filteredCourses.map((course: Course, index: number) => (
                  <tr
                    key={course.id || course.code}
                    onClick={() => onCourseClick(course, index)}
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
        </>
      )}
    </>
  );
};
