import axios from 'axios';
import type { ApiResponse, CoursesResponse, CourseTargetResponse } from '../types/course';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface GetCoursesParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: 'ASC' | 'DESC';
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
};