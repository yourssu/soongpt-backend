export type Category =
  | 'MAJOR_REQUIRED'
  | 'MAJOR_ELECTIVE'
  | 'MAJOR_BASIC'
  | 'GENERAL_REQUIRED'
  | 'GENERAL_ELECTIVE'
  | 'CHAPEL'
  | 'OTHER';

export type ScopeType =
  | 'UNIVERSITY'
  | 'COLLEGE'
  | 'DEPARTMENT';

export type StudentType =
  | 'GENERAL'
  | 'FOREIGNER'
  | 'MILITARY'
  | 'TEACHING_CERT';

export interface Course {
  id: number | null;
  category: Category;
  subCategory: string | null;
  field: string | null;
  code: number;
  name: string;
  professor: string | null;
  department: string;
  division: string | null;
  time: string;
  point: string;
  personeel: number;
  scheduleRoom: string;
  target: string;
}

export interface CoursesResponse {
  content: Course[];
  totalElements: number;
  totalPages: number;
  size: number;
  page: number;
}

export interface TargetInfo {
  id: number | null;
  scopeType: ScopeType;
  scopeId: number | null;
  scopeName: string | null;
  grade1: boolean;
  grade2: boolean;
  grade3: boolean;
  grade4: boolean;
  grade5: boolean;
  studentType: StudentType;
  isStrict: boolean;
  isDenied: boolean;
}

export interface CourseTime {
  week: string;
  start: string;
  end: string;
  classroom: string | null;
}

export interface CourseTargetResponse {
  code: number;
  name: string;
  professor: string | null;
  category: Category;
  subCategory: string | null;
  department: string;
  division: string | null;
  point: string;
  time: string;
  personeel: number;
  scheduleRoom: string;
  targetText: string;
  field: string | null;
  courseTimes: CourseTime[];
  targets: TargetInfo[];
}

export interface ApiResponse<T> {
  result: T;
}

export interface UpdateCourseRequest {
  category: Category;
  subCategory: string | null;
  field: string | null;
  name: string;
  professor: string | null;
  department: string;
  division: string | null;
  time: string;
  point: string;
  personeel: number;
  scheduleRoom: string;
  target: string;
}

export interface UpdateTargetItem {
  scopeType: ScopeType;
  scopeId: number | null;
  scopeName: string | null;
  grade1: boolean;
  grade2: boolean;
  grade3: boolean;
  grade4: boolean;
  grade5: boolean;
  studentType: StudentType;
  isStrict: boolean;
  isDenied: boolean;
}

export interface UpdateTargetsRequest {
  targets: UpdateTargetItem[];
}