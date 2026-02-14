import { useMemo, useState } from 'react';
import { courseApi } from '../api/courseApi';
import type {
  Course,
  SecondaryMajorCompletionType,
  SecondaryMajorTrackType,
} from '../types/course';
import { colleges, categories, grades } from '../data/departments';
import { isTeachingEligible } from '../data/teachingDepartments';

interface FilterTabProps {
  onCourseClick: (course: Course, index: number) => void;
  getCategoryLabel: (category: string) => string;
  onFilterResults: (results: Course[]) => void;
}

type FilterMode = 'category' | 'secondaryMajor' | 'teaching';

const secondaryMajorTrackOptions: Array<{ value: SecondaryMajorTrackType; label: string }> = [
  { value: 'DOUBLE_MAJOR', label: '복수전공' },
  { value: 'MINOR', label: '부전공' },
  { value: 'CROSS_MAJOR', label: '타전공인정' },
];

const teachingAreaOptions: Array<{ value: string; label: string }> = [
  { value: '', label: '전체' },
  { value: 'MAJOR', label: '전공영역' },
  { value: 'TEACHING', label: '교직영역' },
  { value: 'SPECIAL', label: '특성화영역' },
];

const secondaryMajorCompletionOptions: Record<
  SecondaryMajorTrackType,
  Array<{ value: SecondaryMajorCompletionType; label: string }>
> = {
  DOUBLE_MAJOR: [
    { value: 'REQUIRED', label: '복필' },
    { value: 'ELECTIVE', label: '복선' },
  ],
  MINOR: [
    { value: 'REQUIRED', label: '부필' },
    { value: 'ELECTIVE', label: '부선' },
  ],
  CROSS_MAJOR: [{ value: 'RECOGNIZED', label: '타전공인정과목' }],
};

