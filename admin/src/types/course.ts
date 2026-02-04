export enum Category {
  MAJOR_REQUIRED = 'MAJOR_REQUIRED',
  MAJOR_ELECTIVE = 'MAJOR_ELECTIVE',
  MAJOR_BASIC = 'MAJOR_BASIC',
  GENERAL_REQUIRED = 'GENERAL_REQUIRED',
  GENERAL_ELECTIVE = 'GENERAL_ELECTIVE',
  CHAPEL = 'CHAPEL',
  OTHER = 'OTHER',
}

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

export interface ApiResponse<T> {
  result: T;
}