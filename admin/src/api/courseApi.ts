import axios from 'axios';
import type {
  ApiResponse,
  CoursesResponse,
  CourseTargetResponse,
  Course,
  SecondaryMajorTrackType,
  SecondaryMajorCompletionType,
} from '../types/course';

const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add admin password to headers
api.interceptors.request.use((config) => {
  const adminPassword = localStorage.getItem('adminPassword');
  if (adminPassword && config.url?.includes('/admin/')) {
    config.headers['X-Admin-Password'] = adminPassword;
  }
  return config;
});

// Handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear invalid password
      localStorage.removeItem('adminPassword');
      // Dispatch custom event to notify components
      window.dispatchEvent(new CustomEvent('admin-auth-failed'));
    }
    return Promise.reject(error);
  }
);

export interface GetCoursesParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: 'ASC' | 'DESC';
}

export interface FilterCoursesParams {
  schoolId: number;
  department: string;
  grade: number;
  category?: string;
  field?: string;
}

export interface GetCoursesByTrackParams {
  schoolId: number;
  department: string;
  grade: number;
  trackType: SecondaryMajorTrackType;
  completionType?: SecondaryMajorCompletionType;
}

export const courseApi = {
  getAllCourses: async (params: GetCoursesParams = {}): Promise<CoursesResponse> => {
    const response = await api.get<ApiResponse<CoursesResponse>>('/admin/courses', {
      params: {
        q: params.q || '',
        page: params.page || 0,
        size: params.size || 20,
        sort: params.sort || 'ASC',
      },
    });
    return response.data.result;
  },

  getCourseTarget: async (code: number): Promise<CourseTargetResponse> => {
    const response = await api.get<ApiResponse<CourseTargetResponse>>(`/admin/courses/${code}/target`);
    return response.data.result;
  },

  getCoursesByCategory: async (params: FilterCoursesParams): Promise<Course[]> => {
    const response = await api.get<ApiResponse<Course[]>>('/courses/by-category', {
      params,
    });
    return response.data.result;
  },

  getCoursesByTrack: async (params: GetCoursesByTrackParams): Promise<Course[]> => {
    const response = await api.get<ApiResponse<Course[]>>('/courses/by-track', {
      params,
    });
    return response.data.result;
  },

  updateCourse: async (code: number, data: any): Promise<CourseTargetResponse> => {
    const response = await api.put<ApiResponse<CourseTargetResponse>>(`/admin/courses/${code}`, data);
    return response.data.result;
  },

  updateTargets: async (code: number, data: any): Promise<CourseTargetResponse> => {
    const response = await api.put<ApiResponse<CourseTargetResponse>>(`/admin/courses/${code}/target`, data);
    return response.data.result;
  },

  createCourse: async (data: any): Promise<CourseTargetResponse> => {
    const response = await api.post<ApiResponse<CourseTargetResponse>>('/admin/courses', data);
    return response.data.result;
  },

  deleteCourse: async (code: number): Promise<void> => {
    await api.delete(`/admin/courses/${code}`);
  },
};