export const FilterTab = ({ onCourseClick, getCategoryLabel, onFilterResults }: FilterTabProps) => {
  const [filteredCourses, setFilteredCourses] = useState<Course[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filterMode, setFilterMode] = useState<FilterMode>('category');
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [departmentSearch, setDepartmentSearch] = useState('');
  const [selectedGrade, setSelectedGrade] = useState<number>(1);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [isDepartmentDropdownOpen, setIsDepartmentDropdownOpen] = useState(false);
  const [selectedTrackType, setSelectedTrackType] = useState<SecondaryMajorTrackType>('DOUBLE_MAJOR');
  const [selectedCompletionType, setSelectedCompletionType] = useState<SecondaryMajorCompletionType>('REQUIRED');
  const [selectedTeachingArea, setSelectedTeachingArea] = useState('');
  const [schoolId] = useState(20);

  const handleFilterModeChange = (mode: FilterMode) => {
    setFilterMode(mode);
    setFilteredCourses(null);
    setError(null);
    onFilterResults([]);
    setDepartmentSearch('');
    setSelectedDepartment('');
  };

  const handleTrackTypeChange = (value: SecondaryMajorTrackType) => {
    const completionOptions = secondaryMajorCompletionOptions[value];
    setSelectedTrackType(value);
    setSelectedCompletionType(completionOptions[0].value);
  };

  const handleFilterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedDepartmentSearch = departmentSearch.trim();
    const effectiveDepartment = departmentSearchOptions.find(
      (dept) => dept.toLowerCase() === trimmedDepartmentSearch.toLowerCase(),
    );

    if (!trimmedDepartmentSearch) {
      alert('학과를 선택해주세요.');
      return;
    }

    if (!effectiveDepartment) {
      alert('학과를 목록에서 선택해주세요.');
      return;
    }

    try {
      setSelectedDepartment(effectiveDepartment);
      setDepartmentSearch(effectiveDepartment);
      setLoading(true);
      setError(null);
      if (filterMode === 'category') {
        const data = await courseApi.getCoursesByCategory({
          schoolId,
          department: effectiveDepartment,
          grade: selectedGrade,
          category: selectedCategory || undefined,
        });
        setFilteredCourses(data);
        onFilterResults(data);
      } else if (filterMode === 'secondaryMajor') {
        const data = await courseApi.getCoursesByTrack({
          schoolId,
          department: effectiveDepartment,
          trackType: selectedTrackType,
          completionType: selectedCompletionType,
        });
        setFilteredCourses(data);
        onFilterResults(data);
      } else if (filterMode === 'teaching') {
        const data = await courseApi.getTeachingCourses({
          schoolId,
          department: effectiveDepartment,
          majorArea: selectedTeachingArea || undefined,
        });
        setFilteredCourses(data);
        onFilterResults(data);
      }
    } catch (err) {
      setError('과목을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const parseCoursePoint = (point: string): [string, string, string] => {
    const parts = (point || '').split('-').map((part) => part.trim());
    return [
      parts[0] || '-',
      parts[1] || '-',
      parts[2] || '-',
    ];
  };

  const normalizedDepartmentSearch = departmentSearch.trim().toLowerCase();

  const filteredColleges = useMemo(() => {
    return colleges
      .map((college) => {
        const availableDepartments =
          filterMode === 'teaching'
            ? college.departments.filter((dept) => isTeachingEligible(dept))
            : college.departments;

        const matchedDepartments = availableDepartments.filter((dept) => {
          if (!normalizedDepartmentSearch) return true;

          const departmentMatched = dept.toLowerCase().includes(normalizedDepartmentSearch);
          const collegeMatched = college.name.toLowerCase().includes(normalizedDepartmentSearch);
          return departmentMatched || collegeMatched;
        });

        return {
          name: college.name,
          departments: matchedDepartments,
        };
      })
      .filter((college): college is { name: string; departments: string[] } => college.departments.length > 0);
  }, [filterMode, normalizedDepartmentSearch]);

  const departmentSearchOptions = useMemo(() => {
    const seen = new Set<string>();
    filteredColleges.forEach((college) => {
      college.departments.forEach((department) => {
        seen.add(department);
      });
    });
    return Array.from(seen);
  }, [filteredColleges]);

  const handleDepartmentSearchChange = (value: string) => {
    setDepartmentSearch(value);
    setIsDepartmentDropdownOpen(true);
    if (!value) {
      setSelectedDepartment('');
      return;
    }

    const exactMatch = departmentSearchOptions.find((dept) => dept.toLowerCase() === value.toLowerCase());
    setSelectedDepartment(exactMatch || '');
  };

  const handleDepartmentSelect = (department: string) => {
    setDepartmentSearch(department);
    setSelectedDepartment(department);
    setIsDepartmentDropdownOpen(false);

    if (filterMode === 'teaching' && !isTeachingEligible(department)) {
      setFilterMode('category');
      alert('선택한 학과는 교직 이수가 불가능하여 일반 이수구분 필터로 전환되었습니다.');
    }
  };

  return (
    <>
      <form onSubmit={handleFilterSubmit} className="filter-form">
        <div className="filter-row">
          <div className="filter-field filter-department-field">
            <label htmlFor="filter-mode">조회 유형</label>
            <select
              id="filter-mode"
              value={filterMode}
              onChange={(e) => handleFilterModeChange(e.target.value as FilterMode)}
              className="filter-select"
            >
              <option value="category">일반 이수구분</option>
              <option value="secondaryMajor">다전공/부전공</option>
              {(!selectedDepartment || isTeachingEligible(selectedDepartment)) && (
                <option value="teaching">교직</option>
              )}
            </select>
          </div>

          <div className="filter-field">
            <label htmlFor="department-search">학과명 검색</label>
            <div className="filter-select-wrapper">
              <input
                id="department-search"
                type="text"
                value={departmentSearch}
                onChange={(e) => handleDepartmentSearchChange(e.target.value)}
                onFocus={() => setIsDepartmentDropdownOpen(true)}
                onBlur={() => {
                  setTimeout(() => setIsDepartmentDropdownOpen(false), 120);
                }}
                placeholder="학과명 검색"
                className="filter-select"
                aria-label="학과명 검색"
                autoComplete="off"
              />
              {isDepartmentDropdownOpen && (
                <div className="filter-dropdown">
                  {filteredColleges.length === 0 ? (
                    <div className="filter-dropdown-empty">검색 결과가 없습니다</div>
                  ) : (
                    filteredColleges.map((college) => (
                      <div key={college.name} className="filter-dropdown-group">
                        <div className="filter-dropdown-group-label">{college.name}</div>
                        {college.departments.map((department) => (
                          <button
                            key={department}
                            type="button"
                            className="filter-dropdown-option"
                            onMouseDown={() => handleDepartmentSelect(department)}
                          >
                            {department}
                          </button>
                        ))}
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>

          {filterMode === 'category' && (
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
          )}

          {filterMode === 'category' && (
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
          )}

          {filterMode === 'secondaryMajor' && (
            <>
              <div className="filter-field">
                <label htmlFor="track-type">다전공 유형</label>
                <select
                  id="track-type"
                  value={selectedTrackType}
                  onChange={(e) => handleTrackTypeChange(e.target.value as SecondaryMajorTrackType)}
                  className="filter-select"
                >
                  {secondaryMajorTrackOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-field">
                <label htmlFor="completion-type">이수구분</label>
                <select
                  id="completion-type"
                  value={selectedCompletionType}
                  onChange={(e) => setSelectedCompletionType(e.target.value as SecondaryMajorCompletionType)}
                  className="filter-select"
                >
                  {secondaryMajorCompletionOptions[selectedTrackType].map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </>
          )}

          {filterMode === 'teaching' && (
            <div className="filter-field">
              <label htmlFor="teaching-area">교직 영역</label>
              <select
                id="teaching-area"
                value={selectedTeachingArea}
                onChange={(e) => setSelectedTeachingArea(e.target.value)}
                className="filter-select"
              >
                {teachingAreaOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          )}

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
                  {filterMode === 'category' && <th>수강대상</th>}
                </tr>
              </thead>
              <tbody>
                {filteredCourses?.map((course: Course, index: number) => {
                  const [pointClass, pointTime, pointQuota] = parseCoursePoint(course.point);
                  return (
                    <tr
                      key={course.id || course.code}
                      onClick={() => onCourseClick(course, index)}
                      style={{ cursor: 'pointer' }}
                    >
                      <td>{course.code}</td>
                      <td>{course.name}</td>
                      <td>{course.professor || '-'}</td>
                      <td>{filterMode === 'secondaryMajor' ? (course.subCategory || '-') : getCategoryLabel(course.category)}</td>
                      <td>{course.department}</td>
                      <td>{pointClass}</td>
                      <td>{pointTime}</td>
                      <td>{pointQuota}</td>
                      <td>{(filterMode === 'secondaryMajor' || filterMode === 'teaching') ? '-' : course.scheduleRoom}</td>
                      {filterMode === 'category' && <td>{course.target}</td>}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  );
};
