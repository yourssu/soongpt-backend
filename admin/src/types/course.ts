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

export interface CourseTargetResponse {
  code: number;
  name: string;
  department: string;
  targetText: string;
  targets: TargetInfo[];
}

export interface ApiResponse<T> {
  result: T;
}